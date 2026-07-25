package io.ygdrasil.wgpu

import io.ygdrasil.kffi.NativeAddress
import io.ygdrasil.kffi.CallbackExceptionHandler
import io.ygdrasil.kffi.CallbackPolicy
import io.ygdrasil.kffi.CallbackRegistration
import io.ygdrasil.kffi.CallbackRuntime
import io.ygdrasil.kffi.CallbackRuntimeApi
import io.ygdrasil.kffi.PreparedCallbackRegistration
import io.ygdrasil.kffi.UnsafeCallbackRearmApi
import io.ygdrasil.kffi.CString
import io.ygdrasil.kffi.ArrayHolder
import io.ygdrasil.kffi.MemoryAllocator
import io.ygdrasil.kffi.CStructure
import io.ygdrasil.kffi.findOrThrow
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.GroupLayout
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import java.lang.foreign.MemoryLayout.PathElement.groupElement
import kotlin.OptIn
import kotlin.Suppress
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

private object KextractNativeBootstrap {
    @kotlin.jvm.Volatile private var loaded: kotlin.Boolean = false

    fun resolve(name: kotlin.String): MemorySegment {
        load()
        return findOrThrow(name)
    }

    private fun load() {
        if (loaded) return
        kotlin.synchronized(this) {
            if (loaded) return
            val platform = currentPlatform()
            val bundle = bundles[platform]
            val bundleDirectory = bundle?.let { extractBundle(platform, it) }
            val libraryPath0 = bundle?.libraryPaths?.get("wgpu_native")
            if (bundleDirectory != null && libraryPath0 != null) {
                java.lang.System.load(bundleDirectory.resolve(libraryPath0).toAbsolutePath().normalize().toString())
            } else {
                java.lang.System.loadLibrary("wgpu_native")
            }
            loaded = true
        }
    }

    private data class Resource(val path: kotlin.String, val sha256: kotlin.String)
    private data class Bundle(val key: kotlin.String, val resources: kotlin.collections.List<Resource>, val libraryPaths: kotlin.collections.Map<kotlin.String, kotlin.String>)

    private val bundles: kotlin.collections.Map<kotlin.String, Bundle> = kotlin.collections.mapOf(
        "darwin-aarch64" to Bundle(
            key = "3d375bb3335218a005ec01b06b107825cff25f432e5b4cf3ee10bc2cb6a7467a",
            resources = kotlin.collections.listOf(
                Resource("libwgpu_native.dylib", "e72e8f1777dd0315bfee91c6b7e7b105234d5c1cc1621db4cf92ffee2ac2ae32"),
            ),
            libraryPaths = kotlin.collections.mapOf(
                "wgpu_native" to "libwgpu_native.dylib",
            ),
        ),
        "darwin-x86-64" to Bundle(
            key = "146939519caab2e7492b64e78a96b189835852f4f776424025c1afdc35788f5f",
            resources = kotlin.collections.listOf(
                Resource("libwgpu_native.dylib", "d5175b3c0675e7f66b677a7c28537ff260a698c82fff96179247cd9c9dec6c8d"),
            ),
            libraryPaths = kotlin.collections.mapOf(
                "wgpu_native" to "libwgpu_native.dylib",
            ),
        ),
        "linux-aarch64" to Bundle(
            key = "e9b3785130015d2107104ec3e8b72ebc20912e595726c95dc6e58a2cceea22ac",
            resources = kotlin.collections.listOf(
                Resource("libwgpu_native.so", "51d8a711c9a687f824712c42a206800b69f14eff8667a88f2f86936cdef0ebd0"),
            ),
            libraryPaths = kotlin.collections.mapOf(
                "wgpu_native" to "libwgpu_native.so",
            ),
        ),
        "linux-x86-64" to Bundle(
            key = "7260f9fbfb52bc0fd0926ec1ffa3db52786122868d54dfc262752ccf76a09f08",
            resources = kotlin.collections.listOf(
                Resource("libwgpu_native.so", "be0d8270dd9d78e1039eeb413ee21be8a89776a752e56d6b0f57eefbbc2d749c"),
            ),
            libraryPaths = kotlin.collections.mapOf(
                "wgpu_native" to "libwgpu_native.so",
            ),
        ),
        "win32-x86-64" to Bundle(
            key = "94b8466520009dae327f49f0709c1e1acceaf855d4922e4a888fca938241ab11",
            resources = kotlin.collections.listOf(
                Resource("wgpu_native.dll", "5e26249c40dcdac43f5adca3195fcf40e4ec6c67472b38510165f411fcc198e7"),
            ),
            libraryPaths = kotlin.collections.mapOf(
                "wgpu_native" to "wgpu_native.dll",
            ),
        ),
    )

    private fun currentPlatform(): kotlin.String {
        val os = java.lang.System.getProperty("os.name").lowercase(java.util.Locale.ROOT)
        val architecture = java.lang.System.getProperty("os.arch").lowercase(java.util.Locale.ROOT)
        val osId = when {
            os.contains("mac") || os.contains("darwin") -> "darwin"
            os.contains("linux") -> "linux"
            os.contains("windows") -> "win32"
            else -> os.replace(kotlin.text.Regex("[^a-z0-9]+"), "-").trim('-')
        }
        val architectureId = when (architecture) {
            "aarch64", "arm64" -> "aarch64"
            "amd64", "x86_64", "x64" -> "x86-64"
            else -> architecture.replace(kotlin.text.Regex("[^a-z0-9]+"), "-").trim('-')
        }
        return "$osId-$architectureId"
    }

    private fun extractBundle(platform: kotlin.String, bundle: Bundle): java.nio.file.Path {
        val configuredCache = java.lang.System.getProperty("kextract.native.cache.dir")
        val cacheRoot = if (configuredCache.isNullOrBlank()) {
            java.nio.file.Path.of(java.lang.System.getProperty("java.io.tmpdir"), "kextract-native")
        } else {
            java.nio.file.Path.of(configuredCache)
        }
        java.nio.file.Files.createDirectories(cacheRoot)
        val bundleDirectory = cacheRoot.resolve(bundle.key).toAbsolutePath().normalize()
        val lockPath = cacheRoot.resolve("${bundle.key}.lock")
        val processLock = lockPath.toAbsolutePath().normalize().toString().intern()
        kotlin.synchronized(processLock) {
            java.nio.channels.FileChannel.open(lockPath, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE).use { channel ->
                channel.lock().use {
                    java.nio.file.Files.createDirectories(bundleDirectory)
                    bundle.resources.forEach { resource ->
                        val destination = bundleDirectory.resolve(resource.path).normalize()
                        if (!destination.startsWith(bundleDirectory)) {
                            throw java.io.IOException("Native resource escapes cache directory: ${resource.path}")
                        }
                        if (!java.nio.file.Files.isRegularFile(destination) || sha256(destination) != resource.sha256) {
                            copyResource(platform, resource, destination)
                        }
                    }
                }
            }
        }
        return bundleDirectory
    }

    private fun copyResource(platform: kotlin.String, resource: Resource, destination: java.nio.file.Path) {
        java.nio.file.Files.createDirectories(destination.parent)
        val temporary = java.nio.file.Files.createTempFile(destination.parent, ".${destination.fileName}.", ".tmp")
        try {
            val resourceName = "$platform/${resource.path}"
            val classLoader = KextractNativeBootstrap::class.java.classLoader
            val candidates = if (classLoader == null) {
                java.lang.ClassLoader.getSystemResources(resourceName)
            } else {
                classLoader.getResources(resourceName)
            }
            var candidateCount = 0
            var matched = false
            while (candidates.hasMoreElements()) {
                candidateCount += 1
                candidates.nextElement().openStream().use { input ->
                    java.nio.file.Files.copy(input, temporary, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                }
                if (sha256(temporary) == resource.sha256) {
                    matched = true
                    break
                }
            }
            if (!matched) {
                if (candidateCount == 0) {
                    throw java.io.FileNotFoundException("Native resource not found: /$resourceName")
                }
                throw java.io.IOException("No native resource candidate matched SHA-256 ${resource.sha256}: /$resourceName")
            }
            try {
                java.nio.file.Files.move(temporary, destination, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                java.nio.file.Files.move(temporary, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            java.nio.file.Files.deleteIfExists(temporary)
        }
    }

    private fun sha256(path: java.nio.file.Path): kotlin.String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        java.nio.file.Files.newInputStream(path).use { input ->
            val buffer = kotlin.ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return java.util.HexFormat.of().formatHex(digest.digest())
    }
}

actual interface WGPUStringView : CStructure {
    actual var data: CString?
    actual var length: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("data"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")
        ).withByteAlignment(8).withName("WGPUStringView")
        
        val data_VH: VarHandle = layout.varHandle(groupElement("data"))
        val length_VH: VarHandle = layout.varHandle(groupElement("length"))
        
        actual operator fun invoke(address: NativeAddress): WGPUStringView = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStringView = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStringView) -> Unit): ArrayHolder<WGPUStringView> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUStringView {
        override var data: CString?
            get() = (data_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let(::CString)
            set(value) = data_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var length: ULong
            get() = (length_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = length_VH.set(handler.handler, 0L, value.toLong())
    }
}

@kotlin.jvm.JvmInline
actual value class WGPUAdapter actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUBindGroup actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUBindGroupLayout actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUBuffer actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUCommandBuffer actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUCommandEncoder actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUComputePassEncoder actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUComputePipeline actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUDevice actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUExternalTexture actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUInstance actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUPipelineLayout actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUQuerySet actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUQueue actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPURenderBundle actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPURenderBundleEncoder actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPURenderPassEncoder actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPURenderPipeline actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUSampler actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUShaderModule actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUSurface actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUTexture actual constructor(actual val handler: NativeAddress)

@kotlin.jvm.JvmInline
actual value class WGPUTextureView actual constructor(actual val handler: NativeAddress)

actual interface WGPUChainedStruct : CStructure {
    actual var next: WGPUChainedStruct?
    actual var sType: WGPUSType
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("next"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUChainedStruct")
        
        val next_VH: VarHandle = layout.varHandle(groupElement("next"))
        val sType_VH: VarHandle = layout.varHandle(groupElement("sType"))
        
        actual operator fun invoke(address: NativeAddress): WGPUChainedStruct = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUChainedStruct = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUChainedStruct) -> Unit): ArrayHolder<WGPUChainedStruct> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUChainedStruct {
        override var next: WGPUChainedStruct?
            get() = (next_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = next_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var sType: WGPUSType
            get() = (sType_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUSType
            set(value) = sType_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUBufferMapCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUBufferMapCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBufferMapCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferMapCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferMapCallbackInfo) -> Unit): ArrayHolder<WGPUBufferMapCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBufferMapCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUCompilationInfoCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUCompilationInfoCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfoCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfoCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfoCallbackInfo) -> Unit): ArrayHolder<WGPUCompilationInfoCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCompilationInfoCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUCreateComputePipelineAsyncCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUCreateComputePipelineAsyncCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCreateComputePipelineAsyncCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateComputePipelineAsyncCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateComputePipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCreateComputePipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUCreateRenderPipelineAsyncCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUCreateRenderPipelineAsyncCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCreateRenderPipelineAsyncCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateRenderPipelineAsyncCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateRenderPipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUDeviceLostCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUDeviceLostCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUDeviceLostCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceLostCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceLostCallbackInfo) -> Unit): ArrayHolder<WGPUDeviceLostCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUDeviceLostCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUPopErrorScopeCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUPopErrorScopeCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUPopErrorScopeCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPopErrorScopeCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPopErrorScopeCallbackInfo) -> Unit): ArrayHolder<WGPUPopErrorScopeCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUPopErrorScopeCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUQueueWorkDoneCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUQueueWorkDoneCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUQueueWorkDoneCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueWorkDoneCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueWorkDoneCallbackInfo) -> Unit): ArrayHolder<WGPUQueueWorkDoneCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUQueueWorkDoneCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPURequestAdapterCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPURequestAdapterCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterCallbackInfo) -> Unit): ArrayHolder<WGPURequestAdapterCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURequestAdapterCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPURequestDeviceCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPURequestDeviceCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val mode_VH: VarHandle = layout.varHandle(groupElement("mode"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPURequestDeviceCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestDeviceCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestDeviceCallbackInfo) -> Unit): ArrayHolder<WGPURequestDeviceCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURequestDeviceCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mode: WGPUCallbackMode
            get() = (mode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCallbackMode
            set(value) = mode_VH.set(handler.handler, 0L, value.toInt())
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUUncapturedErrorCallbackInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")
        ).withByteAlignment(8).withName("WGPUUncapturedErrorCallbackInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val callback_VH: VarHandle = layout.varHandle(groupElement("callback"))
        val userdata1_VH: VarHandle = layout.varHandle(groupElement("userdata1"))
        val userdata2_VH: VarHandle = layout.varHandle(groupElement("userdata2"))
        
        actual operator fun invoke(address: NativeAddress): WGPUUncapturedErrorCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUUncapturedErrorCallbackInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUUncapturedErrorCallbackInfo) -> Unit): ArrayHolder<WGPUUncapturedErrorCallbackInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUUncapturedErrorCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var callback: NativeAddress?
            get() = (callback_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = callback_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata1: NativeAddress?
            get() = (userdata1_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata1_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var userdata2: NativeAddress?
            get() = (userdata2_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = userdata2_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUAdapterInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var vendor: WGPUStringView
    actual var architecture: WGPUStringView
    actual var device: WGPUStringView
    actual var description: WGPUStringView
    actual var backendType: WGPUBackendType
    actual var adapterType: WGPUAdapterType
    actual var vendorID: UInt
    actual var deviceID: UInt
    actual var subgroupMinSize: UInt
    actual var subgroupMaxSize: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("vendor"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("architecture"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("device"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("description"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("backendType"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("adapterType"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("vendorID"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("deviceID"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("subgroupMinSize"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("subgroupMaxSize")
        ).withByteAlignment(8).withName("WGPUAdapterInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val backendType_VH: VarHandle = layout.varHandle(groupElement("backendType"))
        val adapterType_VH: VarHandle = layout.varHandle(groupElement("adapterType"))
        val vendorID_VH: VarHandle = layout.varHandle(groupElement("vendorID"))
        val deviceID_VH: VarHandle = layout.varHandle(groupElement("deviceID"))
        val subgroupMinSize_VH: VarHandle = layout.varHandle(groupElement("subgroupMinSize"))
        val subgroupMaxSize_VH: VarHandle = layout.varHandle(groupElement("subgroupMaxSize"))
        
        actual operator fun invoke(address: NativeAddress): WGPUAdapterInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUAdapterInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUAdapterInfo) -> Unit): ArrayHolder<WGPUAdapterInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUAdapterInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var vendor: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("vendor")), Companion.layout.select(groupElement("vendor")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("vendor")), Companion.layout.select(groupElement("vendor")).byteSize())
            }
        override var architecture: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("architecture")), Companion.layout.select(groupElement("architecture")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("architecture")), Companion.layout.select(groupElement("architecture")).byteSize())
            }
        override var device: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("device")), Companion.layout.select(groupElement("device")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("device")), Companion.layout.select(groupElement("device")).byteSize())
            }
        override var description: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("description")), Companion.layout.select(groupElement("description")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("description")), Companion.layout.select(groupElement("description")).byteSize())
            }
        override var backendType: WGPUBackendType
            get() = (backendType_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBackendType
            set(value) = backendType_VH.set(handler.handler, 0L, value.toInt())
        override var adapterType: WGPUAdapterType
            get() = (adapterType_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUAdapterType
            set(value) = adapterType_VH.set(handler.handler, 0L, value.toInt())
        override var vendorID: UInt
            get() = (vendorID_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = vendorID_VH.set(handler.handler, 0L, value.toInt())
        override var deviceID: UInt
            get() = (deviceID_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = deviceID_VH.set(handler.handler, 0L, value.toInt())
        override var subgroupMinSize: UInt
            get() = (subgroupMinSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = subgroupMinSize_VH.set(handler.handler, 0L, value.toInt())
        override var subgroupMaxSize: UInt
            get() = (subgroupMaxSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = subgroupMaxSize_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUBlendComponent : CStructure {
    actual var operation: WGPUBlendOperation
    actual var srcFactor: WGPUBlendFactor
    actual var dstFactor: WGPUBlendFactor
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("operation"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("srcFactor"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dstFactor")
        ).withByteAlignment(4).withName("WGPUBlendComponent")
        
        val operation_VH: VarHandle = layout.varHandle(groupElement("operation"))
        val srcFactor_VH: VarHandle = layout.varHandle(groupElement("srcFactor"))
        val dstFactor_VH: VarHandle = layout.varHandle(groupElement("dstFactor"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBlendComponent = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendComponent = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendComponent) -> Unit): ArrayHolder<WGPUBlendComponent> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBlendComponent {
        override var operation: WGPUBlendOperation
            get() = (operation_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBlendOperation
            set(value) = operation_VH.set(handler.handler, 0L, value.toInt())
        override var srcFactor: WGPUBlendFactor
            get() = (srcFactor_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBlendFactor
            set(value) = srcFactor_VH.set(handler.handler, 0L, value.toInt())
        override var dstFactor: WGPUBlendFactor
            get() = (dstFactor_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBlendFactor
            set(value) = dstFactor_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUBufferBindingLayout : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUBufferBindingType
    actual var hasDynamicOffset: UInt
    actual var minBindingSize: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("hasDynamicOffset"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("minBindingSize")
        ).withByteAlignment(8).withName("WGPUBufferBindingLayout")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val type_VH: VarHandle = layout.varHandle(groupElement("type"))
        val hasDynamicOffset_VH: VarHandle = layout.varHandle(groupElement("hasDynamicOffset"))
        val minBindingSize_VH: VarHandle = layout.varHandle(groupElement("minBindingSize"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBufferBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferBindingLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferBindingLayout) -> Unit): ArrayHolder<WGPUBufferBindingLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBufferBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var type: WGPUBufferBindingType
            get() = (type_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBufferBindingType
            set(value) = type_VH.set(handler.handler, 0L, value.toInt())
        override var hasDynamicOffset: UInt
            get() = (hasDynamicOffset_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = hasDynamicOffset_VH.set(handler.handler, 0L, value.toInt())
        override var minBindingSize: ULong
            get() = (minBindingSize_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = minBindingSize_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUBufferDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var usage: ULong
    actual var size: ULong
    actual var mappedAtCreation: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("usage"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("size"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mappedAtCreation"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUBufferDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val usage_VH: VarHandle = layout.varHandle(groupElement("usage"))
        val size_VH: VarHandle = layout.varHandle(groupElement("size"))
        val mappedAtCreation_VH: VarHandle = layout.varHandle(groupElement("mappedAtCreation"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBufferDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferDescriptor) -> Unit): ArrayHolder<WGPUBufferDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var usage: ULong
            get() = (usage_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = usage_VH.set(handler.handler, 0L, value.toLong())
        override var size: ULong
            get() = (size_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = size_VH.set(handler.handler, 0L, value.toLong())
        override var mappedAtCreation: UInt
            get() = (mappedAtCreation_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mappedAtCreation_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUColor : CStructure {
    actual var r: Double
    actual var g: Double
    actual var b: Double
    actual var a: Double
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("r"),
            ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("g"),
            ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("b"),
            ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("a")
        ).withByteAlignment(8).withName("WGPUColor")
        
        val r_VH: VarHandle = layout.varHandle(groupElement("r"))
        val g_VH: VarHandle = layout.varHandle(groupElement("g"))
        val b_VH: VarHandle = layout.varHandle(groupElement("b"))
        val a_VH: VarHandle = layout.varHandle(groupElement("a"))
        
        actual operator fun invoke(address: NativeAddress): WGPUColor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColor) -> Unit): ArrayHolder<WGPUColor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUColor {
        override var r: Double
            get() = r_VH.get(handler.handler, 0L) as Double
            set(value) = r_VH.set(handler.handler, 0L, value)
        override var g: Double
            get() = g_VH.get(handler.handler, 0L) as Double
            set(value) = g_VH.set(handler.handler, 0L, value)
        override var b: Double
            get() = b_VH.get(handler.handler, 0L) as Double
            set(value) = b_VH.set(handler.handler, 0L, value)
        override var a: Double
            get() = a_VH.get(handler.handler, 0L) as Double
            set(value) = a_VH.set(handler.handler, 0L, value)
    }
}

actual interface WGPUCommandBufferDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPUCommandBufferDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCommandBufferDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandBufferDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandBufferDescriptor) -> Unit): ArrayHolder<WGPUCommandBufferDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCommandBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPUCommandEncoderDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPUCommandEncoderDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCommandEncoderDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandEncoderDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandEncoderDescriptor) -> Unit): ArrayHolder<WGPUCommandEncoderDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCommandEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPUCompatibilityModeLimits : CStructure {
    actual var chain: WGPUChainedStruct
    actual var maxStorageBuffersInVertexStage: UInt
    actual var maxStorageTexturesInVertexStage: UInt
    actual var maxStorageBuffersInFragmentStage: UInt
    actual var maxStorageTexturesInFragmentStage: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageBuffersInVertexStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageTexturesInVertexStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageBuffersInFragmentStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageTexturesInFragmentStage")
        ).withByteAlignment(8).withName("WGPUCompatibilityModeLimits")
        
        val maxStorageBuffersInVertexStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageBuffersInVertexStage"))
        val maxStorageTexturesInVertexStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageTexturesInVertexStage"))
        val maxStorageBuffersInFragmentStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageBuffersInFragmentStage"))
        val maxStorageTexturesInFragmentStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageTexturesInFragmentStage"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCompatibilityModeLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompatibilityModeLimits = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompatibilityModeLimits) -> Unit): ArrayHolder<WGPUCompatibilityModeLimits> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCompatibilityModeLimits {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = (maxStorageBuffersInVertexStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageBuffersInVertexStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxStorageTexturesInVertexStage: UInt
            get() = (maxStorageTexturesInVertexStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageTexturesInVertexStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxStorageBuffersInFragmentStage: UInt
            get() = (maxStorageBuffersInFragmentStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageBuffersInFragmentStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxStorageTexturesInFragmentStage: UInt
            get() = (maxStorageTexturesInFragmentStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageTexturesInFragmentStage_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUCompilationMessage : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var message: WGPUStringView
    actual var type: WGPUCompilationMessageType
    actual var lineNum: ULong
    actual var linePos: ULong
    actual var offset: ULong
    actual var length: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("message"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("lineNum"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("linePos"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("offset"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")
        ).withByteAlignment(8).withName("WGPUCompilationMessage")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val type_VH: VarHandle = layout.varHandle(groupElement("type"))
        val lineNum_VH: VarHandle = layout.varHandle(groupElement("lineNum"))
        val linePos_VH: VarHandle = layout.varHandle(groupElement("linePos"))
        val offset_VH: VarHandle = layout.varHandle(groupElement("offset"))
        val length_VH: VarHandle = layout.varHandle(groupElement("length"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCompilationMessage = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationMessage = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationMessage) -> Unit): ArrayHolder<WGPUCompilationMessage> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCompilationMessage {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var message: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("message")), Companion.layout.select(groupElement("message")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("message")), Companion.layout.select(groupElement("message")).byteSize())
            }
        override var type: WGPUCompilationMessageType
            get() = (type_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCompilationMessageType
            set(value) = type_VH.set(handler.handler, 0L, value.toInt())
        override var lineNum: ULong
            get() = (lineNum_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = lineNum_VH.set(handler.handler, 0L, value.toLong())
        override var linePos: ULong
            get() = (linePos_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = linePos_VH.set(handler.handler, 0L, value.toLong())
        override var offset: ULong
            get() = (offset_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = offset_VH.set(handler.handler, 0L, value.toLong())
        override var length: ULong
            get() = (length_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = length_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUConstantEntry : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var key: WGPUStringView
    actual var value: Double
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("key"),
            ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("value")
        ).withByteAlignment(8).withName("WGPUConstantEntry")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val value_VH: VarHandle = layout.varHandle(groupElement("value"))
        
        actual operator fun invoke(address: NativeAddress): WGPUConstantEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUConstantEntry = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUConstantEntry) -> Unit): ArrayHolder<WGPUConstantEntry> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUConstantEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var key: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("key")), Companion.layout.select(groupElement("key")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("key")), Companion.layout.select(groupElement("key")).byteSize())
            }
        override var value: Double
            get() = value_VH.get(handler.handler, 0L) as Double
            set(value) = value_VH.set(handler.handler, 0L, value)
    }
}

actual interface WGPUExtent3D : CStructure {
    actual var width: UInt
    actual var height: UInt
    actual var depthOrArrayLayers: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("width"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("height"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthOrArrayLayers")
        ).withByteAlignment(4).withName("WGPUExtent3D")
        
        val width_VH: VarHandle = layout.varHandle(groupElement("width"))
        val height_VH: VarHandle = layout.varHandle(groupElement("height"))
        val depthOrArrayLayers_VH: VarHandle = layout.varHandle(groupElement("depthOrArrayLayers"))
        
        actual operator fun invoke(address: NativeAddress): WGPUExtent3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExtent3D = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExtent3D) -> Unit): ArrayHolder<WGPUExtent3D> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUExtent3D {
        override var width: UInt
            get() = (width_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = width_VH.set(handler.handler, 0L, value.toInt())
        override var height: UInt
            get() = (height_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = height_VH.set(handler.handler, 0L, value.toInt())
        override var depthOrArrayLayers: UInt
            get() = (depthOrArrayLayers_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = depthOrArrayLayers_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUExternalTextureBindingEntry : CStructure {
    actual var chain: WGPUChainedStruct
    actual var externalTexture: WGPUExternalTexture?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("externalTexture")
        ).withByteAlignment(8).withName("WGPUExternalTextureBindingEntry")
        
        val externalTexture_VH: VarHandle = layout.varHandle(groupElement("externalTexture"))
        
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingEntry = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingEntry) -> Unit): ArrayHolder<WGPUExternalTextureBindingEntry> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUExternalTextureBindingEntry {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var externalTexture: WGPUExternalTexture?
            get() = (externalTexture_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUExternalTexture(it) }
            set(value) = externalTexture_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUExternalTextureBindingLayout : CStructure {
    actual var chain: WGPUChainedStruct
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain")
        ).withByteAlignment(8).withName("WGPUExternalTextureBindingLayout")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingLayout) -> Unit): ArrayHolder<WGPUExternalTextureBindingLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUExternalTextureBindingLayout {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
    }
}

actual interface WGPUFuture : CStructure {
    actual var id: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("id")
        ).withByteAlignment(8).withName("WGPUFuture")
        
        val id_VH: VarHandle = layout.varHandle(groupElement("id"))
        
        actual operator fun invoke(address: NativeAddress): WGPUFuture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFuture = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFuture) -> Unit): ArrayHolder<WGPUFuture> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUFuture {
        override var id: ULong
            get() = (id_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = id_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUInstanceLimits : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var timedWaitAnyMaxCount: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("timedWaitAnyMaxCount")
        ).withByteAlignment(8).withName("WGPUInstanceLimits")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val timedWaitAnyMaxCount_VH: VarHandle = layout.varHandle(groupElement("timedWaitAnyMaxCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPUInstanceLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceLimits = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceLimits) -> Unit): ArrayHolder<WGPUInstanceLimits> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUInstanceLimits {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var timedWaitAnyMaxCount: ULong
            get() = (timedWaitAnyMaxCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = timedWaitAnyMaxCount_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUMultisampleState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var count: UInt
    actual var mask: UInt
    actual var alphaToCoverageEnabled: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("count"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mask"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("alphaToCoverageEnabled"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUMultisampleState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val count_VH: VarHandle = layout.varHandle(groupElement("count"))
        val mask_VH: VarHandle = layout.varHandle(groupElement("mask"))
        val alphaToCoverageEnabled_VH: VarHandle = layout.varHandle(groupElement("alphaToCoverageEnabled"))
        
        actual operator fun invoke(address: NativeAddress): WGPUMultisampleState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUMultisampleState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUMultisampleState) -> Unit): ArrayHolder<WGPUMultisampleState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUMultisampleState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var count: UInt
            get() = (count_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = count_VH.set(handler.handler, 0L, value.toInt())
        override var mask: UInt
            get() = (mask_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mask_VH.set(handler.handler, 0L, value.toInt())
        override var alphaToCoverageEnabled: UInt
            get() = (alphaToCoverageEnabled_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = alphaToCoverageEnabled_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUOrigin3D : CStructure {
    actual var x: UInt
    actual var y: UInt
    actual var z: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("x"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("y"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("z")
        ).withByteAlignment(4).withName("WGPUOrigin3D")
        
        val x_VH: VarHandle = layout.varHandle(groupElement("x"))
        val y_VH: VarHandle = layout.varHandle(groupElement("y"))
        val z_VH: VarHandle = layout.varHandle(groupElement("z"))
        
        actual operator fun invoke(address: NativeAddress): WGPUOrigin3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUOrigin3D = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUOrigin3D) -> Unit): ArrayHolder<WGPUOrigin3D> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUOrigin3D {
        override var x: UInt
            get() = (x_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = x_VH.set(handler.handler, 0L, value.toInt())
        override var y: UInt
            get() = (y_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = y_VH.set(handler.handler, 0L, value.toInt())
        override var z: UInt
            get() = (z_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = z_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUPassTimestampWrites : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var querySet: WGPUQuerySet?
    actual var beginningOfPassWriteIndex: UInt
    actual var endOfPassWriteIndex: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("querySet"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("beginningOfPassWriteIndex"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("endOfPassWriteIndex")
        ).withByteAlignment(8).withName("WGPUPassTimestampWrites")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val querySet_VH: VarHandle = layout.varHandle(groupElement("querySet"))
        val beginningOfPassWriteIndex_VH: VarHandle = layout.varHandle(groupElement("beginningOfPassWriteIndex"))
        val endOfPassWriteIndex_VH: VarHandle = layout.varHandle(groupElement("endOfPassWriteIndex"))
        
        actual operator fun invoke(address: NativeAddress): WGPUPassTimestampWrites = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPassTimestampWrites = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPassTimestampWrites) -> Unit): ArrayHolder<WGPUPassTimestampWrites> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUPassTimestampWrites {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var querySet: WGPUQuerySet?
            get() = (querySet_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
            set(value) = querySet_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var beginningOfPassWriteIndex: UInt
            get() = (beginningOfPassWriteIndex_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = beginningOfPassWriteIndex_VH.set(handler.handler, 0L, value.toInt())
        override var endOfPassWriteIndex: UInt
            get() = (endOfPassWriteIndex_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = endOfPassWriteIndex_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUPipelineLayoutDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var bindGroupLayoutCount: ULong
    actual var bindGroupLayouts: NativeAddress?
    actual var immediateSize: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("bindGroupLayoutCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("bindGroupLayouts"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("immediateSize"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUPipelineLayoutDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val bindGroupLayoutCount_VH: VarHandle = layout.varHandle(groupElement("bindGroupLayoutCount"))
        val bindGroupLayouts_VH: VarHandle = layout.varHandle(groupElement("bindGroupLayouts"))
        val immediateSize_VH: VarHandle = layout.varHandle(groupElement("immediateSize"))
        
        actual operator fun invoke(address: NativeAddress): WGPUPipelineLayoutDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPipelineLayoutDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPipelineLayoutDescriptor) -> Unit): ArrayHolder<WGPUPipelineLayoutDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUPipelineLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var bindGroupLayoutCount: ULong
            get() = (bindGroupLayoutCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = bindGroupLayoutCount_VH.set(handler.handler, 0L, value.toLong())
        override var bindGroupLayouts: NativeAddress?
            get() = (bindGroupLayouts_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = bindGroupLayouts_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var immediateSize: UInt
            get() = (immediateSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = immediateSize_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUPrimitiveState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var topology: WGPUPrimitiveTopology
    actual var stripIndexFormat: WGPUIndexFormat
    actual var frontFace: WGPUFrontFace
    actual var cullMode: WGPUCullMode
    actual var unclippedDepth: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("topology"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stripIndexFormat"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("frontFace"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("cullMode"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("unclippedDepth"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUPrimitiveState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val topology_VH: VarHandle = layout.varHandle(groupElement("topology"))
        val stripIndexFormat_VH: VarHandle = layout.varHandle(groupElement("stripIndexFormat"))
        val frontFace_VH: VarHandle = layout.varHandle(groupElement("frontFace"))
        val cullMode_VH: VarHandle = layout.varHandle(groupElement("cullMode"))
        val unclippedDepth_VH: VarHandle = layout.varHandle(groupElement("unclippedDepth"))
        
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveState) -> Unit): ArrayHolder<WGPUPrimitiveState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUPrimitiveState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var topology: WGPUPrimitiveTopology
            get() = (topology_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUPrimitiveTopology
            set(value) = topology_VH.set(handler.handler, 0L, value.toInt())
        override var stripIndexFormat: WGPUIndexFormat
            get() = (stripIndexFormat_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUIndexFormat
            set(value) = stripIndexFormat_VH.set(handler.handler, 0L, value.toInt())
        override var frontFace: WGPUFrontFace
            get() = (frontFace_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUFrontFace
            set(value) = frontFace_VH.set(handler.handler, 0L, value.toInt())
        override var cullMode: WGPUCullMode
            get() = (cullMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCullMode
            set(value) = cullMode_VH.set(handler.handler, 0L, value.toInt())
        override var unclippedDepth: UInt
            get() = (unclippedDepth_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = unclippedDepth_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUQuerySetDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var type: WGPUQueryType
    actual var count: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("count")
        ).withByteAlignment(8).withName("WGPUQuerySetDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val type_VH: VarHandle = layout.varHandle(groupElement("type"))
        val count_VH: VarHandle = layout.varHandle(groupElement("count"))
        
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptor) -> Unit): ArrayHolder<WGPUQuerySetDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUQuerySetDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var type: WGPUQueryType
            get() = (type_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUQueryType
            set(value) = type_VH.set(handler.handler, 0L, value.toInt())
        override var count: UInt
            get() = (count_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = count_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUQueueDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPUQueueDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPUQueueDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueDescriptor) -> Unit): ArrayHolder<WGPUQueueDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUQueueDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPURenderBundleDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPURenderBundleDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleDescriptor) -> Unit): ArrayHolder<WGPURenderBundleDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderBundleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPURenderBundleEncoderDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var colorFormatCount: ULong
    actual var colorFormats: NativeAddress?
    actual var depthStencilFormat: WGPUTextureFormat
    actual var sampleCount: UInt
    actual var depthReadOnly: UInt
    actual var stencilReadOnly: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("colorFormatCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("colorFormats"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthStencilFormat"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("sampleCount"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthReadOnly"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilReadOnly")
        ).withByteAlignment(8).withName("WGPURenderBundleEncoderDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val colorFormatCount_VH: VarHandle = layout.varHandle(groupElement("colorFormatCount"))
        val colorFormats_VH: VarHandle = layout.varHandle(groupElement("colorFormats"))
        val depthStencilFormat_VH: VarHandle = layout.varHandle(groupElement("depthStencilFormat"))
        val sampleCount_VH: VarHandle = layout.varHandle(groupElement("sampleCount"))
        val depthReadOnly_VH: VarHandle = layout.varHandle(groupElement("depthReadOnly"))
        val stencilReadOnly_VH: VarHandle = layout.varHandle(groupElement("stencilReadOnly"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleEncoderDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleEncoderDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleEncoderDescriptor) -> Unit): ArrayHolder<WGPURenderBundleEncoderDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderBundleEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var colorFormatCount: ULong
            get() = (colorFormatCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = colorFormatCount_VH.set(handler.handler, 0L, value.toLong())
        override var colorFormats: NativeAddress?
            get() = (colorFormats_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = colorFormats_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var depthStencilFormat: WGPUTextureFormat
            get() = (depthStencilFormat_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = depthStencilFormat_VH.set(handler.handler, 0L, value.toInt())
        override var sampleCount: UInt
            get() = (sampleCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = sampleCount_VH.set(handler.handler, 0L, value.toInt())
        override var depthReadOnly: UInt
            get() = (depthReadOnly_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = depthReadOnly_VH.set(handler.handler, 0L, value.toInt())
        override var stencilReadOnly: UInt
            get() = (stencilReadOnly_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = stencilReadOnly_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPURenderPassDepthStencilAttachment : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var view: WGPUTextureView?
    actual var depthLoadOp: WGPULoadOp
    actual var depthStoreOp: WGPUStoreOp
    actual var depthClearValue: Float
    actual var depthReadOnly: UInt
    actual var stencilLoadOp: WGPULoadOp
    actual var stencilStoreOp: WGPUStoreOp
    actual var stencilClearValue: UInt
    actual var stencilReadOnly: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("view"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthLoadOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthStoreOp"),
            ValueLayout.JAVA_FLOAT.withByteAlignment(4).withName("depthClearValue"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthReadOnly"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilLoadOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilStoreOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilClearValue"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilReadOnly")
        ).withByteAlignment(8).withName("WGPURenderPassDepthStencilAttachment")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val view_VH: VarHandle = layout.varHandle(groupElement("view"))
        val depthLoadOp_VH: VarHandle = layout.varHandle(groupElement("depthLoadOp"))
        val depthStoreOp_VH: VarHandle = layout.varHandle(groupElement("depthStoreOp"))
        val depthClearValue_VH: VarHandle = layout.varHandle(groupElement("depthClearValue"))
        val depthReadOnly_VH: VarHandle = layout.varHandle(groupElement("depthReadOnly"))
        val stencilLoadOp_VH: VarHandle = layout.varHandle(groupElement("stencilLoadOp"))
        val stencilStoreOp_VH: VarHandle = layout.varHandle(groupElement("stencilStoreOp"))
        val stencilClearValue_VH: VarHandle = layout.varHandle(groupElement("stencilClearValue"))
        val stencilReadOnly_VH: VarHandle = layout.varHandle(groupElement("stencilReadOnly"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDepthStencilAttachment = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDepthStencilAttachment = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDepthStencilAttachment) -> Unit): ArrayHolder<WGPURenderPassDepthStencilAttachment> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderPassDepthStencilAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var view: WGPUTextureView?
            get() = (view_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) = view_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var depthLoadOp: WGPULoadOp
            get() = (depthLoadOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPULoadOp
            set(value) = depthLoadOp_VH.set(handler.handler, 0L, value.toInt())
        override var depthStoreOp: WGPUStoreOp
            get() = (depthStoreOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStoreOp
            set(value) = depthStoreOp_VH.set(handler.handler, 0L, value.toInt())
        override var depthClearValue: Float
            get() = depthClearValue_VH.get(handler.handler, 0L) as Float
            set(value) = depthClearValue_VH.set(handler.handler, 0L, value)
        override var depthReadOnly: UInt
            get() = (depthReadOnly_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = depthReadOnly_VH.set(handler.handler, 0L, value.toInt())
        override var stencilLoadOp: WGPULoadOp
            get() = (stencilLoadOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPULoadOp
            set(value) = stencilLoadOp_VH.set(handler.handler, 0L, value.toInt())
        override var stencilStoreOp: WGPUStoreOp
            get() = (stencilStoreOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStoreOp
            set(value) = stencilStoreOp_VH.set(handler.handler, 0L, value.toInt())
        override var stencilClearValue: UInt
            get() = (stencilClearValue_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = stencilClearValue_VH.set(handler.handler, 0L, value.toInt())
        override var stencilReadOnly: UInt
            get() = (stencilReadOnly_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = stencilReadOnly_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPURenderPassMaxDrawCount : CStructure {
    actual var chain: WGPUChainedStruct
    actual var maxDrawCount: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("maxDrawCount")
        ).withByteAlignment(8).withName("WGPURenderPassMaxDrawCount")
        
        val maxDrawCount_VH: VarHandle = layout.varHandle(groupElement("maxDrawCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderPassMaxDrawCount = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassMaxDrawCount = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassMaxDrawCount) -> Unit): ArrayHolder<WGPURenderPassMaxDrawCount> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderPassMaxDrawCount {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var maxDrawCount: ULong
            get() = (maxDrawCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = maxDrawCount_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPURequestAdapterWebXROptions : CStructure {
    actual var chain: WGPUChainedStruct
    actual var xrCompatible: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("xrCompatible"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPURequestAdapterWebXROptions")
        
        val xrCompatible_VH: VarHandle = layout.varHandle(groupElement("xrCompatible"))
        
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterWebXROptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterWebXROptions = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterWebXROptions) -> Unit): ArrayHolder<WGPURequestAdapterWebXROptions> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURequestAdapterWebXROptions {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var xrCompatible: UInt
            get() = (xrCompatible_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = xrCompatible_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSamplerBindingLayout : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUSamplerBindingType
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUSamplerBindingLayout")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val type_VH: VarHandle = layout.varHandle(groupElement("type"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSamplerBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerBindingLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerBindingLayout) -> Unit): ArrayHolder<WGPUSamplerBindingLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSamplerBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var type: WGPUSamplerBindingType
            get() = (type_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUSamplerBindingType
            set(value) = type_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSamplerDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var addressModeU: WGPUAddressMode
    actual var addressModeV: WGPUAddressMode
    actual var addressModeW: WGPUAddressMode
    actual var magFilter: WGPUFilterMode
    actual var minFilter: WGPUFilterMode
    actual var mipmapFilter: WGPUMipmapFilterMode
    actual var lodMinClamp: Float
    actual var lodMaxClamp: Float
    actual var compare: WGPUCompareFunction
    actual var maxAnisotropy: UShort
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("addressModeU"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("addressModeV"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("addressModeW"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("magFilter"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("minFilter"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mipmapFilter"),
            ValueLayout.JAVA_FLOAT.withByteAlignment(4).withName("lodMinClamp"),
            ValueLayout.JAVA_FLOAT.withByteAlignment(4).withName("lodMaxClamp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("compare"),
            ValueLayout.JAVA_SHORT.withByteAlignment(2).withName("maxAnisotropy"),
            java.lang.foreign.MemoryLayout.paddingLayout(2)
        ).withByteAlignment(8).withName("WGPUSamplerDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val addressModeU_VH: VarHandle = layout.varHandle(groupElement("addressModeU"))
        val addressModeV_VH: VarHandle = layout.varHandle(groupElement("addressModeV"))
        val addressModeW_VH: VarHandle = layout.varHandle(groupElement("addressModeW"))
        val magFilter_VH: VarHandle = layout.varHandle(groupElement("magFilter"))
        val minFilter_VH: VarHandle = layout.varHandle(groupElement("minFilter"))
        val mipmapFilter_VH: VarHandle = layout.varHandle(groupElement("mipmapFilter"))
        val lodMinClamp_VH: VarHandle = layout.varHandle(groupElement("lodMinClamp"))
        val lodMaxClamp_VH: VarHandle = layout.varHandle(groupElement("lodMaxClamp"))
        val compare_VH: VarHandle = layout.varHandle(groupElement("compare"))
        val maxAnisotropy_VH: VarHandle = layout.varHandle(groupElement("maxAnisotropy"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptor) -> Unit): ArrayHolder<WGPUSamplerDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSamplerDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var addressModeU: WGPUAddressMode
            get() = (addressModeU_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUAddressMode
            set(value) = addressModeU_VH.set(handler.handler, 0L, value.toInt())
        override var addressModeV: WGPUAddressMode
            get() = (addressModeV_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUAddressMode
            set(value) = addressModeV_VH.set(handler.handler, 0L, value.toInt())
        override var addressModeW: WGPUAddressMode
            get() = (addressModeW_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUAddressMode
            set(value) = addressModeW_VH.set(handler.handler, 0L, value.toInt())
        override var magFilter: WGPUFilterMode
            get() = (magFilter_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUFilterMode
            set(value) = magFilter_VH.set(handler.handler, 0L, value.toInt())
        override var minFilter: WGPUFilterMode
            get() = (minFilter_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUFilterMode
            set(value) = minFilter_VH.set(handler.handler, 0L, value.toInt())
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = (mipmapFilter_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUMipmapFilterMode
            set(value) = mipmapFilter_VH.set(handler.handler, 0L, value.toInt())
        override var lodMinClamp: Float
            get() = lodMinClamp_VH.get(handler.handler, 0L) as Float
            set(value) = lodMinClamp_VH.set(handler.handler, 0L, value)
        override var lodMaxClamp: Float
            get() = lodMaxClamp_VH.get(handler.handler, 0L) as Float
            set(value) = lodMaxClamp_VH.set(handler.handler, 0L, value)
        override var compare: WGPUCompareFunction
            get() = (compare_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCompareFunction
            set(value) = compare_VH.set(handler.handler, 0L, value.toInt())
        override var maxAnisotropy: UShort
            get() = (maxAnisotropy_VH.get(handler.handler, 0L) as Short).toUShort() as UShort
            set(value) = maxAnisotropy_VH.set(handler.handler, 0L, value.toShort())
    }
}

actual interface WGPUShaderSourceSPIRV : CStructure {
    actual var chain: WGPUChainedStruct
    actual var codeSize: UInt
    actual var code: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("codeSize"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("code")
        ).withByteAlignment(8).withName("WGPUShaderSourceSPIRV")
        
        val codeSize_VH: VarHandle = layout.varHandle(groupElement("codeSize"))
        val code_VH: VarHandle = layout.varHandle(groupElement("code"))
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceSPIRV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceSPIRV = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceSPIRV) -> Unit): ArrayHolder<WGPUShaderSourceSPIRV> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderSourceSPIRV {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var codeSize: UInt
            get() = (codeSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = codeSize_VH.set(handler.handler, 0L, value.toInt())
        override var code: NativeAddress?
            get() = (code_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = code_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUShaderSourceWGSL : CStructure {
    actual var chain: WGPUChainedStruct
    actual var code: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("code")
        ).withByteAlignment(8).withName("WGPUShaderSourceWGSL")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceWGSL = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceWGSL = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceWGSL) -> Unit): ArrayHolder<WGPUShaderSourceWGSL> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderSourceWGSL {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var code: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("code")), Companion.layout.select(groupElement("code")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("code")), Companion.layout.select(groupElement("code")).byteSize())
            }
    }
}

actual interface WGPUStencilFaceState : CStructure {
    actual var compare: WGPUCompareFunction
    actual var failOp: WGPUStencilOperation
    actual var depthFailOp: WGPUStencilOperation
    actual var passOp: WGPUStencilOperation
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("compare"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("failOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthFailOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("passOp")
        ).withByteAlignment(4).withName("WGPUStencilFaceState")
        
        val compare_VH: VarHandle = layout.varHandle(groupElement("compare"))
        val failOp_VH: VarHandle = layout.varHandle(groupElement("failOp"))
        val depthFailOp_VH: VarHandle = layout.varHandle(groupElement("depthFailOp"))
        val passOp_VH: VarHandle = layout.varHandle(groupElement("passOp"))
        
        actual operator fun invoke(address: NativeAddress): WGPUStencilFaceState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStencilFaceState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStencilFaceState) -> Unit): ArrayHolder<WGPUStencilFaceState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUStencilFaceState {
        override var compare: WGPUCompareFunction
            get() = (compare_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCompareFunction
            set(value) = compare_VH.set(handler.handler, 0L, value.toInt())
        override var failOp: WGPUStencilOperation
            get() = (failOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStencilOperation
            set(value) = failOp_VH.set(handler.handler, 0L, value.toInt())
        override var depthFailOp: WGPUStencilOperation
            get() = (depthFailOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStencilOperation
            set(value) = depthFailOp_VH.set(handler.handler, 0L, value.toInt())
        override var passOp: WGPUStencilOperation
            get() = (passOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStencilOperation
            set(value) = passOp_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUStorageTextureBindingLayout : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var access: WGPUStorageTextureAccess
    actual var format: WGPUTextureFormat
    actual var viewDimension: WGPUTextureViewDimension
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("access"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("viewDimension"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUStorageTextureBindingLayout")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val access_VH: VarHandle = layout.varHandle(groupElement("access"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val viewDimension_VH: VarHandle = layout.varHandle(groupElement("viewDimension"))
        
        actual operator fun invoke(address: NativeAddress): WGPUStorageTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStorageTextureBindingLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStorageTextureBindingLayout) -> Unit): ArrayHolder<WGPUStorageTextureBindingLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUStorageTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var access: WGPUStorageTextureAccess
            get() = (access_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStorageTextureAccess
            set(value) = access_VH.set(handler.handler, 0L, value.toInt())
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var viewDimension: WGPUTextureViewDimension
            get() = (viewDimension_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureViewDimension
            set(value) = viewDimension_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSupportedFeatures : CStructure {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("featureCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("features")
        ).withByteAlignment(8).withName("WGPUSupportedFeatures")
        
        val featureCount_VH: VarHandle = layout.varHandle(groupElement("featureCount"))
        val features_VH: VarHandle = layout.varHandle(groupElement("features"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSupportedFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedFeatures = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedFeatures) -> Unit): ArrayHolder<WGPUSupportedFeatures> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSupportedFeatures {
        override var featureCount: ULong
            get() = (featureCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = featureCount_VH.set(handler.handler, 0L, value.toLong())
        override var features: NativeAddress?
            get() = (features_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = features_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSupportedInstanceFeatures : CStructure {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("featureCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("features")
        ).withByteAlignment(8).withName("WGPUSupportedInstanceFeatures")
        
        val featureCount_VH: VarHandle = layout.varHandle(groupElement("featureCount"))
        val features_VH: VarHandle = layout.varHandle(groupElement("features"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSupportedInstanceFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedInstanceFeatures = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedInstanceFeatures) -> Unit): ArrayHolder<WGPUSupportedInstanceFeatures> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSupportedInstanceFeatures {
        override var featureCount: ULong
            get() = (featureCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = featureCount_VH.set(handler.handler, 0L, value.toLong())
        override var features: NativeAddress?
            get() = (features_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = features_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSupportedWGSLLanguageFeatures : CStructure {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("featureCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("features")
        ).withByteAlignment(8).withName("WGPUSupportedWGSLLanguageFeatures")
        
        val featureCount_VH: VarHandle = layout.varHandle(groupElement("featureCount"))
        val features_VH: VarHandle = layout.varHandle(groupElement("features"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSupportedWGSLLanguageFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedWGSLLanguageFeatures = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedWGSLLanguageFeatures) -> Unit): ArrayHolder<WGPUSupportedWGSLLanguageFeatures> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSupportedWGSLLanguageFeatures {
        override var featureCount: ULong
            get() = (featureCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = featureCount_VH.set(handler.handler, 0L, value.toLong())
        override var features: NativeAddress?
            get() = (features_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = features_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceCapabilities : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var usages: ULong
    actual var formatCount: ULong
    actual var formats: NativeAddress?
    actual var presentModeCount: ULong
    actual var presentModes: NativeAddress?
    actual var alphaModeCount: ULong
    actual var alphaModes: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("usages"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("formatCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("formats"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("presentModeCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("presentModes"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("alphaModeCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("alphaModes")
        ).withByteAlignment(8).withName("WGPUSurfaceCapabilities")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val usages_VH: VarHandle = layout.varHandle(groupElement("usages"))
        val formatCount_VH: VarHandle = layout.varHandle(groupElement("formatCount"))
        val formats_VH: VarHandle = layout.varHandle(groupElement("formats"))
        val presentModeCount_VH: VarHandle = layout.varHandle(groupElement("presentModeCount"))
        val presentModes_VH: VarHandle = layout.varHandle(groupElement("presentModes"))
        val alphaModeCount_VH: VarHandle = layout.varHandle(groupElement("alphaModeCount"))
        val alphaModes_VH: VarHandle = layout.varHandle(groupElement("alphaModes"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceCapabilities = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceCapabilities = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceCapabilities) -> Unit): ArrayHolder<WGPUSurfaceCapabilities> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceCapabilities {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var usages: ULong
            get() = (usages_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = usages_VH.set(handler.handler, 0L, value.toLong())
        override var formatCount: ULong
            get() = (formatCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = formatCount_VH.set(handler.handler, 0L, value.toLong())
        override var formats: NativeAddress?
            get() = (formats_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = formats_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var presentModeCount: ULong
            get() = (presentModeCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = presentModeCount_VH.set(handler.handler, 0L, value.toLong())
        override var presentModes: NativeAddress?
            get() = (presentModes_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = presentModes_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var alphaModeCount: ULong
            get() = (alphaModeCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = alphaModeCount_VH.set(handler.handler, 0L, value.toLong())
        override var alphaModes: NativeAddress?
            get() = (alphaModes_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = alphaModes_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceColorManagement : CStructure {
    actual var chain: WGPUChainedStruct
    actual var colorSpace: WGPUPredefinedColorSpace
    actual var toneMappingMode: WGPUToneMappingMode
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("colorSpace"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("toneMappingMode")
        ).withByteAlignment(8).withName("WGPUSurfaceColorManagement")
        
        val colorSpace_VH: VarHandle = layout.varHandle(groupElement("colorSpace"))
        val toneMappingMode_VH: VarHandle = layout.varHandle(groupElement("toneMappingMode"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceColorManagement = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceColorManagement = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceColorManagement) -> Unit): ArrayHolder<WGPUSurfaceColorManagement> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceColorManagement {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = (colorSpace_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUPredefinedColorSpace
            set(value) = colorSpace_VH.set(handler.handler, 0L, value.toInt())
        override var toneMappingMode: WGPUToneMappingMode
            get() = (toneMappingMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUToneMappingMode
            set(value) = toneMappingMode_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSurfaceConfiguration : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var device: WGPUDevice?
    actual var format: WGPUTextureFormat
    actual var usage: ULong
    actual var width: UInt
    actual var height: UInt
    actual var viewFormatCount: ULong
    actual var viewFormats: NativeAddress?
    actual var alphaMode: WGPUCompositeAlphaMode
    actual var presentMode: WGPUPresentMode
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("device"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("usage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("width"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("height"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("viewFormatCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("viewFormats"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("alphaMode"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("presentMode")
        ).withByteAlignment(8).withName("WGPUSurfaceConfiguration")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val device_VH: VarHandle = layout.varHandle(groupElement("device"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val usage_VH: VarHandle = layout.varHandle(groupElement("usage"))
        val width_VH: VarHandle = layout.varHandle(groupElement("width"))
        val height_VH: VarHandle = layout.varHandle(groupElement("height"))
        val viewFormatCount_VH: VarHandle = layout.varHandle(groupElement("viewFormatCount"))
        val viewFormats_VH: VarHandle = layout.varHandle(groupElement("viewFormats"))
        val alphaMode_VH: VarHandle = layout.varHandle(groupElement("alphaMode"))
        val presentMode_VH: VarHandle = layout.varHandle(groupElement("presentMode"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfiguration = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfiguration = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfiguration) -> Unit): ArrayHolder<WGPUSurfaceConfiguration> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceConfiguration {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var device: WGPUDevice?
            get() = (device_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUDevice(it) }
            set(value) = device_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var usage: ULong
            get() = (usage_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = usage_VH.set(handler.handler, 0L, value.toLong())
        override var width: UInt
            get() = (width_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = width_VH.set(handler.handler, 0L, value.toInt())
        override var height: UInt
            get() = (height_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = height_VH.set(handler.handler, 0L, value.toInt())
        override var viewFormatCount: ULong
            get() = (viewFormatCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = viewFormatCount_VH.set(handler.handler, 0L, value.toLong())
        override var viewFormats: NativeAddress?
            get() = (viewFormats_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = viewFormats_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var alphaMode: WGPUCompositeAlphaMode
            get() = (alphaMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCompositeAlphaMode
            set(value) = alphaMode_VH.set(handler.handler, 0L, value.toInt())
        override var presentMode: WGPUPresentMode
            get() = (presentMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUPresentMode
            set(value) = presentMode_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSurfaceSourceAndroidNativeWindow : CStructure {
    actual var chain: WGPUChainedStruct
    actual var window: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("window")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceAndroidNativeWindow")
        
        val window_VH: VarHandle = layout.varHandle(groupElement("window"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceAndroidNativeWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceAndroidNativeWindow = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceAndroidNativeWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceAndroidNativeWindow {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var window: NativeAddress?
            get() = (window_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = window_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceSourceMetalLayer : CStructure {
    actual var chain: WGPUChainedStruct
    actual var layer: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("layer")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceMetalLayer")
        
        val layer_VH: VarHandle = layout.varHandle(groupElement("layer"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceMetalLayer = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceMetalLayer = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceMetalLayer) -> Unit): ArrayHolder<WGPUSurfaceSourceMetalLayer> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceMetalLayer {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var layer: NativeAddress?
            get() = (layer_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = layer_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceSourceWaylandSurface : CStructure {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var surface: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("display"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("surface")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceWaylandSurface")
        
        val display_VH: VarHandle = layout.varHandle(groupElement("display"))
        val surface_VH: VarHandle = layout.varHandle(groupElement("surface"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWaylandSurface = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWaylandSurface = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWaylandSurface) -> Unit): ArrayHolder<WGPUSurfaceSourceWaylandSurface> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceWaylandSurface {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var display: NativeAddress?
            get() = (display_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = display_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var surface: NativeAddress?
            get() = (surface_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = surface_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceSourceWindowsHWND : CStructure {
    actual var chain: WGPUChainedStruct
    actual var hinstance: NativeAddress?
    actual var hwnd: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("hinstance"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("hwnd")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceWindowsHWND")
        
        val hinstance_VH: VarHandle = layout.varHandle(groupElement("hinstance"))
        val hwnd_VH: VarHandle = layout.varHandle(groupElement("hwnd"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWindowsHWND = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWindowsHWND = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWindowsHWND) -> Unit): ArrayHolder<WGPUSurfaceSourceWindowsHWND> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceWindowsHWND {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var hinstance: NativeAddress?
            get() = (hinstance_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = hinstance_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var hwnd: NativeAddress?
            get() = (hwnd_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = hwnd_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUSurfaceSourceXCBWindow : CStructure {
    actual var chain: WGPUChainedStruct
    actual var connection: NativeAddress?
    actual var window: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("connection"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("window"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUSurfaceSourceXCBWindow")
        
        val connection_VH: VarHandle = layout.varHandle(groupElement("connection"))
        val window_VH: VarHandle = layout.varHandle(groupElement("window"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXCBWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXCBWindow = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXCBWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXCBWindow> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceXCBWindow {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var connection: NativeAddress?
            get() = (connection_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = connection_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var window: UInt
            get() = (window_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = window_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSurfaceSourceXlibWindow : CStructure {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var window: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("display"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("window")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceXlibWindow")
        
        val display_VH: VarHandle = layout.varHandle(groupElement("display"))
        val window_VH: VarHandle = layout.varHandle(groupElement("window"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXlibWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXlibWindow = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXlibWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXlibWindow> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceXlibWindow {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var display: NativeAddress?
            get() = (display_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = display_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var window: ULong
            get() = (window_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = window_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUSurfaceTexture : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var texture: WGPUTexture?
    actual var status: WGPUSurfaceGetCurrentTextureStatus
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("texture"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("status"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUSurfaceTexture")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val texture_VH: VarHandle = layout.varHandle(groupElement("texture"))
        val status_VH: VarHandle = layout.varHandle(groupElement("status"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceTexture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceTexture = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceTexture) -> Unit): ArrayHolder<WGPUSurfaceTexture> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceTexture {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var texture: WGPUTexture?
            get() = (texture_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTexture(it) }
            set(value) = texture_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = (status_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUSurfaceGetCurrentTextureStatus
            set(value) = status_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUTexelCopyBufferLayout : CStructure {
    actual var offset: ULong
    actual var bytesPerRow: UInt
    actual var rowsPerImage: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("offset"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("bytesPerRow"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("rowsPerImage")
        ).withByteAlignment(8).withName("WGPUTexelCopyBufferLayout")
        
        val offset_VH: VarHandle = layout.varHandle(groupElement("offset"))
        val bytesPerRow_VH: VarHandle = layout.varHandle(groupElement("bytesPerRow"))
        val rowsPerImage_VH: VarHandle = layout.varHandle(groupElement("rowsPerImage"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferLayout) -> Unit): ArrayHolder<WGPUTexelCopyBufferLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTexelCopyBufferLayout {
        override var offset: ULong
            get() = (offset_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = offset_VH.set(handler.handler, 0L, value.toLong())
        override var bytesPerRow: UInt
            get() = (bytesPerRow_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = bytesPerRow_VH.set(handler.handler, 0L, value.toInt())
        override var rowsPerImage: UInt
            get() = (rowsPerImage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = rowsPerImage_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUTextureBindingLayout : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var sampleType: WGPUTextureSampleType
    actual var viewDimension: WGPUTextureViewDimension
    actual var multisampled: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("sampleType"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("viewDimension"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("multisampled"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUTextureBindingLayout")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val sampleType_VH: VarHandle = layout.varHandle(groupElement("sampleType"))
        val viewDimension_VH: VarHandle = layout.varHandle(groupElement("viewDimension"))
        val multisampled_VH: VarHandle = layout.varHandle(groupElement("multisampled"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingLayout) -> Unit): ArrayHolder<WGPUTextureBindingLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var sampleType: WGPUTextureSampleType
            get() = (sampleType_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureSampleType
            set(value) = sampleType_VH.set(handler.handler, 0L, value.toInt())
        override var viewDimension: WGPUTextureViewDimension
            get() = (viewDimension_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureViewDimension
            set(value) = viewDimension_VH.set(handler.handler, 0L, value.toInt())
        override var multisampled: UInt
            get() = (multisampled_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = multisampled_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUTextureBindingViewDimension : CStructure {
    actual var chain: WGPUChainedStruct
    actual var textureBindingViewDimension: WGPUTextureViewDimension
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("textureBindingViewDimension"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUTextureBindingViewDimension")
        
        val textureBindingViewDimension_VH: VarHandle = layout.varHandle(groupElement("textureBindingViewDimension"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingViewDimension = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingViewDimension = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingViewDimension) -> Unit): ArrayHolder<WGPUTextureBindingViewDimension> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureBindingViewDimension {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = (textureBindingViewDimension_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureViewDimension
            set(value) = textureBindingViewDimension_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUTextureComponentSwizzle : CStructure {
    actual var r: WGPUComponentSwizzle
    actual var g: WGPUComponentSwizzle
    actual var b: WGPUComponentSwizzle
    actual var a: WGPUComponentSwizzle
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("r"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("g"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("b"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("a")
        ).withByteAlignment(4).withName("WGPUTextureComponentSwizzle")
        
        val r_VH: VarHandle = layout.varHandle(groupElement("r"))
        val g_VH: VarHandle = layout.varHandle(groupElement("g"))
        val b_VH: VarHandle = layout.varHandle(groupElement("b"))
        val a_VH: VarHandle = layout.varHandle(groupElement("a"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzle = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzle) -> Unit): ArrayHolder<WGPUTextureComponentSwizzle> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureComponentSwizzle {
        override var r: WGPUComponentSwizzle
            get() = (r_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUComponentSwizzle
            set(value) = r_VH.set(handler.handler, 0L, value.toInt())
        override var g: WGPUComponentSwizzle
            get() = (g_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUComponentSwizzle
            set(value) = g_VH.set(handler.handler, 0L, value.toInt())
        override var b: WGPUComponentSwizzle
            get() = (b_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUComponentSwizzle
            set(value) = b_VH.set(handler.handler, 0L, value.toInt())
        override var a: WGPUComponentSwizzle
            get() = (a_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUComponentSwizzle
            set(value) = a_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUVertexAttribute : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUVertexFormat
    actual var offset: ULong
    actual var shaderLocation: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("offset"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("shaderLocation"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUVertexAttribute")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val offset_VH: VarHandle = layout.varHandle(groupElement("offset"))
        val shaderLocation_VH: VarHandle = layout.varHandle(groupElement("shaderLocation"))
        
        actual operator fun invoke(address: NativeAddress): WGPUVertexAttribute = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexAttribute = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexAttribute) -> Unit): ArrayHolder<WGPUVertexAttribute> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUVertexAttribute {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var format: WGPUVertexFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUVertexFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var offset: ULong
            get() = (offset_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = offset_VH.set(handler.handler, 0L, value.toLong())
        override var shaderLocation: UInt
            get() = (shaderLocation_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = shaderLocation_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUBindGroupEntry : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var binding: UInt
    actual var buffer: WGPUBuffer?
    actual var offset: ULong
    actual var size: ULong
    actual var sampler: WGPUSampler?
    actual var textureView: WGPUTextureView?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("binding"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("buffer"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("offset"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("size"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("sampler"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("textureView")
        ).withByteAlignment(8).withName("WGPUBindGroupEntry")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val binding_VH: VarHandle = layout.varHandle(groupElement("binding"))
        val buffer_VH: VarHandle = layout.varHandle(groupElement("buffer"))
        val offset_VH: VarHandle = layout.varHandle(groupElement("offset"))
        val size_VH: VarHandle = layout.varHandle(groupElement("size"))
        val sampler_VH: VarHandle = layout.varHandle(groupElement("sampler"))
        val textureView_VH: VarHandle = layout.varHandle(groupElement("textureView"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntry = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntry) -> Unit): ArrayHolder<WGPUBindGroupEntry> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var binding: UInt
            get() = (binding_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = binding_VH.set(handler.handler, 0L, value.toInt())
        override var buffer: WGPUBuffer?
            get() = (buffer_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBuffer(it) }
            set(value) = buffer_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var offset: ULong
            get() = (offset_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = offset_VH.set(handler.handler, 0L, value.toLong())
        override var size: ULong
            get() = (size_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = size_VH.set(handler.handler, 0L, value.toLong())
        override var sampler: WGPUSampler?
            get() = (sampler_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUSampler(it) }
            set(value) = sampler_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var textureView: WGPUTextureView?
            get() = (textureView_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) = textureView_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUBindGroupLayoutEntry : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var binding: UInt
    actual var visibility: ULong
    actual var bindingArraySize: UInt
    actual var buffer: WGPUBufferBindingLayout
    actual var sampler: WGPUSamplerBindingLayout
    actual var texture: WGPUTextureBindingLayout
    actual var storageTexture: WGPUStorageTextureBindingLayout
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("binding"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("visibility"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("bindingArraySize"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("hasDynamicOffset"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("minBindingSize")).withByteAlignment(8).withName("WGPUBufferBindingLayout").withName("buffer"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUSamplerBindingLayout").withName("sampler"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sampleType"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("viewDimension"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("multisampled"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUTextureBindingLayout").withName("texture"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("access"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("viewDimension"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUStorageTextureBindingLayout").withName("storageTexture")
        ).withByteAlignment(8).withName("WGPUBindGroupLayoutEntry")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val binding_VH: VarHandle = layout.varHandle(groupElement("binding"))
        val visibility_VH: VarHandle = layout.varHandle(groupElement("visibility"))
        val bindingArraySize_VH: VarHandle = layout.varHandle(groupElement("bindingArraySize"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntry = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntry) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntry> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var binding: UInt
            get() = (binding_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = binding_VH.set(handler.handler, 0L, value.toInt())
        override var visibility: ULong
            get() = (visibility_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = visibility_VH.set(handler.handler, 0L, value.toLong())
        override var bindingArraySize: UInt
            get() = (bindingArraySize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = bindingArraySize_VH.set(handler.handler, 0L, value.toInt())
        override var buffer: WGPUBufferBindingLayout
            get() = WGPUBufferBindingLayout(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("buffer")), Companion.layout.select(groupElement("buffer")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("buffer")), Companion.layout.select(groupElement("buffer")).byteSize())
            }
        override var sampler: WGPUSamplerBindingLayout
            get() = WGPUSamplerBindingLayout(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("sampler")), Companion.layout.select(groupElement("sampler")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("sampler")), Companion.layout.select(groupElement("sampler")).byteSize())
            }
        override var texture: WGPUTextureBindingLayout
            get() = WGPUTextureBindingLayout(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("texture")), Companion.layout.select(groupElement("texture")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("texture")), Companion.layout.select(groupElement("texture")).byteSize())
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() = WGPUStorageTextureBindingLayout(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("storageTexture")), Companion.layout.select(groupElement("storageTexture")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("storageTexture")), Companion.layout.select(groupElement("storageTexture")).byteSize())
            }
    }
}

actual interface WGPUBlendState : CStructure {
    actual var color: WGPUBlendComponent
    actual var alpha: WGPUBlendComponent
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("operation"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("srcFactor"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("dstFactor")).withByteAlignment(4).withName("WGPUBlendComponent").withName("color"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("operation"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("srcFactor"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("dstFactor")).withByteAlignment(4).withName("WGPUBlendComponent").withName("alpha")
        ).withByteAlignment(4).withName("WGPUBlendState")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUBlendState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendState) -> Unit): ArrayHolder<WGPUBlendState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBlendState {
        override var color: WGPUBlendComponent
            get() = WGPUBlendComponent(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("color")), Companion.layout.select(groupElement("color")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("color")), Companion.layout.select(groupElement("color")).byteSize())
            }
        override var alpha: WGPUBlendComponent
            get() = WGPUBlendComponent(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("alpha")), Companion.layout.select(groupElement("alpha")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("alpha")), Companion.layout.select(groupElement("alpha")).byteSize())
            }
    }
}

actual interface WGPUCompilationInfo : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var messageCount: ULong
    actual var messages: WGPUCompilationMessage?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("messageCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("messages")
        ).withByteAlignment(8).withName("WGPUCompilationInfo")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val messageCount_VH: VarHandle = layout.varHandle(groupElement("messageCount"))
        val messages_VH: VarHandle = layout.varHandle(groupElement("messages"))
        
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfo) -> Unit): ArrayHolder<WGPUCompilationInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUCompilationInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var messageCount: ULong
            get() = (messageCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = messageCount_VH.set(handler.handler, 0L, value.toLong())
        override var messages: WGPUCompilationMessage?
            get() = (messages_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUCompilationMessage(it) }
            set(value) = messages_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUComputePassDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("timestampWrites")
        ).withByteAlignment(8).withName("WGPUComputePassDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val timestampWrites_VH: VarHandle = layout.varHandle(groupElement("timestampWrites"))
        
        actual operator fun invoke(address: NativeAddress): WGPUComputePassDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePassDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePassDescriptor) -> Unit): ArrayHolder<WGPUComputePassDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUComputePassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = (timestampWrites_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) }
            set(value) = timestampWrites_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUComputeState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("module"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("entryPoint"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("constantCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("constants")
        ).withByteAlignment(8).withName("WGPUComputeState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val module_VH: VarHandle = layout.varHandle(groupElement("module"))
        val constantCount_VH: VarHandle = layout.varHandle(groupElement("constantCount"))
        val constants_VH: VarHandle = layout.varHandle(groupElement("constants"))
        
        actual operator fun invoke(address: NativeAddress): WGPUComputeState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputeState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputeState) -> Unit): ArrayHolder<WGPUComputeState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUComputeState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var module: WGPUShaderModule?
            get() = (module_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) = module_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var entryPoint: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())
            }
        override var constantCount: ULong
            get() = (constantCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = constantCount_VH.set(handler.handler, 0L, value.toLong())
        override var constants: WGPUConstantEntry?
            get() = (constants_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) = constants_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUDepthStencilState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUTextureFormat
    actual var depthWriteEnabled: WGPUOptionalBool
    actual var depthCompare: WGPUCompareFunction
    actual var stencilFront: WGPUStencilFaceState
    actual var stencilBack: WGPUStencilFaceState
    actual var stencilReadMask: UInt
    actual var stencilWriteMask: UInt
    actual var depthBias: Int
    actual var depthBiasSlopeScale: Float
    actual var depthBiasClamp: Float
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthWriteEnabled"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthCompare"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("compare"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("failOp"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthFailOp"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("passOp")).withByteAlignment(4).withName("WGPUStencilFaceState").withName("stencilFront"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("compare"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("failOp"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthFailOp"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("passOp")).withByteAlignment(4).withName("WGPUStencilFaceState").withName("stencilBack"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilReadMask"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stencilWriteMask"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthBias"),
            ValueLayout.JAVA_FLOAT.withByteAlignment(4).withName("depthBiasSlopeScale"),
            ValueLayout.JAVA_FLOAT.withByteAlignment(4).withName("depthBiasClamp")
        ).withByteAlignment(8).withName("WGPUDepthStencilState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val depthWriteEnabled_VH: VarHandle = layout.varHandle(groupElement("depthWriteEnabled"))
        val depthCompare_VH: VarHandle = layout.varHandle(groupElement("depthCompare"))
        val stencilReadMask_VH: VarHandle = layout.varHandle(groupElement("stencilReadMask"))
        val stencilWriteMask_VH: VarHandle = layout.varHandle(groupElement("stencilWriteMask"))
        val depthBias_VH: VarHandle = layout.varHandle(groupElement("depthBias"))
        val depthBiasSlopeScale_VH: VarHandle = layout.varHandle(groupElement("depthBiasSlopeScale"))
        val depthBiasClamp_VH: VarHandle = layout.varHandle(groupElement("depthBiasClamp"))
        
        actual operator fun invoke(address: NativeAddress): WGPUDepthStencilState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDepthStencilState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDepthStencilState) -> Unit): ArrayHolder<WGPUDepthStencilState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUDepthStencilState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var depthWriteEnabled: WGPUOptionalBool
            get() = (depthWriteEnabled_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUOptionalBool
            set(value) = depthWriteEnabled_VH.set(handler.handler, 0L, value.toInt())
        override var depthCompare: WGPUCompareFunction
            get() = (depthCompare_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUCompareFunction
            set(value) = depthCompare_VH.set(handler.handler, 0L, value.toInt())
        override var stencilFront: WGPUStencilFaceState
            get() = WGPUStencilFaceState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("stencilFront")), Companion.layout.select(groupElement("stencilFront")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("stencilFront")), Companion.layout.select(groupElement("stencilFront")).byteSize())
            }
        override var stencilBack: WGPUStencilFaceState
            get() = WGPUStencilFaceState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("stencilBack")), Companion.layout.select(groupElement("stencilBack")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("stencilBack")), Companion.layout.select(groupElement("stencilBack")).byteSize())
            }
        override var stencilReadMask: UInt
            get() = (stencilReadMask_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = stencilReadMask_VH.set(handler.handler, 0L, value.toInt())
        override var stencilWriteMask: UInt
            get() = (stencilWriteMask_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = stencilWriteMask_VH.set(handler.handler, 0L, value.toInt())
        override var depthBias: Int
            get() = depthBias_VH.get(handler.handler, 0L) as Int
            set(value) = depthBias_VH.set(handler.handler, 0L, value)
        override var depthBiasSlopeScale: Float
            get() = depthBiasSlopeScale_VH.get(handler.handler, 0L) as Float
            set(value) = depthBiasSlopeScale_VH.set(handler.handler, 0L, value)
        override var depthBiasClamp: Float
            get() = depthBiasClamp_VH.get(handler.handler, 0L) as Float
            set(value) = depthBiasClamp_VH.set(handler.handler, 0L, value)
    }
}

actual interface WGPUFutureWaitInfo : CStructure {
    actual var future: WGPUFuture
    actual var completed: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("id")).withByteAlignment(8).withName("WGPUFuture").withName("future"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("completed"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUFutureWaitInfo")
        
        val completed_VH: VarHandle = layout.varHandle(groupElement("completed"))
        
        actual operator fun invoke(address: NativeAddress): WGPUFutureWaitInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFutureWaitInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFutureWaitInfo) -> Unit): ArrayHolder<WGPUFutureWaitInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUFutureWaitInfo {
        override var future: WGPUFuture
            get() = WGPUFuture(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("future")), Companion.layout.select(groupElement("future")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("future")), Companion.layout.select(groupElement("future")).byteSize())
            }
        override var completed: UInt
            get() = (completed_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = completed_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUInstanceDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPUInstanceLimits?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("requiredFeatureCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("requiredFeatures"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("requiredLimits")
        ).withByteAlignment(8).withName("WGPUInstanceDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val requiredFeatureCount_VH: VarHandle = layout.varHandle(groupElement("requiredFeatureCount"))
        val requiredFeatures_VH: VarHandle = layout.varHandle(groupElement("requiredFeatures"))
        val requiredLimits_VH: VarHandle = layout.varHandle(groupElement("requiredLimits"))
        
        actual operator fun invoke(address: NativeAddress): WGPUInstanceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceDescriptor) -> Unit): ArrayHolder<WGPUInstanceDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUInstanceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var requiredFeatureCount: ULong
            get() = (requiredFeatureCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = requiredFeatureCount_VH.set(handler.handler, 0L, value.toLong())
        override var requiredFeatures: NativeAddress?
            get() = (requiredFeatures_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = requiredFeatures_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var requiredLimits: WGPUInstanceLimits?
            get() = (requiredLimits_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUInstanceLimits(it) }
            set(value) = requiredLimits_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPULimits : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var maxTextureDimension1D: UInt
    actual var maxTextureDimension2D: UInt
    actual var maxTextureDimension3D: UInt
    actual var maxTextureArrayLayers: UInt
    actual var maxBindGroups: UInt
    actual var maxBindGroupsPlusVertexBuffers: UInt
    actual var maxBindingsPerBindGroup: UInt
    actual var maxDynamicUniformBuffersPerPipelineLayout: UInt
    actual var maxDynamicStorageBuffersPerPipelineLayout: UInt
    actual var maxSampledTexturesPerShaderStage: UInt
    actual var maxSamplersPerShaderStage: UInt
    actual var maxStorageBuffersPerShaderStage: UInt
    actual var maxStorageTexturesPerShaderStage: UInt
    actual var maxUniformBuffersPerShaderStage: UInt
    actual var maxUniformBufferBindingSize: ULong
    actual var maxStorageBufferBindingSize: ULong
    actual var minUniformBufferOffsetAlignment: UInt
    actual var minStorageBufferOffsetAlignment: UInt
    actual var maxVertexBuffers: UInt
    actual var maxBufferSize: ULong
    actual var maxVertexAttributes: UInt
    actual var maxVertexBufferArrayStride: UInt
    actual var maxInterStageShaderVariables: UInt
    actual var maxColorAttachments: UInt
    actual var maxColorAttachmentBytesPerSample: UInt
    actual var maxComputeWorkgroupStorageSize: UInt
    actual var maxComputeInvocationsPerWorkgroup: UInt
    actual var maxComputeWorkgroupSizeX: UInt
    actual var maxComputeWorkgroupSizeY: UInt
    actual var maxComputeWorkgroupSizeZ: UInt
    actual var maxComputeWorkgroupsPerDimension: UInt
    actual var maxImmediateSize: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxTextureDimension1D"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxTextureDimension2D"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxTextureDimension3D"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxTextureArrayLayers"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxBindGroups"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxBindGroupsPlusVertexBuffers"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxBindingsPerBindGroup"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxDynamicUniformBuffersPerPipelineLayout"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxDynamicStorageBuffersPerPipelineLayout"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxSampledTexturesPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxSamplersPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageBuffersPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxStorageTexturesPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxUniformBuffersPerShaderStage"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("maxUniformBufferBindingSize"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("maxStorageBufferBindingSize"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("minUniformBufferOffsetAlignment"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("minStorageBufferOffsetAlignment"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxVertexBuffers"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("maxBufferSize"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxVertexAttributes"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxVertexBufferArrayStride"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxInterStageShaderVariables"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxColorAttachments"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxColorAttachmentBytesPerSample"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeWorkgroupStorageSize"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeInvocationsPerWorkgroup"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeWorkgroupSizeX"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeWorkgroupSizeY"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeWorkgroupSizeZ"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxComputeWorkgroupsPerDimension"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxImmediateSize")
        ).withByteAlignment(8).withName("WGPULimits")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val maxTextureDimension1D_VH: VarHandle = layout.varHandle(groupElement("maxTextureDimension1D"))
        val maxTextureDimension2D_VH: VarHandle = layout.varHandle(groupElement("maxTextureDimension2D"))
        val maxTextureDimension3D_VH: VarHandle = layout.varHandle(groupElement("maxTextureDimension3D"))
        val maxTextureArrayLayers_VH: VarHandle = layout.varHandle(groupElement("maxTextureArrayLayers"))
        val maxBindGroups_VH: VarHandle = layout.varHandle(groupElement("maxBindGroups"))
        val maxBindGroupsPlusVertexBuffers_VH: VarHandle = layout.varHandle(groupElement("maxBindGroupsPlusVertexBuffers"))
        val maxBindingsPerBindGroup_VH: VarHandle = layout.varHandle(groupElement("maxBindingsPerBindGroup"))
        val maxDynamicUniformBuffersPerPipelineLayout_VH: VarHandle = layout.varHandle(groupElement("maxDynamicUniformBuffersPerPipelineLayout"))
        val maxDynamicStorageBuffersPerPipelineLayout_VH: VarHandle = layout.varHandle(groupElement("maxDynamicStorageBuffersPerPipelineLayout"))
        val maxSampledTexturesPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxSampledTexturesPerShaderStage"))
        val maxSamplersPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxSamplersPerShaderStage"))
        val maxStorageBuffersPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageBuffersPerShaderStage"))
        val maxStorageTexturesPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxStorageTexturesPerShaderStage"))
        val maxUniformBuffersPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxUniformBuffersPerShaderStage"))
        val maxUniformBufferBindingSize_VH: VarHandle = layout.varHandle(groupElement("maxUniformBufferBindingSize"))
        val maxStorageBufferBindingSize_VH: VarHandle = layout.varHandle(groupElement("maxStorageBufferBindingSize"))
        val minUniformBufferOffsetAlignment_VH: VarHandle = layout.varHandle(groupElement("minUniformBufferOffsetAlignment"))
        val minStorageBufferOffsetAlignment_VH: VarHandle = layout.varHandle(groupElement("minStorageBufferOffsetAlignment"))
        val maxVertexBuffers_VH: VarHandle = layout.varHandle(groupElement("maxVertexBuffers"))
        val maxBufferSize_VH: VarHandle = layout.varHandle(groupElement("maxBufferSize"))
        val maxVertexAttributes_VH: VarHandle = layout.varHandle(groupElement("maxVertexAttributes"))
        val maxVertexBufferArrayStride_VH: VarHandle = layout.varHandle(groupElement("maxVertexBufferArrayStride"))
        val maxInterStageShaderVariables_VH: VarHandle = layout.varHandle(groupElement("maxInterStageShaderVariables"))
        val maxColorAttachments_VH: VarHandle = layout.varHandle(groupElement("maxColorAttachments"))
        val maxColorAttachmentBytesPerSample_VH: VarHandle = layout.varHandle(groupElement("maxColorAttachmentBytesPerSample"))
        val maxComputeWorkgroupStorageSize_VH: VarHandle = layout.varHandle(groupElement("maxComputeWorkgroupStorageSize"))
        val maxComputeInvocationsPerWorkgroup_VH: VarHandle = layout.varHandle(groupElement("maxComputeInvocationsPerWorkgroup"))
        val maxComputeWorkgroupSizeX_VH: VarHandle = layout.varHandle(groupElement("maxComputeWorkgroupSizeX"))
        val maxComputeWorkgroupSizeY_VH: VarHandle = layout.varHandle(groupElement("maxComputeWorkgroupSizeY"))
        val maxComputeWorkgroupSizeZ_VH: VarHandle = layout.varHandle(groupElement("maxComputeWorkgroupSizeZ"))
        val maxComputeWorkgroupsPerDimension_VH: VarHandle = layout.varHandle(groupElement("maxComputeWorkgroupsPerDimension"))
        val maxImmediateSize_VH: VarHandle = layout.varHandle(groupElement("maxImmediateSize"))
        
        actual operator fun invoke(address: NativeAddress): WGPULimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPULimits = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPULimits) -> Unit): ArrayHolder<WGPULimits> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPULimits {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var maxTextureDimension1D: UInt
            get() = (maxTextureDimension1D_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxTextureDimension1D_VH.set(handler.handler, 0L, value.toInt())
        override var maxTextureDimension2D: UInt
            get() = (maxTextureDimension2D_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxTextureDimension2D_VH.set(handler.handler, 0L, value.toInt())
        override var maxTextureDimension3D: UInt
            get() = (maxTextureDimension3D_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxTextureDimension3D_VH.set(handler.handler, 0L, value.toInt())
        override var maxTextureArrayLayers: UInt
            get() = (maxTextureArrayLayers_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxTextureArrayLayers_VH.set(handler.handler, 0L, value.toInt())
        override var maxBindGroups: UInt
            get() = (maxBindGroups_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxBindGroups_VH.set(handler.handler, 0L, value.toInt())
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = (maxBindGroupsPlusVertexBuffers_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxBindGroupsPlusVertexBuffers_VH.set(handler.handler, 0L, value.toInt())
        override var maxBindingsPerBindGroup: UInt
            get() = (maxBindingsPerBindGroup_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxBindingsPerBindGroup_VH.set(handler.handler, 0L, value.toInt())
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = (maxDynamicUniformBuffersPerPipelineLayout_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxDynamicUniformBuffersPerPipelineLayout_VH.set(handler.handler, 0L, value.toInt())
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = (maxDynamicStorageBuffersPerPipelineLayout_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxDynamicStorageBuffersPerPipelineLayout_VH.set(handler.handler, 0L, value.toInt())
        override var maxSampledTexturesPerShaderStage: UInt
            get() = (maxSampledTexturesPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxSampledTexturesPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxSamplersPerShaderStage: UInt
            get() = (maxSamplersPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxSamplersPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxStorageBuffersPerShaderStage: UInt
            get() = (maxStorageBuffersPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageBuffersPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxStorageTexturesPerShaderStage: UInt
            get() = (maxStorageTexturesPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxStorageTexturesPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxUniformBuffersPerShaderStage: UInt
            get() = (maxUniformBuffersPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxUniformBuffersPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxUniformBufferBindingSize: ULong
            get() = (maxUniformBufferBindingSize_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = maxUniformBufferBindingSize_VH.set(handler.handler, 0L, value.toLong())
        override var maxStorageBufferBindingSize: ULong
            get() = (maxStorageBufferBindingSize_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = maxStorageBufferBindingSize_VH.set(handler.handler, 0L, value.toLong())
        override var minUniformBufferOffsetAlignment: UInt
            get() = (minUniformBufferOffsetAlignment_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = minUniformBufferOffsetAlignment_VH.set(handler.handler, 0L, value.toInt())
        override var minStorageBufferOffsetAlignment: UInt
            get() = (minStorageBufferOffsetAlignment_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = minStorageBufferOffsetAlignment_VH.set(handler.handler, 0L, value.toInt())
        override var maxVertexBuffers: UInt
            get() = (maxVertexBuffers_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxVertexBuffers_VH.set(handler.handler, 0L, value.toInt())
        override var maxBufferSize: ULong
            get() = (maxBufferSize_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = maxBufferSize_VH.set(handler.handler, 0L, value.toLong())
        override var maxVertexAttributes: UInt
            get() = (maxVertexAttributes_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxVertexAttributes_VH.set(handler.handler, 0L, value.toInt())
        override var maxVertexBufferArrayStride: UInt
            get() = (maxVertexBufferArrayStride_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxVertexBufferArrayStride_VH.set(handler.handler, 0L, value.toInt())
        override var maxInterStageShaderVariables: UInt
            get() = (maxInterStageShaderVariables_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxInterStageShaderVariables_VH.set(handler.handler, 0L, value.toInt())
        override var maxColorAttachments: UInt
            get() = (maxColorAttachments_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxColorAttachments_VH.set(handler.handler, 0L, value.toInt())
        override var maxColorAttachmentBytesPerSample: UInt
            get() = (maxColorAttachmentBytesPerSample_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxColorAttachmentBytesPerSample_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeWorkgroupStorageSize: UInt
            get() = (maxComputeWorkgroupStorageSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeWorkgroupStorageSize_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = (maxComputeInvocationsPerWorkgroup_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeInvocationsPerWorkgroup_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeWorkgroupSizeX: UInt
            get() = (maxComputeWorkgroupSizeX_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeWorkgroupSizeX_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeWorkgroupSizeY: UInt
            get() = (maxComputeWorkgroupSizeY_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeWorkgroupSizeY_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeWorkgroupSizeZ: UInt
            get() = (maxComputeWorkgroupSizeZ_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeWorkgroupSizeZ_VH.set(handler.handler, 0L, value.toInt())
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = (maxComputeWorkgroupsPerDimension_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxComputeWorkgroupsPerDimension_VH.set(handler.handler, 0L, value.toInt())
        override var maxImmediateSize: UInt
            get() = (maxImmediateSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxImmediateSize_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPURenderPassColorAttachment : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var view: WGPUTextureView?
    actual var depthSlice: UInt
    actual var resolveTarget: WGPUTextureView?
    actual var loadOp: WGPULoadOp
    actual var storeOp: WGPUStoreOp
    actual var clearValue: WGPUColor
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("view"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthSlice"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("resolveTarget"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("loadOp"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("storeOp"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("r"), ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("g"), ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("b"), ValueLayout.JAVA_DOUBLE.withByteAlignment(8).withName("a")).withByteAlignment(8).withName("WGPUColor").withName("clearValue")
        ).withByteAlignment(8).withName("WGPURenderPassColorAttachment")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val view_VH: VarHandle = layout.varHandle(groupElement("view"))
        val depthSlice_VH: VarHandle = layout.varHandle(groupElement("depthSlice"))
        val resolveTarget_VH: VarHandle = layout.varHandle(groupElement("resolveTarget"))
        val loadOp_VH: VarHandle = layout.varHandle(groupElement("loadOp"))
        val storeOp_VH: VarHandle = layout.varHandle(groupElement("storeOp"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderPassColorAttachment = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassColorAttachment = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassColorAttachment) -> Unit): ArrayHolder<WGPURenderPassColorAttachment> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderPassColorAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var view: WGPUTextureView?
            get() = (view_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) = view_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var depthSlice: UInt
            get() = (depthSlice_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = depthSlice_VH.set(handler.handler, 0L, value.toInt())
        override var resolveTarget: WGPUTextureView?
            get() = (resolveTarget_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) = resolveTarget_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var loadOp: WGPULoadOp
            get() = (loadOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPULoadOp
            set(value) = loadOp_VH.set(handler.handler, 0L, value.toInt())
        override var storeOp: WGPUStoreOp
            get() = (storeOp_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUStoreOp
            set(value) = storeOp_VH.set(handler.handler, 0L, value.toInt())
        override var clearValue: WGPUColor
            get() = WGPUColor(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("clearValue")), Companion.layout.select(groupElement("clearValue")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("clearValue")), Companion.layout.select(groupElement("clearValue")).byteSize())
            }
    }
}

actual interface WGPURequestAdapterOptions : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var featureLevel: WGPUFeatureLevel
    actual var powerPreference: WGPUPowerPreference
    actual var forceFallbackAdapter: UInt
    actual var backendType: WGPUBackendType
    actual var compatibleSurface: WGPUSurface?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("featureLevel"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("powerPreference"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("forceFallbackAdapter"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("backendType"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("compatibleSurface")
        ).withByteAlignment(8).withName("WGPURequestAdapterOptions")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val featureLevel_VH: VarHandle = layout.varHandle(groupElement("featureLevel"))
        val powerPreference_VH: VarHandle = layout.varHandle(groupElement("powerPreference"))
        val forceFallbackAdapter_VH: VarHandle = layout.varHandle(groupElement("forceFallbackAdapter"))
        val backendType_VH: VarHandle = layout.varHandle(groupElement("backendType"))
        val compatibleSurface_VH: VarHandle = layout.varHandle(groupElement("compatibleSurface"))
        
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterOptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterOptions = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterOptions) -> Unit): ArrayHolder<WGPURequestAdapterOptions> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURequestAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var featureLevel: WGPUFeatureLevel
            get() = (featureLevel_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUFeatureLevel
            set(value) = featureLevel_VH.set(handler.handler, 0L, value.toInt())
        override var powerPreference: WGPUPowerPreference
            get() = (powerPreference_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUPowerPreference
            set(value) = powerPreference_VH.set(handler.handler, 0L, value.toInt())
        override var forceFallbackAdapter: UInt
            get() = (forceFallbackAdapter_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = forceFallbackAdapter_VH.set(handler.handler, 0L, value.toInt())
        override var backendType: WGPUBackendType
            get() = (backendType_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUBackendType
            set(value) = backendType_VH.set(handler.handler, 0L, value.toInt())
        override var compatibleSurface: WGPUSurface?
            get() = (compatibleSurface_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUSurface(it) }
            set(value) = compatibleSurface_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUShaderModuleDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPUShaderModuleDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptor) -> Unit): ArrayHolder<WGPUShaderModuleDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderModuleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPUSurfaceDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")
        ).withByteAlignment(8).withName("WGPUSurfaceDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceDescriptor) -> Unit): ArrayHolder<WGPUSurfaceDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
    }
}

actual interface WGPUTexelCopyBufferInfo : CStructure {
    actual var layout: WGPUTexelCopyBufferLayout
    actual var buffer: WGPUBuffer?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("offset"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("bytesPerRow"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("rowsPerImage")).withByteAlignment(8).withName("WGPUTexelCopyBufferLayout").withName("layout"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("buffer")
        ).withByteAlignment(8).withName("WGPUTexelCopyBufferInfo")
        
        val buffer_VH: VarHandle = layout.varHandle(groupElement("buffer"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferInfo) -> Unit): ArrayHolder<WGPUTexelCopyBufferInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTexelCopyBufferInfo {
        override var layout: WGPUTexelCopyBufferLayout
            get() = WGPUTexelCopyBufferLayout(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("layout")), Companion.layout.select(groupElement("layout")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("layout")), Companion.layout.select(groupElement("layout")).byteSize())
            }
        override var buffer: WGPUBuffer?
            get() = (buffer_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBuffer(it) }
            set(value) = buffer_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUTexelCopyTextureInfo : CStructure {
    actual var texture: WGPUTexture?
    actual var mipLevel: UInt
    actual var origin: WGPUOrigin3D
    actual var aspect: WGPUTextureAspect
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("texture"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mipLevel"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("x"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("y"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("z")).withByteAlignment(4).withName("WGPUOrigin3D").withName("origin"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("aspect"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUTexelCopyTextureInfo")
        
        val texture_VH: VarHandle = layout.varHandle(groupElement("texture"))
        val mipLevel_VH: VarHandle = layout.varHandle(groupElement("mipLevel"))
        val aspect_VH: VarHandle = layout.varHandle(groupElement("aspect"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyTextureInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyTextureInfo = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyTextureInfo) -> Unit): ArrayHolder<WGPUTexelCopyTextureInfo> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTexelCopyTextureInfo {
        override var texture: WGPUTexture?
            get() = (texture_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTexture(it) }
            set(value) = texture_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var mipLevel: UInt
            get() = (mipLevel_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mipLevel_VH.set(handler.handler, 0L, value.toInt())
        override var origin: WGPUOrigin3D
            get() = WGPUOrigin3D(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("origin")), Companion.layout.select(groupElement("origin")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("origin")), Companion.layout.select(groupElement("origin")).byteSize())
            }
        override var aspect: WGPUTextureAspect
            get() = (aspect_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureAspect
            set(value) = aspect_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUTextureComponentSwizzleDescriptor : CStructure {
    actual var chain: WGPUChainedStruct
    actual var swizzle: WGPUTextureComponentSwizzle
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("r"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("g"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("b"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("a")).withByteAlignment(4).withName("WGPUTextureComponentSwizzle").withName("swizzle")
        ).withByteAlignment(8).withName("WGPUTextureComponentSwizzleDescriptor")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzleDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzleDescriptor) -> Unit): ArrayHolder<WGPUTextureComponentSwizzleDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureComponentSwizzleDescriptor {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() = WGPUTextureComponentSwizzle(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("swizzle")), Companion.layout.select(groupElement("swizzle")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("swizzle")), Companion.layout.select(groupElement("swizzle")).byteSize())
            }
    }
}

actual interface WGPUTextureDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var usage: ULong
    actual var dimension: WGPUTextureDimension
    actual var size: WGPUExtent3D
    actual var format: WGPUTextureFormat
    actual var mipLevelCount: UInt
    actual var sampleCount: UInt
    actual var viewFormatCount: ULong
    actual var viewFormats: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("usage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dimension"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("width"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("height"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("depthOrArrayLayers")).withByteAlignment(4).withName("WGPUExtent3D").withName("size"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mipLevelCount"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("sampleCount"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("viewFormatCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("viewFormats")
        ).withByteAlignment(8).withName("WGPUTextureDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val usage_VH: VarHandle = layout.varHandle(groupElement("usage"))
        val dimension_VH: VarHandle = layout.varHandle(groupElement("dimension"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val mipLevelCount_VH: VarHandle = layout.varHandle(groupElement("mipLevelCount"))
        val sampleCount_VH: VarHandle = layout.varHandle(groupElement("sampleCount"))
        val viewFormatCount_VH: VarHandle = layout.varHandle(groupElement("viewFormatCount"))
        val viewFormats_VH: VarHandle = layout.varHandle(groupElement("viewFormats"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureDescriptor) -> Unit): ArrayHolder<WGPUTextureDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var usage: ULong
            get() = (usage_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = usage_VH.set(handler.handler, 0L, value.toLong())
        override var dimension: WGPUTextureDimension
            get() = (dimension_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureDimension
            set(value) = dimension_VH.set(handler.handler, 0L, value.toInt())
        override var size: WGPUExtent3D
            get() = WGPUExtent3D(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("size")), Companion.layout.select(groupElement("size")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("size")), Companion.layout.select(groupElement("size")).byteSize())
            }
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var mipLevelCount: UInt
            get() = (mipLevelCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mipLevelCount_VH.set(handler.handler, 0L, value.toInt())
        override var sampleCount: UInt
            get() = (sampleCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = sampleCount_VH.set(handler.handler, 0L, value.toInt())
        override var viewFormatCount: ULong
            get() = (viewFormatCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = viewFormatCount_VH.set(handler.handler, 0L, value.toLong())
        override var viewFormats: NativeAddress?
            get() = (viewFormats_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = viewFormats_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUVertexBufferLayout : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var stepMode: WGPUVertexStepMode
    actual var arrayStride: ULong
    actual var attributeCount: ULong
    actual var attributes: WGPUVertexAttribute?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("stepMode"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("arrayStride"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("attributeCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("attributes")
        ).withByteAlignment(8).withName("WGPUVertexBufferLayout")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val stepMode_VH: VarHandle = layout.varHandle(groupElement("stepMode"))
        val arrayStride_VH: VarHandle = layout.varHandle(groupElement("arrayStride"))
        val attributeCount_VH: VarHandle = layout.varHandle(groupElement("attributeCount"))
        val attributes_VH: VarHandle = layout.varHandle(groupElement("attributes"))
        
        actual operator fun invoke(address: NativeAddress): WGPUVertexBufferLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexBufferLayout = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexBufferLayout) -> Unit): ArrayHolder<WGPUVertexBufferLayout> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUVertexBufferLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var stepMode: WGPUVertexStepMode
            get() = (stepMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUVertexStepMode
            set(value) = stepMode_VH.set(handler.handler, 0L, value.toInt())
        override var arrayStride: ULong
            get() = (arrayStride_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = arrayStride_VH.set(handler.handler, 0L, value.toLong())
        override var attributeCount: ULong
            get() = (attributeCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = attributeCount_VH.set(handler.handler, 0L, value.toLong())
        override var attributes: WGPUVertexAttribute?
            get() = (attributes_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUVertexAttribute(it) }
            set(value) = attributes_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUBindGroupDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUBindGroupLayout?
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupEntry?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("layout"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("entryCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("entries")
        ).withByteAlignment(8).withName("WGPUBindGroupDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val layout_VH: VarHandle = layout.varHandle(groupElement("layout"))
        val entryCount_VH: VarHandle = layout.varHandle(groupElement("entryCount"))
        val entries_VH: VarHandle = layout.varHandle(groupElement("entries"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupDescriptor) -> Unit): ArrayHolder<WGPUBindGroupDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var layout: WGPUBindGroupLayout?
            get() = (layout_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
            set(value) = layout_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var entryCount: ULong
            get() = (entryCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = entryCount_VH.set(handler.handler, 0L, value.toLong())
        override var entries: WGPUBindGroupEntry?
            get() = (entries_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupEntry(it) }
            set(value) = entries_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUBindGroupLayoutDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupLayoutEntry?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("entryCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("entries")
        ).withByteAlignment(8).withName("WGPUBindGroupLayoutDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val entryCount_VH: VarHandle = layout.varHandle(groupElement("entryCount"))
        val entries_VH: VarHandle = layout.varHandle(groupElement("entries"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutDescriptor) -> Unit): ArrayHolder<WGPUBindGroupLayoutDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var entryCount: ULong
            get() = (entryCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = entryCount_VH.set(handler.handler, 0L, value.toLong())
        override var entries: WGPUBindGroupLayoutEntry?
            get() = (entries_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) = entries_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUColorTargetState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUTextureFormat
    actual var blend: WGPUBlendState?
    actual var writeMask: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("blend"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("writeMask")
        ).withByteAlignment(8).withName("WGPUColorTargetState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val blend_VH: VarHandle = layout.varHandle(groupElement("blend"))
        val writeMask_VH: VarHandle = layout.varHandle(groupElement("writeMask"))
        
        actual operator fun invoke(address: NativeAddress): WGPUColorTargetState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColorTargetState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColorTargetState) -> Unit): ArrayHolder<WGPUColorTargetState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUColorTargetState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var blend: WGPUBlendState?
            get() = (blend_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBlendState(it) }
            set(value) = blend_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var writeMask: ULong
            get() = (writeMask_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = writeMask_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUComputePipelineDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var compute: WGPUComputeState
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("layout"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.ADDRESS.withByteAlignment(8).withName("module"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("entryPoint"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("constantCount"), ValueLayout.ADDRESS.withByteAlignment(8).withName("constants")).withByteAlignment(8).withName("WGPUComputeState").withName("compute")
        ).withByteAlignment(8).withName("WGPUComputePipelineDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val layout_VH: VarHandle = layout.varHandle(groupElement("layout"))
        
        actual operator fun invoke(address: NativeAddress): WGPUComputePipelineDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePipelineDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePipelineDescriptor) -> Unit): ArrayHolder<WGPUComputePipelineDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUComputePipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var layout: WGPUPipelineLayout?
            get() = (layout_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
            set(value) = layout_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var compute: WGPUComputeState
            get() = WGPUComputeState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("compute")), Companion.layout.select(groupElement("compute")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("compute")), Companion.layout.select(groupElement("compute")).byteSize())
            }
    }
}

actual interface WGPUDeviceDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPULimits?
    actual var defaultQueue: WGPUQueueDescriptor
    actual var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
    actual var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("requiredFeatureCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("requiredFeatures"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("requiredLimits"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label")).withByteAlignment(8).withName("WGPUQueueDescriptor").withName("defaultQueue"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("mode"), java.lang.foreign.MemoryLayout.paddingLayout(4), ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"), ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"), ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")).withByteAlignment(8).withName("WGPUDeviceLostCallbackInfo").withName("deviceLostCallbackInfo"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.ADDRESS.withByteAlignment(8).withName("callback"), ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata1"), ValueLayout.ADDRESS.withByteAlignment(8).withName("userdata2")).withByteAlignment(8).withName("WGPUUncapturedErrorCallbackInfo").withName("uncapturedErrorCallbackInfo")
        ).withByteAlignment(8).withName("WGPUDeviceDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val requiredFeatureCount_VH: VarHandle = layout.varHandle(groupElement("requiredFeatureCount"))
        val requiredFeatures_VH: VarHandle = layout.varHandle(groupElement("requiredFeatures"))
        val requiredLimits_VH: VarHandle = layout.varHandle(groupElement("requiredLimits"))
        
        actual operator fun invoke(address: NativeAddress): WGPUDeviceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceDescriptor) -> Unit): ArrayHolder<WGPUDeviceDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUDeviceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var requiredFeatureCount: ULong
            get() = (requiredFeatureCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = requiredFeatureCount_VH.set(handler.handler, 0L, value.toLong())
        override var requiredFeatures: NativeAddress?
            get() = (requiredFeatures_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = requiredFeatures_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var requiredLimits: WGPULimits?
            get() = (requiredLimits_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPULimits(it) }
            set(value) = requiredLimits_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var defaultQueue: WGPUQueueDescriptor
            get() = WGPUQueueDescriptor(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("defaultQueue")), Companion.layout.select(groupElement("defaultQueue")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("defaultQueue")), Companion.layout.select(groupElement("defaultQueue")).byteSize())
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() = WGPUDeviceLostCallbackInfo(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("deviceLostCallbackInfo")), Companion.layout.select(groupElement("deviceLostCallbackInfo")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("deviceLostCallbackInfo")), Companion.layout.select(groupElement("deviceLostCallbackInfo")).byteSize())
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() = WGPUUncapturedErrorCallbackInfo(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("uncapturedErrorCallbackInfo")), Companion.layout.select(groupElement("uncapturedErrorCallbackInfo")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("uncapturedErrorCallbackInfo")), Companion.layout.select(groupElement("uncapturedErrorCallbackInfo")).byteSize())
            }
    }
}

actual interface WGPURenderPassDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var colorAttachmentCount: ULong
    actual var colorAttachments: WGPURenderPassColorAttachment?
    actual var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
    actual var occlusionQuerySet: WGPUQuerySet?
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("colorAttachmentCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("colorAttachments"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("depthStencilAttachment"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("occlusionQuerySet"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("timestampWrites")
        ).withByteAlignment(8).withName("WGPURenderPassDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val colorAttachmentCount_VH: VarHandle = layout.varHandle(groupElement("colorAttachmentCount"))
        val colorAttachments_VH: VarHandle = layout.varHandle(groupElement("colorAttachments"))
        val depthStencilAttachment_VH: VarHandle = layout.varHandle(groupElement("depthStencilAttachment"))
        val occlusionQuerySet_VH: VarHandle = layout.varHandle(groupElement("occlusionQuerySet"))
        val timestampWrites_VH: VarHandle = layout.varHandle(groupElement("timestampWrites"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDescriptor) -> Unit): ArrayHolder<WGPURenderPassDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderPassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var colorAttachmentCount: ULong
            get() = (colorAttachmentCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = colorAttachmentCount_VH.set(handler.handler, 0L, value.toLong())
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = (colorAttachments_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderPassColorAttachment(it) }
            set(value) = colorAttachments_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = (depthStencilAttachment_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) = depthStencilAttachment_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var occlusionQuerySet: WGPUQuerySet?
            get() = (occlusionQuerySet_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
            set(value) = occlusionQuerySet_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = (timestampWrites_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) }
            set(value) = timestampWrites_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUTextureViewDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var format: WGPUTextureFormat
    actual var dimension: WGPUTextureViewDimension
    actual var baseMipLevel: UInt
    actual var mipLevelCount: UInt
    actual var baseArrayLayer: UInt
    actual var arrayLayerCount: UInt
    actual var aspect: WGPUTextureAspect
    actual var usage: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("format"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dimension"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("baseMipLevel"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mipLevelCount"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("baseArrayLayer"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("arrayLayerCount"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("aspect"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("usage")
        ).withByteAlignment(8).withName("WGPUTextureViewDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val format_VH: VarHandle = layout.varHandle(groupElement("format"))
        val dimension_VH: VarHandle = layout.varHandle(groupElement("dimension"))
        val baseMipLevel_VH: VarHandle = layout.varHandle(groupElement("baseMipLevel"))
        val mipLevelCount_VH: VarHandle = layout.varHandle(groupElement("mipLevelCount"))
        val baseArrayLayer_VH: VarHandle = layout.varHandle(groupElement("baseArrayLayer"))
        val arrayLayerCount_VH: VarHandle = layout.varHandle(groupElement("arrayLayerCount"))
        val aspect_VH: VarHandle = layout.varHandle(groupElement("aspect"))
        val usage_VH: VarHandle = layout.varHandle(groupElement("usage"))
        
        actual operator fun invoke(address: NativeAddress): WGPUTextureViewDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureViewDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureViewDescriptor) -> Unit): ArrayHolder<WGPUTextureViewDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUTextureViewDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var format: WGPUTextureFormat
            get() = (format_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureFormat
            set(value) = format_VH.set(handler.handler, 0L, value.toInt())
        override var dimension: WGPUTextureViewDimension
            get() = (dimension_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureViewDimension
            set(value) = dimension_VH.set(handler.handler, 0L, value.toInt())
        override var baseMipLevel: UInt
            get() = (baseMipLevel_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = baseMipLevel_VH.set(handler.handler, 0L, value.toInt())
        override var mipLevelCount: UInt
            get() = (mipLevelCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mipLevelCount_VH.set(handler.handler, 0L, value.toInt())
        override var baseArrayLayer: UInt
            get() = (baseArrayLayer_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = baseArrayLayer_VH.set(handler.handler, 0L, value.toInt())
        override var arrayLayerCount: UInt
            get() = (arrayLayerCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = arrayLayerCount_VH.set(handler.handler, 0L, value.toInt())
        override var aspect: WGPUTextureAspect
            get() = (aspect_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureAspect
            set(value) = aspect_VH.set(handler.handler, 0L, value.toInt())
        override var usage: ULong
            get() = (usage_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = usage_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUVertexState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual var bufferCount: ULong
    actual var buffers: WGPUVertexBufferLayout?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("module"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("entryPoint"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("constantCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("constants"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("bufferCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("buffers")
        ).withByteAlignment(8).withName("WGPUVertexState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val module_VH: VarHandle = layout.varHandle(groupElement("module"))
        val constantCount_VH: VarHandle = layout.varHandle(groupElement("constantCount"))
        val constants_VH: VarHandle = layout.varHandle(groupElement("constants"))
        val bufferCount_VH: VarHandle = layout.varHandle(groupElement("bufferCount"))
        val buffers_VH: VarHandle = layout.varHandle(groupElement("buffers"))
        
        actual operator fun invoke(address: NativeAddress): WGPUVertexState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexState) -> Unit): ArrayHolder<WGPUVertexState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUVertexState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var module: WGPUShaderModule?
            get() = (module_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) = module_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var entryPoint: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())
            }
        override var constantCount: ULong
            get() = (constantCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = constantCount_VH.set(handler.handler, 0L, value.toLong())
        override var constants: WGPUConstantEntry?
            get() = (constants_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) = constants_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var bufferCount: ULong
            get() = (bufferCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = bufferCount_VH.set(handler.handler, 0L, value.toLong())
        override var buffers: WGPUVertexBufferLayout?
            get() = (buffers_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUVertexBufferLayout(it) }
            set(value) = buffers_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUFragmentState : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual var targetCount: ULong
    actual var targets: WGPUColorTargetState?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("module"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("entryPoint"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("constantCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("constants"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("targetCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("targets")
        ).withByteAlignment(8).withName("WGPUFragmentState")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val module_VH: VarHandle = layout.varHandle(groupElement("module"))
        val constantCount_VH: VarHandle = layout.varHandle(groupElement("constantCount"))
        val constants_VH: VarHandle = layout.varHandle(groupElement("constants"))
        val targetCount_VH: VarHandle = layout.varHandle(groupElement("targetCount"))
        val targets_VH: VarHandle = layout.varHandle(groupElement("targets"))
        
        actual operator fun invoke(address: NativeAddress): WGPUFragmentState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFragmentState = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFragmentState) -> Unit): ArrayHolder<WGPUFragmentState> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUFragmentState {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var module: WGPUShaderModule?
            get() = (module_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) = module_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var entryPoint: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("entryPoint")), Companion.layout.select(groupElement("entryPoint")).byteSize())
            }
        override var constantCount: ULong
            get() = (constantCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = constantCount_VH.set(handler.handler, 0L, value.toLong())
        override var constants: WGPUConstantEntry?
            get() = (constants_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) = constants_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var targetCount: ULong
            get() = (targetCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = targetCount_VH.set(handler.handler, 0L, value.toLong())
        override var targets: WGPUColorTargetState?
            get() = (targets_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUColorTargetState(it) }
            set(value) = targets_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPURenderPipelineDescriptor : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var vertex: WGPUVertexState
    actual var primitive: WGPUPrimitiveState
    actual var depthStencil: WGPUDepthStencilState?
    actual var multisample: WGPUMultisampleState
    actual var fragment: WGPUFragmentState?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("layout"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.ADDRESS.withByteAlignment(8).withName("module"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("entryPoint"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("constantCount"), ValueLayout.ADDRESS.withByteAlignment(8).withName("constants"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("bufferCount"), ValueLayout.ADDRESS.withByteAlignment(8).withName("buffers")).withByteAlignment(8).withName("WGPUVertexState").withName("vertex"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("topology"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("stripIndexFormat"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("frontFace"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("cullMode"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("unclippedDepth"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUPrimitiveState").withName("primitive"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("depthStencil"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("count"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("mask"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("alphaToCoverageEnabled"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUMultisampleState").withName("multisample"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("fragment")
        ).withByteAlignment(8).withName("WGPURenderPipelineDescriptor")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val layout_VH: VarHandle = layout.varHandle(groupElement("layout"))
        val depthStencil_VH: VarHandle = layout.varHandle(groupElement("depthStencil"))
        val fragment_VH: VarHandle = layout.varHandle(groupElement("fragment"))
        
        actual operator fun invoke(address: NativeAddress): WGPURenderPipelineDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPipelineDescriptor = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPipelineDescriptor) -> Unit): ArrayHolder<WGPURenderPipelineDescriptor> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURenderPipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var layout: WGPUPipelineLayout?
            get() = (layout_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
            set(value) = layout_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var vertex: WGPUVertexState
            get() = WGPUVertexState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("vertex")), Companion.layout.select(groupElement("vertex")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("vertex")), Companion.layout.select(groupElement("vertex")).byteSize())
            }
        override var primitive: WGPUPrimitiveState
            get() = WGPUPrimitiveState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("primitive")), Companion.layout.select(groupElement("primitive")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("primitive")), Companion.layout.select(groupElement("primitive")).byteSize())
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = (depthStencil_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUDepthStencilState(it) }
            set(value) = depthStencil_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var multisample: WGPUMultisampleState
            get() = WGPUMultisampleState(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("multisample")), Companion.layout.select(groupElement("multisample")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("multisample")), Companion.layout.select(groupElement("multisample")).byteSize())
            }
        override var fragment: WGPUFragmentState?
            get() = (fragment_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUFragmentState(it) }
            set(value) = fragment_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

private val wgpuCreateInstance_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCreateInstance_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCreateInstance") }
private val wgpuCreateInstance_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCreateInstance_ADDR, wgpuCreateInstance_DESC) }
actual fun wgpuCreateInstance(descriptor: WGPUInstanceDescriptor?): WGPUInstance? {
    return (wgpuCreateInstance_HANDLE.invokeExact(descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUInstance(it) }
}

private val wgpuGetInstanceFeatures_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuGetInstanceFeatures_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuGetInstanceFeatures") }
private val wgpuGetInstanceFeatures_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuGetInstanceFeatures_ADDR, wgpuGetInstanceFeatures_DESC) }
actual fun wgpuGetInstanceFeatures(features: WGPUSupportedInstanceFeatures?): Unit {
    wgpuGetInstanceFeatures_HANDLE.invokeExact(features?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuGetInstanceLimits_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuGetInstanceLimits_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuGetInstanceLimits") }
private val wgpuGetInstanceLimits_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuGetInstanceLimits_ADDR, wgpuGetInstanceLimits_DESC) }
actual fun wgpuGetInstanceLimits(limits: WGPUInstanceLimits?): WGPUStatus {
    return (wgpuGetInstanceLimits_HANDLE.invokeExact(limits?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuHasInstanceFeature_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuHasInstanceFeature_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuHasInstanceFeature") }
private val wgpuHasInstanceFeature_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuHasInstanceFeature_ADDR, wgpuHasInstanceFeature_DESC) }
actual fun wgpuHasInstanceFeature(feature: WGPUInstanceFeatureName): UInt {
    return (wgpuHasInstanceFeature_HANDLE.invokeExact(feature.toInt()) as Int).toUInt()
}

private val wgpuGetProcAddress_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuGetProcAddress_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuGetProcAddress") }
private val wgpuGetProcAddress_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuGetProcAddress_ADDR, wgpuGetProcAddress_DESC) }
actual fun wgpuGetProcAddress(procName: WGPUStringView): NativeAddress? {
    return (wgpuGetProcAddress_HANDLE.invokeExact(procName.handler.handler) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuAdapterGetFeatures_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuAdapterGetFeatures_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetFeatures") }
private val wgpuAdapterGetFeatures_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterGetFeatures_ADDR, wgpuAdapterGetFeatures_DESC) }
actual fun wgpuAdapterGetFeatures(adapter: WGPUAdapter?, features: WGPUSupportedFeatures?): Unit {
    wgpuAdapterGetFeatures_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL, features?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuAdapterGetInfo_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuAdapterGetInfo_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetInfo") }
private val wgpuAdapterGetInfo_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterGetInfo_ADDR, wgpuAdapterGetInfo_DESC) }
actual fun wgpuAdapterGetInfo(adapter: WGPUAdapter?, info: WGPUAdapterInfo?): WGPUStatus {
    return (wgpuAdapterGetInfo_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL, info?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuAdapterGetLimits_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuAdapterGetLimits_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetLimits") }
private val wgpuAdapterGetLimits_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterGetLimits_ADDR, wgpuAdapterGetLimits_DESC) }
actual fun wgpuAdapterGetLimits(adapter: WGPUAdapter?, limits: WGPULimits?): WGPUStatus {
    return (wgpuAdapterGetLimits_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL, limits?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuAdapterHasFeature_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuAdapterHasFeature_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterHasFeature") }
private val wgpuAdapterHasFeature_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterHasFeature_ADDR, wgpuAdapterHasFeature_DESC) }
actual fun wgpuAdapterHasFeature(adapter: WGPUAdapter?, feature: WGPUFeatureName): UInt {
    return (wgpuAdapterHasFeature_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL, feature.toInt()) as Int).toUInt()
}

private val wgpuAdapterRequestDevice_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS, WGPURequestDeviceCallbackInfo.layout)
private val wgpuAdapterRequestDevice_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterRequestDevice") }
private val wgpuAdapterRequestDevice_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterRequestDevice_ADDR, wgpuAdapterRequestDevice_DESC) }
actual fun wgpuAdapterRequestDevice(adapter: WGPUAdapter?, descriptor: WGPUDeviceDescriptor?, callbackInfo: WGPURequestDeviceCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuAdapterRequestDevice_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), adapter?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuAdapterAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuAdapterAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterAddRef") }
private val wgpuAdapterAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterAddRef_ADDR, wgpuAdapterAddRef_DESC) }
actual fun wgpuAdapterAddRef(adapter: WGPUAdapter?): Unit {
    wgpuAdapterAddRef_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuAdapterRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuAdapterRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterRelease") }
private val wgpuAdapterRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterRelease_ADDR, wgpuAdapterRelease_DESC) }
actual fun wgpuAdapterRelease(adapter: WGPUAdapter?): Unit {
    wgpuAdapterRelease_HANDLE.invokeExact(adapter?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuAdapterInfoFreeMembers_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(WGPUAdapterInfo.layout)
private val wgpuAdapterInfoFreeMembers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuAdapterInfoFreeMembers") }
private val wgpuAdapterInfoFreeMembers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuAdapterInfoFreeMembers_ADDR, wgpuAdapterInfoFreeMembers_DESC) }
actual fun wgpuAdapterInfoFreeMembers(adapterInfo: WGPUAdapterInfo): Unit {
    wgpuAdapterInfoFreeMembers_HANDLE.invokeExact(adapterInfo.handler.handler)
    return
}

private val wgpuBindGroupSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuBindGroupSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupSetLabel") }
private val wgpuBindGroupSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupSetLabel_ADDR, wgpuBindGroupSetLabel_DESC) }
actual fun wgpuBindGroupSetLabel(bindGroup: WGPUBindGroup?, label: WGPUStringView): Unit {
    wgpuBindGroupSetLabel_HANDLE.invokeExact(bindGroup?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuBindGroupAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBindGroupAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupAddRef") }
private val wgpuBindGroupAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupAddRef_ADDR, wgpuBindGroupAddRef_DESC) }
actual fun wgpuBindGroupAddRef(bindGroup: WGPUBindGroup?): Unit {
    wgpuBindGroupAddRef_HANDLE.invokeExact(bindGroup?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBindGroupRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBindGroupRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupRelease") }
private val wgpuBindGroupRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupRelease_ADDR, wgpuBindGroupRelease_DESC) }
actual fun wgpuBindGroupRelease(bindGroup: WGPUBindGroup?): Unit {
    wgpuBindGroupRelease_HANDLE.invokeExact(bindGroup?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBindGroupLayoutSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuBindGroupLayoutSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutSetLabel") }
private val wgpuBindGroupLayoutSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupLayoutSetLabel_ADDR, wgpuBindGroupLayoutSetLabel_DESC) }
actual fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: WGPUBindGroupLayout?, label: WGPUStringView): Unit {
    wgpuBindGroupLayoutSetLabel_HANDLE.invokeExact(bindGroupLayout?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuBindGroupLayoutAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBindGroupLayoutAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutAddRef") }
private val wgpuBindGroupLayoutAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupLayoutAddRef_ADDR, wgpuBindGroupLayoutAddRef_DESC) }
actual fun wgpuBindGroupLayoutAddRef(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    wgpuBindGroupLayoutAddRef_HANDLE.invokeExact(bindGroupLayout?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBindGroupLayoutRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBindGroupLayoutRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutRelease") }
private val wgpuBindGroupLayoutRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBindGroupLayoutRelease_ADDR, wgpuBindGroupLayoutRelease_DESC) }
actual fun wgpuBindGroupLayoutRelease(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    wgpuBindGroupLayoutRelease_HANDLE.invokeExact(bindGroupLayout?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBufferDestroy_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBufferDestroy_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferDestroy") }
private val wgpuBufferDestroy_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferDestroy_ADDR, wgpuBufferDestroy_DESC) }
actual fun wgpuBufferDestroy(buffer: WGPUBuffer?): Unit {
    wgpuBufferDestroy_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBufferGetConstMappedRange_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuBufferGetConstMappedRange_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetConstMappedRange") }
private val wgpuBufferGetConstMappedRange_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferGetConstMappedRange_ADDR, wgpuBufferGetConstMappedRange_DESC) }
actual fun wgpuBufferGetConstMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return (wgpuBufferGetConstMappedRange_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), size.toLong()) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuBufferGetMappedRange_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuBufferGetMappedRange_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetMappedRange") }
private val wgpuBufferGetMappedRange_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferGetMappedRange_ADDR, wgpuBufferGetMappedRange_DESC) }
actual fun wgpuBufferGetMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return (wgpuBufferGetMappedRange_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), size.toLong()) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuBufferGetMapState_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuBufferGetMapState_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetMapState") }
private val wgpuBufferGetMapState_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferGetMapState_ADDR, wgpuBufferGetMapState_DESC) }
actual fun wgpuBufferGetMapState(buffer: WGPUBuffer?): WGPUBufferMapState {
    return (wgpuBufferGetMapState_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuBufferGetSize_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuBufferGetSize_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetSize") }
private val wgpuBufferGetSize_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferGetSize_ADDR, wgpuBufferGetSize_DESC) }
actual fun wgpuBufferGetSize(buffer: WGPUBuffer?): ULong {
    return (wgpuBufferGetSize_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL) as Long).toULong()
}

private val wgpuBufferGetUsage_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuBufferGetUsage_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetUsage") }
private val wgpuBufferGetUsage_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferGetUsage_ADDR, wgpuBufferGetUsage_DESC) }
actual fun wgpuBufferGetUsage(buffer: WGPUBuffer?): ULong {
    return (wgpuBufferGetUsage_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL) as Long).toULong()
}

private val wgpuBufferMapAsync_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG, WGPUBufferMapCallbackInfo.layout)
private val wgpuBufferMapAsync_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferMapAsync") }
private val wgpuBufferMapAsync_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferMapAsync_ADDR, wgpuBufferMapAsync_DESC) }
actual fun wgpuBufferMapAsync(buffer: WGPUBuffer?, mode: ULong, offset: ULong, size: ULong, callbackInfo: WGPUBufferMapCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuBufferMapAsync_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), buffer?.handler?.handler ?: MemorySegment.NULL, mode.toLong(), offset.toLong(), size.toLong(), callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuBufferReadMappedRange_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuBufferReadMappedRange_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferReadMappedRange") }
private val wgpuBufferReadMappedRange_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferReadMappedRange_ADDR, wgpuBufferReadMappedRange_DESC) }
actual fun wgpuBufferReadMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (wgpuBufferReadMappedRange_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), data?.handler ?: MemorySegment.NULL, size.toLong()) as Int).toUInt()
}

private val wgpuBufferSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuBufferSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferSetLabel") }
private val wgpuBufferSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferSetLabel_ADDR, wgpuBufferSetLabel_DESC) }
actual fun wgpuBufferSetLabel(buffer: WGPUBuffer?, label: WGPUStringView): Unit {
    wgpuBufferSetLabel_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuBufferUnmap_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBufferUnmap_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferUnmap") }
private val wgpuBufferUnmap_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferUnmap_ADDR, wgpuBufferUnmap_DESC) }
actual fun wgpuBufferUnmap(buffer: WGPUBuffer?): Unit {
    wgpuBufferUnmap_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBufferWriteMappedRange_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuBufferWriteMappedRange_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferWriteMappedRange") }
private val wgpuBufferWriteMappedRange_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferWriteMappedRange_ADDR, wgpuBufferWriteMappedRange_DESC) }
actual fun wgpuBufferWriteMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (wgpuBufferWriteMappedRange_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), data?.handler ?: MemorySegment.NULL, size.toLong()) as Int).toUInt()
}

private val wgpuBufferAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBufferAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferAddRef") }
private val wgpuBufferAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferAddRef_ADDR, wgpuBufferAddRef_DESC) }
actual fun wgpuBufferAddRef(buffer: WGPUBuffer?): Unit {
    wgpuBufferAddRef_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuBufferRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuBufferRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuBufferRelease") }
private val wgpuBufferRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuBufferRelease_ADDR, wgpuBufferRelease_DESC) }
actual fun wgpuBufferRelease(buffer: WGPUBuffer?): Unit {
    wgpuBufferRelease_HANDLE.invokeExact(buffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandBufferSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuCommandBufferSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferSetLabel") }
private val wgpuCommandBufferSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandBufferSetLabel_ADDR, wgpuCommandBufferSetLabel_DESC) }
actual fun wgpuCommandBufferSetLabel(commandBuffer: WGPUCommandBuffer?, label: WGPUStringView): Unit {
    wgpuCommandBufferSetLabel_HANDLE.invokeExact(commandBuffer?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuCommandBufferAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuCommandBufferAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferAddRef") }
private val wgpuCommandBufferAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandBufferAddRef_ADDR, wgpuCommandBufferAddRef_DESC) }
actual fun wgpuCommandBufferAddRef(commandBuffer: WGPUCommandBuffer?): Unit {
    wgpuCommandBufferAddRef_HANDLE.invokeExact(commandBuffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandBufferRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuCommandBufferRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferRelease") }
private val wgpuCommandBufferRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandBufferRelease_ADDR, wgpuCommandBufferRelease_DESC) }
actual fun wgpuCommandBufferRelease(commandBuffer: WGPUCommandBuffer?): Unit {
    wgpuCommandBufferRelease_HANDLE.invokeExact(commandBuffer?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderBeginComputePass_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderBeginComputePass_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderBeginComputePass") }
private val wgpuCommandEncoderBeginComputePass_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderBeginComputePass_ADDR, wgpuCommandEncoderBeginComputePass_DESC) }
actual fun wgpuCommandEncoderBeginComputePass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUComputePassDescriptor?): WGPUComputePassEncoder? {
    return (wgpuCommandEncoderBeginComputePass_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUComputePassEncoder(it) }
}

private val wgpuCommandEncoderBeginRenderPass_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderBeginRenderPass_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderBeginRenderPass") }
private val wgpuCommandEncoderBeginRenderPass_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderBeginRenderPass_ADDR, wgpuCommandEncoderBeginRenderPass_DESC) }
actual fun wgpuCommandEncoderBeginRenderPass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPURenderPassDescriptor?): WGPURenderPassEncoder? {
    return (wgpuCommandEncoderBeginRenderPass_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderPassEncoder(it) }
}

private val wgpuCommandEncoderClearBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuCommandEncoderClearBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderClearBuffer") }
private val wgpuCommandEncoderClearBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderClearBuffer_ADDR, wgpuCommandEncoderClearBuffer_DESC) }
actual fun wgpuCommandEncoderClearBuffer(commandEncoder: WGPUCommandEncoder?, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    wgpuCommandEncoderClearBuffer_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), size.toLong())
    return
}

private val wgpuCommandEncoderCopyBufferToBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuCommandEncoderCopyBufferToBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyBufferToBuffer") }
private val wgpuCommandEncoderCopyBufferToBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderCopyBufferToBuffer_ADDR, wgpuCommandEncoderCopyBufferToBuffer_DESC) }
actual fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUBuffer?, sourceOffset: ULong, destination: WGPUBuffer?, destinationOffset: ULong, size: ULong): Unit {
    wgpuCommandEncoderCopyBufferToBuffer_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, source?.handler?.handler ?: MemorySegment.NULL, sourceOffset.toLong(), destination?.handler?.handler ?: MemorySegment.NULL, destinationOffset.toLong(), size.toLong())
    return
}

private val wgpuCommandEncoderCopyBufferToTexture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderCopyBufferToTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyBufferToTexture") }
private val wgpuCommandEncoderCopyBufferToTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderCopyBufferToTexture_ADDR, wgpuCommandEncoderCopyBufferToTexture_DESC) }
actual fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyBufferInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    wgpuCommandEncoderCopyBufferToTexture_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, source?.handler?.handler ?: MemorySegment.NULL, destination?.handler?.handler ?: MemorySegment.NULL, copySize?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderCopyTextureToBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderCopyTextureToBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyTextureToBuffer") }
private val wgpuCommandEncoderCopyTextureToBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderCopyTextureToBuffer_ADDR, wgpuCommandEncoderCopyTextureToBuffer_DESC) }
actual fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyBufferInfo?, copySize: WGPUExtent3D?): Unit {
    wgpuCommandEncoderCopyTextureToBuffer_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, source?.handler?.handler ?: MemorySegment.NULL, destination?.handler?.handler ?: MemorySegment.NULL, copySize?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderCopyTextureToTexture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderCopyTextureToTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyTextureToTexture") }
private val wgpuCommandEncoderCopyTextureToTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderCopyTextureToTexture_ADDR, wgpuCommandEncoderCopyTextureToTexture_DESC) }
actual fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    wgpuCommandEncoderCopyTextureToTexture_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, source?.handler?.handler ?: MemorySegment.NULL, destination?.handler?.handler ?: MemorySegment.NULL, copySize?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderFinish_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderFinish_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderFinish") }
private val wgpuCommandEncoderFinish_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderFinish_ADDR, wgpuCommandEncoderFinish_DESC) }
actual fun wgpuCommandEncoderFinish(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUCommandBufferDescriptor?): WGPUCommandBuffer? {
    return (wgpuCommandEncoderFinish_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUCommandBuffer(it) }
}

private val wgpuCommandEncoderInsertDebugMarker_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuCommandEncoderInsertDebugMarker_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderInsertDebugMarker") }
private val wgpuCommandEncoderInsertDebugMarker_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderInsertDebugMarker_ADDR, wgpuCommandEncoderInsertDebugMarker_DESC) }
actual fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: WGPUCommandEncoder?, markerLabel: WGPUStringView): Unit {
    wgpuCommandEncoderInsertDebugMarker_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, markerLabel.handler.handler)
    return
}

private val wgpuCommandEncoderPopDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuCommandEncoderPopDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderPopDebugGroup") }
private val wgpuCommandEncoderPopDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderPopDebugGroup_ADDR, wgpuCommandEncoderPopDebugGroup_DESC) }
actual fun wgpuCommandEncoderPopDebugGroup(commandEncoder: WGPUCommandEncoder?): Unit {
    wgpuCommandEncoderPopDebugGroup_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderPushDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuCommandEncoderPushDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderPushDebugGroup") }
private val wgpuCommandEncoderPushDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderPushDebugGroup_ADDR, wgpuCommandEncoderPushDebugGroup_DESC) }
actual fun wgpuCommandEncoderPushDebugGroup(commandEncoder: WGPUCommandEncoder?, groupLabel: WGPUStringView): Unit {
    wgpuCommandEncoderPushDebugGroup_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, groupLabel.handler.handler)
    return
}

private val wgpuCommandEncoderResolveQuerySet_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuCommandEncoderResolveQuerySet_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderResolveQuerySet") }
private val wgpuCommandEncoderResolveQuerySet_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderResolveQuerySet_ADDR, wgpuCommandEncoderResolveQuerySet_DESC) }
actual fun wgpuCommandEncoderResolveQuerySet(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, firstQuery: UInt, queryCount: UInt, destination: WGPUBuffer?, destinationOffset: ULong): Unit {
    wgpuCommandEncoderResolveQuerySet_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, firstQuery.toInt(), queryCount.toInt(), destination?.handler?.handler ?: MemorySegment.NULL, destinationOffset.toLong())
    return
}

private val wgpuCommandEncoderSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuCommandEncoderSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderSetLabel") }
private val wgpuCommandEncoderSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderSetLabel_ADDR, wgpuCommandEncoderSetLabel_DESC) }
actual fun wgpuCommandEncoderSetLabel(commandEncoder: WGPUCommandEncoder?, label: WGPUStringView): Unit {
    wgpuCommandEncoderSetLabel_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuCommandEncoderWriteTimestamp_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuCommandEncoderWriteTimestamp_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderWriteTimestamp") }
private val wgpuCommandEncoderWriteTimestamp_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderWriteTimestamp_ADDR, wgpuCommandEncoderWriteTimestamp_DESC) }
actual fun wgpuCommandEncoderWriteTimestamp(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    wgpuCommandEncoderWriteTimestamp_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuCommandEncoderAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuCommandEncoderAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderAddRef") }
private val wgpuCommandEncoderAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderAddRef_ADDR, wgpuCommandEncoderAddRef_DESC) }
actual fun wgpuCommandEncoderAddRef(commandEncoder: WGPUCommandEncoder?): Unit {
    wgpuCommandEncoderAddRef_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuCommandEncoderRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderRelease") }
private val wgpuCommandEncoderRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderRelease_ADDR, wgpuCommandEncoderRelease_DESC) }
actual fun wgpuCommandEncoderRelease(commandEncoder: WGPUCommandEncoder?): Unit {
    wgpuCommandEncoderRelease_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderDispatchWorkgroups_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuComputePassEncoderDispatchWorkgroups_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderDispatchWorkgroups") }
private val wgpuComputePassEncoderDispatchWorkgroups_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderDispatchWorkgroups_ADDR, wgpuComputePassEncoderDispatchWorkgroups_DESC) }
actual fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: WGPUComputePassEncoder?, workgroupCountX: UInt, workgroupCountY: UInt, workgroupCountZ: UInt): Unit {
    wgpuComputePassEncoderDispatchWorkgroups_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, workgroupCountX.toInt(), workgroupCountY.toInt(), workgroupCountZ.toInt())
    return
}

private val wgpuComputePassEncoderDispatchWorkgroupsIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuComputePassEncoderDispatchWorkgroupsIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderDispatchWorkgroupsIndirect") }
private val wgpuComputePassEncoderDispatchWorkgroupsIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderDispatchWorkgroupsIndirect_ADDR, wgpuComputePassEncoderDispatchWorkgroupsIndirect_DESC) }
actual fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: WGPUComputePassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    wgpuComputePassEncoderDispatchWorkgroupsIndirect_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, indirectBuffer?.handler?.handler ?: MemorySegment.NULL, indirectOffset.toLong())
    return
}

private val wgpuComputePassEncoderEnd_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePassEncoderEnd_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderEnd") }
private val wgpuComputePassEncoderEnd_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderEnd_ADDR, wgpuComputePassEncoderEnd_DESC) }
actual fun wgpuComputePassEncoderEnd(computePassEncoder: WGPUComputePassEncoder?): Unit {
    wgpuComputePassEncoderEnd_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderInsertDebugMarker_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuComputePassEncoderInsertDebugMarker_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderInsertDebugMarker") }
private val wgpuComputePassEncoderInsertDebugMarker_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderInsertDebugMarker_ADDR, wgpuComputePassEncoderInsertDebugMarker_DESC) }
actual fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: WGPUComputePassEncoder?, markerLabel: WGPUStringView): Unit {
    wgpuComputePassEncoderInsertDebugMarker_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, markerLabel.handler.handler)
    return
}

private val wgpuComputePassEncoderPopDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePassEncoderPopDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderPopDebugGroup") }
private val wgpuComputePassEncoderPopDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderPopDebugGroup_ADDR, wgpuComputePassEncoderPopDebugGroup_DESC) }
actual fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: WGPUComputePassEncoder?): Unit {
    wgpuComputePassEncoderPopDebugGroup_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderPushDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuComputePassEncoderPushDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderPushDebugGroup") }
private val wgpuComputePassEncoderPushDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderPushDebugGroup_ADDR, wgpuComputePassEncoderPushDebugGroup_DESC) }
actual fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: WGPUComputePassEncoder?, groupLabel: WGPUStringView): Unit {
    wgpuComputePassEncoderPushDebugGroup_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, groupLabel.handler.handler)
    return
}

private val wgpuComputePassEncoderSetBindGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuComputePassEncoderSetBindGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetBindGroup") }
private val wgpuComputePassEncoderSetBindGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderSetBindGroup_ADDR, wgpuComputePassEncoderSetBindGroup_DESC) }
actual fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: WGPUComputePassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    wgpuComputePassEncoderSetBindGroup_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, groupIndex.toInt(), group?.handler?.handler ?: MemorySegment.NULL, dynamicOffsetCount.toLong(), dynamicOffsets?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderSetImmediates_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuComputePassEncoderSetImmediates_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetImmediates") }
private val wgpuComputePassEncoderSetImmediates_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderSetImmediates_ADDR, wgpuComputePassEncoderSetImmediates_DESC) }
actual fun wgpuComputePassEncoderSetImmediates(computePassEncoder: WGPUComputePassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    wgpuComputePassEncoderSetImmediates_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, offset.toInt(), data?.handler ?: MemorySegment.NULL, size.toLong())
    return
}

private val wgpuComputePassEncoderSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuComputePassEncoderSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetLabel") }
private val wgpuComputePassEncoderSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderSetLabel_ADDR, wgpuComputePassEncoderSetLabel_DESC) }
actual fun wgpuComputePassEncoderSetLabel(computePassEncoder: WGPUComputePassEncoder?, label: WGPUStringView): Unit {
    wgpuComputePassEncoderSetLabel_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuComputePassEncoderSetPipeline_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuComputePassEncoderSetPipeline_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetPipeline") }
private val wgpuComputePassEncoderSetPipeline_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderSetPipeline_ADDR, wgpuComputePassEncoderSetPipeline_DESC) }
actual fun wgpuComputePassEncoderSetPipeline(computePassEncoder: WGPUComputePassEncoder?, pipeline: WGPUComputePipeline?): Unit {
    wgpuComputePassEncoderSetPipeline_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, pipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePassEncoderAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderAddRef") }
private val wgpuComputePassEncoderAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderAddRef_ADDR, wgpuComputePassEncoderAddRef_DESC) }
actual fun wgpuComputePassEncoderAddRef(computePassEncoder: WGPUComputePassEncoder?): Unit {
    wgpuComputePassEncoderAddRef_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePassEncoderRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderRelease") }
private val wgpuComputePassEncoderRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderRelease_ADDR, wgpuComputePassEncoderRelease_DESC) }
actual fun wgpuComputePassEncoderRelease(computePassEncoder: WGPUComputePassEncoder?): Unit {
    wgpuComputePassEncoderRelease_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePipelineGetBindGroupLayout_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuComputePipelineGetBindGroupLayout_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineGetBindGroupLayout") }
private val wgpuComputePipelineGetBindGroupLayout_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePipelineGetBindGroupLayout_ADDR, wgpuComputePipelineGetBindGroupLayout_DESC) }
actual fun wgpuComputePipelineGetBindGroupLayout(computePipeline: WGPUComputePipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return (wgpuComputePipelineGetBindGroupLayout_HANDLE.invokeExact(computePipeline?.handler?.handler ?: MemorySegment.NULL, groupIndex.toInt()) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

private val wgpuComputePipelineSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuComputePipelineSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineSetLabel") }
private val wgpuComputePipelineSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePipelineSetLabel_ADDR, wgpuComputePipelineSetLabel_DESC) }
actual fun wgpuComputePipelineSetLabel(computePipeline: WGPUComputePipeline?, label: WGPUStringView): Unit {
    wgpuComputePipelineSetLabel_HANDLE.invokeExact(computePipeline?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuComputePipelineAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePipelineAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineAddRef") }
private val wgpuComputePipelineAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePipelineAddRef_ADDR, wgpuComputePipelineAddRef_DESC) }
actual fun wgpuComputePipelineAddRef(computePipeline: WGPUComputePipeline?): Unit {
    wgpuComputePipelineAddRef_HANDLE.invokeExact(computePipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePipelineRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePipelineRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineRelease") }
private val wgpuComputePipelineRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePipelineRelease_ADDR, wgpuComputePipelineRelease_DESC) }
actual fun wgpuComputePipelineRelease(computePipeline: WGPUComputePipeline?): Unit {
    wgpuComputePipelineRelease_HANDLE.invokeExact(computePipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuDeviceCreateBindGroup_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateBindGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBindGroup") }
private val wgpuDeviceCreateBindGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateBindGroup_ADDR, wgpuDeviceCreateBindGroup_DESC) }
actual fun wgpuDeviceCreateBindGroup(device: WGPUDevice?, descriptor: WGPUBindGroupDescriptor?): WGPUBindGroup? {
    return (wgpuDeviceCreateBindGroup_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroup(it) }
}

private val wgpuDeviceCreateBindGroupLayout_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateBindGroupLayout_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBindGroupLayout") }
private val wgpuDeviceCreateBindGroupLayout_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateBindGroupLayout_ADDR, wgpuDeviceCreateBindGroupLayout_DESC) }
actual fun wgpuDeviceCreateBindGroupLayout(device: WGPUDevice?, descriptor: WGPUBindGroupLayoutDescriptor?): WGPUBindGroupLayout? {
    return (wgpuDeviceCreateBindGroupLayout_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

private val wgpuDeviceCreateBuffer_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBuffer") }
private val wgpuDeviceCreateBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateBuffer_ADDR, wgpuDeviceCreateBuffer_DESC) }
actual fun wgpuDeviceCreateBuffer(device: WGPUDevice?, descriptor: WGPUBufferDescriptor?): WGPUBuffer? {
    return (wgpuDeviceCreateBuffer_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBuffer(it) }
}

private val wgpuDeviceCreateCommandEncoder_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateCommandEncoder_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateCommandEncoder") }
private val wgpuDeviceCreateCommandEncoder_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateCommandEncoder_ADDR, wgpuDeviceCreateCommandEncoder_DESC) }
actual fun wgpuDeviceCreateCommandEncoder(device: WGPUDevice?, descriptor: WGPUCommandEncoderDescriptor?): WGPUCommandEncoder? {
    return (wgpuDeviceCreateCommandEncoder_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUCommandEncoder(it) }
}

private val wgpuDeviceCreateComputePipeline_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateComputePipeline_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateComputePipeline") }
private val wgpuDeviceCreateComputePipeline_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateComputePipeline_ADDR, wgpuDeviceCreateComputePipeline_DESC) }
actual fun wgpuDeviceCreateComputePipeline(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?): WGPUComputePipeline? {
    return (wgpuDeviceCreateComputePipeline_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUComputePipeline(it) }
}

private val wgpuDeviceCreateComputePipelineAsync_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS, WGPUCreateComputePipelineAsyncCallbackInfo.layout)
private val wgpuDeviceCreateComputePipelineAsync_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateComputePipelineAsync") }
private val wgpuDeviceCreateComputePipelineAsync_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateComputePipelineAsync_ADDR, wgpuDeviceCreateComputePipelineAsync_DESC) }
actual fun wgpuDeviceCreateComputePipelineAsync(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?, callbackInfo: WGPUCreateComputePipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuDeviceCreateComputePipelineAsync_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuDeviceCreatePipelineLayout_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreatePipelineLayout_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreatePipelineLayout") }
private val wgpuDeviceCreatePipelineLayout_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreatePipelineLayout_ADDR, wgpuDeviceCreatePipelineLayout_DESC) }
actual fun wgpuDeviceCreatePipelineLayout(device: WGPUDevice?, descriptor: WGPUPipelineLayoutDescriptor?): WGPUPipelineLayout? {
    return (wgpuDeviceCreatePipelineLayout_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
}

private val wgpuDeviceCreateQuerySet_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateQuerySet_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateQuerySet") }
private val wgpuDeviceCreateQuerySet_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateQuerySet_ADDR, wgpuDeviceCreateQuerySet_DESC) }
actual fun wgpuDeviceCreateQuerySet(device: WGPUDevice?, descriptor: WGPUQuerySetDescriptor?): WGPUQuerySet? {
    return (wgpuDeviceCreateQuerySet_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
}

private val wgpuDeviceCreateRenderBundleEncoder_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateRenderBundleEncoder_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderBundleEncoder") }
private val wgpuDeviceCreateRenderBundleEncoder_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateRenderBundleEncoder_ADDR, wgpuDeviceCreateRenderBundleEncoder_DESC) }
actual fun wgpuDeviceCreateRenderBundleEncoder(device: WGPUDevice?, descriptor: WGPURenderBundleEncoderDescriptor?): WGPURenderBundleEncoder? {
    return (wgpuDeviceCreateRenderBundleEncoder_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderBundleEncoder(it) }
}

private val wgpuDeviceCreateRenderPipeline_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateRenderPipeline_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderPipeline") }
private val wgpuDeviceCreateRenderPipeline_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateRenderPipeline_ADDR, wgpuDeviceCreateRenderPipeline_DESC) }
actual fun wgpuDeviceCreateRenderPipeline(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?): WGPURenderPipeline? {
    return (wgpuDeviceCreateRenderPipeline_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderPipeline(it) }
}

private val wgpuDeviceCreateRenderPipelineAsync_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS, WGPUCreateRenderPipelineAsyncCallbackInfo.layout)
private val wgpuDeviceCreateRenderPipelineAsync_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderPipelineAsync") }
private val wgpuDeviceCreateRenderPipelineAsync_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateRenderPipelineAsync_ADDR, wgpuDeviceCreateRenderPipelineAsync_DESC) }
actual fun wgpuDeviceCreateRenderPipelineAsync(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?, callbackInfo: WGPUCreateRenderPipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuDeviceCreateRenderPipelineAsync_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuDeviceCreateSampler_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateSampler_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateSampler") }
private val wgpuDeviceCreateSampler_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateSampler_ADDR, wgpuDeviceCreateSampler_DESC) }
actual fun wgpuDeviceCreateSampler(device: WGPUDevice?, descriptor: WGPUSamplerDescriptor?): WGPUSampler? {
    return (wgpuDeviceCreateSampler_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUSampler(it) }
}

private val wgpuDeviceCreateShaderModule_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateShaderModule_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModule") }
private val wgpuDeviceCreateShaderModule_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateShaderModule_ADDR, wgpuDeviceCreateShaderModule_DESC) }
actual fun wgpuDeviceCreateShaderModule(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?): WGPUShaderModule? {
    return (wgpuDeviceCreateShaderModule_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

private val wgpuDeviceCreateTexture_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateTexture") }
private val wgpuDeviceCreateTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateTexture_ADDR, wgpuDeviceCreateTexture_DESC) }
actual fun wgpuDeviceCreateTexture(device: WGPUDevice?, descriptor: WGPUTextureDescriptor?): WGPUTexture? {
    return (wgpuDeviceCreateTexture_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTexture(it) }
}

private val wgpuDeviceDestroy_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuDeviceDestroy_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceDestroy") }
private val wgpuDeviceDestroy_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceDestroy_ADDR, wgpuDeviceDestroy_DESC) }
actual fun wgpuDeviceDestroy(device: WGPUDevice?): Unit {
    wgpuDeviceDestroy_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuDeviceGetAdapterInfo_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceGetAdapterInfo_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetAdapterInfo") }
private val wgpuDeviceGetAdapterInfo_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetAdapterInfo_ADDR, wgpuDeviceGetAdapterInfo_DESC) }
actual fun wgpuDeviceGetAdapterInfo(device: WGPUDevice?, adapterInfo: WGPUAdapterInfo?): WGPUStatus {
    return (wgpuDeviceGetAdapterInfo_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, adapterInfo?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuDeviceGetFeatures_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceGetFeatures_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetFeatures") }
private val wgpuDeviceGetFeatures_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetFeatures_ADDR, wgpuDeviceGetFeatures_DESC) }
actual fun wgpuDeviceGetFeatures(device: WGPUDevice?, features: WGPUSupportedFeatures?): Unit {
    wgpuDeviceGetFeatures_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, features?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuDeviceGetLimits_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceGetLimits_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetLimits") }
private val wgpuDeviceGetLimits_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetLimits_ADDR, wgpuDeviceGetLimits_DESC) }
actual fun wgpuDeviceGetLimits(device: WGPUDevice?, limits: WGPULimits?): WGPUStatus {
    return (wgpuDeviceGetLimits_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, limits?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuDeviceGetLostFuture_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS)
private val wgpuDeviceGetLostFuture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetLostFuture") }
private val wgpuDeviceGetLostFuture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetLostFuture_ADDR, wgpuDeviceGetLostFuture_DESC) }
actual fun wgpuDeviceGetLostFuture(device: WGPUDevice?): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuDeviceGetLostFuture_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), device?.handler?.handler ?: MemorySegment.NULL) as MemorySegment))
}

private val wgpuDeviceGetQueue_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceGetQueue_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetQueue") }
private val wgpuDeviceGetQueue_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetQueue_ADDR, wgpuDeviceGetQueue_DESC) }
actual fun wgpuDeviceGetQueue(device: WGPUDevice?): WGPUQueue? {
    return (wgpuDeviceGetQueue_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUQueue(it) }
}

private val wgpuDeviceHasFeature_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuDeviceHasFeature_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceHasFeature") }
private val wgpuDeviceHasFeature_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceHasFeature_ADDR, wgpuDeviceHasFeature_DESC) }
actual fun wgpuDeviceHasFeature(device: WGPUDevice?, feature: WGPUFeatureName): UInt {
    return (wgpuDeviceHasFeature_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, feature.toInt()) as Int).toUInt()
}

private val wgpuDevicePopErrorScope_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, WGPUPopErrorScopeCallbackInfo.layout)
private val wgpuDevicePopErrorScope_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDevicePopErrorScope") }
private val wgpuDevicePopErrorScope_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDevicePopErrorScope_ADDR, wgpuDevicePopErrorScope_DESC) }
actual fun wgpuDevicePopErrorScope(device: WGPUDevice?, callbackInfo: WGPUPopErrorScopeCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuDevicePopErrorScope_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), device?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuDevicePushErrorScope_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuDevicePushErrorScope_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDevicePushErrorScope") }
private val wgpuDevicePushErrorScope_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDevicePushErrorScope_ADDR, wgpuDevicePushErrorScope_DESC) }
actual fun wgpuDevicePushErrorScope(device: WGPUDevice?, filter: WGPUErrorFilter): Unit {
    wgpuDevicePushErrorScope_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, filter.toInt())
    return
}

private val wgpuDeviceSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuDeviceSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceSetLabel") }
private val wgpuDeviceSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceSetLabel_ADDR, wgpuDeviceSetLabel_DESC) }
actual fun wgpuDeviceSetLabel(device: WGPUDevice?, label: WGPUStringView): Unit {
    wgpuDeviceSetLabel_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuDeviceAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuDeviceAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceAddRef") }
private val wgpuDeviceAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceAddRef_ADDR, wgpuDeviceAddRef_DESC) }
actual fun wgpuDeviceAddRef(device: WGPUDevice?): Unit {
    wgpuDeviceAddRef_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuDeviceRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuDeviceRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceRelease") }
private val wgpuDeviceRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceRelease_ADDR, wgpuDeviceRelease_DESC) }
actual fun wgpuDeviceRelease(device: WGPUDevice?): Unit {
    wgpuDeviceRelease_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuExternalTextureSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuExternalTextureSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureSetLabel") }
private val wgpuExternalTextureSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuExternalTextureSetLabel_ADDR, wgpuExternalTextureSetLabel_DESC) }
actual fun wgpuExternalTextureSetLabel(externalTexture: WGPUExternalTexture?, label: WGPUStringView): Unit {
    wgpuExternalTextureSetLabel_HANDLE.invokeExact(externalTexture?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuExternalTextureAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuExternalTextureAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureAddRef") }
private val wgpuExternalTextureAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuExternalTextureAddRef_ADDR, wgpuExternalTextureAddRef_DESC) }
actual fun wgpuExternalTextureAddRef(externalTexture: WGPUExternalTexture?): Unit {
    wgpuExternalTextureAddRef_HANDLE.invokeExact(externalTexture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuExternalTextureRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuExternalTextureRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureRelease") }
private val wgpuExternalTextureRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuExternalTextureRelease_ADDR, wgpuExternalTextureRelease_DESC) }
actual fun wgpuExternalTextureRelease(externalTexture: WGPUExternalTexture?): Unit {
    wgpuExternalTextureRelease_HANDLE.invokeExact(externalTexture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuInstanceCreateSurface_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuInstanceCreateSurface_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceCreateSurface") }
private val wgpuInstanceCreateSurface_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceCreateSurface_ADDR, wgpuInstanceCreateSurface_DESC) }
actual fun wgpuInstanceCreateSurface(instance: WGPUInstance?, descriptor: WGPUSurfaceDescriptor?): WGPUSurface? {
    return (wgpuInstanceCreateSurface_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUSurface(it) }
}

private val wgpuInstanceGetWGSLLanguageFeatures_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuInstanceGetWGSLLanguageFeatures_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceGetWGSLLanguageFeatures") }
private val wgpuInstanceGetWGSLLanguageFeatures_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceGetWGSLLanguageFeatures_ADDR, wgpuInstanceGetWGSLLanguageFeatures_DESC) }
actual fun wgpuInstanceGetWGSLLanguageFeatures(instance: WGPUInstance?, features: WGPUSupportedWGSLLanguageFeatures?): Unit {
    wgpuInstanceGetWGSLLanguageFeatures_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, features?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuInstanceHasWGSLLanguageFeature_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuInstanceHasWGSLLanguageFeature_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceHasWGSLLanguageFeature") }
private val wgpuInstanceHasWGSLLanguageFeature_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceHasWGSLLanguageFeature_ADDR, wgpuInstanceHasWGSLLanguageFeature_DESC) }
actual fun wgpuInstanceHasWGSLLanguageFeature(instance: WGPUInstance?, feature: WGPUWGSLLanguageFeatureName): UInt {
    return (wgpuInstanceHasWGSLLanguageFeature_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, feature.toInt()) as Int).toUInt()
}

private val wgpuInstanceProcessEvents_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuInstanceProcessEvents_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceProcessEvents") }
private val wgpuInstanceProcessEvents_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceProcessEvents_ADDR, wgpuInstanceProcessEvents_DESC) }
actual fun wgpuInstanceProcessEvents(instance: WGPUInstance?): Unit {
    wgpuInstanceProcessEvents_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuInstanceRequestAdapter_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS, WGPURequestAdapterCallbackInfo.layout)
private val wgpuInstanceRequestAdapter_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceRequestAdapter") }
private val wgpuInstanceRequestAdapter_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceRequestAdapter_ADDR, wgpuInstanceRequestAdapter_DESC) }
actual fun wgpuInstanceRequestAdapter(instance: WGPUInstance?, options: WGPURequestAdapterOptions?, callbackInfo: WGPURequestAdapterCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuInstanceRequestAdapter_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), instance?.handler?.handler ?: MemorySegment.NULL, options?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuInstanceWaitAny_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuInstanceWaitAny_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceWaitAny") }
private val wgpuInstanceWaitAny_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceWaitAny_ADDR, wgpuInstanceWaitAny_DESC) }
actual fun wgpuInstanceWaitAny(instance: WGPUInstance?, futureCount: ULong, futures: WGPUFutureWaitInfo?, timeoutNS: ULong): WGPUWaitStatus {
    return (wgpuInstanceWaitAny_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, futureCount.toLong(), futures?.handler?.handler ?: MemorySegment.NULL, timeoutNS.toLong()) as Int).toUInt()
}

private val wgpuInstanceAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuInstanceAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceAddRef") }
private val wgpuInstanceAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceAddRef_ADDR, wgpuInstanceAddRef_DESC) }
actual fun wgpuInstanceAddRef(instance: WGPUInstance?): Unit {
    wgpuInstanceAddRef_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuInstanceRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuInstanceRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceRelease") }
private val wgpuInstanceRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceRelease_ADDR, wgpuInstanceRelease_DESC) }
actual fun wgpuInstanceRelease(instance: WGPUInstance?): Unit {
    wgpuInstanceRelease_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuPipelineLayoutSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuPipelineLayoutSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutSetLabel") }
private val wgpuPipelineLayoutSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuPipelineLayoutSetLabel_ADDR, wgpuPipelineLayoutSetLabel_DESC) }
actual fun wgpuPipelineLayoutSetLabel(pipelineLayout: WGPUPipelineLayout?, label: WGPUStringView): Unit {
    wgpuPipelineLayoutSetLabel_HANDLE.invokeExact(pipelineLayout?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuPipelineLayoutAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuPipelineLayoutAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutAddRef") }
private val wgpuPipelineLayoutAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuPipelineLayoutAddRef_ADDR, wgpuPipelineLayoutAddRef_DESC) }
actual fun wgpuPipelineLayoutAddRef(pipelineLayout: WGPUPipelineLayout?): Unit {
    wgpuPipelineLayoutAddRef_HANDLE.invokeExact(pipelineLayout?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuPipelineLayoutRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuPipelineLayoutRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutRelease") }
private val wgpuPipelineLayoutRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuPipelineLayoutRelease_ADDR, wgpuPipelineLayoutRelease_DESC) }
actual fun wgpuPipelineLayoutRelease(pipelineLayout: WGPUPipelineLayout?): Unit {
    wgpuPipelineLayoutRelease_HANDLE.invokeExact(pipelineLayout?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQuerySetDestroy_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuQuerySetDestroy_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetDestroy") }
private val wgpuQuerySetDestroy_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetDestroy_ADDR, wgpuQuerySetDestroy_DESC) }
actual fun wgpuQuerySetDestroy(querySet: WGPUQuerySet?): Unit {
    wgpuQuerySetDestroy_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQuerySetGetCount_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuQuerySetGetCount_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetGetCount") }
private val wgpuQuerySetGetCount_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetGetCount_ADDR, wgpuQuerySetGetCount_DESC) }
actual fun wgpuQuerySetGetCount(querySet: WGPUQuerySet?): UInt {
    return (wgpuQuerySetGetCount_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuQuerySetGetType_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuQuerySetGetType_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetGetType") }
private val wgpuQuerySetGetType_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetGetType_ADDR, wgpuQuerySetGetType_DESC) }
actual fun wgpuQuerySetGetType(querySet: WGPUQuerySet?): WGPUQueryType {
    return (wgpuQuerySetGetType_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuQuerySetSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuQuerySetSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetSetLabel") }
private val wgpuQuerySetSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetSetLabel_ADDR, wgpuQuerySetSetLabel_DESC) }
actual fun wgpuQuerySetSetLabel(querySet: WGPUQuerySet?, label: WGPUStringView): Unit {
    wgpuQuerySetSetLabel_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuQuerySetAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuQuerySetAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetAddRef") }
private val wgpuQuerySetAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetAddRef_ADDR, wgpuQuerySetAddRef_DESC) }
actual fun wgpuQuerySetAddRef(querySet: WGPUQuerySet?): Unit {
    wgpuQuerySetAddRef_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQuerySetRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuQuerySetRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetRelease") }
private val wgpuQuerySetRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQuerySetRelease_ADDR, wgpuQuerySetRelease_DESC) }
actual fun wgpuQuerySetRelease(querySet: WGPUQuerySet?): Unit {
    wgpuQuerySetRelease_HANDLE.invokeExact(querySet?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQueueOnSubmittedWorkDone_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, WGPUQueueWorkDoneCallbackInfo.layout)
private val wgpuQueueOnSubmittedWorkDone_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueOnSubmittedWorkDone") }
private val wgpuQueueOnSubmittedWorkDone_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueOnSubmittedWorkDone_ADDR, wgpuQueueOnSubmittedWorkDone_DESC) }
actual fun wgpuQueueOnSubmittedWorkDone(queue: WGPUQueue?, callbackInfo: WGPUQueueWorkDoneCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuQueueOnSubmittedWorkDone_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), queue?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuQueueSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuQueueSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueSetLabel") }
private val wgpuQueueSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueSetLabel_ADDR, wgpuQueueSetLabel_DESC) }
actual fun wgpuQueueSetLabel(queue: WGPUQueue?, label: WGPUStringView): Unit {
    wgpuQueueSetLabel_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuQueueSubmit_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuQueueSubmit_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueSubmit") }
private val wgpuQueueSubmit_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueSubmit_ADDR, wgpuQueueSubmit_DESC) }
actual fun wgpuQueueSubmit(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): Unit {
    wgpuQueueSubmit_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL, commandCount.toLong(), commands?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQueueWriteBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuQueueWriteBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueWriteBuffer") }
private val wgpuQueueWriteBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueWriteBuffer_ADDR, wgpuQueueWriteBuffer_DESC) }
actual fun wgpuQueueWriteBuffer(queue: WGPUQueue?, buffer: WGPUBuffer?, bufferOffset: ULong, data: NativeAddress?, size: ULong): Unit {
    wgpuQueueWriteBuffer_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, bufferOffset.toLong(), data?.handler ?: MemorySegment.NULL, size.toLong())
    return
}

private val wgpuQueueWriteTexture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuQueueWriteTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueWriteTexture") }
private val wgpuQueueWriteTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueWriteTexture_ADDR, wgpuQueueWriteTexture_DESC) }
actual fun wgpuQueueWriteTexture(queue: WGPUQueue?, destination: WGPUTexelCopyTextureInfo?, data: NativeAddress?, dataSize: ULong, dataLayout: WGPUTexelCopyBufferLayout?, writeSize: WGPUExtent3D?): Unit {
    wgpuQueueWriteTexture_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL, destination?.handler?.handler ?: MemorySegment.NULL, data?.handler ?: MemorySegment.NULL, dataSize.toLong(), dataLayout?.handler?.handler ?: MemorySegment.NULL, writeSize?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQueueAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuQueueAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueAddRef") }
private val wgpuQueueAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueAddRef_ADDR, wgpuQueueAddRef_DESC) }
actual fun wgpuQueueAddRef(queue: WGPUQueue?): Unit {
    wgpuQueueAddRef_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuQueueRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuQueueRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueRelease") }
private val wgpuQueueRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueRelease_ADDR, wgpuQueueRelease_DESC) }
actual fun wgpuQueueRelease(queue: WGPUQueue?): Unit {
    wgpuQueueRelease_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderBundleSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleSetLabel") }
private val wgpuRenderBundleSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleSetLabel_ADDR, wgpuRenderBundleSetLabel_DESC) }
actual fun wgpuRenderBundleSetLabel(renderBundle: WGPURenderBundle?, label: WGPUStringView): Unit {
    wgpuRenderBundleSetLabel_HANDLE.invokeExact(renderBundle?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuRenderBundleAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderBundleAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleAddRef") }
private val wgpuRenderBundleAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleAddRef_ADDR, wgpuRenderBundleAddRef_DESC) }
actual fun wgpuRenderBundleAddRef(renderBundle: WGPURenderBundle?): Unit {
    wgpuRenderBundleAddRef_HANDLE.invokeExact(renderBundle?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderBundleRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleRelease") }
private val wgpuRenderBundleRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleRelease_ADDR, wgpuRenderBundleRelease_DESC) }
actual fun wgpuRenderBundleRelease(renderBundle: WGPURenderBundle?): Unit {
    wgpuRenderBundleRelease_HANDLE.invokeExact(renderBundle?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleEncoderDraw_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuRenderBundleEncoderDraw_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDraw") }
private val wgpuRenderBundleEncoderDraw_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderDraw_ADDR, wgpuRenderBundleEncoderDraw_DESC) }
actual fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: WGPURenderBundleEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    wgpuRenderBundleEncoderDraw_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

private val wgpuRenderBundleEncoderDrawIndexed_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuRenderBundleEncoderDrawIndexed_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndexed") }
private val wgpuRenderBundleEncoderDrawIndexed_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderDrawIndexed_ADDR, wgpuRenderBundleEncoderDrawIndexed_DESC) }
actual fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: WGPURenderBundleEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    wgpuRenderBundleEncoderDrawIndexed_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

private val wgpuRenderBundleEncoderDrawIndexedIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderBundleEncoderDrawIndexedIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndexedIndirect") }
private val wgpuRenderBundleEncoderDrawIndexedIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderDrawIndexedIndirect_ADDR, wgpuRenderBundleEncoderDrawIndexedIndirect_DESC) }
actual fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    wgpuRenderBundleEncoderDrawIndexedIndirect_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, indirectBuffer?.handler?.handler ?: MemorySegment.NULL, indirectOffset.toLong())
    return
}

private val wgpuRenderBundleEncoderDrawIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderBundleEncoderDrawIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndirect") }
private val wgpuRenderBundleEncoderDrawIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderDrawIndirect_ADDR, wgpuRenderBundleEncoderDrawIndirect_DESC) }
actual fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    wgpuRenderBundleEncoderDrawIndirect_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, indirectBuffer?.handler?.handler ?: MemorySegment.NULL, indirectOffset.toLong())
    return
}

private val wgpuRenderBundleEncoderFinish_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderFinish_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderFinish") }
private val wgpuRenderBundleEncoderFinish_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderFinish_ADDR, wgpuRenderBundleEncoderFinish_DESC) }
actual fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: WGPURenderBundleEncoder?, descriptor: WGPURenderBundleDescriptor?): WGPURenderBundle? {
    return (wgpuRenderBundleEncoderFinish_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderBundle(it) }
}

private val wgpuRenderBundleEncoderInsertDebugMarker_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderBundleEncoderInsertDebugMarker_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderInsertDebugMarker") }
private val wgpuRenderBundleEncoderInsertDebugMarker_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderInsertDebugMarker_ADDR, wgpuRenderBundleEncoderInsertDebugMarker_DESC) }
actual fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: WGPURenderBundleEncoder?, markerLabel: WGPUStringView): Unit {
    wgpuRenderBundleEncoderInsertDebugMarker_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, markerLabel.handler.handler)
    return
}

private val wgpuRenderBundleEncoderPopDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderPopDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderPopDebugGroup") }
private val wgpuRenderBundleEncoderPopDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderPopDebugGroup_ADDR, wgpuRenderBundleEncoderPopDebugGroup_DESC) }
actual fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    wgpuRenderBundleEncoderPopDebugGroup_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleEncoderPushDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderBundleEncoderPushDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderPushDebugGroup") }
private val wgpuRenderBundleEncoderPushDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderPushDebugGroup_ADDR, wgpuRenderBundleEncoderPushDebugGroup_DESC) }
actual fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupLabel: WGPUStringView): Unit {
    wgpuRenderBundleEncoderPushDebugGroup_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, groupLabel.handler.handler)
    return
}

private val wgpuRenderBundleEncoderSetBindGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderSetBindGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetBindGroup") }
private val wgpuRenderBundleEncoderSetBindGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetBindGroup_ADDR, wgpuRenderBundleEncoderSetBindGroup_DESC) }
actual fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    wgpuRenderBundleEncoderSetBindGroup_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, groupIndex.toInt(), group?.handler?.handler ?: MemorySegment.NULL, dynamicOffsetCount.toLong(), dynamicOffsets?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleEncoderSetImmediates_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderBundleEncoderSetImmediates_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetImmediates") }
private val wgpuRenderBundleEncoderSetImmediates_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetImmediates_ADDR, wgpuRenderBundleEncoderSetImmediates_DESC) }
actual fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: WGPURenderBundleEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    wgpuRenderBundleEncoderSetImmediates_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, offset.toInt(), data?.handler ?: MemorySegment.NULL, size.toLong())
    return
}

private val wgpuRenderBundleEncoderSetIndexBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuRenderBundleEncoderSetIndexBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetIndexBuffer") }
private val wgpuRenderBundleEncoderSetIndexBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetIndexBuffer_ADDR, wgpuRenderBundleEncoderSetIndexBuffer_DESC) }
actual fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    wgpuRenderBundleEncoderSetIndexBuffer_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, format.toInt(), offset.toLong(), size.toLong())
    return
}

private val wgpuRenderBundleEncoderSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderBundleEncoderSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetLabel") }
private val wgpuRenderBundleEncoderSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetLabel_ADDR, wgpuRenderBundleEncoderSetLabel_DESC) }
actual fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: WGPURenderBundleEncoder?, label: WGPUStringView): Unit {
    wgpuRenderBundleEncoderSetLabel_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuRenderBundleEncoderSetPipeline_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderSetPipeline_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetPipeline") }
private val wgpuRenderBundleEncoderSetPipeline_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetPipeline_ADDR, wgpuRenderBundleEncoderSetPipeline_DESC) }
actual fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: WGPURenderBundleEncoder?, pipeline: WGPURenderPipeline?): Unit {
    wgpuRenderBundleEncoderSetPipeline_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, pipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleEncoderSetVertexBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuRenderBundleEncoderSetVertexBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetVertexBuffer") }
private val wgpuRenderBundleEncoderSetVertexBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderSetVertexBuffer_ADDR, wgpuRenderBundleEncoderSetVertexBuffer_DESC) }
actual fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    wgpuRenderBundleEncoderSetVertexBuffer_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL, slot.toInt(), buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), size.toLong())
    return
}

private val wgpuRenderBundleEncoderAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderAddRef") }
private val wgpuRenderBundleEncoderAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderAddRef_ADDR, wgpuRenderBundleEncoderAddRef_DESC) }
actual fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    wgpuRenderBundleEncoderAddRef_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderBundleEncoderRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderBundleEncoderRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderRelease") }
private val wgpuRenderBundleEncoderRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderBundleEncoderRelease_ADDR, wgpuRenderBundleEncoderRelease_DESC) }
actual fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    wgpuRenderBundleEncoderRelease_HANDLE.invokeExact(renderBundleEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderBeginOcclusionQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderBeginOcclusionQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderBeginOcclusionQuery") }
private val wgpuRenderPassEncoderBeginOcclusionQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderBeginOcclusionQuery_ADDR, wgpuRenderPassEncoderBeginOcclusionQuery_DESC) }
actual fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?, queryIndex: UInt): Unit {
    wgpuRenderPassEncoderBeginOcclusionQuery_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderDraw_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderDraw_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDraw") }
private val wgpuRenderPassEncoderDraw_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderDraw_ADDR, wgpuRenderPassEncoderDraw_DESC) }
actual fun wgpuRenderPassEncoderDraw(renderPassEncoder: WGPURenderPassEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    wgpuRenderPassEncoderDraw_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

private val wgpuRenderPassEncoderDrawIndexed_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderDrawIndexed_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndexed") }
private val wgpuRenderPassEncoderDrawIndexed_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderDrawIndexed_ADDR, wgpuRenderPassEncoderDrawIndexed_DESC) }
actual fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: WGPURenderPassEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    wgpuRenderPassEncoderDrawIndexed_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

private val wgpuRenderPassEncoderDrawIndexedIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderPassEncoderDrawIndexedIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndexedIndirect") }
private val wgpuRenderPassEncoderDrawIndexedIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderDrawIndexedIndirect_ADDR, wgpuRenderPassEncoderDrawIndexedIndirect_DESC) }
actual fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    wgpuRenderPassEncoderDrawIndexedIndirect_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, indirectBuffer?.handler?.handler ?: MemorySegment.NULL, indirectOffset.toLong())
    return
}

private val wgpuRenderPassEncoderDrawIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderPassEncoderDrawIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndirect") }
private val wgpuRenderPassEncoderDrawIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderDrawIndirect_ADDR, wgpuRenderPassEncoderDrawIndirect_DESC) }
actual fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    wgpuRenderPassEncoderDrawIndirect_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, indirectBuffer?.handler?.handler ?: MemorySegment.NULL, indirectOffset.toLong())
    return
}

private val wgpuRenderPassEncoderEnd_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderEnd_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEnd") }
private val wgpuRenderPassEncoderEnd_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderEnd_ADDR, wgpuRenderPassEncoderEnd_DESC) }
actual fun wgpuRenderPassEncoderEnd(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderEnd_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderEndOcclusionQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderEndOcclusionQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEndOcclusionQuery") }
private val wgpuRenderPassEncoderEndOcclusionQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderEndOcclusionQuery_ADDR, wgpuRenderPassEncoderEndOcclusionQuery_DESC) }
actual fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderEndOcclusionQuery_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderExecuteBundles_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderExecuteBundles_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderExecuteBundles") }
private val wgpuRenderPassEncoderExecuteBundles_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderExecuteBundles_ADDR, wgpuRenderPassEncoderExecuteBundles_DESC) }
actual fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: WGPURenderPassEncoder?, bundleCount: ULong, bundles: NativeAddress?): Unit {
    wgpuRenderPassEncoderExecuteBundles_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, bundleCount.toLong(), bundles?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderInsertDebugMarker_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderPassEncoderInsertDebugMarker_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderInsertDebugMarker") }
private val wgpuRenderPassEncoderInsertDebugMarker_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderInsertDebugMarker_ADDR, wgpuRenderPassEncoderInsertDebugMarker_DESC) }
actual fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: WGPURenderPassEncoder?, markerLabel: WGPUStringView): Unit {
    wgpuRenderPassEncoderInsertDebugMarker_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, markerLabel.handler.handler)
    return
}

private val wgpuRenderPassEncoderPopDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderPopDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderPopDebugGroup") }
private val wgpuRenderPassEncoderPopDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderPopDebugGroup_ADDR, wgpuRenderPassEncoderPopDebugGroup_DESC) }
actual fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderPopDebugGroup_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderPushDebugGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderPassEncoderPushDebugGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderPushDebugGroup") }
private val wgpuRenderPassEncoderPushDebugGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderPushDebugGroup_ADDR, wgpuRenderPassEncoderPushDebugGroup_DESC) }
actual fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: WGPURenderPassEncoder?, groupLabel: WGPUStringView): Unit {
    wgpuRenderPassEncoderPushDebugGroup_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, groupLabel.handler.handler)
    return
}

private val wgpuRenderPassEncoderSetBindGroup_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderSetBindGroup_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetBindGroup") }
private val wgpuRenderPassEncoderSetBindGroup_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetBindGroup_ADDR, wgpuRenderPassEncoderSetBindGroup_DESC) }
actual fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: WGPURenderPassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    wgpuRenderPassEncoderSetBindGroup_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, groupIndex.toInt(), group?.handler?.handler ?: MemorySegment.NULL, dynamicOffsetCount.toLong(), dynamicOffsets?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderSetBlendConstant_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderSetBlendConstant_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetBlendConstant") }
private val wgpuRenderPassEncoderSetBlendConstant_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetBlendConstant_ADDR, wgpuRenderPassEncoderSetBlendConstant_DESC) }
actual fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: WGPURenderPassEncoder?, color: WGPUColor?): Unit {
    wgpuRenderPassEncoderSetBlendConstant_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, color?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderSetImmediates_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuRenderPassEncoderSetImmediates_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetImmediates") }
private val wgpuRenderPassEncoderSetImmediates_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetImmediates_ADDR, wgpuRenderPassEncoderSetImmediates_DESC) }
actual fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: WGPURenderPassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    wgpuRenderPassEncoderSetImmediates_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, offset.toInt(), data?.handler ?: MemorySegment.NULL, size.toLong())
    return
}

private val wgpuRenderPassEncoderSetIndexBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuRenderPassEncoderSetIndexBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetIndexBuffer") }
private val wgpuRenderPassEncoderSetIndexBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetIndexBuffer_ADDR, wgpuRenderPassEncoderSetIndexBuffer_DESC) }
actual fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    wgpuRenderPassEncoderSetIndexBuffer_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, format.toInt(), offset.toLong(), size.toLong())
    return
}

private val wgpuRenderPassEncoderSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderPassEncoderSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetLabel") }
private val wgpuRenderPassEncoderSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetLabel_ADDR, wgpuRenderPassEncoderSetLabel_DESC) }
actual fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: WGPURenderPassEncoder?, label: WGPUStringView): Unit {
    wgpuRenderPassEncoderSetLabel_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuRenderPassEncoderSetPipeline_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderSetPipeline_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetPipeline") }
private val wgpuRenderPassEncoderSetPipeline_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetPipeline_ADDR, wgpuRenderPassEncoderSetPipeline_DESC) }
actual fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: WGPURenderPassEncoder?, pipeline: WGPURenderPipeline?): Unit {
    wgpuRenderPassEncoderSetPipeline_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, pipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderSetScissorRect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderSetScissorRect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetScissorRect") }
private val wgpuRenderPassEncoderSetScissorRect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetScissorRect_ADDR, wgpuRenderPassEncoderSetScissorRect_DESC) }
actual fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: WGPURenderPassEncoder?, x: UInt, y: UInt, width: UInt, height: UInt): Unit {
    wgpuRenderPassEncoderSetScissorRect_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    return
}

private val wgpuRenderPassEncoderSetStencilReference_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderSetStencilReference_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetStencilReference") }
private val wgpuRenderPassEncoderSetStencilReference_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetStencilReference_ADDR, wgpuRenderPassEncoderSetStencilReference_DESC) }
actual fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: WGPURenderPassEncoder?, reference: UInt): Unit {
    wgpuRenderPassEncoderSetStencilReference_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, reference.toInt())
    return
}

private val wgpuRenderPassEncoderSetVertexBuffer_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG)
private val wgpuRenderPassEncoderSetVertexBuffer_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetVertexBuffer") }
private val wgpuRenderPassEncoderSetVertexBuffer_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetVertexBuffer_ADDR, wgpuRenderPassEncoderSetVertexBuffer_DESC) }
actual fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: WGPURenderPassEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    wgpuRenderPassEncoderSetVertexBuffer_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, slot.toInt(), buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), size.toLong())
    return
}

private val wgpuRenderPassEncoderSetViewport_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT, ValueLayout.JAVA_FLOAT)
private val wgpuRenderPassEncoderSetViewport_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetViewport") }
private val wgpuRenderPassEncoderSetViewport_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderSetViewport_ADDR, wgpuRenderPassEncoderSetViewport_DESC) }
actual fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: WGPURenderPassEncoder?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit {
    wgpuRenderPassEncoderSetViewport_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, x, y, width, height, minDepth, maxDepth)
    return
}

private val wgpuRenderPassEncoderAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderAddRef") }
private val wgpuRenderPassEncoderAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderAddRef_ADDR, wgpuRenderPassEncoderAddRef_DESC) }
actual fun wgpuRenderPassEncoderAddRef(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderAddRef_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderRelease") }
private val wgpuRenderPassEncoderRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderRelease_ADDR, wgpuRenderPassEncoderRelease_DESC) }
actual fun wgpuRenderPassEncoderRelease(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderRelease_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPipelineGetBindGroupLayout_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuRenderPipelineGetBindGroupLayout_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineGetBindGroupLayout") }
private val wgpuRenderPipelineGetBindGroupLayout_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPipelineGetBindGroupLayout_ADDR, wgpuRenderPipelineGetBindGroupLayout_DESC) }
actual fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: WGPURenderPipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return (wgpuRenderPipelineGetBindGroupLayout_HANDLE.invokeExact(renderPipeline?.handler?.handler ?: MemorySegment.NULL, groupIndex.toInt()) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

private val wgpuRenderPipelineSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuRenderPipelineSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineSetLabel") }
private val wgpuRenderPipelineSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPipelineSetLabel_ADDR, wgpuRenderPipelineSetLabel_DESC) }
actual fun wgpuRenderPipelineSetLabel(renderPipeline: WGPURenderPipeline?, label: WGPUStringView): Unit {
    wgpuRenderPipelineSetLabel_HANDLE.invokeExact(renderPipeline?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuRenderPipelineAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPipelineAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineAddRef") }
private val wgpuRenderPipelineAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPipelineAddRef_ADDR, wgpuRenderPipelineAddRef_DESC) }
actual fun wgpuRenderPipelineAddRef(renderPipeline: WGPURenderPipeline?): Unit {
    wgpuRenderPipelineAddRef_HANDLE.invokeExact(renderPipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPipelineRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPipelineRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineRelease") }
private val wgpuRenderPipelineRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPipelineRelease_ADDR, wgpuRenderPipelineRelease_DESC) }
actual fun wgpuRenderPipelineRelease(renderPipeline: WGPURenderPipeline?): Unit {
    wgpuRenderPipelineRelease_HANDLE.invokeExact(renderPipeline?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSamplerSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuSamplerSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSamplerSetLabel") }
private val wgpuSamplerSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSamplerSetLabel_ADDR, wgpuSamplerSetLabel_DESC) }
actual fun wgpuSamplerSetLabel(sampler: WGPUSampler?, label: WGPUStringView): Unit {
    wgpuSamplerSetLabel_HANDLE.invokeExact(sampler?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuSamplerAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuSamplerAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSamplerAddRef") }
private val wgpuSamplerAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSamplerAddRef_ADDR, wgpuSamplerAddRef_DESC) }
actual fun wgpuSamplerAddRef(sampler: WGPUSampler?): Unit {
    wgpuSamplerAddRef_HANDLE.invokeExact(sampler?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSamplerRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuSamplerRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSamplerRelease") }
private val wgpuSamplerRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSamplerRelease_ADDR, wgpuSamplerRelease_DESC) }
actual fun wgpuSamplerRelease(sampler: WGPUSampler?): Unit {
    wgpuSamplerRelease_HANDLE.invokeExact(sampler?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuShaderModuleGetCompilationInfo_DESC: FunctionDescriptor = FunctionDescriptor.of(WGPUFuture.layout, ValueLayout.ADDRESS, WGPUCompilationInfoCallbackInfo.layout)
private val wgpuShaderModuleGetCompilationInfo_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleGetCompilationInfo") }
private val wgpuShaderModuleGetCompilationInfo_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuShaderModuleGetCompilationInfo_ADDR, wgpuShaderModuleGetCompilationInfo_DESC) }
actual fun wgpuShaderModuleGetCompilationInfo(shaderModule: WGPUShaderModule?, callbackInfo: WGPUCompilationInfoCallbackInfo): WGPUFuture {
    return WGPUFuture(NativeAddress(wgpuShaderModuleGetCompilationInfo_HANDLE.invokeExact((Arena.ofAuto() as SegmentAllocator), shaderModule?.handler?.handler ?: MemorySegment.NULL, callbackInfo.handler.handler) as MemorySegment))
}

private val wgpuShaderModuleSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuShaderModuleSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleSetLabel") }
private val wgpuShaderModuleSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuShaderModuleSetLabel_ADDR, wgpuShaderModuleSetLabel_DESC) }
actual fun wgpuShaderModuleSetLabel(shaderModule: WGPUShaderModule?, label: WGPUStringView): Unit {
    wgpuShaderModuleSetLabel_HANDLE.invokeExact(shaderModule?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuShaderModuleAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuShaderModuleAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleAddRef") }
private val wgpuShaderModuleAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuShaderModuleAddRef_ADDR, wgpuShaderModuleAddRef_DESC) }
actual fun wgpuShaderModuleAddRef(shaderModule: WGPUShaderModule?): Unit {
    wgpuShaderModuleAddRef_HANDLE.invokeExact(shaderModule?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuShaderModuleRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuShaderModuleRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleRelease") }
private val wgpuShaderModuleRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuShaderModuleRelease_ADDR, wgpuShaderModuleRelease_DESC) }
actual fun wgpuShaderModuleRelease(shaderModule: WGPUShaderModule?): Unit {
    wgpuShaderModuleRelease_HANDLE.invokeExact(shaderModule?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSupportedFeaturesFreeMembers_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(WGPUSupportedFeatures.layout)
private val wgpuSupportedFeaturesFreeMembers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSupportedFeaturesFreeMembers") }
private val wgpuSupportedFeaturesFreeMembers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSupportedFeaturesFreeMembers_ADDR, wgpuSupportedFeaturesFreeMembers_DESC) }
actual fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: WGPUSupportedFeatures): Unit {
    wgpuSupportedFeaturesFreeMembers_HANDLE.invokeExact(supportedFeatures.handler.handler)
    return
}

private val wgpuSupportedInstanceFeaturesFreeMembers_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(WGPUSupportedInstanceFeatures.layout)
private val wgpuSupportedInstanceFeaturesFreeMembers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSupportedInstanceFeaturesFreeMembers") }
private val wgpuSupportedInstanceFeaturesFreeMembers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSupportedInstanceFeaturesFreeMembers_ADDR, wgpuSupportedInstanceFeaturesFreeMembers_DESC) }
actual fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: WGPUSupportedInstanceFeatures): Unit {
    wgpuSupportedInstanceFeaturesFreeMembers_HANDLE.invokeExact(supportedInstanceFeatures.handler.handler)
    return
}

private val wgpuSupportedWGSLLanguageFeaturesFreeMembers_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(WGPUSupportedWGSLLanguageFeatures.layout)
private val wgpuSupportedWGSLLanguageFeaturesFreeMembers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSupportedWGSLLanguageFeaturesFreeMembers") }
private val wgpuSupportedWGSLLanguageFeaturesFreeMembers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSupportedWGSLLanguageFeaturesFreeMembers_ADDR, wgpuSupportedWGSLLanguageFeaturesFreeMembers_DESC) }
actual fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: WGPUSupportedWGSLLanguageFeatures): Unit {
    wgpuSupportedWGSLLanguageFeaturesFreeMembers_HANDLE.invokeExact(supportedWGSLLanguageFeatures.handler.handler)
    return
}

private val wgpuSurfaceConfigure_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuSurfaceConfigure_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceConfigure") }
private val wgpuSurfaceConfigure_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceConfigure_ADDR, wgpuSurfaceConfigure_DESC) }
actual fun wgpuSurfaceConfigure(surface: WGPUSurface?, config: WGPUSurfaceConfiguration?): Unit {
    wgpuSurfaceConfigure_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL, config?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSurfaceGetCapabilities_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuSurfaceGetCapabilities_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceGetCapabilities") }
private val wgpuSurfaceGetCapabilities_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceGetCapabilities_ADDR, wgpuSurfaceGetCapabilities_DESC) }
actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    return (wgpuSurfaceGetCapabilities_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL, adapter?.handler?.handler ?: MemorySegment.NULL, capabilities?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuSurfaceGetCurrentTexture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuSurfaceGetCurrentTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceGetCurrentTexture") }
private val wgpuSurfaceGetCurrentTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceGetCurrentTexture_ADDR, wgpuSurfaceGetCurrentTexture_DESC) }
actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    wgpuSurfaceGetCurrentTexture_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL, surfaceTexture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSurfacePresent_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuSurfacePresent_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfacePresent") }
private val wgpuSurfacePresent_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfacePresent_ADDR, wgpuSurfacePresent_DESC) }
actual fun wgpuSurfacePresent(surface: WGPUSurface?): WGPUStatus {
    return (wgpuSurfacePresent_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuSurfaceSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuSurfaceSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceSetLabel") }
private val wgpuSurfaceSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceSetLabel_ADDR, wgpuSurfaceSetLabel_DESC) }
actual fun wgpuSurfaceSetLabel(surface: WGPUSurface?, label: WGPUStringView): Unit {
    wgpuSurfaceSetLabel_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuSurfaceUnconfigure_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuSurfaceUnconfigure_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceUnconfigure") }
private val wgpuSurfaceUnconfigure_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceUnconfigure_ADDR, wgpuSurfaceUnconfigure_DESC) }
actual fun wgpuSurfaceUnconfigure(surface: WGPUSurface?): Unit {
    wgpuSurfaceUnconfigure_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSurfaceAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuSurfaceAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceAddRef") }
private val wgpuSurfaceAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceAddRef_ADDR, wgpuSurfaceAddRef_DESC) }
actual fun wgpuSurfaceAddRef(surface: WGPUSurface?): Unit {
    wgpuSurfaceAddRef_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSurfaceRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuSurfaceRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceRelease") }
private val wgpuSurfaceRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceRelease_ADDR, wgpuSurfaceRelease_DESC) }
actual fun wgpuSurfaceRelease(surface: WGPUSurface?): Unit {
    wgpuSurfaceRelease_HANDLE.invokeExact(surface?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSurfaceCapabilitiesFreeMembers_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(WGPUSurfaceCapabilities.layout)
private val wgpuSurfaceCapabilitiesFreeMembers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceCapabilitiesFreeMembers") }
private val wgpuSurfaceCapabilitiesFreeMembers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSurfaceCapabilitiesFreeMembers_ADDR, wgpuSurfaceCapabilitiesFreeMembers_DESC) }
actual fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: WGPUSurfaceCapabilities): Unit {
    wgpuSurfaceCapabilitiesFreeMembers_HANDLE.invokeExact(surfaceCapabilities.handler.handler)
    return
}

private val wgpuTextureCreateView_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuTextureCreateView_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureCreateView") }
private val wgpuTextureCreateView_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureCreateView_ADDR, wgpuTextureCreateView_DESC) }
actual fun wgpuTextureCreateView(texture: WGPUTexture?, descriptor: WGPUTextureViewDescriptor?): WGPUTextureView? {
    return (wgpuTextureCreateView_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUTextureView(it) }
}

private val wgpuTextureDestroy_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuTextureDestroy_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureDestroy") }
private val wgpuTextureDestroy_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureDestroy_ADDR, wgpuTextureDestroy_DESC) }
actual fun wgpuTextureDestroy(texture: WGPUTexture?): Unit {
    wgpuTextureDestroy_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuTextureGetDepthOrArrayLayers_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetDepthOrArrayLayers_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetDepthOrArrayLayers") }
private val wgpuTextureGetDepthOrArrayLayers_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetDepthOrArrayLayers_ADDR, wgpuTextureGetDepthOrArrayLayers_DESC) }
actual fun wgpuTextureGetDepthOrArrayLayers(texture: WGPUTexture?): UInt {
    return (wgpuTextureGetDepthOrArrayLayers_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetDimension_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetDimension_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetDimension") }
private val wgpuTextureGetDimension_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetDimension_ADDR, wgpuTextureGetDimension_DESC) }
actual fun wgpuTextureGetDimension(texture: WGPUTexture?): WGPUTextureDimension {
    return (wgpuTextureGetDimension_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetFormat_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetFormat_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetFormat") }
private val wgpuTextureGetFormat_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetFormat_ADDR, wgpuTextureGetFormat_DESC) }
actual fun wgpuTextureGetFormat(texture: WGPUTexture?): WGPUTextureFormat {
    return (wgpuTextureGetFormat_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetHeight_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetHeight_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetHeight") }
private val wgpuTextureGetHeight_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetHeight_ADDR, wgpuTextureGetHeight_DESC) }
actual fun wgpuTextureGetHeight(texture: WGPUTexture?): UInt {
    return (wgpuTextureGetHeight_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetMipLevelCount_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetMipLevelCount_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetMipLevelCount") }
private val wgpuTextureGetMipLevelCount_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetMipLevelCount_ADDR, wgpuTextureGetMipLevelCount_DESC) }
actual fun wgpuTextureGetMipLevelCount(texture: WGPUTexture?): UInt {
    return (wgpuTextureGetMipLevelCount_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetSampleCount_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetSampleCount_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetSampleCount") }
private val wgpuTextureGetSampleCount_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetSampleCount_ADDR, wgpuTextureGetSampleCount_DESC) }
actual fun wgpuTextureGetSampleCount(texture: WGPUTexture?): UInt {
    return (wgpuTextureGetSampleCount_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetTextureBindingViewDimension_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetTextureBindingViewDimension_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetTextureBindingViewDimension") }
private val wgpuTextureGetTextureBindingViewDimension_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetTextureBindingViewDimension_ADDR, wgpuTextureGetTextureBindingViewDimension_DESC) }
actual fun wgpuTextureGetTextureBindingViewDimension(texture: WGPUTexture?): WGPUTextureViewDimension {
    return (wgpuTextureGetTextureBindingViewDimension_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureGetUsage_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuTextureGetUsage_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetUsage") }
private val wgpuTextureGetUsage_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetUsage_ADDR, wgpuTextureGetUsage_DESC) }
actual fun wgpuTextureGetUsage(texture: WGPUTexture?): ULong {
    return (wgpuTextureGetUsage_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Long).toULong()
}

private val wgpuTextureGetWidth_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuTextureGetWidth_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetWidth") }
private val wgpuTextureGetWidth_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetWidth_ADDR, wgpuTextureGetWidth_DESC) }
actual fun wgpuTextureGetWidth(texture: WGPUTexture?): UInt {
    return (wgpuTextureGetWidth_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuTextureSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuTextureSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureSetLabel") }
private val wgpuTextureSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureSetLabel_ADDR, wgpuTextureSetLabel_DESC) }
actual fun wgpuTextureSetLabel(texture: WGPUTexture?, label: WGPUStringView): Unit {
    wgpuTextureSetLabel_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuTextureAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuTextureAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureAddRef") }
private val wgpuTextureAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureAddRef_ADDR, wgpuTextureAddRef_DESC) }
actual fun wgpuTextureAddRef(texture: WGPUTexture?): Unit {
    wgpuTextureAddRef_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuTextureRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuTextureRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureRelease") }
private val wgpuTextureRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureRelease_ADDR, wgpuTextureRelease_DESC) }
actual fun wgpuTextureRelease(texture: WGPUTexture?): Unit {
    wgpuTextureRelease_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuTextureViewSetLabel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, WGPUStringView.layout)
private val wgpuTextureViewSetLabel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewSetLabel") }
private val wgpuTextureViewSetLabel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureViewSetLabel_ADDR, wgpuTextureViewSetLabel_DESC) }
actual fun wgpuTextureViewSetLabel(textureView: WGPUTextureView?, label: WGPUStringView): Unit {
    wgpuTextureViewSetLabel_HANDLE.invokeExact(textureView?.handler?.handler ?: MemorySegment.NULL, label.handler.handler)
    return
}

private val wgpuTextureViewAddRef_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuTextureViewAddRef_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewAddRef") }
private val wgpuTextureViewAddRef_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureViewAddRef_ADDR, wgpuTextureViewAddRef_DESC) }
actual fun wgpuTextureViewAddRef(textureView: WGPUTextureView?): Unit {
    wgpuTextureViewAddRef_HANDLE.invokeExact(textureView?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuTextureViewRelease_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuTextureViewRelease_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewRelease") }
private val wgpuTextureViewRelease_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureViewRelease_ADDR, wgpuTextureViewRelease_DESC) }
actual fun wgpuTextureViewRelease(textureView: WGPUTextureView?): Unit {
    wgpuTextureViewRelease_HANDLE.invokeExact(textureView?.handler?.handler ?: MemorySegment.NULL)
    return
}

actual interface WGPUXlibDisplayHandle : CStructure {
    actual var display: NativeAddress?
    actual var screen: Int
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("display"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("screen"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUXlibDisplayHandle")
        
        val display_VH: VarHandle = layout.varHandle(groupElement("display"))
        val screen_VH: VarHandle = layout.varHandle(groupElement("screen"))
        
        actual operator fun invoke(address: NativeAddress): WGPUXlibDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXlibDisplayHandle = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXlibDisplayHandle) -> Unit): ArrayHolder<WGPUXlibDisplayHandle> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUXlibDisplayHandle {
        override var display: NativeAddress?
            get() = (display_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = display_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var screen: Int
            get() = screen_VH.get(handler.handler, 0L) as Int
            set(value) = screen_VH.set(handler.handler, 0L, value)
    }
}

actual interface WGPUXcbDisplayHandle : CStructure {
    actual var connection: NativeAddress?
    actual var screen: Int
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("connection"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("screen"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUXcbDisplayHandle")
        
        val connection_VH: VarHandle = layout.varHandle(groupElement("connection"))
        val screen_VH: VarHandle = layout.varHandle(groupElement("screen"))
        
        actual operator fun invoke(address: NativeAddress): WGPUXcbDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXcbDisplayHandle = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXcbDisplayHandle) -> Unit): ArrayHolder<WGPUXcbDisplayHandle> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUXcbDisplayHandle {
        override var connection: NativeAddress?
            get() = (connection_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = connection_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var screen: Int
            get() = screen_VH.get(handler.handler, 0L) as Int
            set(value) = screen_VH.set(handler.handler, 0L, value)
    }
}

actual interface WGPUWaylandDisplayHandle : CStructure {
    actual var display: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("display")
        ).withByteAlignment(8).withName("WGPUWaylandDisplayHandle")
        
        val display_VH: VarHandle = layout.varHandle(groupElement("display"))
        
        actual operator fun invoke(address: NativeAddress): WGPUWaylandDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUWaylandDisplayHandle = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUWaylandDisplayHandle) -> Unit): ArrayHolder<WGPUWaylandDisplayHandle> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUWaylandDisplayHandle {
        override var display: NativeAddress?
            get() = (display_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = display_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUNativeDisplayHandle : CStructure {
    actual var type: WGPUNativeDisplayHandleType
    actual val xlib: WGPUXlibDisplayHandle?
    actual fun setXlib(value: WGPUXlibDisplayHandle)
    actual val xcb: WGPUXcbDisplayHandle?
    actual fun setXcb(value: WGPUXcbDisplayHandle)
    actual val wayland: WGPUWaylandDisplayHandle?
    actual fun setWayland(value: WGPUWaylandDisplayHandle)
    actual override val handler: NativeAddress
    actual companion object {
        val layout: GroupLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("type"),
            MemoryLayout.paddingLayout(4),
            MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_BYTE).withName("value")
        ).withName("WGPUNativeDisplayHandle")
        
        val type_VH: VarHandle = layout.varHandle(groupElement("type"))
        private val valueOffset: Long = layout.byteOffset(groupElement("value"))
        
        actual operator fun invoke(address: NativeAddress): WGPUNativeDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeDisplayHandle = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeDisplayHandle) -> Unit): ArrayHolder<WGPUNativeDisplayHandle> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUNativeDisplayHandle {
        override var type: WGPUNativeDisplayHandleType
            get() = (type_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUNativeDisplayHandleType
            set(value) = type_VH.set(handler.handler, 0L, value.toInt())
        override val xlib: WGPUXlibDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xlib) WGPUXlibDisplayHandle(NativeAddress(handler.handler.asSlice(valueOffset, WGPUXlibDisplayHandle.layout.byteSize()))) else null
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xlib
            MemorySegment.copy(value.handler.handler, 0L, handler.handler, valueOffset, WGPUXlibDisplayHandle.layout.byteSize())
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xcb) WGPUXcbDisplayHandle(NativeAddress(handler.handler.asSlice(valueOffset, WGPUXcbDisplayHandle.layout.byteSize()))) else null
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xcb
            MemorySegment.copy(value.handler.handler, 0L, handler.handler, valueOffset, WGPUXcbDisplayHandle.layout.byteSize())
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Wayland) WGPUWaylandDisplayHandle(NativeAddress(handler.handler.asSlice(valueOffset, WGPUWaylandDisplayHandle.layout.byteSize()))) else null
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Wayland
            MemorySegment.copy(value.handler.handler, 0L, handler.handler, valueOffset, WGPUWaylandDisplayHandle.layout.byteSize())
        }
    }
}

actual interface WGPUInstanceExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var backends: ULong
    actual var flags: ULong
    actual var dx12ShaderCompiler: WGPUDx12Compiler
    actual var gles3MinorVersion: WGPUGles3MinorVersion
    actual var glFenceBehaviour: WGPUGLFenceBehaviour
    actual var dxcPath: WGPUStringView
    actual var dxcMaxShaderModel: WGPUDxcMaxShaderModel
    actual var dx12PresentationSystem: WGPUDx12SwapchainKind
    actual var budgetForDeviceCreation: NativeAddress?
    actual var budgetForDeviceLoss: NativeAddress?
    actual var displayHandle: WGPUNativeDisplayHandle
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("backends"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("flags"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dx12ShaderCompiler"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("gles3MinorVersion"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("glFenceBehaviour"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("dxcPath"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dxcMaxShaderModel"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("dx12PresentationSystem"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("budgetForDeviceCreation"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("budgetForDeviceLoss"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_INT.withByteAlignment(4).withName("type"), java.lang.foreign.MemoryLayout.paddingLayout(4), java.lang.foreign.MemoryLayout.unionLayout(java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("display"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("screen"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUXlibDisplayHandle").withName("xlib"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("connection"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("screen"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUXcbDisplayHandle").withName("xcb"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("display")).withByteAlignment(8).withName("WGPUWaylandDisplayHandle").withName("wayland")).withByteAlignment(8).withName("data")).withByteAlignment(8).withName("WGPUNativeDisplayHandle").withName("displayHandle")
        ).withByteAlignment(8).withName("WGPUInstanceExtras")
        
        val backends_VH: VarHandle = layout.varHandle(groupElement("backends"))
        val flags_VH: VarHandle = layout.varHandle(groupElement("flags"))
        val dx12ShaderCompiler_VH: VarHandle = layout.varHandle(groupElement("dx12ShaderCompiler"))
        val gles3MinorVersion_VH: VarHandle = layout.varHandle(groupElement("gles3MinorVersion"))
        val glFenceBehaviour_VH: VarHandle = layout.varHandle(groupElement("glFenceBehaviour"))
        val dxcMaxShaderModel_VH: VarHandle = layout.varHandle(groupElement("dxcMaxShaderModel"))
        val dx12PresentationSystem_VH: VarHandle = layout.varHandle(groupElement("dx12PresentationSystem"))
        val budgetForDeviceCreation_VH: VarHandle = layout.varHandle(groupElement("budgetForDeviceCreation"))
        val budgetForDeviceLoss_VH: VarHandle = layout.varHandle(groupElement("budgetForDeviceLoss"))
        
        actual operator fun invoke(address: NativeAddress): WGPUInstanceExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceExtras) -> Unit): ArrayHolder<WGPUInstanceExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUInstanceExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var backends: ULong
            get() = (backends_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = backends_VH.set(handler.handler, 0L, value.toLong())
        override var flags: ULong
            get() = (flags_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = flags_VH.set(handler.handler, 0L, value.toLong())
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = (dx12ShaderCompiler_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUDx12Compiler
            set(value) = dx12ShaderCompiler_VH.set(handler.handler, 0L, value.toInt())
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = (gles3MinorVersion_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUGles3MinorVersion
            set(value) = gles3MinorVersion_VH.set(handler.handler, 0L, value.toInt())
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = (glFenceBehaviour_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUGLFenceBehaviour
            set(value) = glFenceBehaviour_VH.set(handler.handler, 0L, value.toInt())
        override var dxcPath: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("dxcPath")), Companion.layout.select(groupElement("dxcPath")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("dxcPath")), Companion.layout.select(groupElement("dxcPath")).byteSize())
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = (dxcMaxShaderModel_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUDxcMaxShaderModel
            set(value) = dxcMaxShaderModel_VH.set(handler.handler, 0L, value.toInt())
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = (dx12PresentationSystem_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUDx12SwapchainKind
            set(value) = dx12PresentationSystem_VH.set(handler.handler, 0L, value.toInt())
        override var budgetForDeviceCreation: NativeAddress?
            get() = (budgetForDeviceCreation_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = budgetForDeviceCreation_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var budgetForDeviceLoss: NativeAddress?
            get() = (budgetForDeviceLoss_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = budgetForDeviceLoss_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var displayHandle: WGPUNativeDisplayHandle
            get() = WGPUNativeDisplayHandle(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("displayHandle")), Companion.layout.select(groupElement("displayHandle")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("displayHandle")), Companion.layout.select(groupElement("displayHandle")).byteSize())
            }
    }
}

actual interface WGPUDeviceExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var tracePath: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("tracePath")
        ).withByteAlignment(8).withName("WGPUDeviceExtras")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUDeviceExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceExtras) -> Unit): ArrayHolder<WGPUDeviceExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUDeviceExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var tracePath: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("tracePath")), Companion.layout.select(groupElement("tracePath")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("tracePath")), Companion.layout.select(groupElement("tracePath")).byteSize())
            }
    }
}

actual interface WGPUNativeLimits : CStructure {
    actual var chain: WGPUChainedStruct
    actual var maxNonSamplerBindings: UInt
    actual var maxBindingArrayElementsPerShaderStage: UInt
    actual var maxBindingArraySamplerElementsPerShaderStage: UInt
    actual var maxMultiviewViewCount: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxNonSamplerBindings"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxBindingArrayElementsPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxBindingArraySamplerElementsPerShaderStage"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("maxMultiviewViewCount")
        ).withByteAlignment(8).withName("WGPUNativeLimits")
        
        val maxNonSamplerBindings_VH: VarHandle = layout.varHandle(groupElement("maxNonSamplerBindings"))
        val maxBindingArrayElementsPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxBindingArrayElementsPerShaderStage"))
        val maxBindingArraySamplerElementsPerShaderStage_VH: VarHandle = layout.varHandle(groupElement("maxBindingArraySamplerElementsPerShaderStage"))
        val maxMultiviewViewCount_VH: VarHandle = layout.varHandle(groupElement("maxMultiviewViewCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPUNativeLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeLimits = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeLimits) -> Unit): ArrayHolder<WGPUNativeLimits> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUNativeLimits {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var maxNonSamplerBindings: UInt
            get() = (maxNonSamplerBindings_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxNonSamplerBindings_VH.set(handler.handler, 0L, value.toInt())
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = (maxBindingArrayElementsPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxBindingArrayElementsPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = (maxBindingArraySamplerElementsPerShaderStage_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxBindingArraySamplerElementsPerShaderStage_VH.set(handler.handler, 0L, value.toInt())
        override var maxMultiviewViewCount: UInt
            get() = (maxMultiviewViewCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = maxMultiviewViewCount_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUShaderDefine : CStructure {
    actual var name: WGPUStringView
    actual var value: WGPUStringView
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("name"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("value")
        ).withByteAlignment(8).withName("WGPUShaderDefine")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderDefine = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderDefine = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderDefine) -> Unit): ArrayHolder<WGPUShaderDefine> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderDefine {
        override var name: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("name")), Companion.layout.select(groupElement("name")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("name")), Companion.layout.select(groupElement("name")).byteSize())
            }
        override var value: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("value")), Companion.layout.select(groupElement("value")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("value")), Companion.layout.select(groupElement("value")).byteSize())
            }
    }
}

actual interface WGPUShaderSourceGLSL : CStructure {
    actual var chain: WGPUChainedStruct
    actual var stage: ULong
    actual var code: WGPUStringView
    actual var defineCount: UInt
    actual var defines: WGPUShaderDefine?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("stage"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("code"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("defineCount"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("defines")
        ).withByteAlignment(8).withName("WGPUShaderSourceGLSL")
        
        val stage_VH: VarHandle = layout.varHandle(groupElement("stage"))
        val defineCount_VH: VarHandle = layout.varHandle(groupElement("defineCount"))
        val defines_VH: VarHandle = layout.varHandle(groupElement("defines"))
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceGLSL = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceGLSL = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceGLSL) -> Unit): ArrayHolder<WGPUShaderSourceGLSL> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderSourceGLSL {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var stage: ULong
            get() = (stage_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = stage_VH.set(handler.handler, 0L, value.toLong())
        override var code: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("code")), Companion.layout.select(groupElement("code")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("code")), Companion.layout.select(groupElement("code")).byteSize())
            }
        override var defineCount: UInt
            get() = (defineCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = defineCount_VH.set(handler.handler, 0L, value.toInt())
        override var defines: WGPUShaderDefine?
            get() = (defines_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderDefine(it) }
            set(value) = defines_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUShaderModuleDescriptorSpirV : CStructure {
    actual var label: WGPUStringView
    actual var sourceSize: UInt
    actual var source: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("data"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("length")).withByteAlignment(8).withName("WGPUStringView").withName("label"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("sourceSize"),
            java.lang.foreign.MemoryLayout.paddingLayout(4),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("source")
        ).withByteAlignment(8).withName("WGPUShaderModuleDescriptorSpirV")
        
        val sourceSize_VH: VarHandle = layout.varHandle(groupElement("sourceSize"))
        val source_VH: VarHandle = layout.varHandle(groupElement("source"))
        
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptorSpirV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptorSpirV = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptorSpirV) -> Unit): ArrayHolder<WGPUShaderModuleDescriptorSpirV> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUShaderModuleDescriptorSpirV {
        override var label: WGPUStringView
            get() = WGPUStringView(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("label")), Companion.layout.select(groupElement("label")).byteSize())
            }
        override var sourceSize: UInt
            get() = (sourceSize_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = sourceSize_VH.set(handler.handler, 0L, value.toInt())
        override var source: NativeAddress?
            get() = (source_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = source_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPURegistryReport : CStructure {
    actual var numAllocated: ULong
    actual var numKeptFromUser: ULong
    actual var numReleasedFromUser: ULong
    actual var elementSize: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")
        ).withByteAlignment(8).withName("WGPURegistryReport")
        
        val numAllocated_VH: VarHandle = layout.varHandle(groupElement("numAllocated"))
        val numKeptFromUser_VH: VarHandle = layout.varHandle(groupElement("numKeptFromUser"))
        val numReleasedFromUser_VH: VarHandle = layout.varHandle(groupElement("numReleasedFromUser"))
        val elementSize_VH: VarHandle = layout.varHandle(groupElement("elementSize"))
        
        actual operator fun invoke(address: NativeAddress): WGPURegistryReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURegistryReport = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURegistryReport) -> Unit): ArrayHolder<WGPURegistryReport> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPURegistryReport {
        override var numAllocated: ULong
            get() = (numAllocated_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = numAllocated_VH.set(handler.handler, 0L, value.toLong())
        override var numKeptFromUser: ULong
            get() = (numKeptFromUser_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = numKeptFromUser_VH.set(handler.handler, 0L, value.toLong())
        override var numReleasedFromUser: ULong
            get() = (numReleasedFromUser_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = numReleasedFromUser_VH.set(handler.handler, 0L, value.toLong())
        override var elementSize: ULong
            get() = (elementSize_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = elementSize_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUHubReport : CStructure {
    actual var adapters: WGPURegistryReport
    actual var devices: WGPURegistryReport
    actual var queues: WGPURegistryReport
    actual var pipelineLayouts: WGPURegistryReport
    actual var shaderModules: WGPURegistryReport
    actual var bindGroupLayouts: WGPURegistryReport
    actual var bindGroups: WGPURegistryReport
    actual var commandBuffers: WGPURegistryReport
    actual var renderBundles: WGPURegistryReport
    actual var renderPipelines: WGPURegistryReport
    actual var computePipelines: WGPURegistryReport
    actual var pipelineCaches: WGPURegistryReport
    actual var querySets: WGPURegistryReport
    actual var buffers: WGPURegistryReport
    actual var textures: WGPURegistryReport
    actual var textureViews: WGPURegistryReport
    actual var samplers: WGPURegistryReport
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("adapters"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("devices"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("queues"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("pipelineLayouts"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("shaderModules"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("bindGroupLayouts"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("bindGroups"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("commandBuffers"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("renderBundles"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("renderPipelines"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("computePipelines"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("pipelineCaches"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("querySets"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("buffers"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("textures"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("textureViews"),
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("samplers")
        ).withByteAlignment(8).withName("WGPUHubReport")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUHubReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUHubReport = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUHubReport) -> Unit): ArrayHolder<WGPUHubReport> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUHubReport {
        override var adapters: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("adapters")), Companion.layout.select(groupElement("adapters")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("adapters")), Companion.layout.select(groupElement("adapters")).byteSize())
            }
        override var devices: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("devices")), Companion.layout.select(groupElement("devices")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("devices")), Companion.layout.select(groupElement("devices")).byteSize())
            }
        override var queues: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("queues")), Companion.layout.select(groupElement("queues")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("queues")), Companion.layout.select(groupElement("queues")).byteSize())
            }
        override var pipelineLayouts: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("pipelineLayouts")), Companion.layout.select(groupElement("pipelineLayouts")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("pipelineLayouts")), Companion.layout.select(groupElement("pipelineLayouts")).byteSize())
            }
        override var shaderModules: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("shaderModules")), Companion.layout.select(groupElement("shaderModules")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("shaderModules")), Companion.layout.select(groupElement("shaderModules")).byteSize())
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("bindGroupLayouts")), Companion.layout.select(groupElement("bindGroupLayouts")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("bindGroupLayouts")), Companion.layout.select(groupElement("bindGroupLayouts")).byteSize())
            }
        override var bindGroups: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("bindGroups")), Companion.layout.select(groupElement("bindGroups")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("bindGroups")), Companion.layout.select(groupElement("bindGroups")).byteSize())
            }
        override var commandBuffers: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("commandBuffers")), Companion.layout.select(groupElement("commandBuffers")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("commandBuffers")), Companion.layout.select(groupElement("commandBuffers")).byteSize())
            }
        override var renderBundles: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("renderBundles")), Companion.layout.select(groupElement("renderBundles")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("renderBundles")), Companion.layout.select(groupElement("renderBundles")).byteSize())
            }
        override var renderPipelines: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("renderPipelines")), Companion.layout.select(groupElement("renderPipelines")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("renderPipelines")), Companion.layout.select(groupElement("renderPipelines")).byteSize())
            }
        override var computePipelines: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("computePipelines")), Companion.layout.select(groupElement("computePipelines")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("computePipelines")), Companion.layout.select(groupElement("computePipelines")).byteSize())
            }
        override var pipelineCaches: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("pipelineCaches")), Companion.layout.select(groupElement("pipelineCaches")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("pipelineCaches")), Companion.layout.select(groupElement("pipelineCaches")).byteSize())
            }
        override var querySets: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("querySets")), Companion.layout.select(groupElement("querySets")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("querySets")), Companion.layout.select(groupElement("querySets")).byteSize())
            }
        override var buffers: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("buffers")), Companion.layout.select(groupElement("buffers")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("buffers")), Companion.layout.select(groupElement("buffers")).byteSize())
            }
        override var textures: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("textures")), Companion.layout.select(groupElement("textures")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("textures")), Companion.layout.select(groupElement("textures")).byteSize())
            }
        override var textureViews: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("textureViews")), Companion.layout.select(groupElement("textureViews")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("textureViews")), Companion.layout.select(groupElement("textureViews")).byteSize())
            }
        override var samplers: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("samplers")), Companion.layout.select(groupElement("samplers")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("samplers")), Companion.layout.select(groupElement("samplers")).byteSize())
            }
    }
}

actual interface WGPUGlobalReport : CStructure {
    actual var surfaces: WGPURegistryReport
    actual var hub: WGPUHubReport
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("surfaces"),
            java.lang.foreign.MemoryLayout.structLayout(java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("adapters"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("devices"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("queues"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("pipelineLayouts"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("shaderModules"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("bindGroupLayouts"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("bindGroups"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("commandBuffers"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("renderBundles"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("renderPipelines"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("computePipelines"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("pipelineCaches"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("querySets"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("buffers"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("textures"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("textureViews"), java.lang.foreign.MemoryLayout.structLayout(ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numAllocated"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numKeptFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("numReleasedFromUser"), ValueLayout.JAVA_LONG.withByteAlignment(8).withName("elementSize")).withByteAlignment(8).withName("WGPURegistryReport").withName("samplers")).withByteAlignment(8).withName("WGPUHubReport").withName("hub")
        ).withByteAlignment(8).withName("WGPUGlobalReport")
        
        
        actual operator fun invoke(address: NativeAddress): WGPUGlobalReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUGlobalReport = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUGlobalReport) -> Unit): ArrayHolder<WGPUGlobalReport> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUGlobalReport {
        override var surfaces: WGPURegistryReport
            get() = WGPURegistryReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("surfaces")), Companion.layout.select(groupElement("surfaces")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("surfaces")), Companion.layout.select(groupElement("surfaces")).byteSize())
            }
        override var hub: WGPUHubReport
            get() = WGPUHubReport(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("hub")), Companion.layout.select(groupElement("hub")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("hub")), Companion.layout.select(groupElement("hub")).byteSize())
            }
    }
}

actual interface WGPUInstanceEnumerateAdapterOptions : CStructure {
    actual var nextInChain: WGPUChainedStruct?
    actual var backends: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withByteAlignment(8).withName("nextInChain"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("backends")
        ).withByteAlignment(8).withName("WGPUInstanceEnumerateAdapterOptions")
        
        val nextInChain_VH: VarHandle = layout.varHandle(groupElement("nextInChain"))
        val backends_VH: VarHandle = layout.varHandle(groupElement("backends"))
        
        actual operator fun invoke(address: NativeAddress): WGPUInstanceEnumerateAdapterOptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceEnumerateAdapterOptions = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceEnumerateAdapterOptions) -> Unit): ArrayHolder<WGPUInstanceEnumerateAdapterOptions> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUInstanceEnumerateAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = (nextInChain_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) = nextInChain_VH.set(handler.handler, 0L, value?.handler?.handler ?: MemorySegment.NULL)
        override var backends: ULong
            get() = (backends_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = backends_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUBindGroupEntryExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var buffers: NativeAddress?
    actual var bufferCount: ULong
    actual var samplers: NativeAddress?
    actual var samplerCount: ULong
    actual var textureViews: NativeAddress?
    actual var textureViewCount: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("buffers"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("bufferCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("samplers"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("samplerCount"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("textureViews"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("textureViewCount")
        ).withByteAlignment(8).withName("WGPUBindGroupEntryExtras")
        
        val buffers_VH: VarHandle = layout.varHandle(groupElement("buffers"))
        val bufferCount_VH: VarHandle = layout.varHandle(groupElement("bufferCount"))
        val samplers_VH: VarHandle = layout.varHandle(groupElement("samplers"))
        val samplerCount_VH: VarHandle = layout.varHandle(groupElement("samplerCount"))
        val textureViews_VH: VarHandle = layout.varHandle(groupElement("textureViews"))
        val textureViewCount_VH: VarHandle = layout.varHandle(groupElement("textureViewCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntryExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntryExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupEntryExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupEntryExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var buffers: NativeAddress?
            get() = (buffers_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = buffers_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var bufferCount: ULong
            get() = (bufferCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = bufferCount_VH.set(handler.handler, 0L, value.toLong())
        override var samplers: NativeAddress?
            get() = (samplers_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = samplers_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var samplerCount: ULong
            get() = (samplerCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = samplerCount_VH.set(handler.handler, 0L, value.toLong())
        override var textureViews: NativeAddress?
            get() = (textureViews_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = textureViews_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var textureViewCount: ULong
            get() = (textureViewCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = textureViewCount_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUBindGroupLayoutEntryExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var count: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("count"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUBindGroupLayoutEntryExtras")
        
        val count_VH: VarHandle = layout.varHandle(groupElement("count"))
        
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntryExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntryExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntryExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutEntryExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var count: UInt
            get() = (count_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = count_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUQuerySetDescriptorExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var pipelineStatistics: NativeAddress?
    actual var pipelineStatisticCount: ULong
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("pipelineStatistics"),
            ValueLayout.JAVA_LONG.withByteAlignment(8).withName("pipelineStatisticCount")
        ).withByteAlignment(8).withName("WGPUQuerySetDescriptorExtras")
        
        val pipelineStatistics_VH: VarHandle = layout.varHandle(groupElement("pipelineStatistics"))
        val pipelineStatisticCount_VH: VarHandle = layout.varHandle(groupElement("pipelineStatisticCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptorExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptorExtras) -> Unit): ArrayHolder<WGPUQuerySetDescriptorExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUQuerySetDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var pipelineStatistics: NativeAddress?
            get() = (pipelineStatistics_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = pipelineStatistics_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
        override var pipelineStatisticCount: ULong
            get() = (pipelineStatisticCount_VH.get(handler.handler, 0L) as Long).toULong() as ULong
            set(value) = pipelineStatisticCount_VH.set(handler.handler, 0L, value.toLong())
    }
}

actual interface WGPUSurfaceConfigurationExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var desiredMaximumFrameLatency: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("desiredMaximumFrameLatency"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUSurfaceConfigurationExtras")
        
        val desiredMaximumFrameLatency_VH: VarHandle = layout.varHandle(groupElement("desiredMaximumFrameLatency"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfigurationExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfigurationExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfigurationExtras) -> Unit): ArrayHolder<WGPUSurfaceConfigurationExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceConfigurationExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var desiredMaximumFrameLatency: UInt
            get() = (desiredMaximumFrameLatency_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = desiredMaximumFrameLatency_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSurfaceSourceSwapChainPanel : CStructure {
    actual var chain: WGPUChainedStruct
    actual var panelNative: NativeAddress?
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.ADDRESS.withByteAlignment(8).withName("panelNative")
        ).withByteAlignment(8).withName("WGPUSurfaceSourceSwapChainPanel")
        
        val panelNative_VH: VarHandle = layout.varHandle(groupElement("panelNative"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceSwapChainPanel = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceSwapChainPanel = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceSwapChainPanel) -> Unit): ArrayHolder<WGPUSurfaceSourceSwapChainPanel> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceSwapChainPanel {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var panelNative: NativeAddress?
            get() = (panelNative_VH.get(handler.handler, 0L) as? MemorySegment)?.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
            set(value) = panelNative_VH.set(handler.handler, 0L, value?.handler ?: MemorySegment.NULL)
    }
}

actual interface WGPUPrimitiveStateExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var polygonMode: WGPUPolygonMode
    actual var conservative: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("polygonMode"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("conservative")
        ).withByteAlignment(8).withName("WGPUPrimitiveStateExtras")
        
        val polygonMode_VH: VarHandle = layout.varHandle(groupElement("polygonMode"))
        val conservative_VH: VarHandle = layout.varHandle(groupElement("conservative"))
        
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveStateExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveStateExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveStateExtras) -> Unit): ArrayHolder<WGPUPrimitiveStateExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUPrimitiveStateExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var polygonMode: WGPUPolygonMode
            get() = (polygonMode_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUPolygonMode
            set(value) = polygonMode_VH.set(handler.handler, 0L, value.toInt())
        override var conservative: UInt
            get() = (conservative_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = conservative_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUImageSubresourceRange : CStructure {
    actual var aspect: WGPUTextureAspect
    actual var baseMipLevel: UInt
    actual var mipLevelCount: UInt
    actual var baseArrayLayer: UInt
    actual var arrayLayerCount: UInt
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("aspect"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("baseMipLevel"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("mipLevelCount"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("baseArrayLayer"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("arrayLayerCount")
        ).withByteAlignment(4).withName("WGPUImageSubresourceRange")
        
        val aspect_VH: VarHandle = layout.varHandle(groupElement("aspect"))
        val baseMipLevel_VH: VarHandle = layout.varHandle(groupElement("baseMipLevel"))
        val mipLevelCount_VH: VarHandle = layout.varHandle(groupElement("mipLevelCount"))
        val baseArrayLayer_VH: VarHandle = layout.varHandle(groupElement("baseArrayLayer"))
        val arrayLayerCount_VH: VarHandle = layout.varHandle(groupElement("arrayLayerCount"))
        
        actual operator fun invoke(address: NativeAddress): WGPUImageSubresourceRange = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUImageSubresourceRange = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUImageSubresourceRange) -> Unit): ArrayHolder<WGPUImageSubresourceRange> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUImageSubresourceRange {
        override var aspect: WGPUTextureAspect
            get() = (aspect_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUTextureAspect
            set(value) = aspect_VH.set(handler.handler, 0L, value.toInt())
        override var baseMipLevel: UInt
            get() = (baseMipLevel_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = baseMipLevel_VH.set(handler.handler, 0L, value.toInt())
        override var mipLevelCount: UInt
            get() = (mipLevelCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = mipLevelCount_VH.set(handler.handler, 0L, value.toInt())
        override var baseArrayLayer: UInt
            get() = (baseArrayLayer_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = baseArrayLayer_VH.set(handler.handler, 0L, value.toInt())
        override var arrayLayerCount: UInt
            get() = (arrayLayerCount_VH.get(handler.handler, 0L) as Int).toUInt() as UInt
            set(value) = arrayLayerCount_VH.set(handler.handler, 0L, value.toInt())
    }
}

actual interface WGPUSamplerDescriptorExtras : CStructure {
    actual var chain: WGPUChainedStruct
    actual var samplerBorderColor: WGPUSamplerBorderColor
    actual override val handler: NativeAddress
    actual companion object {
        val layout: java.lang.foreign.GroupLayout = java.lang.foreign.MemoryLayout.structLayout(
            java.lang.foreign.MemoryLayout.structLayout(ValueLayout.ADDRESS.withByteAlignment(8).withName("next"), ValueLayout.JAVA_INT.withByteAlignment(4).withName("sType"), java.lang.foreign.MemoryLayout.paddingLayout(4)).withByteAlignment(8).withName("WGPUChainedStruct").withName("chain"),
            ValueLayout.JAVA_INT.withByteAlignment(4).withName("samplerBorderColor"),
            java.lang.foreign.MemoryLayout.paddingLayout(4)
        ).withByteAlignment(8).withName("WGPUSamplerDescriptorExtras")
        
        val samplerBorderColor_VH: VarHandle = layout.varHandle(groupElement("samplerBorderColor"))
        
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptorExtras = ByReference(allocator.allocate(layout.byteSize()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptorExtras) -> Unit): ArrayHolder<WGPUSamplerDescriptorExtras> {
            val byteSize = layout.byteSize()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val slice = segment.handler.asSlice(i.toLong() * byteSize, byteSize).let(::NativeAddress)
                provider(i.toUInt(), ByReference(slice))
            }
            return ArrayHolder(segment)
        }
    }
    
    @JvmInline
    value class ByReference(override val handler: NativeAddress) : WGPUSamplerDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct(NativeAddress(handler.handler.asSlice(Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())))
            set(value) {
                MemorySegment.copy(value.handler.handler, 0L, handler.handler, Companion.layout.byteOffset(groupElement("chain")), Companion.layout.select(groupElement("chain")).byteSize())
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = (samplerBorderColor_VH.get(handler.handler, 0L) as Int).toUInt() as WGPUSamplerBorderColor
            set(value) = samplerBorderColor_VH.set(handler.handler, 0L, value.toInt())
    }
}

private val wgpuGenerateReport_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuGenerateReport_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuGenerateReport") }
private val wgpuGenerateReport_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuGenerateReport_ADDR, wgpuGenerateReport_DESC) }
actual fun wgpuGenerateReport(instance: WGPUInstance?, report: WGPUGlobalReport?): Unit {
    wgpuGenerateReport_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, report?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuInstanceEnumerateAdapters_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuInstanceEnumerateAdapters_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuInstanceEnumerateAdapters") }
private val wgpuInstanceEnumerateAdapters_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuInstanceEnumerateAdapters_ADDR, wgpuInstanceEnumerateAdapters_DESC) }
actual fun wgpuInstanceEnumerateAdapters(instance: WGPUInstance?, options: WGPUInstanceEnumerateAdapterOptions?, adapters: NativeAddress?): ULong {
    return (wgpuInstanceEnumerateAdapters_HANDLE.invokeExact(instance?.handler?.handler ?: MemorySegment.NULL, options?.handler?.handler ?: MemorySegment.NULL, adapters?.handler ?: MemorySegment.NULL) as Long).toULong()
}

private val wgpuQueueSubmitForIndex_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
private val wgpuQueueSubmitForIndex_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueSubmitForIndex") }
private val wgpuQueueSubmitForIndex_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueSubmitForIndex_ADDR, wgpuQueueSubmitForIndex_DESC) }
actual fun wgpuQueueSubmitForIndex(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): ULong {
    return (wgpuQueueSubmitForIndex_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL, commandCount.toLong(), commands?.handler ?: MemorySegment.NULL) as Long).toULong()
}

private val wgpuQueueGetTimestampPeriod_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_FLOAT, ValueLayout.ADDRESS)
private val wgpuQueueGetTimestampPeriod_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueGetTimestampPeriod") }
private val wgpuQueueGetTimestampPeriod_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueGetTimestampPeriod_ADDR, wgpuQueueGetTimestampPeriod_DESC) }
actual fun wgpuQueueGetTimestampPeriod(queue: WGPUQueue?): Float {
    return wgpuQueueGetTimestampPeriod_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL) as Float
}

private val wgpuDevicePoll_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuDevicePoll_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDevicePoll") }
private val wgpuDevicePoll_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDevicePoll_ADDR, wgpuDevicePoll_DESC) }
actual fun wgpuDevicePoll(device: WGPUDevice?, wait: UInt, submissionIndex: NativeAddress?): UInt {
    return (wgpuDevicePoll_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, wait.toInt(), submissionIndex?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuDeviceCreateShaderModuleSpirV_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceCreateShaderModuleSpirV_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModuleSpirV") }
private val wgpuDeviceCreateShaderModuleSpirV_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateShaderModuleSpirV_ADDR, wgpuDeviceCreateShaderModuleSpirV_DESC) }
actual fun wgpuDeviceCreateShaderModuleSpirV(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptorSpirV?): WGPUShaderModule? {
    return (wgpuDeviceCreateShaderModuleSpirV_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

private val wgpuSetLogCallback_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuSetLogCallback_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSetLogCallback") }
private val wgpuSetLogCallback_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSetLogCallback_ADDR, wgpuSetLogCallback_DESC) }
actual fun wgpuSetLogCallback(callback: NativeAddress?, userdata: NativeAddress?): Unit {
    wgpuSetLogCallback_HANDLE.invokeExact(callback?.handler ?: MemorySegment.NULL, userdata?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuSetLogLevel_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT)
private val wgpuSetLogLevel_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuSetLogLevel") }
private val wgpuSetLogLevel_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuSetLogLevel_ADDR, wgpuSetLogLevel_DESC) }
actual fun wgpuSetLogLevel(level: WGPULogLevel): Unit {
    wgpuSetLogLevel_HANDLE.invokeExact(level.toInt())
    return
}

private val wgpuGetVersion_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT)
private val wgpuGetVersion_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuGetVersion") }
private val wgpuGetVersion_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuGetVersion_ADDR, wgpuGetVersion_DESC) }
actual fun wgpuGetVersion(): UInt {
    return (wgpuGetVersion_HANDLE.invokeExact() as Int).toUInt()
}

private val wgpuDeviceGetNativeMetalDevice_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuDeviceGetNativeMetalDevice_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetNativeMetalDevice") }
private val wgpuDeviceGetNativeMetalDevice_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceGetNativeMetalDevice_ADDR, wgpuDeviceGetNativeMetalDevice_DESC) }
actual fun wgpuDeviceGetNativeMetalDevice(device: WGPUDevice?): NativeAddress? {
    return (wgpuDeviceGetNativeMetalDevice_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuQueueGetNativeMetalCommandQueue_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuQueueGetNativeMetalCommandQueue_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuQueueGetNativeMetalCommandQueue") }
private val wgpuQueueGetNativeMetalCommandQueue_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuQueueGetNativeMetalCommandQueue_ADDR, wgpuQueueGetNativeMetalCommandQueue_DESC) }
actual fun wgpuQueueGetNativeMetalCommandQueue(queue: WGPUQueue?): NativeAddress? {
    return (wgpuQueueGetNativeMetalCommandQueue_HANDLE.invokeExact(queue?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuTextureGetNativeMetalTexture_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuTextureGetNativeMetalTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetNativeMetalTexture") }
private val wgpuTextureGetNativeMetalTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuTextureGetNativeMetalTexture_ADDR, wgpuTextureGetNativeMetalTexture_DESC) }
actual fun wgpuTextureGetNativeMetalTexture(texture: WGPUTexture?): NativeAddress? {
    return (wgpuTextureGetNativeMetalTexture_HANDLE.invokeExact(texture?.handler?.handler ?: MemorySegment.NULL) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)
}

private val wgpuRenderPassEncoderMultiDrawIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderMultiDrawIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndirect") }
private val wgpuRenderPassEncoderMultiDrawIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderMultiDrawIndirect_ADDR, wgpuRenderPassEncoderMultiDrawIndirect_DESC) }
actual fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    wgpuRenderPassEncoderMultiDrawIndirect_HANDLE.invokeExact(encoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndexedIndirect_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderMultiDrawIndexedIndirect_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndexedIndirect") }
private val wgpuRenderPassEncoderMultiDrawIndexedIndirect_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderMultiDrawIndexedIndirect_ADDR, wgpuRenderPassEncoderMultiDrawIndexedIndirect_DESC) }
actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    wgpuRenderPassEncoderMultiDrawIndexedIndirect_HANDLE.invokeExact(encoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndirectCount_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderMultiDrawIndirectCount_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndirectCount") }
private val wgpuRenderPassEncoderMultiDrawIndirectCount_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderMultiDrawIndirectCount_ADDR, wgpuRenderPassEncoderMultiDrawIndirectCount_DESC) }
actual fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    wgpuRenderPassEncoderMultiDrawIndirectCount_HANDLE.invokeExact(encoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), count_buffer?.handler?.handler ?: MemorySegment.NULL, count_buffer_offset.toLong(), max_count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndexedIndirectCount") }
private val wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_ADDR, wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_DESC) }
actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_HANDLE.invokeExact(encoder?.handler?.handler ?: MemorySegment.NULL, buffer?.handler?.handler ?: MemorySegment.NULL, offset.toLong(), count_buffer?.handler?.handler ?: MemorySegment.NULL, count_buffer_offset.toLong(), max_count.toInt())
    return
}

private val wgpuComputePassEncoderBeginPipelineStatisticsQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuComputePassEncoderBeginPipelineStatisticsQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderBeginPipelineStatisticsQuery") }
private val wgpuComputePassEncoderBeginPipelineStatisticsQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderBeginPipelineStatisticsQuery_ADDR, wgpuComputePassEncoderBeginPipelineStatisticsQuery_DESC) }
actual fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    wgpuComputePassEncoderBeginPipelineStatisticsQuery_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuComputePassEncoderEndPipelineStatisticsQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuComputePassEncoderEndPipelineStatisticsQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderEndPipelineStatisticsQuery") }
private val wgpuComputePassEncoderEndPipelineStatisticsQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderEndPipelineStatisticsQuery_ADDR, wgpuComputePassEncoderEndPipelineStatisticsQuery_DESC) }
actual fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?): Unit {
    wgpuComputePassEncoderEndPipelineStatisticsQuery_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuRenderPassEncoderBeginPipelineStatisticsQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderBeginPipelineStatisticsQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderBeginPipelineStatisticsQuery") }
private val wgpuRenderPassEncoderBeginPipelineStatisticsQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderBeginPipelineStatisticsQuery_ADDR, wgpuRenderPassEncoderBeginPipelineStatisticsQuery_DESC) }
actual fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    wgpuRenderPassEncoderBeginPipelineStatisticsQuery_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderEndPipelineStatisticsQuery_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuRenderPassEncoderEndPipelineStatisticsQuery_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEndPipelineStatisticsQuery") }
private val wgpuRenderPassEncoderEndPipelineStatisticsQuery_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderEndPipelineStatisticsQuery_ADDR, wgpuRenderPassEncoderEndPipelineStatisticsQuery_DESC) }
actual fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    wgpuRenderPassEncoderEndPipelineStatisticsQuery_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuComputePassEncoderWriteTimestamp_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuComputePassEncoderWriteTimestamp_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderWriteTimestamp") }
private val wgpuComputePassEncoderWriteTimestamp_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuComputePassEncoderWriteTimestamp_ADDR, wgpuComputePassEncoderWriteTimestamp_DESC) }
actual fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    wgpuComputePassEncoderWriteTimestamp_HANDLE.invokeExact(computePassEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderWriteTimestamp_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
private val wgpuRenderPassEncoderWriteTimestamp_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderWriteTimestamp") }
private val wgpuRenderPassEncoderWriteTimestamp_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuRenderPassEncoderWriteTimestamp_ADDR, wgpuRenderPassEncoderWriteTimestamp_DESC) }
actual fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    wgpuRenderPassEncoderWriteTimestamp_HANDLE.invokeExact(renderPassEncoder?.handler?.handler ?: MemorySegment.NULL, querySet?.handler?.handler ?: MemorySegment.NULL, queryIndex.toInt())
    return
}

private val wgpuDeviceStartGraphicsDebuggerCapture_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
private val wgpuDeviceStartGraphicsDebuggerCapture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceStartGraphicsDebuggerCapture") }
private val wgpuDeviceStartGraphicsDebuggerCapture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceStartGraphicsDebuggerCapture_ADDR, wgpuDeviceStartGraphicsDebuggerCapture_DESC) }
actual fun wgpuDeviceStartGraphicsDebuggerCapture(device: WGPUDevice?): UInt {
    return (wgpuDeviceStartGraphicsDebuggerCapture_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL) as Int).toUInt()
}

private val wgpuDeviceStopGraphicsDebuggerCapture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
private val wgpuDeviceStopGraphicsDebuggerCapture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceStopGraphicsDebuggerCapture") }
private val wgpuDeviceStopGraphicsDebuggerCapture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceStopGraphicsDebuggerCapture_ADDR, wgpuDeviceStopGraphicsDebuggerCapture_DESC) }
actual fun wgpuDeviceStopGraphicsDebuggerCapture(device: WGPUDevice?): Unit {
    wgpuDeviceStopGraphicsDebuggerCapture_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuCommandEncoderClearTexture_DESC: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
private val wgpuCommandEncoderClearTexture_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderClearTexture") }
private val wgpuCommandEncoderClearTexture_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuCommandEncoderClearTexture_ADDR, wgpuCommandEncoderClearTexture_DESC) }
actual fun wgpuCommandEncoderClearTexture(commandEncoder: WGPUCommandEncoder?, texture: WGPUTexture?, range: WGPUImageSubresourceRange?): Unit {
    wgpuCommandEncoderClearTexture_HANDLE.invokeExact(commandEncoder?.handler?.handler ?: MemorySegment.NULL, texture?.handler?.handler ?: MemorySegment.NULL, range?.handler?.handler ?: MemorySegment.NULL)
    return
}

private val wgpuDeviceCreateShaderModuleTrusted_DESC: FunctionDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
private val wgpuDeviceCreateShaderModuleTrusted_ADDR: MemorySegment by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModuleTrusted") }
private val wgpuDeviceCreateShaderModuleTrusted_HANDLE: MethodHandle by lazy { Linker.nativeLinker().downcallHandle(wgpuDeviceCreateShaderModuleTrusted_ADDR, wgpuDeviceCreateShaderModuleTrusted_DESC) }
actual fun wgpuDeviceCreateShaderModuleTrusted(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?, runtimeChecks: ULong): WGPUShaderModule? {
    return (wgpuDeviceCreateShaderModuleTrusted_HANDLE.invokeExact(device?.handler?.handler ?: MemorySegment.NULL, descriptor?.handler?.handler ?: MemorySegment.NULL, runtimeChecks.toLong()) as MemorySegment).takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUProcTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid()
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUProcTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUProcType,
                userdata = null,
            ) { callback ->
                callback.invoke()
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUProc.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUProc,
): CallbackRegistration<WGPUProc> = CallbackRuntime.register(
    type = WGPUProcType,
    trampoline = WGPUProcTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUProc.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUProc,
): PreparedCallbackRegistration<WGPUProc> = CallbackRuntime.prepare(
    type = WGPUProcType,
    trampoline = WGPUProcTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@UnsafeCallbackRearmApi
@OptIn(CallbackRuntimeApi::class)
actual fun WGPUProc.Companion.rearmAfterNativeQuiescence(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUProc,
): CallbackRegistration<WGPUProc> = CallbackRuntime.rearmAfterNativeQuiescence(
    type = WGPUProcType,
    trampoline = WGPUProcTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUBufferMapCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUBufferMapCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUBufferMapCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUMapAsyncStatus,
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUBufferMapCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUBufferMapCallback,
): CallbackRegistration<WGPUBufferMapCallback> = CallbackRuntime.register(
    type = WGPUBufferMapCallbackType,
    trampoline = WGPUBufferMapCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUBufferMapCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUBufferMapCallback,
): PreparedCallbackRegistration<WGPUBufferMapCallback> = CallbackRuntime.prepare(
    type = WGPUBufferMapCallbackType,
    trampoline = WGPUBufferMapCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUCompilationInfoCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUCompilationInfoCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        compilationInfo: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCompilationInfoCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCompilationInfoRequestStatus,
                    compilationInfo.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCompilationInfoCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCompilationInfoCallback,
): CallbackRegistration<WGPUCompilationInfoCallback> = CallbackRuntime.register(
    type = WGPUCompilationInfoCallbackType,
    trampoline = WGPUCompilationInfoCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUCompilationInfoCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCompilationInfoCallback,
): PreparedCallbackRegistration<WGPUCompilationInfoCallback> = CallbackRuntime.prepare(
    type = WGPUCompilationInfoCallbackType,
    trampoline = WGPUCompilationInfoCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUCreateComputePipelineAsyncCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUCreateComputePipelineAsyncCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        pipeline: MemorySegment,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCreateComputePipelineAsyncCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUComputePipeline(it) },
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCreateComputePipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateComputePipelineAsyncCallback,
): CallbackRegistration<WGPUCreateComputePipelineAsyncCallback> = CallbackRuntime.register(
    type = WGPUCreateComputePipelineAsyncCallbackType,
    trampoline = WGPUCreateComputePipelineAsyncCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUCreateComputePipelineAsyncCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateComputePipelineAsyncCallback,
): PreparedCallbackRegistration<WGPUCreateComputePipelineAsyncCallback> = CallbackRuntime.prepare(
    type = WGPUCreateComputePipelineAsyncCallbackType,
    trampoline = WGPUCreateComputePipelineAsyncCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUCreateRenderPipelineAsyncCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUCreateRenderPipelineAsyncCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        pipeline: MemorySegment,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCreateRenderPipelineAsyncCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPURenderPipeline(it) },
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCreateRenderPipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateRenderPipelineAsyncCallback,
): CallbackRegistration<WGPUCreateRenderPipelineAsyncCallback> = CallbackRuntime.register(
    type = WGPUCreateRenderPipelineAsyncCallbackType,
    trampoline = WGPUCreateRenderPipelineAsyncCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUCreateRenderPipelineAsyncCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateRenderPipelineAsyncCallback,
): PreparedCallbackRegistration<WGPUCreateRenderPipelineAsyncCallback> = CallbackRuntime.prepare(
    type = WGPUCreateRenderPipelineAsyncCallbackType,
    trampoline = WGPUCreateRenderPipelineAsyncCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUDeviceLostCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUDeviceLostCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        device: MemorySegment,
        reason: Int,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUDeviceLostCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    device.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                    reason.toUInt() as WGPUDeviceLostReason,
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUDeviceLostCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUDeviceLostCallback,
): CallbackRegistration<WGPUDeviceLostCallback> = CallbackRuntime.register(
    type = WGPUDeviceLostCallbackType,
    trampoline = WGPUDeviceLostCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUDeviceLostCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUDeviceLostCallback,
): PreparedCallbackRegistration<WGPUDeviceLostCallback> = CallbackRuntime.prepare(
    type = WGPUDeviceLostCallbackType,
    trampoline = WGPUDeviceLostCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUPopErrorScopeCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUPopErrorScopeCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        type: Int,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUPopErrorScopeCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUPopErrorScopeStatus,
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUPopErrorScopeCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUPopErrorScopeCallback,
): CallbackRegistration<WGPUPopErrorScopeCallback> = CallbackRuntime.register(
    type = WGPUPopErrorScopeCallbackType,
    trampoline = WGPUPopErrorScopeCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUPopErrorScopeCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUPopErrorScopeCallback,
): PreparedCallbackRegistration<WGPUPopErrorScopeCallback> = CallbackRuntime.prepare(
    type = WGPUPopErrorScopeCallbackType,
    trampoline = WGPUPopErrorScopeCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUQueueWorkDoneCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUQueueWorkDoneCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUQueueWorkDoneCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUQueueWorkDoneStatus,
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUQueueWorkDoneCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUQueueWorkDoneCallback,
): CallbackRegistration<WGPUQueueWorkDoneCallback> = CallbackRuntime.register(
    type = WGPUQueueWorkDoneCallbackType,
    trampoline = WGPUQueueWorkDoneCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUQueueWorkDoneCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUQueueWorkDoneCallback,
): PreparedCallbackRegistration<WGPUQueueWorkDoneCallback> = CallbackRuntime.prepare(
    type = WGPUQueueWorkDoneCallbackType,
    trampoline = WGPUQueueWorkDoneCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPURequestAdapterCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPURequestAdapterCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        adapter: MemorySegment,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPURequestAdapterCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestAdapterStatus,
                    adapter.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUAdapter(it) },
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPURequestAdapterCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestAdapterCallback,
): CallbackRegistration<WGPURequestAdapterCallback> = CallbackRuntime.register(
    type = WGPURequestAdapterCallbackType,
    trampoline = WGPURequestAdapterCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPURequestAdapterCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestAdapterCallback,
): PreparedCallbackRegistration<WGPURequestAdapterCallback> = CallbackRuntime.prepare(
    type = WGPURequestAdapterCallbackType,
    trampoline = WGPURequestAdapterCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPURequestDeviceCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPURequestDeviceCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        status: Int,
        device: MemorySegment,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPURequestDeviceCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestDeviceStatus,
                    device.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress)?.let { WGPUDevice(it) },
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPURequestDeviceCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestDeviceCallback,
): CallbackRegistration<WGPURequestDeviceCallback> = CallbackRuntime.register(
    type = WGPURequestDeviceCallbackType,
    trampoline = WGPURequestDeviceCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPURequestDeviceCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestDeviceCallback,
): PreparedCallbackRegistration<WGPURequestDeviceCallback> = CallbackRuntime.prepare(
    type = WGPURequestDeviceCallbackType,
    trampoline = WGPURequestDeviceCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPUUncapturedErrorCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUUncapturedErrorCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        device: MemorySegment,
        type: Int,
        message: MemorySegment,
        userdata1: MemorySegment,
        userdata2: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUUncapturedErrorCallbackType,
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    device.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView(NativeAddress(message)),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUUncapturedErrorCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUUncapturedErrorCallback,
): CallbackRegistration<WGPUUncapturedErrorCallback> = CallbackRuntime.register(
    type = WGPUUncapturedErrorCallbackType,
    trampoline = WGPUUncapturedErrorCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPUUncapturedErrorCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUUncapturedErrorCallback,
): PreparedCallbackRegistration<WGPUUncapturedErrorCallback> = CallbackRuntime.prepare(
    type = WGPUUncapturedErrorCallbackType,
    trampoline = WGPUUncapturedErrorCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private object WGPULogCallbackTrampoline {
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, WGPUStringView.layout, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPULogCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()))
    }
    
    @JvmStatic
    private fun invoke(
        level: Int,
        message: MemorySegment,
        userdata: MemorySegment,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPULogCallbackType,
                userdata = userdata.takeIf { it != MemorySegment.NULL }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    level.toUInt() as WGPULogLevel,
                    WGPUStringView(NativeAddress(message)),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPULogCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPULogCallback,
): CallbackRegistration<WGPULogCallback> = CallbackRuntime.register(
    type = WGPULogCallbackType,
    trampoline = WGPULogCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
internal actual fun WGPULogCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPULogCallback,
): PreparedCallbackRegistration<WGPULogCallback> = CallbackRuntime.prepare(
    type = WGPULogCallbackType,
    trampoline = WGPULogCallbackTrampoline.address,
    policy = policy,
    onError = onError,
    callback = callback,
)

@Suppress("UNUSED_VARIABLE")
internal actual fun wgpuSetLogCallbackCallbackBindingPreflight(): (NativeAddress?, NativeAddress?) -> Unit {
    val address = wgpuSetLogCallback_ADDR
    val handle = wgpuSetLogCallback_HANDLE
    return { callback, userdata ->
        handle.invokeExact(
            callback?.handler ?: MemorySegment.NULL,
            userdata?.handler ?: MemorySegment.NULL,
        )
    }
}
