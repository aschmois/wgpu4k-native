package io.ygdrasil.wgpu

import org.graphiks.kffi.NativeAddress
import org.graphiks.kffi.engine.JvmDowncallEngine
import org.graphiks.kffi.engine.JvmUpcallEngine
import org.graphiks.kffi.CallbackExceptionHandler
import org.graphiks.kffi.CallbackPolicy
import org.graphiks.kffi.CallbackRegistration
import org.graphiks.kffi.CallbackRuntime
import org.graphiks.kffi.CallbackRuntimeApi
import org.graphiks.kffi.PreparedCallbackRegistration
import org.graphiks.kffi.UnsafeCallbackRearmApi
import org.graphiks.kffi.CString
import org.graphiks.kffi.ArrayHolder
import org.graphiks.kffi.MemoryAllocator
import org.graphiks.kffi.MemoryBuffer
import org.graphiks.kffi.findOrThrow
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import kotlin.OptIn
import kotlin.Suppress
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

private object KextractNativeBootstrap {
    @kotlin.jvm.Volatile private var loaded: kotlin.Boolean = false

    fun resolve(name: kotlin.String): kotlin.Long {
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

actual interface WGPUStringView {
    actual var data: CString?
    actual var length: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStringView = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStringView = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStringView) -> Unit): ArrayHolder<WGPUStringView> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUStringView>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUStringView {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var data: CString?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let(::CString)
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var length: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUStringView {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var data: CString?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let(::CString)
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var length: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
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

actual interface WGPUChainedStruct {
    actual var next: WGPUChainedStruct?
    actual var sType: WGPUSType
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUChainedStruct = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUChainedStruct = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUChainedStruct) -> Unit): ArrayHolder<WGPUChainedStruct> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUChainedStruct>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUChainedStruct {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var next: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var sType: WGPUSType
            get() = mem.readUInt(8uL) as WGPUSType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUChainedStruct {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var next: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var sType: WGPUSType
            get() = mem.readUInt(8uL) as WGPUSType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBufferMapCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBufferMapCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferMapCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferMapCallbackInfo) -> Unit): ArrayHolder<WGPUBufferMapCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUBufferMapCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferMapCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferMapCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCompilationInfoCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfoCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfoCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfoCallbackInfo) -> Unit): ArrayHolder<WGPUCompilationInfoCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUCompilationInfoCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationInfoCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationInfoCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCreateComputePipelineAsyncCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCreateComputePipelineAsyncCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateComputePipelineAsyncCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateComputePipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCreateComputePipelineAsyncCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCreateComputePipelineAsyncCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCreateRenderPipelineAsyncCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCreateRenderPipelineAsyncCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateRenderPipelineAsyncCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateRenderPipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUDeviceLostCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDeviceLostCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceLostCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceLostCallbackInfo) -> Unit): ArrayHolder<WGPUDeviceLostCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUDeviceLostCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceLostCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceLostCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUPopErrorScopeCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPopErrorScopeCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPopErrorScopeCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPopErrorScopeCallbackInfo) -> Unit): ArrayHolder<WGPUPopErrorScopeCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUPopErrorScopeCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUPopErrorScopeCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUPopErrorScopeCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUQueueWorkDoneCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQueueWorkDoneCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueWorkDoneCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueWorkDoneCallbackInfo) -> Unit): ArrayHolder<WGPUQueueWorkDoneCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUQueueWorkDoneCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUQueueWorkDoneCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUQueueWorkDoneCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURequestAdapterCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterCallbackInfo) -> Unit): ArrayHolder<WGPURequestAdapterCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPURequestAdapterCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURequestDeviceCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var mode: WGPUCallbackMode
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestDeviceCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestDeviceCallbackInfo = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestDeviceCallbackInfo) -> Unit): ArrayHolder<WGPURequestDeviceCallbackInfo> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPURequestDeviceCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestDeviceCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestDeviceCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mode: WGPUCallbackMode
            get() = mem.readUInt(8uL) as WGPUCallbackMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUUncapturedErrorCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUUncapturedErrorCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUUncapturedErrorCallbackInfo = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUUncapturedErrorCallbackInfo) -> Unit): ArrayHolder<WGPUUncapturedErrorCallbackInfo> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUUncapturedErrorCallbackInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUUncapturedErrorCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUUncapturedErrorCallbackInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var callback: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override var userdata1: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var userdata2: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUAdapterInfo {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUAdapterInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUAdapterInfo = ByReference(allocator.allocateBuffer(96uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUAdapterInfo) -> Unit): ArrayHolder<WGPUAdapterInfo> {
            val buffer = allocator.allocateBuffer(96uL * size)
            val result = ArrayHolder<WGPUAdapterInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 96L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUAdapterInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 96uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var vendor: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var architecture: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 24L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 24uL, 16uL)
            }
        override var device: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 40L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 40uL, 16uL)
            }
        override var description: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 56L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 56uL, 16uL)
            }
        override var backendType: WGPUBackendType
            get() = mem.readUInt(72uL) as WGPUBackendType
            set(value) { mem.writeUInt(value.toUInt(), 72uL) }
        override var adapterType: WGPUAdapterType
            get() = mem.readUInt(76uL) as WGPUAdapterType
            set(value) { mem.writeUInt(value.toUInt(), 76uL) }
        override var vendorID: UInt
            get() = mem.readUInt(80uL)
            set(value) { mem.writeUInt(value, 80uL) }
        override var deviceID: UInt
            get() = mem.readUInt(84uL)
            set(value) { mem.writeUInt(value, 84uL) }
        override var subgroupMinSize: UInt
            get() = mem.readUInt(88uL)
            set(value) { mem.writeUInt(value, 88uL) }
        override var subgroupMaxSize: UInt
            get() = mem.readUInt(92uL)
            set(value) { mem.writeUInt(value, 92uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUAdapterInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 96uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var vendor: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var architecture: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 24L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 24uL, 16uL)
            }
        override var device: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 40L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 40uL, 16uL)
            }
        override var description: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 56L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 56uL, 16uL)
            }
        override var backendType: WGPUBackendType
            get() = mem.readUInt(72uL) as WGPUBackendType
            set(value) { mem.writeUInt(value.toUInt(), 72uL) }
        override var adapterType: WGPUAdapterType
            get() = mem.readUInt(76uL) as WGPUAdapterType
            set(value) { mem.writeUInt(value.toUInt(), 76uL) }
        override var vendorID: UInt
            get() = mem.readUInt(80uL)
            set(value) { mem.writeUInt(value, 80uL) }
        override var deviceID: UInt
            get() = mem.readUInt(84uL)
            set(value) { mem.writeUInt(value, 84uL) }
        override var subgroupMinSize: UInt
            get() = mem.readUInt(88uL)
            set(value) { mem.writeUInt(value, 88uL) }
        override var subgroupMaxSize: UInt
            get() = mem.readUInt(92uL)
            set(value) { mem.writeUInt(value, 92uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBlendComponent {
    actual var operation: WGPUBlendOperation
    actual var srcFactor: WGPUBlendFactor
    actual var dstFactor: WGPUBlendFactor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendComponent = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendComponent = ByReference(allocator.allocateBuffer(12uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendComponent) -> Unit): ArrayHolder<WGPUBlendComponent> {
            val buffer = allocator.allocateBuffer(12uL * size)
            val result = ArrayHolder<WGPUBlendComponent>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 12L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBlendComponent {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var operation: WGPUBlendOperation
            get() = mem.readUInt(0uL) as WGPUBlendOperation
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var srcFactor: WGPUBlendFactor
            get() = mem.readUInt(4uL) as WGPUBlendFactor
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var dstFactor: WGPUBlendFactor
            get() = mem.readUInt(8uL) as WGPUBlendFactor
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBlendComponent {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var operation: WGPUBlendOperation
            get() = mem.readUInt(0uL) as WGPUBlendOperation
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var srcFactor: WGPUBlendFactor
            get() = mem.readUInt(4uL) as WGPUBlendFactor
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var dstFactor: WGPUBlendFactor
            get() = mem.readUInt(8uL) as WGPUBlendFactor
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBufferBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUBufferBindingType
    actual var hasDynamicOffset: UInt
    actual var minBindingSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBufferBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferBindingLayout = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferBindingLayout) -> Unit): ArrayHolder<WGPUBufferBindingLayout> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUBufferBindingLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var type: WGPUBufferBindingType
            get() = mem.readUInt(8uL) as WGPUBufferBindingType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var hasDynamicOffset: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var minBindingSize: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var type: WGPUBufferBindingType
            get() = mem.readUInt(8uL) as WGPUBufferBindingType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var hasDynamicOffset: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var minBindingSize: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBufferDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var usage: ULong
    actual var size: ULong
    actual var mappedAtCreation: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBufferDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferDescriptor = ByReference(allocator.allocateBuffer(48uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferDescriptor) -> Unit): ArrayHolder<WGPUBufferDescriptor> {
            val buffer = allocator.allocateBuffer(48uL * size)
            val result = ArrayHolder<WGPUBufferDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 48L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var size: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var mappedAtCreation: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBufferDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var size: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var mappedAtCreation: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUColor {
    actual var r: Double
    actual var g: Double
    actual var b: Double
    actual var a: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColor = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColor) -> Unit): ArrayHolder<WGPUColor> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUColor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUColor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var r: Double
            get() = mem.readDouble(0uL)
            set(value) { mem.writeDouble(value, 0uL) }
        override var g: Double
            get() = mem.readDouble(8uL)
            set(value) { mem.writeDouble(value, 8uL) }
        override var b: Double
            get() = mem.readDouble(16uL)
            set(value) { mem.writeDouble(value, 16uL) }
        override var a: Double
            get() = mem.readDouble(24uL)
            set(value) { mem.writeDouble(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUColor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var r: Double
            get() = mem.readDouble(0uL)
            set(value) { mem.writeDouble(value, 0uL) }
        override var g: Double
            get() = mem.readDouble(8uL)
            set(value) { mem.writeDouble(value, 8uL) }
        override var b: Double
            get() = mem.readDouble(16uL)
            set(value) { mem.writeDouble(value, 16uL) }
        override var a: Double
            get() = mem.readDouble(24uL)
            set(value) { mem.writeDouble(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCommandBufferDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandBufferDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandBufferDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandBufferDescriptor) -> Unit): ArrayHolder<WGPUCommandBufferDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUCommandBufferDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCommandBufferDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCommandBufferDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCommandEncoderDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandEncoderDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandEncoderDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandEncoderDescriptor) -> Unit): ArrayHolder<WGPUCommandEncoderDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUCommandEncoderDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCommandEncoderDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCommandEncoderDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCompatibilityModeLimits {
    actual var chain: WGPUChainedStruct
    actual var maxStorageBuffersInVertexStage: UInt
    actual var maxStorageTexturesInVertexStage: UInt
    actual var maxStorageBuffersInFragmentStage: UInt
    actual var maxStorageTexturesInFragmentStage: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompatibilityModeLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompatibilityModeLimits = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompatibilityModeLimits) -> Unit): ArrayHolder<WGPUCompatibilityModeLimits> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUCompatibilityModeLimits>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompatibilityModeLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxStorageTexturesInVertexStage: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompatibilityModeLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxStorageTexturesInVertexStage: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCompilationMessage {
    actual var nextInChain: WGPUChainedStruct?
    actual var message: WGPUStringView
    actual var type: WGPUCompilationMessageType
    actual var lineNum: ULong
    actual var linePos: ULong
    actual var offset: ULong
    actual var length: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompilationMessage = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationMessage = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationMessage) -> Unit): ArrayHolder<WGPUCompilationMessage> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUCompilationMessage>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationMessage {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var message: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var type: WGPUCompilationMessageType
            get() = mem.readUInt(24uL) as WGPUCompilationMessageType
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var lineNum: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var linePos: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var offset: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var length: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationMessage {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var message: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var type: WGPUCompilationMessageType
            get() = mem.readUInt(24uL) as WGPUCompilationMessageType
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var lineNum: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var linePos: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var offset: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var length: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUConstantEntry {
    actual var nextInChain: WGPUChainedStruct?
    actual var key: WGPUStringView
    actual var value: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUConstantEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUConstantEntry = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUConstantEntry) -> Unit): ArrayHolder<WGPUConstantEntry> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUConstantEntry>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUConstantEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var key: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var value: Double
            get() = mem.readDouble(24uL)
            set(value) { mem.writeDouble(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUConstantEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var key: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var value: Double
            get() = mem.readDouble(24uL)
            set(value) { mem.writeDouble(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUExtent3D {
    actual var width: UInt
    actual var height: UInt
    actual var depthOrArrayLayers: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExtent3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExtent3D = ByReference(allocator.allocateBuffer(12uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExtent3D) -> Unit): ArrayHolder<WGPUExtent3D> {
            val buffer = allocator.allocateBuffer(12uL * size)
            val result = ArrayHolder<WGPUExtent3D>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 12L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUExtent3D {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var width: UInt
            get() = mem.readUInt(0uL)
            set(value) { mem.writeUInt(value, 0uL) }
        override var height: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var depthOrArrayLayers: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUExtent3D {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var width: UInt
            get() = mem.readUInt(0uL)
            set(value) { mem.writeUInt(value, 0uL) }
        override var height: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var depthOrArrayLayers: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUExternalTextureBindingEntry {
    actual var chain: WGPUChainedStruct
    actual var externalTexture: WGPUExternalTexture?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingEntry = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingEntry) -> Unit): ArrayHolder<WGPUExternalTextureBindingEntry> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUExternalTextureBindingEntry>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUExternalTextureBindingEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var externalTexture: WGPUExternalTexture?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUExternalTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUExternalTextureBindingEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var externalTexture: WGPUExternalTexture?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUExternalTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUExternalTextureBindingLayout {
    actual var chain: WGPUChainedStruct
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingLayout = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingLayout) -> Unit): ArrayHolder<WGPUExternalTextureBindingLayout> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUExternalTextureBindingLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUExternalTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUExternalTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUFuture {
    actual var id: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFuture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFuture = ByReference(allocator.allocateBuffer(8uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFuture) -> Unit): ArrayHolder<WGPUFuture> {
            val buffer = allocator.allocateBuffer(8uL * size)
            val result = ArrayHolder<WGPUFuture>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 8L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUFuture {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 8uL) }
        override var id: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUFuture {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 8uL) }
        override var id: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUInstanceLimits {
    actual var nextInChain: WGPUChainedStruct?
    actual var timedWaitAnyMaxCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceLimits = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceLimits) -> Unit): ArrayHolder<WGPUInstanceLimits> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUInstanceLimits>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var timedWaitAnyMaxCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var timedWaitAnyMaxCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUMultisampleState {
    actual var nextInChain: WGPUChainedStruct?
    actual var count: UInt
    actual var mask: UInt
    actual var alphaToCoverageEnabled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUMultisampleState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUMultisampleState = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUMultisampleState) -> Unit): ArrayHolder<WGPUMultisampleState> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUMultisampleState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUMultisampleState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var count: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var mask: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var alphaToCoverageEnabled: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUMultisampleState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var count: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var mask: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var alphaToCoverageEnabled: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUOrigin3D {
    actual var x: UInt
    actual var y: UInt
    actual var z: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUOrigin3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUOrigin3D = ByReference(allocator.allocateBuffer(12uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUOrigin3D) -> Unit): ArrayHolder<WGPUOrigin3D> {
            val buffer = allocator.allocateBuffer(12uL * size)
            val result = ArrayHolder<WGPUOrigin3D>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 12L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUOrigin3D {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var x: UInt
            get() = mem.readUInt(0uL)
            set(value) { mem.writeUInt(value, 0uL) }
        override var y: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var z: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUOrigin3D {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 12uL) }
        override var x: UInt
            get() = mem.readUInt(0uL)
            set(value) { mem.writeUInt(value, 0uL) }
        override var y: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var z: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUPassTimestampWrites {
    actual var nextInChain: WGPUChainedStruct?
    actual var querySet: WGPUQuerySet?
    actual var beginningOfPassWriteIndex: UInt
    actual var endOfPassWriteIndex: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPassTimestampWrites = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPassTimestampWrites = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPassTimestampWrites) -> Unit): ArrayHolder<WGPUPassTimestampWrites> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUPassTimestampWrites>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUPassTimestampWrites {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var querySet: WGPUQuerySet?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUQuerySet(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var beginningOfPassWriteIndex: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var endOfPassWriteIndex: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUPassTimestampWrites {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var querySet: WGPUQuerySet?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUQuerySet(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var beginningOfPassWriteIndex: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var endOfPassWriteIndex: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUPipelineLayoutDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var bindGroupLayoutCount: ULong
    actual var bindGroupLayouts: NativeAddress?
    actual var immediateSize: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPipelineLayoutDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPipelineLayoutDescriptor = ByReference(allocator.allocateBuffer(48uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPipelineLayoutDescriptor) -> Unit): ArrayHolder<WGPUPipelineLayoutDescriptor> {
            val buffer = allocator.allocateBuffer(48uL * size)
            val result = ArrayHolder<WGPUPipelineLayoutDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 48L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUPipelineLayoutDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var bindGroupLayoutCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var bindGroupLayouts: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var immediateSize: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUPipelineLayoutDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var bindGroupLayoutCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var bindGroupLayouts: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var immediateSize: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUPrimitiveState {
    actual var nextInChain: WGPUChainedStruct?
    actual var topology: WGPUPrimitiveTopology
    actual var stripIndexFormat: WGPUIndexFormat
    actual var frontFace: WGPUFrontFace
    actual var cullMode: WGPUCullMode
    actual var unclippedDepth: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveState = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveState) -> Unit): ArrayHolder<WGPUPrimitiveState> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUPrimitiveState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUPrimitiveState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var topology: WGPUPrimitiveTopology
            get() = mem.readUInt(8uL) as WGPUPrimitiveTopology
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var stripIndexFormat: WGPUIndexFormat
            get() = mem.readUInt(12uL) as WGPUIndexFormat
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var frontFace: WGPUFrontFace
            get() = mem.readUInt(16uL) as WGPUFrontFace
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var cullMode: WGPUCullMode
            get() = mem.readUInt(20uL) as WGPUCullMode
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var unclippedDepth: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUPrimitiveState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var topology: WGPUPrimitiveTopology
            get() = mem.readUInt(8uL) as WGPUPrimitiveTopology
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var stripIndexFormat: WGPUIndexFormat
            get() = mem.readUInt(12uL) as WGPUIndexFormat
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var frontFace: WGPUFrontFace
            get() = mem.readUInt(16uL) as WGPUFrontFace
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var cullMode: WGPUCullMode
            get() = mem.readUInt(20uL) as WGPUCullMode
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var unclippedDepth: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUQuerySetDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var type: WGPUQueryType
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptor = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptor) -> Unit): ArrayHolder<WGPUQuerySetDescriptor> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUQuerySetDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUQuerySetDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var type: WGPUQueryType
            get() = mem.readUInt(24uL) as WGPUQueryType
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var count: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUQuerySetDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var type: WGPUQueryType
            get() = mem.readUInt(24uL) as WGPUQueryType
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var count: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUQueueDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQueueDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueDescriptor) -> Unit): ArrayHolder<WGPUQueueDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUQueueDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUQueueDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUQueueDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderBundleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleDescriptor) -> Unit): ArrayHolder<WGPURenderBundleDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPURenderBundleDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderBundleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderBundleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderBundleEncoderDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var colorFormatCount: ULong
    actual var colorFormats: NativeAddress?
    actual var depthStencilFormat: WGPUTextureFormat
    actual var sampleCount: UInt
    actual var depthReadOnly: UInt
    actual var stencilReadOnly: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleEncoderDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleEncoderDescriptor = ByReference(allocator.allocateBuffer(56uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleEncoderDescriptor) -> Unit): ArrayHolder<WGPURenderBundleEncoderDescriptor> {
            val buffer = allocator.allocateBuffer(56uL * size)
            val result = ArrayHolder<WGPURenderBundleEncoderDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 56L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderBundleEncoderDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var colorFormatCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var colorFormats: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var depthStencilFormat: WGPUTextureFormat
            get() = mem.readUInt(40uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var sampleCount: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var depthReadOnly: UInt
            get() = mem.readUInt(48uL)
            set(value) { mem.writeUInt(value, 48uL) }
        override var stencilReadOnly: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderBundleEncoderDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var colorFormatCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var colorFormats: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var depthStencilFormat: WGPUTextureFormat
            get() = mem.readUInt(40uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var sampleCount: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var depthReadOnly: UInt
            get() = mem.readUInt(48uL)
            set(value) { mem.writeUInt(value, 48uL) }
        override var stencilReadOnly: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderPassDepthStencilAttachment {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDepthStencilAttachment = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDepthStencilAttachment = ByReference(allocator.allocateBuffer(48uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDepthStencilAttachment) -> Unit): ArrayHolder<WGPURenderPassDepthStencilAttachment> {
            val buffer = allocator.allocateBuffer(48uL * size)
            val result = ArrayHolder<WGPURenderPassDepthStencilAttachment>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 48L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassDepthStencilAttachment {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var view: WGPUTextureView?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var depthLoadOp: WGPULoadOp
            get() = mem.readUInt(16uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var depthStoreOp: WGPUStoreOp
            get() = mem.readUInt(20uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var depthClearValue: Float
            get() = mem.readFloat(24uL)
            set(value) { mem.writeFloat(value, 24uL) }
        override var depthReadOnly: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override var stencilLoadOp: WGPULoadOp
            get() = mem.readUInt(32uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var stencilStoreOp: WGPUStoreOp
            get() = mem.readUInt(36uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var stencilClearValue: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var stencilReadOnly: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassDepthStencilAttachment {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var view: WGPUTextureView?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var depthLoadOp: WGPULoadOp
            get() = mem.readUInt(16uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var depthStoreOp: WGPUStoreOp
            get() = mem.readUInt(20uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var depthClearValue: Float
            get() = mem.readFloat(24uL)
            set(value) { mem.writeFloat(value, 24uL) }
        override var depthReadOnly: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override var stencilLoadOp: WGPULoadOp
            get() = mem.readUInt(32uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var stencilStoreOp: WGPUStoreOp
            get() = mem.readUInt(36uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var stencilClearValue: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var stencilReadOnly: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderPassMaxDrawCount {
    actual var chain: WGPUChainedStruct
    actual var maxDrawCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassMaxDrawCount = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassMaxDrawCount = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassMaxDrawCount) -> Unit): ArrayHolder<WGPURenderPassMaxDrawCount> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPURenderPassMaxDrawCount>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassMaxDrawCount {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxDrawCount: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassMaxDrawCount {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxDrawCount: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURequestAdapterWebXROptions {
    actual var chain: WGPUChainedStruct
    actual var xrCompatible: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterWebXROptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterWebXROptions = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterWebXROptions) -> Unit): ArrayHolder<WGPURequestAdapterWebXROptions> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPURequestAdapterWebXROptions>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterWebXROptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var xrCompatible: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterWebXROptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var xrCompatible: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSamplerBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUSamplerBindingType
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerBindingLayout = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerBindingLayout) -> Unit): ArrayHolder<WGPUSamplerBindingLayout> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUSamplerBindingLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var type: WGPUSamplerBindingType
            get() = mem.readUInt(8uL) as WGPUSamplerBindingType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var type: WGPUSamplerBindingType
            get() = mem.readUInt(8uL) as WGPUSamplerBindingType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSamplerDescriptor {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptor = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptor) -> Unit): ArrayHolder<WGPUSamplerDescriptor> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUSamplerDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var addressModeU: WGPUAddressMode
            get() = mem.readUInt(24uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var addressModeV: WGPUAddressMode
            get() = mem.readUInt(28uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 28uL) }
        override var addressModeW: WGPUAddressMode
            get() = mem.readUInt(32uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var magFilter: WGPUFilterMode
            get() = mem.readUInt(36uL) as WGPUFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var minFilter: WGPUFilterMode
            get() = mem.readUInt(40uL) as WGPUFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = mem.readUInt(44uL) as WGPUMipmapFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 44uL) }
        override var lodMinClamp: Float
            get() = mem.readFloat(48uL)
            set(value) { mem.writeFloat(value, 48uL) }
        override var lodMaxClamp: Float
            get() = mem.readFloat(52uL)
            set(value) { mem.writeFloat(value, 52uL) }
        override var compare: WGPUCompareFunction
            get() = mem.readUInt(56uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 56uL) }
        override var maxAnisotropy: UShort
            get() = mem.readUShort(60uL)
            set(value) { mem.writeUShort(value, 60uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var addressModeU: WGPUAddressMode
            get() = mem.readUInt(24uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var addressModeV: WGPUAddressMode
            get() = mem.readUInt(28uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 28uL) }
        override var addressModeW: WGPUAddressMode
            get() = mem.readUInt(32uL) as WGPUAddressMode
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var magFilter: WGPUFilterMode
            get() = mem.readUInt(36uL) as WGPUFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var minFilter: WGPUFilterMode
            get() = mem.readUInt(40uL) as WGPUFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = mem.readUInt(44uL) as WGPUMipmapFilterMode
            set(value) { mem.writeUInt(value.toUInt(), 44uL) }
        override var lodMinClamp: Float
            get() = mem.readFloat(48uL)
            set(value) { mem.writeFloat(value, 48uL) }
        override var lodMaxClamp: Float
            get() = mem.readFloat(52uL)
            set(value) { mem.writeFloat(value, 52uL) }
        override var compare: WGPUCompareFunction
            get() = mem.readUInt(56uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 56uL) }
        override var maxAnisotropy: UShort
            get() = mem.readUShort(60uL)
            set(value) { mem.writeUShort(value, 60uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderSourceSPIRV {
    actual var chain: WGPUChainedStruct
    actual var codeSize: UInt
    actual var code: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceSPIRV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceSPIRV = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceSPIRV) -> Unit): ArrayHolder<WGPUShaderSourceSPIRV> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUShaderSourceSPIRV>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceSPIRV {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var codeSize: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var code: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceSPIRV {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var codeSize: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var code: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderSourceWGSL {
    actual var chain: WGPUChainedStruct
    actual var code: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceWGSL = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceWGSL = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceWGSL) -> Unit): ArrayHolder<WGPUShaderSourceWGSL> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUShaderSourceWGSL>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceWGSL {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var code: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceWGSL {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var code: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUStencilFaceState {
    actual var compare: WGPUCompareFunction
    actual var failOp: WGPUStencilOperation
    actual var depthFailOp: WGPUStencilOperation
    actual var passOp: WGPUStencilOperation
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStencilFaceState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStencilFaceState = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStencilFaceState) -> Unit): ArrayHolder<WGPUStencilFaceState> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUStencilFaceState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUStencilFaceState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var compare: WGPUCompareFunction
            get() = mem.readUInt(0uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var failOp: WGPUStencilOperation
            get() = mem.readUInt(4uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var depthFailOp: WGPUStencilOperation
            get() = mem.readUInt(8uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var passOp: WGPUStencilOperation
            get() = mem.readUInt(12uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUStencilFaceState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var compare: WGPUCompareFunction
            get() = mem.readUInt(0uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var failOp: WGPUStencilOperation
            get() = mem.readUInt(4uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var depthFailOp: WGPUStencilOperation
            get() = mem.readUInt(8uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var passOp: WGPUStencilOperation
            get() = mem.readUInt(12uL) as WGPUStencilOperation
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUStorageTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var access: WGPUStorageTextureAccess
    actual var format: WGPUTextureFormat
    actual var viewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStorageTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStorageTextureBindingLayout = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStorageTextureBindingLayout) -> Unit): ArrayHolder<WGPUStorageTextureBindingLayout> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUStorageTextureBindingLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUStorageTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var access: WGPUStorageTextureAccess
            get() = mem.readUInt(8uL) as WGPUStorageTextureAccess
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(12uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var viewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(16uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUStorageTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var access: WGPUStorageTextureAccess
            get() = mem.readUInt(8uL) as WGPUStorageTextureAccess
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(12uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var viewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(16uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSupportedFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedFeatures = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedFeatures) -> Unit): ArrayHolder<WGPUSupportedFeatures> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUSupportedFeatures>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSupportedInstanceFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedInstanceFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedInstanceFeatures = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedInstanceFeatures) -> Unit): ArrayHolder<WGPUSupportedInstanceFeatures> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUSupportedInstanceFeatures>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedInstanceFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedInstanceFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSupportedWGSLLanguageFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedWGSLLanguageFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedWGSLLanguageFeatures = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedWGSLLanguageFeatures) -> Unit): ArrayHolder<WGPUSupportedWGSLLanguageFeatures> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUSupportedWGSLLanguageFeatures>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedWGSLLanguageFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSupportedWGSLLanguageFeatures {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var featureCount: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var features: NativeAddress?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceCapabilities {
    actual var nextInChain: WGPUChainedStruct?
    actual var usages: ULong
    actual var formatCount: ULong
    actual var formats: NativeAddress?
    actual var presentModeCount: ULong
    actual var presentModes: NativeAddress?
    actual var alphaModeCount: ULong
    actual var alphaModes: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceCapabilities = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceCapabilities = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceCapabilities) -> Unit): ArrayHolder<WGPUSurfaceCapabilities> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUSurfaceCapabilities>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceCapabilities {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var usages: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var formatCount: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var formats: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var presentModeCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var presentModes: NativeAddress?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 40uL) }
        override var alphaModeCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var alphaModes: NativeAddress?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceCapabilities {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var usages: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var formatCount: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var formats: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override var presentModeCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var presentModes: NativeAddress?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 40uL) }
        override var alphaModeCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var alphaModes: NativeAddress?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceColorManagement {
    actual var chain: WGPUChainedStruct
    actual var colorSpace: WGPUPredefinedColorSpace
    actual var toneMappingMode: WGPUToneMappingMode
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceColorManagement = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceColorManagement = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceColorManagement) -> Unit): ArrayHolder<WGPUSurfaceColorManagement> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceColorManagement>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceColorManagement {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = mem.readUInt(16uL) as WGPUPredefinedColorSpace
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var toneMappingMode: WGPUToneMappingMode
            get() = mem.readUInt(20uL) as WGPUToneMappingMode
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceColorManagement {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = mem.readUInt(16uL) as WGPUPredefinedColorSpace
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var toneMappingMode: WGPUToneMappingMode
            get() = mem.readUInt(20uL) as WGPUToneMappingMode
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceConfiguration {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfiguration = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfiguration = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfiguration) -> Unit): ArrayHolder<WGPUSurfaceConfiguration> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUSurfaceConfiguration>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceConfiguration {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var device: WGPUDevice?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUDevice(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(16uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var width: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var height: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var viewFormatCount: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var viewFormats: NativeAddress?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 48uL) }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = mem.readUInt(56uL) as WGPUCompositeAlphaMode
            set(value) { mem.writeUInt(value.toUInt(), 56uL) }
        override var presentMode: WGPUPresentMode
            get() = mem.readUInt(60uL) as WGPUPresentMode
            set(value) { mem.writeUInt(value.toUInt(), 60uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceConfiguration {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var device: WGPUDevice?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUDevice(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(16uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var width: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var height: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var viewFormatCount: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var viewFormats: NativeAddress?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 48uL) }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = mem.readUInt(56uL) as WGPUCompositeAlphaMode
            set(value) { mem.writeUInt(value.toUInt(), 56uL) }
        override var presentMode: WGPUPresentMode
            get() = mem.readUInt(60uL) as WGPUPresentMode
            set(value) { mem.writeUInt(value.toUInt(), 60uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceAndroidNativeWindow {
    actual var chain: WGPUChainedStruct
    actual var window: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceAndroidNativeWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceAndroidNativeWindow = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceAndroidNativeWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceAndroidNativeWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var window: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceAndroidNativeWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var window: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceMetalLayer {
    actual var chain: WGPUChainedStruct
    actual var layer: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceMetalLayer = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceMetalLayer = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceMetalLayer) -> Unit): ArrayHolder<WGPUSurfaceSourceMetalLayer> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceMetalLayer>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceMetalLayer {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var layer: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceMetalLayer {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var layer: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceWaylandSurface {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var surface: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWaylandSurface = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWaylandSurface = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWaylandSurface) -> Unit): ArrayHolder<WGPUSurfaceSourceWaylandSurface> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceWaylandSurface>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceWaylandSurface {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var display: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var surface: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceWaylandSurface {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var display: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var surface: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceWindowsHWND {
    actual var chain: WGPUChainedStruct
    actual var hinstance: NativeAddress?
    actual var hwnd: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWindowsHWND = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWindowsHWND = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWindowsHWND) -> Unit): ArrayHolder<WGPUSurfaceSourceWindowsHWND> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceWindowsHWND>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceWindowsHWND {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var hinstance: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var hwnd: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceWindowsHWND {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var hinstance: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var hwnd: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceXCBWindow {
    actual var chain: WGPUChainedStruct
    actual var connection: NativeAddress?
    actual var window: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXCBWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXCBWindow = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXCBWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXCBWindow> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceXCBWindow>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceXCBWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var connection: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var window: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceXCBWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var connection: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var window: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceXlibWindow {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var window: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXlibWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXlibWindow = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXlibWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXlibWindow> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceXlibWindow>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceXlibWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var display: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var window: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceXlibWindow {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var display: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var window: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceTexture {
    actual var nextInChain: WGPUChainedStruct?
    actual var texture: WGPUTexture?
    actual var status: WGPUSurfaceGetCurrentTextureStatus
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceTexture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceTexture = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceTexture) -> Unit): ArrayHolder<WGPUSurfaceTexture> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceTexture>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceTexture {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var texture: WGPUTexture?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = mem.readUInt(16uL) as WGPUSurfaceGetCurrentTextureStatus
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceTexture {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var texture: WGPUTexture?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = mem.readUInt(16uL) as WGPUSurfaceGetCurrentTextureStatus
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTexelCopyBufferLayout {
    actual var offset: ULong
    actual var bytesPerRow: UInt
    actual var rowsPerImage: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferLayout = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferLayout) -> Unit): ArrayHolder<WGPUTexelCopyBufferLayout> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUTexelCopyBufferLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyBufferLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var offset: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var bytesPerRow: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var rowsPerImage: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyBufferLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var offset: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var bytesPerRow: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var rowsPerImage: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var sampleType: WGPUTextureSampleType
    actual var viewDimension: WGPUTextureViewDimension
    actual var multisampled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingLayout = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingLayout) -> Unit): ArrayHolder<WGPUTextureBindingLayout> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUTextureBindingLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var sampleType: WGPUTextureSampleType
            get() = mem.readUInt(8uL) as WGPUTextureSampleType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var viewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(12uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var multisampled: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureBindingLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var sampleType: WGPUTextureSampleType
            get() = mem.readUInt(8uL) as WGPUTextureSampleType
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var viewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(12uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var multisampled: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureBindingViewDimension {
    actual var chain: WGPUChainedStruct
    actual var textureBindingViewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingViewDimension = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingViewDimension = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingViewDimension) -> Unit): ArrayHolder<WGPUTextureBindingViewDimension> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUTextureBindingViewDimension>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureBindingViewDimension {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(16uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureBindingViewDimension {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = mem.readUInt(16uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureComponentSwizzle {
    actual var r: WGPUComponentSwizzle
    actual var g: WGPUComponentSwizzle
    actual var b: WGPUComponentSwizzle
    actual var a: WGPUComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzle = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzle) -> Unit): ArrayHolder<WGPUTextureComponentSwizzle> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUTextureComponentSwizzle>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureComponentSwizzle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var r: WGPUComponentSwizzle
            get() = mem.readUInt(0uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var g: WGPUComponentSwizzle
            get() = mem.readUInt(4uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var b: WGPUComponentSwizzle
            get() = mem.readUInt(8uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var a: WGPUComponentSwizzle
            get() = mem.readUInt(12uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureComponentSwizzle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var r: WGPUComponentSwizzle
            get() = mem.readUInt(0uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var g: WGPUComponentSwizzle
            get() = mem.readUInt(4uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 4uL) }
        override var b: WGPUComponentSwizzle
            get() = mem.readUInt(8uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var a: WGPUComponentSwizzle
            get() = mem.readUInt(12uL) as WGPUComponentSwizzle
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUVertexAttribute {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUVertexFormat
    actual var offset: ULong
    actual var shaderLocation: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUVertexAttribute = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexAttribute = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexAttribute) -> Unit): ArrayHolder<WGPUVertexAttribute> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUVertexAttribute>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexAttribute {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUVertexFormat
            get() = mem.readUInt(8uL) as WGPUVertexFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var offset: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var shaderLocation: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexAttribute {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUVertexFormat
            get() = mem.readUInt(8uL) as WGPUVertexFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var offset: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var shaderLocation: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupEntry {
    actual var nextInChain: WGPUChainedStruct?
    actual var binding: UInt
    actual var buffer: WGPUBuffer?
    actual var offset: ULong
    actual var size: ULong
    actual var sampler: WGPUSampler?
    actual var textureView: WGPUTextureView?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntry = ByReference(allocator.allocateBuffer(56uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntry) -> Unit): ArrayHolder<WGPUBindGroupEntry> {
            val buffer = allocator.allocateBuffer(56uL * size)
            val result = ArrayHolder<WGPUBindGroupEntry>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 56L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var binding: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var buffer: WGPUBuffer?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBuffer(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override var offset: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var size: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var sampler: WGPUSampler?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUSampler(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var textureView: WGPUTextureView?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var binding: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var buffer: WGPUBuffer?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBuffer(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override var offset: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var size: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var sampler: WGPUSampler?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUSampler(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var textureView: WGPUTextureView?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupLayoutEntry {
    actual var nextInChain: WGPUChainedStruct?
    actual var binding: UInt
    actual var visibility: ULong
    actual var bindingArraySize: UInt
    actual var buffer: WGPUBufferBindingLayout
    actual var sampler: WGPUSamplerBindingLayout
    actual var texture: WGPUTextureBindingLayout
    actual var storageTexture: WGPUStorageTextureBindingLayout
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntry = ByReference(allocator.allocateBuffer(120uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntry) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntry> {
            val buffer = allocator.allocateBuffer(120uL * size)
            val result = ArrayHolder<WGPUBindGroupLayoutEntry>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 120L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 120uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var binding: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var visibility: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var bindingArraySize: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var buffer: WGPUBufferBindingLayout
            get() = WGPUBufferBindingLayout.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 32uL, 24uL)
            }
        override var sampler: WGPUSamplerBindingLayout
            get() = WGPUSamplerBindingLayout.ByValue(NativeAddress(handle.rawValue + 56L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 56uL, 16uL)
            }
        override var texture: WGPUTextureBindingLayout
            get() = WGPUTextureBindingLayout.ByValue(NativeAddress(handle.rawValue + 72L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 72uL, 24uL)
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() = WGPUStorageTextureBindingLayout.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 96uL, 24uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutEntry {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 120uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var binding: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var visibility: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var bindingArraySize: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var buffer: WGPUBufferBindingLayout
            get() = WGPUBufferBindingLayout.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 32uL, 24uL)
            }
        override var sampler: WGPUSamplerBindingLayout
            get() = WGPUSamplerBindingLayout.ByValue(NativeAddress(handle.rawValue + 56L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 56uL, 16uL)
            }
        override var texture: WGPUTextureBindingLayout
            get() = WGPUTextureBindingLayout.ByValue(NativeAddress(handle.rawValue + 72L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 72uL, 24uL)
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() = WGPUStorageTextureBindingLayout.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 96uL, 24uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBlendState {
    actual var color: WGPUBlendComponent
    actual var alpha: WGPUBlendComponent
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendState = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendState) -> Unit): ArrayHolder<WGPUBlendState> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUBlendState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBlendState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var color: WGPUBlendComponent
            get() = WGPUBlendComponent.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 0uL, 12uL)
            }
        override var alpha: WGPUBlendComponent
            get() = WGPUBlendComponent.ByValue(NativeAddress(handle.rawValue + 12L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 12uL, 12uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBlendState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var color: WGPUBlendComponent
            get() = WGPUBlendComponent.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 0uL, 12uL)
            }
        override var alpha: WGPUBlendComponent
            get() = WGPUBlendComponent.ByValue(NativeAddress(handle.rawValue + 12L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 12uL, 12uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUCompilationInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var messageCount: ULong
    actual var messages: WGPUCompilationMessage?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfo = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfo) -> Unit): ArrayHolder<WGPUCompilationInfo> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUCompilationInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var messageCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var messages: WGPUCompilationMessage?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUCompilationMessage(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUCompilationInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var messageCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var messages: WGPUCompilationMessage?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUCompilationMessage(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUComputePassDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePassDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePassDescriptor = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePassDescriptor) -> Unit): ArrayHolder<WGPUComputePassDescriptor> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUComputePassDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputePassDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPassTimestampWrites(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputePassDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPassTimestampWrites(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUComputeState {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputeState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputeState = ByReference(allocator.allocateBuffer(48uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputeState) -> Unit): ArrayHolder<WGPUComputeState> {
            val buffer = allocator.allocateBuffer(48uL * size)
            val result = ArrayHolder<WGPUComputeState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 48L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputeState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputeState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUDepthStencilState {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDepthStencilState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDepthStencilState = ByReference(allocator.allocateBuffer(72uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDepthStencilState) -> Unit): ArrayHolder<WGPUDepthStencilState> {
            val buffer = allocator.allocateBuffer(72uL * size)
            val result = ArrayHolder<WGPUDepthStencilState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 72L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUDepthStencilState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 72uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(8uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = mem.readUInt(12uL) as WGPUOptionalBool
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var depthCompare: WGPUCompareFunction
            get() = mem.readUInt(16uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var stencilFront: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByValue(NativeAddress(handle.rawValue + 20L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 20uL, 16uL)
            }
        override var stencilBack: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByValue(NativeAddress(handle.rawValue + 36L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 36uL, 16uL)
            }
        override var stencilReadMask: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var stencilWriteMask: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var depthBias: Int
            get() = mem.readInt(60uL)
            set(value) { mem.writeInt(value, 60uL) }
        override var depthBiasSlopeScale: Float
            get() = mem.readFloat(64uL)
            set(value) { mem.writeFloat(value, 64uL) }
        override var depthBiasClamp: Float
            get() = mem.readFloat(68uL)
            set(value) { mem.writeFloat(value, 68uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUDepthStencilState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 72uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(8uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = mem.readUInt(12uL) as WGPUOptionalBool
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var depthCompare: WGPUCompareFunction
            get() = mem.readUInt(16uL) as WGPUCompareFunction
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var stencilFront: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByValue(NativeAddress(handle.rawValue + 20L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 20uL, 16uL)
            }
        override var stencilBack: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByValue(NativeAddress(handle.rawValue + 36L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 36uL, 16uL)
            }
        override var stencilReadMask: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var stencilWriteMask: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var depthBias: Int
            get() = mem.readInt(60uL)
            set(value) { mem.writeInt(value, 60uL) }
        override var depthBiasSlopeScale: Float
            get() = mem.readFloat(64uL)
            set(value) { mem.writeFloat(value, 64uL) }
        override var depthBiasClamp: Float
            get() = mem.readFloat(68uL)
            set(value) { mem.writeFloat(value, 68uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUFutureWaitInfo {
    actual var future: WGPUFuture
    actual var completed: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFutureWaitInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFutureWaitInfo = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFutureWaitInfo) -> Unit): ArrayHolder<WGPUFutureWaitInfo> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUFutureWaitInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUFutureWaitInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var future: WGPUFuture
            get() = WGPUFuture.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(8)
                MemoryBuffer(value.handler, 8uL).readBytes(bytes, 0u, 0uL, 8uL)
                mem.writeBytes(bytes, 0u, 0uL, 8uL)
            }
        override var completed: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUFutureWaitInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var future: WGPUFuture
            get() = WGPUFuture.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(8)
                MemoryBuffer(value.handler, 8uL).readBytes(bytes, 0u, 0uL, 8uL)
                mem.writeBytes(bytes, 0u, 0uL, 8uL)
            }
        override var completed: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUInstanceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPUInstanceLimits?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceDescriptor = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceDescriptor) -> Unit): ArrayHolder<WGPUInstanceDescriptor> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUInstanceDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var requiredFeatureCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var requiredFeatures: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var requiredLimits: WGPUInstanceLimits?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUInstanceLimits(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var requiredFeatureCount: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var requiredFeatures: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var requiredLimits: WGPUInstanceLimits?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUInstanceLimits(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPULimits {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPULimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPULimits = ByReference(allocator.allocateBuffer(152uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPULimits) -> Unit): ArrayHolder<WGPULimits> {
            val buffer = allocator.allocateBuffer(152uL * size)
            val result = ArrayHolder<WGPULimits>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 152L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPULimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 152uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var maxTextureDimension1D: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var maxTextureDimension2D: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var maxTextureDimension3D: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxTextureArrayLayers: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxBindGroups: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override var maxBindingsPerBindGroup: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var maxSamplersPerShaderStage: UInt
            get() = mem.readUInt(48uL)
            set(value) { mem.writeUInt(value, 48uL) }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = mem.readUInt(60uL)
            set(value) { mem.writeUInt(value, 60uL) }
        override var maxUniformBufferBindingSize: ULong
            get() = mem.readULong(64uL)
            set(value) { mem.writeULong(value, 64uL) }
        override var maxStorageBufferBindingSize: ULong
            get() = mem.readULong(72uL)
            set(value) { mem.writeULong(value, 72uL) }
        override var minUniformBufferOffsetAlignment: UInt
            get() = mem.readUInt(80uL)
            set(value) { mem.writeUInt(value, 80uL) }
        override var minStorageBufferOffsetAlignment: UInt
            get() = mem.readUInt(84uL)
            set(value) { mem.writeUInt(value, 84uL) }
        override var maxVertexBuffers: UInt
            get() = mem.readUInt(88uL)
            set(value) { mem.writeUInt(value, 88uL) }
        override var maxBufferSize: ULong
            get() = mem.readULong(96uL)
            set(value) { mem.writeULong(value, 96uL) }
        override var maxVertexAttributes: UInt
            get() = mem.readUInt(104uL)
            set(value) { mem.writeUInt(value, 104uL) }
        override var maxVertexBufferArrayStride: UInt
            get() = mem.readUInt(108uL)
            set(value) { mem.writeUInt(value, 108uL) }
        override var maxInterStageShaderVariables: UInt
            get() = mem.readUInt(112uL)
            set(value) { mem.writeUInt(value, 112uL) }
        override var maxColorAttachments: UInt
            get() = mem.readUInt(116uL)
            set(value) { mem.writeUInt(value, 116uL) }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = mem.readUInt(120uL)
            set(value) { mem.writeUInt(value, 120uL) }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = mem.readUInt(124uL)
            set(value) { mem.writeUInt(value, 124uL) }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = mem.readUInt(128uL)
            set(value) { mem.writeUInt(value, 128uL) }
        override var maxComputeWorkgroupSizeX: UInt
            get() = mem.readUInt(132uL)
            set(value) { mem.writeUInt(value, 132uL) }
        override var maxComputeWorkgroupSizeY: UInt
            get() = mem.readUInt(136uL)
            set(value) { mem.writeUInt(value, 136uL) }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = mem.readUInt(140uL)
            set(value) { mem.writeUInt(value, 140uL) }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = mem.readUInt(144uL)
            set(value) { mem.writeUInt(value, 144uL) }
        override var maxImmediateSize: UInt
            get() = mem.readUInt(148uL)
            set(value) { mem.writeUInt(value, 148uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPULimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 152uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var maxTextureDimension1D: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var maxTextureDimension2D: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var maxTextureDimension3D: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxTextureArrayLayers: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxBindGroups: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override var maxBindingsPerBindGroup: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var maxSamplersPerShaderStage: UInt
            get() = mem.readUInt(48uL)
            set(value) { mem.writeUInt(value, 48uL) }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = mem.readUInt(60uL)
            set(value) { mem.writeUInt(value, 60uL) }
        override var maxUniformBufferBindingSize: ULong
            get() = mem.readULong(64uL)
            set(value) { mem.writeULong(value, 64uL) }
        override var maxStorageBufferBindingSize: ULong
            get() = mem.readULong(72uL)
            set(value) { mem.writeULong(value, 72uL) }
        override var minUniformBufferOffsetAlignment: UInt
            get() = mem.readUInt(80uL)
            set(value) { mem.writeUInt(value, 80uL) }
        override var minStorageBufferOffsetAlignment: UInt
            get() = mem.readUInt(84uL)
            set(value) { mem.writeUInt(value, 84uL) }
        override var maxVertexBuffers: UInt
            get() = mem.readUInt(88uL)
            set(value) { mem.writeUInt(value, 88uL) }
        override var maxBufferSize: ULong
            get() = mem.readULong(96uL)
            set(value) { mem.writeULong(value, 96uL) }
        override var maxVertexAttributes: UInt
            get() = mem.readUInt(104uL)
            set(value) { mem.writeUInt(value, 104uL) }
        override var maxVertexBufferArrayStride: UInt
            get() = mem.readUInt(108uL)
            set(value) { mem.writeUInt(value, 108uL) }
        override var maxInterStageShaderVariables: UInt
            get() = mem.readUInt(112uL)
            set(value) { mem.writeUInt(value, 112uL) }
        override var maxColorAttachments: UInt
            get() = mem.readUInt(116uL)
            set(value) { mem.writeUInt(value, 116uL) }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = mem.readUInt(120uL)
            set(value) { mem.writeUInt(value, 120uL) }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = mem.readUInt(124uL)
            set(value) { mem.writeUInt(value, 124uL) }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = mem.readUInt(128uL)
            set(value) { mem.writeUInt(value, 128uL) }
        override var maxComputeWorkgroupSizeX: UInt
            get() = mem.readUInt(132uL)
            set(value) { mem.writeUInt(value, 132uL) }
        override var maxComputeWorkgroupSizeY: UInt
            get() = mem.readUInt(136uL)
            set(value) { mem.writeUInt(value, 136uL) }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = mem.readUInt(140uL)
            set(value) { mem.writeUInt(value, 140uL) }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = mem.readUInt(144uL)
            set(value) { mem.writeUInt(value, 144uL) }
        override var maxImmediateSize: UInt
            get() = mem.readUInt(148uL)
            set(value) { mem.writeUInt(value, 148uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderPassColorAttachment {
    actual var nextInChain: WGPUChainedStruct?
    actual var view: WGPUTextureView?
    actual var depthSlice: UInt
    actual var resolveTarget: WGPUTextureView?
    actual var loadOp: WGPULoadOp
    actual var storeOp: WGPUStoreOp
    actual var clearValue: WGPUColor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassColorAttachment = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassColorAttachment = ByReference(allocator.allocateBuffer(72uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassColorAttachment) -> Unit): ArrayHolder<WGPURenderPassColorAttachment> {
            val buffer = allocator.allocateBuffer(72uL * size)
            val result = ArrayHolder<WGPURenderPassColorAttachment>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 72L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassColorAttachment {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 72uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var view: WGPUTextureView?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var depthSlice: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var resolveTarget: WGPUTextureView?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var loadOp: WGPULoadOp
            get() = mem.readUInt(32uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var storeOp: WGPUStoreOp
            get() = mem.readUInt(36uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var clearValue: WGPUColor
            get() = WGPUColor.ByValue(NativeAddress(handle.rawValue + 40L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 40uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassColorAttachment {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 72uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var view: WGPUTextureView?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var depthSlice: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var resolveTarget: WGPUTextureView?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUTextureView(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var loadOp: WGPULoadOp
            get() = mem.readUInt(32uL) as WGPULoadOp
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var storeOp: WGPUStoreOp
            get() = mem.readUInt(36uL) as WGPUStoreOp
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var clearValue: WGPUColor
            get() = WGPUColor.ByValue(NativeAddress(handle.rawValue + 40L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 40uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURequestAdapterOptions {
    actual var nextInChain: WGPUChainedStruct?
    actual var featureLevel: WGPUFeatureLevel
    actual var powerPreference: WGPUPowerPreference
    actual var forceFallbackAdapter: UInt
    actual var backendType: WGPUBackendType
    actual var compatibleSurface: WGPUSurface?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterOptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterOptions = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterOptions) -> Unit): ArrayHolder<WGPURequestAdapterOptions> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPURequestAdapterOptions>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterOptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var featureLevel: WGPUFeatureLevel
            get() = mem.readUInt(8uL) as WGPUFeatureLevel
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var powerPreference: WGPUPowerPreference
            get() = mem.readUInt(12uL) as WGPUPowerPreference
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var forceFallbackAdapter: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var backendType: WGPUBackendType
            get() = mem.readUInt(20uL) as WGPUBackendType
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var compatibleSurface: WGPUSurface?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUSurface(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURequestAdapterOptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var featureLevel: WGPUFeatureLevel
            get() = mem.readUInt(8uL) as WGPUFeatureLevel
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var powerPreference: WGPUPowerPreference
            get() = mem.readUInt(12uL) as WGPUPowerPreference
            set(value) { mem.writeUInt(value.toUInt(), 12uL) }
        override var forceFallbackAdapter: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var backendType: WGPUBackendType
            get() = mem.readUInt(20uL) as WGPUBackendType
            set(value) { mem.writeUInt(value.toUInt(), 20uL) }
        override var compatibleSurface: WGPUSurface?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUSurface(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderModuleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptor) -> Unit): ArrayHolder<WGPUShaderModuleDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUShaderModuleDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderModuleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderModuleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceDescriptor = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceDescriptor) -> Unit): ArrayHolder<WGPUSurfaceDescriptor> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTexelCopyBufferInfo {
    actual var layout: WGPUTexelCopyBufferLayout
    actual var buffer: WGPUBuffer?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferInfo = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferInfo) -> Unit): ArrayHolder<WGPUTexelCopyBufferInfo> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUTexelCopyBufferInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyBufferInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var layout: WGPUTexelCopyBufferLayout
            get() = WGPUTexelCopyBufferLayout.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var buffer: WGPUBuffer?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBuffer(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyBufferInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var layout: WGPUTexelCopyBufferLayout
            get() = WGPUTexelCopyBufferLayout.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var buffer: WGPUBuffer?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBuffer(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTexelCopyTextureInfo {
    actual var texture: WGPUTexture?
    actual var mipLevel: UInt
    actual var origin: WGPUOrigin3D
    actual var aspect: WGPUTextureAspect
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyTextureInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyTextureInfo = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyTextureInfo) -> Unit): ArrayHolder<WGPUTexelCopyTextureInfo> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUTexelCopyTextureInfo>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyTextureInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var texture: WGPUTexture?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mipLevel: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var origin: WGPUOrigin3D
            get() = WGPUOrigin3D.ByValue(NativeAddress(handle.rawValue + 12L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 12uL, 12uL)
            }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(24uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTexelCopyTextureInfo {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var texture: WGPUTexture?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUTexture(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var mipLevel: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var origin: WGPUOrigin3D
            get() = WGPUOrigin3D.ByValue(NativeAddress(handle.rawValue + 12L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 12uL, 12uL)
            }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(24uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureComponentSwizzleDescriptor {
    actual var chain: WGPUChainedStruct
    actual var swizzle: WGPUTextureComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzleDescriptor = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzleDescriptor) -> Unit): ArrayHolder<WGPUTextureComponentSwizzleDescriptor> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUTextureComponentSwizzleDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureComponentSwizzleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() = WGPUTextureComponentSwizzle.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureComponentSwizzleDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() = WGPUTextureComponentSwizzle.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureDescriptor {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureDescriptor = ByReference(allocator.allocateBuffer(80uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureDescriptor) -> Unit): ArrayHolder<WGPUTextureDescriptor> {
            val buffer = allocator.allocateBuffer(80uL * size)
            val result = ArrayHolder<WGPUTextureDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 80L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 80uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var dimension: WGPUTextureDimension
            get() = mem.readUInt(32uL) as WGPUTextureDimension
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var size: WGPUExtent3D
            get() = WGPUExtent3D.ByValue(NativeAddress(handle.rawValue + 36L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 36uL, 12uL)
            }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(48uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 48uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var sampleCount: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var viewFormatCount: ULong
            get() = mem.readULong(64uL)
            set(value) { mem.writeULong(value, 64uL) }
        override var viewFormats: NativeAddress?
            get() = mem.readPointer(72uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 72uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 80uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var usage: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var dimension: WGPUTextureDimension
            get() = mem.readUInt(32uL) as WGPUTextureDimension
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var size: WGPUExtent3D
            get() = WGPUExtent3D.ByValue(NativeAddress(handle.rawValue + 36L))
            set(value) {
                val bytes = ByteArray(12)
                MemoryBuffer(value.handler, 12uL).readBytes(bytes, 0u, 0uL, 12uL)
                mem.writeBytes(bytes, 0u, 36uL, 12uL)
            }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(48uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 48uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(52uL)
            set(value) { mem.writeUInt(value, 52uL) }
        override var sampleCount: UInt
            get() = mem.readUInt(56uL)
            set(value) { mem.writeUInt(value, 56uL) }
        override var viewFormatCount: ULong
            get() = mem.readULong(64uL)
            set(value) { mem.writeULong(value, 64uL) }
        override var viewFormats: NativeAddress?
            get() = mem.readPointer(72uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 72uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUVertexBufferLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var stepMode: WGPUVertexStepMode
    actual var arrayStride: ULong
    actual var attributeCount: ULong
    actual var attributes: WGPUVertexAttribute?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUVertexBufferLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexBufferLayout = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexBufferLayout) -> Unit): ArrayHolder<WGPUVertexBufferLayout> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUVertexBufferLayout>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexBufferLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var stepMode: WGPUVertexStepMode
            get() = mem.readUInt(8uL) as WGPUVertexStepMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var arrayStride: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var attributeCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var attributes: WGPUVertexAttribute?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPUVertexAttribute(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexBufferLayout {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var stepMode: WGPUVertexStepMode
            get() = mem.readUInt(8uL) as WGPUVertexStepMode
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var arrayStride: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var attributeCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var attributes: WGPUVertexAttribute?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPUVertexAttribute(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUBindGroupLayout?
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupEntry?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupDescriptor = ByReference(allocator.allocateBuffer(48uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupDescriptor) -> Unit): ArrayHolder<WGPUBindGroupDescriptor> {
            val buffer = allocator.allocateBuffer(48uL * size)
            val result = ArrayHolder<WGPUBindGroupDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 48L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUBindGroupLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var entryCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var entries: WGPUBindGroupEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 48uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUBindGroupLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var entryCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var entries: WGPUBindGroupEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupLayoutDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupLayoutEntry?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutDescriptor = ByReference(allocator.allocateBuffer(40uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutDescriptor) -> Unit): ArrayHolder<WGPUBindGroupLayoutDescriptor> {
            val buffer = allocator.allocateBuffer(40uL * size)
            val result = ArrayHolder<WGPUBindGroupLayoutDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 40L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var entryCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 40uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var entryCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUColorTargetState {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUTextureFormat
    actual var blend: WGPUBlendState?
    actual var writeMask: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColorTargetState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColorTargetState = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColorTargetState) -> Unit): ArrayHolder<WGPUColorTargetState> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUColorTargetState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUColorTargetState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(8uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var blend: WGPUBlendState?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBlendState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override var writeMask: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUColorTargetState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(8uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 8uL) }
        override var blend: WGPUBlendState?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }?.let { WGPUBlendState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 16uL) }
        override var writeMask: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUComputePipelineDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var compute: WGPUComputeState
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePipelineDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePipelineDescriptor = ByReference(allocator.allocateBuffer(80uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePipelineDescriptor) -> Unit): ArrayHolder<WGPUComputePipelineDescriptor> {
            val buffer = allocator.allocateBuffer(80uL * size)
            val result = ArrayHolder<WGPUComputePipelineDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 80L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputePipelineDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 80uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUPipelineLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPipelineLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var compute: WGPUComputeState
            get() = WGPUComputeState.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(48)
                MemoryBuffer(value.handler, 48uL).readBytes(bytes, 0u, 0uL, 48uL)
                mem.writeBytes(bytes, 0u, 32uL, 48uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUComputePipelineDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 80uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUPipelineLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPipelineLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var compute: WGPUComputeState
            get() = WGPUComputeState.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(48)
                MemoryBuffer(value.handler, 48uL).readBytes(bytes, 0u, 0uL, 48uL)
                mem.writeBytes(bytes, 0u, 32uL, 48uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUDeviceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPULimits?
    actual var defaultQueue: WGPUQueueDescriptor
    actual var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
    actual var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDeviceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceDescriptor = ByReference(allocator.allocateBuffer(144uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceDescriptor) -> Unit): ArrayHolder<WGPUDeviceDescriptor> {
            val buffer = allocator.allocateBuffer(144uL * size)
            val result = ArrayHolder<WGPUDeviceDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 144L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 144uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var requiredFeatureCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var requiredFeatures: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var requiredLimits: WGPULimits?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPULimits(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var defaultQueue: WGPUQueueDescriptor
            get() = WGPUQueueDescriptor.ByValue(NativeAddress(handle.rawValue + 48L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 48uL, 24uL)
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() = WGPUDeviceLostCallbackInfo.ByValue(NativeAddress(handle.rawValue + 72L))
            set(value) {
                val bytes = ByteArray(40)
                MemoryBuffer(value.handler, 40uL).readBytes(bytes, 0u, 0uL, 40uL)
                mem.writeBytes(bytes, 0u, 72uL, 40uL)
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() = WGPUUncapturedErrorCallbackInfo.ByValue(NativeAddress(handle.rawValue + 112L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 112uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 144uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var requiredFeatureCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var requiredFeatures: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var requiredLimits: WGPULimits?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPULimits(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var defaultQueue: WGPUQueueDescriptor
            get() = WGPUQueueDescriptor.ByValue(NativeAddress(handle.rawValue + 48L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 48uL, 24uL)
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() = WGPUDeviceLostCallbackInfo.ByValue(NativeAddress(handle.rawValue + 72L))
            set(value) {
                val bytes = ByteArray(40)
                MemoryBuffer(value.handler, 40uL).readBytes(bytes, 0u, 0uL, 40uL)
                mem.writeBytes(bytes, 0u, 72uL, 40uL)
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() = WGPUUncapturedErrorCallbackInfo.ByValue(NativeAddress(handle.rawValue + 112L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 112uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderPassDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var colorAttachmentCount: ULong
    actual var colorAttachments: WGPURenderPassColorAttachment?
    actual var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
    actual var occlusionQuerySet: WGPUQuerySet?
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDescriptor = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDescriptor) -> Unit): ArrayHolder<WGPURenderPassDescriptor> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPURenderPassDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var colorAttachmentCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPURenderPassColorAttachment(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUQuerySet(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUPassTimestampWrites(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPassDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var colorAttachmentCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }?.let { WGPURenderPassColorAttachment(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 32uL) }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUQuerySet(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUPassTimestampWrites(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUTextureViewDescriptor {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureViewDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureViewDescriptor = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureViewDescriptor) -> Unit): ArrayHolder<WGPUTextureViewDescriptor> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUTextureViewDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureViewDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(24uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var dimension: WGPUTextureViewDimension
            get() = mem.readUInt(28uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 28uL) }
        override var baseMipLevel: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var baseArrayLayer: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var arrayLayerCount: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(48uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 48uL) }
        override var usage: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUTextureViewDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var format: WGPUTextureFormat
            get() = mem.readUInt(24uL) as WGPUTextureFormat
            set(value) { mem.writeUInt(value.toUInt(), 24uL) }
        override var dimension: WGPUTextureViewDimension
            get() = mem.readUInt(28uL) as WGPUTextureViewDimension
            set(value) { mem.writeUInt(value.toUInt(), 28uL) }
        override var baseMipLevel: UInt
            get() = mem.readUInt(32uL)
            set(value) { mem.writeUInt(value, 32uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(36uL)
            set(value) { mem.writeUInt(value, 36uL) }
        override var baseArrayLayer: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var arrayLayerCount: UInt
            get() = mem.readUInt(44uL)
            set(value) { mem.writeUInt(value, 44uL) }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(48uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 48uL) }
        override var usage: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUVertexState {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual var bufferCount: ULong
    actual var buffers: WGPUVertexBufferLayout?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUVertexState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexState = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexState) -> Unit): ArrayHolder<WGPUVertexState> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUVertexState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var bufferCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var buffers: WGPUVertexBufferLayout?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUVertexBufferLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUVertexState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var bufferCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var buffers: WGPUVertexBufferLayout?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUVertexBufferLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUFragmentState {
    actual var nextInChain: WGPUChainedStruct?
    actual var module: WGPUShaderModule?
    actual var entryPoint: WGPUStringView
    actual var constantCount: ULong
    actual var constants: WGPUConstantEntry?
    actual var targetCount: ULong
    actual var targets: WGPUColorTargetState?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFragmentState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFragmentState = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFragmentState) -> Unit): ArrayHolder<WGPUFragmentState> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUFragmentState>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUFragmentState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var targetCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var targets: WGPUColorTargetState?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUColorTargetState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUFragmentState {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var module: WGPUShaderModule?
            get() = mem.readPointer(8uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderModule(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 8uL) }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override var constantCount: ULong
            get() = mem.readULong(32uL)
            set(value) { mem.writeULong(value, 32uL) }
        override var constants: WGPUConstantEntry?
            get() = mem.readPointer(40uL).takeIf { it.rawValue != 0L }?.let { WGPUConstantEntry(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 40uL) }
        override var targetCount: ULong
            get() = mem.readULong(48uL)
            set(value) { mem.writeULong(value, 48uL) }
        override var targets: WGPUColorTargetState?
            get() = mem.readPointer(56uL).takeIf { it.rawValue != 0L }?.let { WGPUColorTargetState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURenderPipelineDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var vertex: WGPUVertexState
    actual var primitive: WGPUPrimitiveState
    actual var depthStencil: WGPUDepthStencilState?
    actual var multisample: WGPUMultisampleState
    actual var fragment: WGPUFragmentState?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPipelineDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPipelineDescriptor = ByReference(allocator.allocateBuffer(168uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPipelineDescriptor) -> Unit): ArrayHolder<WGPURenderPipelineDescriptor> {
            val buffer = allocator.allocateBuffer(168uL * size)
            val result = ArrayHolder<WGPURenderPipelineDescriptor>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 168L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPipelineDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 168uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUPipelineLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPipelineLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var vertex: WGPUVertexState
            get() = WGPUVertexState.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(64)
                MemoryBuffer(value.handler, 64uL).readBytes(bytes, 0u, 0uL, 64uL)
                mem.writeBytes(bytes, 0u, 32uL, 64uL)
            }
        override var primitive: WGPUPrimitiveState
            get() = WGPUPrimitiveState.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 96uL, 32uL)
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = mem.readPointer(128uL).takeIf { it.rawValue != 0L }?.let { WGPUDepthStencilState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 128uL) }
        override var multisample: WGPUMultisampleState
            get() = WGPUMultisampleState.ByValue(NativeAddress(handle.rawValue + 136L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 136uL, 24uL)
            }
        override var fragment: WGPUFragmentState?
            get() = mem.readPointer(160uL).takeIf { it.rawValue != 0L }?.let { WGPUFragmentState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 160uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURenderPipelineDescriptor {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 168uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 8L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 8uL, 16uL)
            }
        override var layout: WGPUPipelineLayout?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }?.let { WGPUPipelineLayout(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 24uL) }
        override var vertex: WGPUVertexState
            get() = WGPUVertexState.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(64)
                MemoryBuffer(value.handler, 64uL).readBytes(bytes, 0u, 0uL, 64uL)
                mem.writeBytes(bytes, 0u, 32uL, 64uL)
            }
        override var primitive: WGPUPrimitiveState
            get() = WGPUPrimitiveState.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 96uL, 32uL)
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = mem.readPointer(128uL).takeIf { it.rawValue != 0L }?.let { WGPUDepthStencilState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 128uL) }
        override var multisample: WGPUMultisampleState
            get() = WGPUMultisampleState.ByValue(NativeAddress(handle.rawValue + 136L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 136uL, 24uL)
            }
        override var fragment: WGPUFragmentState?
            get() = mem.readPointer(160uL).takeIf { it.rawValue != 0L }?.let { WGPUFragmentState(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 160uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

private val wgpuCreateInstance_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCreateInstance") }
actual fun wgpuCreateInstance(descriptor: WGPUInstanceDescriptor?): WGPUInstance? {
    return JvmDowncallEngine.callP1P(wgpuCreateInstance_ADDR, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUInstance)
}

private val wgpuGetInstanceFeatures_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuGetInstanceFeatures") }
actual fun wgpuGetInstanceFeatures(features: WGPUSupportedInstanceFeatures?): Unit {
    JvmDowncallEngine.callV1P(wgpuGetInstanceFeatures_ADDR, features?.handler?.rawValue ?: 0L)
    return
}

private val wgpuGetInstanceLimits_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuGetInstanceLimits") }
actual fun wgpuGetInstanceLimits(limits: WGPUInstanceLimits?): WGPUStatus {
    return (JvmDowncallEngine.callI1P(wgpuGetInstanceLimits_ADDR, limits?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuHasInstanceFeature_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuHasInstanceFeature") }
actual fun wgpuHasInstanceFeature(feature: WGPUInstanceFeatureName): UInt {
    return JvmDowncallEngine.callI1I(wgpuHasInstanceFeature_ADDR, feature.toInt()).toInt().toUInt()
}

private val wgpuGetProcAddress_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuGetProcAddress") }
actual fun wgpuGetProcAddress(procName: WGPUStringView): NativeAddress? {
    return JvmDowncallEngine.callStructArgWGPUStringViewRetP(wgpuGetProcAddress_ADDR, procName.handler.rawValue).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuAdapterGetFeatures_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetFeatures") }
actual fun wgpuAdapterGetFeatures(adapter: WGPUAdapter?, features: WGPUSupportedFeatures?): Unit {
    JvmDowncallEngine.callV2PP(wgpuAdapterGetFeatures_ADDR, adapter?.handler?.rawValue ?: 0L, features?.handler?.rawValue ?: 0L)
    return
}

private val wgpuAdapterGetInfo_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetInfo") }
actual fun wgpuAdapterGetInfo(adapter: WGPUAdapter?, info: WGPUAdapterInfo?): WGPUStatus {
    return (JvmDowncallEngine.callI2PP(wgpuAdapterGetInfo_ADDR, adapter?.handler?.rawValue ?: 0L, info?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuAdapterGetLimits_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterGetLimits") }
actual fun wgpuAdapterGetLimits(adapter: WGPUAdapter?, limits: WGPULimits?): WGPUStatus {
    return (JvmDowncallEngine.callI2PP(wgpuAdapterGetLimits_ADDR, adapter?.handler?.rawValue ?: 0L, limits?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuAdapterHasFeature_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterHasFeature") }
actual fun wgpuAdapterHasFeature(adapter: WGPUAdapter?, feature: WGPUFeatureName): UInt {
    return JvmDowncallEngine.callI2PI(wgpuAdapterHasFeature_ADDR, adapter?.handler?.rawValue ?: 0L, feature.toInt()).toInt().toUInt()
}

private val wgpuAdapterRequestDevice_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterRequestDevice") }
actual fun wgpuAdapterRequestDevice(allocator: MemoryAllocator, adapter: WGPUAdapter?, descriptor: WGPUDeviceDescriptor?, callbackInfo: WGPURequestDeviceCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPURequestDeviceCallbackInfo(wgpuAdapterRequestDevice_ADDR, allocator, adapter?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuAdapterAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterAddRef") }
actual fun wgpuAdapterAddRef(adapter: WGPUAdapter?): Unit {
    JvmDowncallEngine.callV1P(wgpuAdapterAddRef_ADDR, adapter?.handler?.rawValue ?: 0L)
    return
}

private val wgpuAdapterRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterRelease") }
actual fun wgpuAdapterRelease(adapter: WGPUAdapter?): Unit {
    JvmDowncallEngine.callV1P(wgpuAdapterRelease_ADDR, adapter?.handler?.rawValue ?: 0L)
    return
}

private val wgpuAdapterInfoFreeMembers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuAdapterInfoFreeMembers") }
actual fun wgpuAdapterInfoFreeMembers(adapterInfo: WGPUAdapterInfo): Unit {
    JvmDowncallEngine.callStructArgWGPUAdapterInfo(wgpuAdapterInfoFreeMembers_ADDR, adapterInfo.handler.rawValue)
    return
}

private val wgpuBindGroupSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupSetLabel") }
actual fun wgpuBindGroupSetLabel(bindGroup: WGPUBindGroup?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuBindGroupSetLabel_ADDR, bindGroup?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuBindGroupAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupAddRef") }
actual fun wgpuBindGroupAddRef(bindGroup: WGPUBindGroup?): Unit {
    JvmDowncallEngine.callV1P(wgpuBindGroupAddRef_ADDR, bindGroup?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBindGroupRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupRelease") }
actual fun wgpuBindGroupRelease(bindGroup: WGPUBindGroup?): Unit {
    JvmDowncallEngine.callV1P(wgpuBindGroupRelease_ADDR, bindGroup?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBindGroupLayoutSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutSetLabel") }
actual fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: WGPUBindGroupLayout?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuBindGroupLayoutSetLabel_ADDR, bindGroupLayout?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuBindGroupLayoutAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutAddRef") }
actual fun wgpuBindGroupLayoutAddRef(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    JvmDowncallEngine.callV1P(wgpuBindGroupLayoutAddRef_ADDR, bindGroupLayout?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBindGroupLayoutRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBindGroupLayoutRelease") }
actual fun wgpuBindGroupLayoutRelease(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    JvmDowncallEngine.callV1P(wgpuBindGroupLayoutRelease_ADDR, bindGroupLayout?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBufferDestroy_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferDestroy") }
actual fun wgpuBufferDestroy(buffer: WGPUBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuBufferDestroy_ADDR, buffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBufferGetConstMappedRange_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetConstMappedRange") }
actual fun wgpuBufferGetConstMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return JvmDowncallEngine.callP3PLL(wgpuBufferGetConstMappedRange_ADDR, buffer?.handler?.rawValue ?: 0L, offset.toLong(), size.toLong()).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuBufferGetMappedRange_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetMappedRange") }
actual fun wgpuBufferGetMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return JvmDowncallEngine.callP3PLL(wgpuBufferGetMappedRange_ADDR, buffer?.handler?.rawValue ?: 0L, offset.toLong(), size.toLong()).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuBufferGetMapState_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetMapState") }
actual fun wgpuBufferGetMapState(buffer: WGPUBuffer?): WGPUBufferMapState {
    return (JvmDowncallEngine.callI1P(wgpuBufferGetMapState_ADDR, buffer?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuBufferGetSize_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetSize") }
actual fun wgpuBufferGetSize(buffer: WGPUBuffer?): ULong {
    return JvmDowncallEngine.callL1P(wgpuBufferGetSize_ADDR, buffer?.handler?.rawValue ?: 0L).toULong()
}

private val wgpuBufferGetUsage_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferGetUsage") }
actual fun wgpuBufferGetUsage(buffer: WGPUBuffer?): ULong {
    return JvmDowncallEngine.callL1P(wgpuBufferGetUsage_ADDR, buffer?.handler?.rawValue ?: 0L).toULong()
}

private val wgpuBufferMapAsync_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferMapAsync") }
actual fun wgpuBufferMapAsync(allocator: MemoryAllocator, buffer: WGPUBuffer?, mode: ULong, offset: ULong, size: ULong, callbackInfo: WGPUBufferMapCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUBufferMapCallbackInfo(wgpuBufferMapAsync_ADDR, allocator, buffer?.handler?.rawValue ?: 0L, mode.toLong(), offset.toLong(), size.toLong(), callbackInfo.handler.rawValue))
}

private val wgpuBufferReadMappedRange_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferReadMappedRange") }
actual fun wgpuBufferReadMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (JvmDowncallEngine.callI4PLPL(wgpuBufferReadMappedRange_ADDR, buffer?.handler?.rawValue ?: 0L, offset.toLong(), data?.rawValue ?: 0L, size.toLong()).toInt()).toUInt()
}

private val wgpuBufferSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferSetLabel") }
actual fun wgpuBufferSetLabel(buffer: WGPUBuffer?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuBufferSetLabel_ADDR, buffer?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuBufferUnmap_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferUnmap") }
actual fun wgpuBufferUnmap(buffer: WGPUBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuBufferUnmap_ADDR, buffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBufferWriteMappedRange_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferWriteMappedRange") }
actual fun wgpuBufferWriteMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (JvmDowncallEngine.callI4PLPL(wgpuBufferWriteMappedRange_ADDR, buffer?.handler?.rawValue ?: 0L, offset.toLong(), data?.rawValue ?: 0L, size.toLong()).toInt()).toUInt()
}

private val wgpuBufferAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferAddRef") }
actual fun wgpuBufferAddRef(buffer: WGPUBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuBufferAddRef_ADDR, buffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuBufferRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuBufferRelease") }
actual fun wgpuBufferRelease(buffer: WGPUBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuBufferRelease_ADDR, buffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandBufferSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferSetLabel") }
actual fun wgpuCommandBufferSetLabel(commandBuffer: WGPUCommandBuffer?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuCommandBufferSetLabel_ADDR, commandBuffer?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuCommandBufferAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferAddRef") }
actual fun wgpuCommandBufferAddRef(commandBuffer: WGPUCommandBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuCommandBufferAddRef_ADDR, commandBuffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandBufferRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandBufferRelease") }
actual fun wgpuCommandBufferRelease(commandBuffer: WGPUCommandBuffer?): Unit {
    JvmDowncallEngine.callV1P(wgpuCommandBufferRelease_ADDR, commandBuffer?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderBeginComputePass_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderBeginComputePass") }
actual fun wgpuCommandEncoderBeginComputePass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUComputePassDescriptor?): WGPUComputePassEncoder? {
    return JvmDowncallEngine.callP2PP(wgpuCommandEncoderBeginComputePass_ADDR, commandEncoder?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUComputePassEncoder)
}

private val wgpuCommandEncoderBeginRenderPass_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderBeginRenderPass") }
actual fun wgpuCommandEncoderBeginRenderPass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPURenderPassDescriptor?): WGPURenderPassEncoder? {
    return JvmDowncallEngine.callP2PP(wgpuCommandEncoderBeginRenderPass_ADDR, commandEncoder?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPURenderPassEncoder)
}

private val wgpuCommandEncoderClearBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderClearBuffer") }
actual fun wgpuCommandEncoderClearBuffer(commandEncoder: WGPUCommandEncoder?, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV4PPLL(wgpuCommandEncoderClearBuffer_ADDR, commandEncoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, offset.toLong(), size.toLong())
    return
}

private val wgpuCommandEncoderCopyBufferToBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyBufferToBuffer") }
actual fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUBuffer?, sourceOffset: ULong, destination: WGPUBuffer?, destinationOffset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV6PPLPLL(wgpuCommandEncoderCopyBufferToBuffer_ADDR, commandEncoder?.handler?.rawValue ?: 0L, source?.handler?.rawValue ?: 0L, sourceOffset.toLong(), destination?.handler?.rawValue ?: 0L, destinationOffset.toLong(), size.toLong())
    return
}

private val wgpuCommandEncoderCopyBufferToTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyBufferToTexture") }
actual fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyBufferInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    JvmDowncallEngine.callV4PPPP(wgpuCommandEncoderCopyBufferToTexture_ADDR, commandEncoder?.handler?.rawValue ?: 0L, source?.handler?.rawValue ?: 0L, destination?.handler?.rawValue ?: 0L, copySize?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderCopyTextureToBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyTextureToBuffer") }
actual fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyBufferInfo?, copySize: WGPUExtent3D?): Unit {
    JvmDowncallEngine.callV4PPPP(wgpuCommandEncoderCopyTextureToBuffer_ADDR, commandEncoder?.handler?.rawValue ?: 0L, source?.handler?.rawValue ?: 0L, destination?.handler?.rawValue ?: 0L, copySize?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderCopyTextureToTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderCopyTextureToTexture") }
actual fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    JvmDowncallEngine.callV4PPPP(wgpuCommandEncoderCopyTextureToTexture_ADDR, commandEncoder?.handler?.rawValue ?: 0L, source?.handler?.rawValue ?: 0L, destination?.handler?.rawValue ?: 0L, copySize?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderFinish_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderFinish") }
actual fun wgpuCommandEncoderFinish(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUCommandBufferDescriptor?): WGPUCommandBuffer? {
    return JvmDowncallEngine.callP2PP(wgpuCommandEncoderFinish_ADDR, commandEncoder?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUCommandBuffer)
}

private val wgpuCommandEncoderInsertDebugMarker_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderInsertDebugMarker") }
actual fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: WGPUCommandEncoder?, markerLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuCommandEncoderInsertDebugMarker_ADDR, commandEncoder?.handler?.rawValue ?: 0L, markerLabel.handler.rawValue)
    return
}

private val wgpuCommandEncoderPopDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderPopDebugGroup") }
actual fun wgpuCommandEncoderPopDebugGroup(commandEncoder: WGPUCommandEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuCommandEncoderPopDebugGroup_ADDR, commandEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderPushDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderPushDebugGroup") }
actual fun wgpuCommandEncoderPushDebugGroup(commandEncoder: WGPUCommandEncoder?, groupLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuCommandEncoderPushDebugGroup_ADDR, commandEncoder?.handler?.rawValue ?: 0L, groupLabel.handler.rawValue)
    return
}

private val wgpuCommandEncoderResolveQuerySet_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderResolveQuerySet") }
actual fun wgpuCommandEncoderResolveQuerySet(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, firstQuery: UInt, queryCount: UInt, destination: WGPUBuffer?, destinationOffset: ULong): Unit {
    JvmDowncallEngine.callV6PPIIPL(wgpuCommandEncoderResolveQuerySet_ADDR, commandEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, firstQuery.toInt(), queryCount.toInt(), destination?.handler?.rawValue ?: 0L, destinationOffset.toLong())
    return
}

private val wgpuCommandEncoderSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderSetLabel") }
actual fun wgpuCommandEncoderSetLabel(commandEncoder: WGPUCommandEncoder?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuCommandEncoderSetLabel_ADDR, commandEncoder?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuCommandEncoderWriteTimestamp_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderWriteTimestamp") }
actual fun wgpuCommandEncoderWriteTimestamp(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV3PPI(wgpuCommandEncoderWriteTimestamp_ADDR, commandEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuCommandEncoderAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderAddRef") }
actual fun wgpuCommandEncoderAddRef(commandEncoder: WGPUCommandEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuCommandEncoderAddRef_ADDR, commandEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderRelease") }
actual fun wgpuCommandEncoderRelease(commandEncoder: WGPUCommandEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuCommandEncoderRelease_ADDR, commandEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderDispatchWorkgroups_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderDispatchWorkgroups") }
actual fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: WGPUComputePassEncoder?, workgroupCountX: UInt, workgroupCountY: UInt, workgroupCountZ: UInt): Unit {
    JvmDowncallEngine.callV4PIII(wgpuComputePassEncoderDispatchWorkgroups_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, workgroupCountX.toInt(), workgroupCountY.toInt(), workgroupCountZ.toInt())
    return
}

private val wgpuComputePassEncoderDispatchWorkgroupsIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderDispatchWorkgroupsIndirect") }
actual fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: WGPUComputePassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    JvmDowncallEngine.callV3PPL(wgpuComputePassEncoderDispatchWorkgroupsIndirect_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, indirectBuffer?.handler?.rawValue ?: 0L, indirectOffset.toLong())
    return
}

private val wgpuComputePassEncoderEnd_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderEnd") }
actual fun wgpuComputePassEncoderEnd(computePassEncoder: WGPUComputePassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePassEncoderEnd_ADDR, computePassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderInsertDebugMarker_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderInsertDebugMarker") }
actual fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: WGPUComputePassEncoder?, markerLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuComputePassEncoderInsertDebugMarker_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, markerLabel.handler.rawValue)
    return
}

private val wgpuComputePassEncoderPopDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderPopDebugGroup") }
actual fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: WGPUComputePassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePassEncoderPopDebugGroup_ADDR, computePassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderPushDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderPushDebugGroup") }
actual fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: WGPUComputePassEncoder?, groupLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuComputePassEncoderPushDebugGroup_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, groupLabel.handler.rawValue)
    return
}

private val wgpuComputePassEncoderSetBindGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetBindGroup") }
actual fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: WGPUComputePassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    JvmDowncallEngine.callV5PIPLP(wgpuComputePassEncoderSetBindGroup_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, groupIndex.toInt(), group?.handler?.rawValue ?: 0L, dynamicOffsetCount.toLong(), dynamicOffsets?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderSetImmediates_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetImmediates") }
actual fun wgpuComputePassEncoderSetImmediates(computePassEncoder: WGPUComputePassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    JvmDowncallEngine.callGeneric(wgpuComputePassEncoderSetImmediates_ADDR, JvmDowncallEngine.FunctionShape(result = JvmDowncallEngine.AbiType.Void, arguments = listOf(JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I32, JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I64)), computePassEncoder?.handler?.rawValue ?: 0L, offset.toInt(), data?.rawValue ?: 0L, size.toLong())
    return
}

private val wgpuComputePassEncoderSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetLabel") }
actual fun wgpuComputePassEncoderSetLabel(computePassEncoder: WGPUComputePassEncoder?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuComputePassEncoderSetLabel_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuComputePassEncoderSetPipeline_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderSetPipeline") }
actual fun wgpuComputePassEncoderSetPipeline(computePassEncoder: WGPUComputePassEncoder?, pipeline: WGPUComputePipeline?): Unit {
    JvmDowncallEngine.callV2PP(wgpuComputePassEncoderSetPipeline_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, pipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderAddRef") }
actual fun wgpuComputePassEncoderAddRef(computePassEncoder: WGPUComputePassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePassEncoderAddRef_ADDR, computePassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderRelease") }
actual fun wgpuComputePassEncoderRelease(computePassEncoder: WGPUComputePassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePassEncoderRelease_ADDR, computePassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePipelineGetBindGroupLayout_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineGetBindGroupLayout") }
actual fun wgpuComputePipelineGetBindGroupLayout(computePipeline: WGPUComputePipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return JvmDowncallEngine.callP2PI(wgpuComputePipelineGetBindGroupLayout_ADDR, computePipeline?.handler?.rawValue ?: 0L, groupIndex.toInt()).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUBindGroupLayout)
}

private val wgpuComputePipelineSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineSetLabel") }
actual fun wgpuComputePipelineSetLabel(computePipeline: WGPUComputePipeline?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuComputePipelineSetLabel_ADDR, computePipeline?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuComputePipelineAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineAddRef") }
actual fun wgpuComputePipelineAddRef(computePipeline: WGPUComputePipeline?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePipelineAddRef_ADDR, computePipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePipelineRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePipelineRelease") }
actual fun wgpuComputePipelineRelease(computePipeline: WGPUComputePipeline?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePipelineRelease_ADDR, computePipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuDeviceCreateBindGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBindGroup") }
actual fun wgpuDeviceCreateBindGroup(device: WGPUDevice?, descriptor: WGPUBindGroupDescriptor?): WGPUBindGroup? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateBindGroup_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUBindGroup)
}

private val wgpuDeviceCreateBindGroupLayout_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBindGroupLayout") }
actual fun wgpuDeviceCreateBindGroupLayout(device: WGPUDevice?, descriptor: WGPUBindGroupLayoutDescriptor?): WGPUBindGroupLayout? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateBindGroupLayout_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUBindGroupLayout)
}

private val wgpuDeviceCreateBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateBuffer") }
actual fun wgpuDeviceCreateBuffer(device: WGPUDevice?, descriptor: WGPUBufferDescriptor?): WGPUBuffer? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateBuffer_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUBuffer)
}

private val wgpuDeviceCreateCommandEncoder_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateCommandEncoder") }
actual fun wgpuDeviceCreateCommandEncoder(device: WGPUDevice?, descriptor: WGPUCommandEncoderDescriptor?): WGPUCommandEncoder? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateCommandEncoder_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUCommandEncoder)
}

private val wgpuDeviceCreateComputePipeline_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateComputePipeline") }
actual fun wgpuDeviceCreateComputePipeline(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?): WGPUComputePipeline? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateComputePipeline_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUComputePipeline)
}

private val wgpuDeviceCreateComputePipelineAsync_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateComputePipelineAsync") }
actual fun wgpuDeviceCreateComputePipelineAsync(allocator: MemoryAllocator, device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?, callbackInfo: WGPUCreateComputePipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUCreateComputePipelineAsyncCallbackInfo(wgpuDeviceCreateComputePipelineAsync_ADDR, allocator, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuDeviceCreatePipelineLayout_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreatePipelineLayout") }
actual fun wgpuDeviceCreatePipelineLayout(device: WGPUDevice?, descriptor: WGPUPipelineLayoutDescriptor?): WGPUPipelineLayout? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreatePipelineLayout_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUPipelineLayout)
}

private val wgpuDeviceCreateQuerySet_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateQuerySet") }
actual fun wgpuDeviceCreateQuerySet(device: WGPUDevice?, descriptor: WGPUQuerySetDescriptor?): WGPUQuerySet? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateQuerySet_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUQuerySet)
}

private val wgpuDeviceCreateRenderBundleEncoder_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderBundleEncoder") }
actual fun wgpuDeviceCreateRenderBundleEncoder(device: WGPUDevice?, descriptor: WGPURenderBundleEncoderDescriptor?): WGPURenderBundleEncoder? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateRenderBundleEncoder_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPURenderBundleEncoder)
}

private val wgpuDeviceCreateRenderPipeline_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderPipeline") }
actual fun wgpuDeviceCreateRenderPipeline(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?): WGPURenderPipeline? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateRenderPipeline_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPURenderPipeline)
}

private val wgpuDeviceCreateRenderPipelineAsync_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateRenderPipelineAsync") }
actual fun wgpuDeviceCreateRenderPipelineAsync(allocator: MemoryAllocator, device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?, callbackInfo: WGPUCreateRenderPipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUCreateRenderPipelineAsyncCallbackInfo(wgpuDeviceCreateRenderPipelineAsync_ADDR, allocator, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuDeviceCreateSampler_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateSampler") }
actual fun wgpuDeviceCreateSampler(device: WGPUDevice?, descriptor: WGPUSamplerDescriptor?): WGPUSampler? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateSampler_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUSampler)
}

private val wgpuDeviceCreateShaderModule_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModule") }
actual fun wgpuDeviceCreateShaderModule(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?): WGPUShaderModule? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateShaderModule_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUShaderModule)
}

private val wgpuDeviceCreateTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateTexture") }
actual fun wgpuDeviceCreateTexture(device: WGPUDevice?, descriptor: WGPUTextureDescriptor?): WGPUTexture? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateTexture_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUTexture)
}

private val wgpuDeviceDestroy_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceDestroy") }
actual fun wgpuDeviceDestroy(device: WGPUDevice?): Unit {
    JvmDowncallEngine.callV1P(wgpuDeviceDestroy_ADDR, device?.handler?.rawValue ?: 0L)
    return
}

private val wgpuDeviceGetAdapterInfo_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetAdapterInfo") }
actual fun wgpuDeviceGetAdapterInfo(device: WGPUDevice?, adapterInfo: WGPUAdapterInfo?): WGPUStatus {
    return (JvmDowncallEngine.callI2PP(wgpuDeviceGetAdapterInfo_ADDR, device?.handler?.rawValue ?: 0L, adapterInfo?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuDeviceGetFeatures_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetFeatures") }
actual fun wgpuDeviceGetFeatures(device: WGPUDevice?, features: WGPUSupportedFeatures?): Unit {
    JvmDowncallEngine.callV2PP(wgpuDeviceGetFeatures_ADDR, device?.handler?.rawValue ?: 0L, features?.handler?.rawValue ?: 0L)
    return
}

private val wgpuDeviceGetLimits_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetLimits") }
actual fun wgpuDeviceGetLimits(device: WGPUDevice?, limits: WGPULimits?): WGPUStatus {
    return (JvmDowncallEngine.callI2PP(wgpuDeviceGetLimits_ADDR, device?.handler?.rawValue ?: 0L, limits?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuDeviceGetLostFuture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetLostFuture") }
actual fun wgpuDeviceGetLostFuture(allocator: MemoryAllocator, device: WGPUDevice?): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFuture(wgpuDeviceGetLostFuture_ADDR, allocator, device?.handler?.rawValue ?: 0L))
}

private val wgpuDeviceGetQueue_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetQueue") }
actual fun wgpuDeviceGetQueue(device: WGPUDevice?): WGPUQueue? {
    return JvmDowncallEngine.callP1P(wgpuDeviceGetQueue_ADDR, device?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUQueue)
}

private val wgpuDeviceHasFeature_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceHasFeature") }
actual fun wgpuDeviceHasFeature(device: WGPUDevice?, feature: WGPUFeatureName): UInt {
    return JvmDowncallEngine.callI2PI(wgpuDeviceHasFeature_ADDR, device?.handler?.rawValue ?: 0L, feature.toInt()).toInt().toUInt()
}

private val wgpuDevicePopErrorScope_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDevicePopErrorScope") }
actual fun wgpuDevicePopErrorScope(allocator: MemoryAllocator, device: WGPUDevice?, callbackInfo: WGPUPopErrorScopeCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUPopErrorScopeCallbackInfo(wgpuDevicePopErrorScope_ADDR, allocator, device?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuDevicePushErrorScope_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDevicePushErrorScope") }
actual fun wgpuDevicePushErrorScope(device: WGPUDevice?, filter: WGPUErrorFilter): Unit {
    JvmDowncallEngine.callV2PI(wgpuDevicePushErrorScope_ADDR, device?.handler?.rawValue ?: 0L, filter.toInt())
    return
}

private val wgpuDeviceSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceSetLabel") }
actual fun wgpuDeviceSetLabel(device: WGPUDevice?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuDeviceSetLabel_ADDR, device?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuDeviceAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceAddRef") }
actual fun wgpuDeviceAddRef(device: WGPUDevice?): Unit {
    JvmDowncallEngine.callV1P(wgpuDeviceAddRef_ADDR, device?.handler?.rawValue ?: 0L)
    return
}

private val wgpuDeviceRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceRelease") }
actual fun wgpuDeviceRelease(device: WGPUDevice?): Unit {
    JvmDowncallEngine.callV1P(wgpuDeviceRelease_ADDR, device?.handler?.rawValue ?: 0L)
    return
}

private val wgpuExternalTextureSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureSetLabel") }
actual fun wgpuExternalTextureSetLabel(externalTexture: WGPUExternalTexture?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuExternalTextureSetLabel_ADDR, externalTexture?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuExternalTextureAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureAddRef") }
actual fun wgpuExternalTextureAddRef(externalTexture: WGPUExternalTexture?): Unit {
    JvmDowncallEngine.callV1P(wgpuExternalTextureAddRef_ADDR, externalTexture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuExternalTextureRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuExternalTextureRelease") }
actual fun wgpuExternalTextureRelease(externalTexture: WGPUExternalTexture?): Unit {
    JvmDowncallEngine.callV1P(wgpuExternalTextureRelease_ADDR, externalTexture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuInstanceCreateSurface_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceCreateSurface") }
actual fun wgpuInstanceCreateSurface(instance: WGPUInstance?, descriptor: WGPUSurfaceDescriptor?): WGPUSurface? {
    return JvmDowncallEngine.callP2PP(wgpuInstanceCreateSurface_ADDR, instance?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUSurface)
}

private val wgpuInstanceGetWGSLLanguageFeatures_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceGetWGSLLanguageFeatures") }
actual fun wgpuInstanceGetWGSLLanguageFeatures(instance: WGPUInstance?, features: WGPUSupportedWGSLLanguageFeatures?): Unit {
    JvmDowncallEngine.callV2PP(wgpuInstanceGetWGSLLanguageFeatures_ADDR, instance?.handler?.rawValue ?: 0L, features?.handler?.rawValue ?: 0L)
    return
}

private val wgpuInstanceHasWGSLLanguageFeature_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceHasWGSLLanguageFeature") }
actual fun wgpuInstanceHasWGSLLanguageFeature(instance: WGPUInstance?, feature: WGPUWGSLLanguageFeatureName): UInt {
    return JvmDowncallEngine.callI2PI(wgpuInstanceHasWGSLLanguageFeature_ADDR, instance?.handler?.rawValue ?: 0L, feature.toInt()).toInt().toUInt()
}

private val wgpuInstanceProcessEvents_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceProcessEvents") }
actual fun wgpuInstanceProcessEvents(instance: WGPUInstance?): Unit {
    JvmDowncallEngine.callV1P(wgpuInstanceProcessEvents_ADDR, instance?.handler?.rawValue ?: 0L)
    return
}

private val wgpuInstanceRequestAdapter_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceRequestAdapter") }
actual fun wgpuInstanceRequestAdapter(allocator: MemoryAllocator, instance: WGPUInstance?, options: WGPURequestAdapterOptions?, callbackInfo: WGPURequestAdapterCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPURequestAdapterCallbackInfo(wgpuInstanceRequestAdapter_ADDR, allocator, instance?.handler?.rawValue ?: 0L, options?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuInstanceWaitAny_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceWaitAny") }
actual fun wgpuInstanceWaitAny(instance: WGPUInstance?, futureCount: ULong, futures: WGPUFutureWaitInfo?, timeoutNS: ULong): WGPUWaitStatus {
    return (JvmDowncallEngine.callI4PLPL(wgpuInstanceWaitAny_ADDR, instance?.handler?.rawValue ?: 0L, futureCount.toLong(), futures?.handler?.rawValue ?: 0L, timeoutNS.toLong()).toInt()).toUInt()
}

private val wgpuInstanceAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceAddRef") }
actual fun wgpuInstanceAddRef(instance: WGPUInstance?): Unit {
    JvmDowncallEngine.callV1P(wgpuInstanceAddRef_ADDR, instance?.handler?.rawValue ?: 0L)
    return
}

private val wgpuInstanceRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceRelease") }
actual fun wgpuInstanceRelease(instance: WGPUInstance?): Unit {
    JvmDowncallEngine.callV1P(wgpuInstanceRelease_ADDR, instance?.handler?.rawValue ?: 0L)
    return
}

private val wgpuPipelineLayoutSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutSetLabel") }
actual fun wgpuPipelineLayoutSetLabel(pipelineLayout: WGPUPipelineLayout?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuPipelineLayoutSetLabel_ADDR, pipelineLayout?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuPipelineLayoutAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutAddRef") }
actual fun wgpuPipelineLayoutAddRef(pipelineLayout: WGPUPipelineLayout?): Unit {
    JvmDowncallEngine.callV1P(wgpuPipelineLayoutAddRef_ADDR, pipelineLayout?.handler?.rawValue ?: 0L)
    return
}

private val wgpuPipelineLayoutRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuPipelineLayoutRelease") }
actual fun wgpuPipelineLayoutRelease(pipelineLayout: WGPUPipelineLayout?): Unit {
    JvmDowncallEngine.callV1P(wgpuPipelineLayoutRelease_ADDR, pipelineLayout?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQuerySetDestroy_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetDestroy") }
actual fun wgpuQuerySetDestroy(querySet: WGPUQuerySet?): Unit {
    JvmDowncallEngine.callV1P(wgpuQuerySetDestroy_ADDR, querySet?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQuerySetGetCount_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetGetCount") }
actual fun wgpuQuerySetGetCount(querySet: WGPUQuerySet?): UInt {
    return JvmDowncallEngine.callI1P(wgpuQuerySetGetCount_ADDR, querySet?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuQuerySetGetType_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetGetType") }
actual fun wgpuQuerySetGetType(querySet: WGPUQuerySet?): WGPUQueryType {
    return (JvmDowncallEngine.callI1P(wgpuQuerySetGetType_ADDR, querySet?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuQuerySetSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetSetLabel") }
actual fun wgpuQuerySetSetLabel(querySet: WGPUQuerySet?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuQuerySetSetLabel_ADDR, querySet?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuQuerySetAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetAddRef") }
actual fun wgpuQuerySetAddRef(querySet: WGPUQuerySet?): Unit {
    JvmDowncallEngine.callV1P(wgpuQuerySetAddRef_ADDR, querySet?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQuerySetRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQuerySetRelease") }
actual fun wgpuQuerySetRelease(querySet: WGPUQuerySet?): Unit {
    JvmDowncallEngine.callV1P(wgpuQuerySetRelease_ADDR, querySet?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQueueOnSubmittedWorkDone_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueOnSubmittedWorkDone") }
actual fun wgpuQueueOnSubmittedWorkDone(allocator: MemoryAllocator, queue: WGPUQueue?, callbackInfo: WGPUQueueWorkDoneCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUQueueWorkDoneCallbackInfo(wgpuQueueOnSubmittedWorkDone_ADDR, allocator, queue?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuQueueSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueSetLabel") }
actual fun wgpuQueueSetLabel(queue: WGPUQueue?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuQueueSetLabel_ADDR, queue?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuQueueSubmit_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueSubmit") }
actual fun wgpuQueueSubmit(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): Unit {
    JvmDowncallEngine.callV3PLP(wgpuQueueSubmit_ADDR, queue?.handler?.rawValue ?: 0L, commandCount.toLong(), commands?.rawValue ?: 0L)
    return
}

private val wgpuQueueWriteBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueWriteBuffer") }
actual fun wgpuQueueWriteBuffer(queue: WGPUQueue?, buffer: WGPUBuffer?, bufferOffset: ULong, data: NativeAddress?, size: ULong): Unit {
    JvmDowncallEngine.callV5PPLPL(wgpuQueueWriteBuffer_ADDR, queue?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, bufferOffset.toLong(), data?.rawValue ?: 0L, size.toLong())
    return
}

private val wgpuQueueWriteTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueWriteTexture") }
actual fun wgpuQueueWriteTexture(queue: WGPUQueue?, destination: WGPUTexelCopyTextureInfo?, data: NativeAddress?, dataSize: ULong, dataLayout: WGPUTexelCopyBufferLayout?, writeSize: WGPUExtent3D?): Unit {
    JvmDowncallEngine.callV6PPPLPP(wgpuQueueWriteTexture_ADDR, queue?.handler?.rawValue ?: 0L, destination?.handler?.rawValue ?: 0L, data?.rawValue ?: 0L, dataSize.toLong(), dataLayout?.handler?.rawValue ?: 0L, writeSize?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQueueAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueAddRef") }
actual fun wgpuQueueAddRef(queue: WGPUQueue?): Unit {
    JvmDowncallEngine.callV1P(wgpuQueueAddRef_ADDR, queue?.handler?.rawValue ?: 0L)
    return
}

private val wgpuQueueRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueRelease") }
actual fun wgpuQueueRelease(queue: WGPUQueue?): Unit {
    JvmDowncallEngine.callV1P(wgpuQueueRelease_ADDR, queue?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleSetLabel") }
actual fun wgpuRenderBundleSetLabel(renderBundle: WGPURenderBundle?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderBundleSetLabel_ADDR, renderBundle?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuRenderBundleAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleAddRef") }
actual fun wgpuRenderBundleAddRef(renderBundle: WGPURenderBundle?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderBundleAddRef_ADDR, renderBundle?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleRelease") }
actual fun wgpuRenderBundleRelease(renderBundle: WGPURenderBundle?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderBundleRelease_ADDR, renderBundle?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleEncoderDraw_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDraw") }
actual fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: WGPURenderBundleEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    JvmDowncallEngine.callV5PIIII(wgpuRenderBundleEncoderDraw_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

private val wgpuRenderBundleEncoderDrawIndexed_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndexed") }
actual fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: WGPURenderBundleEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    JvmDowncallEngine.callV6PIIIII(wgpuRenderBundleEncoderDrawIndexed_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

private val wgpuRenderBundleEncoderDrawIndexedIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndexedIndirect") }
actual fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    JvmDowncallEngine.callV3PPL(wgpuRenderBundleEncoderDrawIndexedIndirect_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, indirectBuffer?.handler?.rawValue ?: 0L, indirectOffset.toLong())
    return
}

private val wgpuRenderBundleEncoderDrawIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderDrawIndirect") }
actual fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    JvmDowncallEngine.callV3PPL(wgpuRenderBundleEncoderDrawIndirect_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, indirectBuffer?.handler?.rawValue ?: 0L, indirectOffset.toLong())
    return
}

private val wgpuRenderBundleEncoderFinish_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderFinish") }
actual fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: WGPURenderBundleEncoder?, descriptor: WGPURenderBundleDescriptor?): WGPURenderBundle? {
    return JvmDowncallEngine.callP2PP(wgpuRenderBundleEncoderFinish_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPURenderBundle)
}

private val wgpuRenderBundleEncoderInsertDebugMarker_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderInsertDebugMarker") }
actual fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: WGPURenderBundleEncoder?, markerLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderBundleEncoderInsertDebugMarker_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, markerLabel.handler.rawValue)
    return
}

private val wgpuRenderBundleEncoderPopDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderPopDebugGroup") }
actual fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderBundleEncoderPopDebugGroup_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleEncoderPushDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderPushDebugGroup") }
actual fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderBundleEncoderPushDebugGroup_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, groupLabel.handler.rawValue)
    return
}

private val wgpuRenderBundleEncoderSetBindGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetBindGroup") }
actual fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    JvmDowncallEngine.callV5PIPLP(wgpuRenderBundleEncoderSetBindGroup_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, groupIndex.toInt(), group?.handler?.rawValue ?: 0L, dynamicOffsetCount.toLong(), dynamicOffsets?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleEncoderSetImmediates_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetImmediates") }
actual fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: WGPURenderBundleEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    JvmDowncallEngine.callGeneric(wgpuRenderBundleEncoderSetImmediates_ADDR, JvmDowncallEngine.FunctionShape(result = JvmDowncallEngine.AbiType.Void, arguments = listOf(JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I32, JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I64)), renderBundleEncoder?.handler?.rawValue ?: 0L, offset.toInt(), data?.rawValue ?: 0L, size.toLong())
    return
}

private val wgpuRenderBundleEncoderSetIndexBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetIndexBuffer") }
actual fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV5PPILL(wgpuRenderBundleEncoderSetIndexBuffer_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, format.toInt(), offset.toLong(), size.toLong())
    return
}

private val wgpuRenderBundleEncoderSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetLabel") }
actual fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: WGPURenderBundleEncoder?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderBundleEncoderSetLabel_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuRenderBundleEncoderSetPipeline_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetPipeline") }
actual fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: WGPURenderBundleEncoder?, pipeline: WGPURenderPipeline?): Unit {
    JvmDowncallEngine.callV2PP(wgpuRenderBundleEncoderSetPipeline_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, pipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleEncoderSetVertexBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderSetVertexBuffer") }
actual fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV5PIPLL(wgpuRenderBundleEncoderSetVertexBuffer_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L, slot.toInt(), buffer?.handler?.rawValue ?: 0L, offset.toLong(), size.toLong())
    return
}

private val wgpuRenderBundleEncoderAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderAddRef") }
actual fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderBundleEncoderAddRef_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderBundleEncoderRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderBundleEncoderRelease") }
actual fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderBundleEncoderRelease_ADDR, renderBundleEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderBeginOcclusionQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderBeginOcclusionQuery") }
actual fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV2PI(wgpuRenderPassEncoderBeginOcclusionQuery_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderDraw_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDraw") }
actual fun wgpuRenderPassEncoderDraw(renderPassEncoder: WGPURenderPassEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    JvmDowncallEngine.callV5PIIII(wgpuRenderPassEncoderDraw_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

private val wgpuRenderPassEncoderDrawIndexed_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndexed") }
actual fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: WGPURenderPassEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    JvmDowncallEngine.callV6PIIIII(wgpuRenderPassEncoderDrawIndexed_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

private val wgpuRenderPassEncoderDrawIndexedIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndexedIndirect") }
actual fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    JvmDowncallEngine.callV3PPL(wgpuRenderPassEncoderDrawIndexedIndirect_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, indirectBuffer?.handler?.rawValue ?: 0L, indirectOffset.toLong())
    return
}

private val wgpuRenderPassEncoderDrawIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderDrawIndirect") }
actual fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    JvmDowncallEngine.callV3PPL(wgpuRenderPassEncoderDrawIndirect_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, indirectBuffer?.handler?.rawValue ?: 0L, indirectOffset.toLong())
    return
}

private val wgpuRenderPassEncoderEnd_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEnd") }
actual fun wgpuRenderPassEncoderEnd(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderEnd_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderEndOcclusionQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEndOcclusionQuery") }
actual fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderEndOcclusionQuery_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderExecuteBundles_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderExecuteBundles") }
actual fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: WGPURenderPassEncoder?, bundleCount: ULong, bundles: NativeAddress?): Unit {
    JvmDowncallEngine.callV3PLP(wgpuRenderPassEncoderExecuteBundles_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, bundleCount.toLong(), bundles?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderInsertDebugMarker_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderInsertDebugMarker") }
actual fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: WGPURenderPassEncoder?, markerLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderPassEncoderInsertDebugMarker_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, markerLabel.handler.rawValue)
    return
}

private val wgpuRenderPassEncoderPopDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderPopDebugGroup") }
actual fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderPopDebugGroup_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderPushDebugGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderPushDebugGroup") }
actual fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: WGPURenderPassEncoder?, groupLabel: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderPassEncoderPushDebugGroup_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, groupLabel.handler.rawValue)
    return
}

private val wgpuRenderPassEncoderSetBindGroup_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetBindGroup") }
actual fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: WGPURenderPassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    JvmDowncallEngine.callV5PIPLP(wgpuRenderPassEncoderSetBindGroup_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, groupIndex.toInt(), group?.handler?.rawValue ?: 0L, dynamicOffsetCount.toLong(), dynamicOffsets?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderSetBlendConstant_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetBlendConstant") }
actual fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: WGPURenderPassEncoder?, color: WGPUColor?): Unit {
    JvmDowncallEngine.callV2PP(wgpuRenderPassEncoderSetBlendConstant_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, color?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderSetImmediates_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetImmediates") }
actual fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: WGPURenderPassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    JvmDowncallEngine.callGeneric(wgpuRenderPassEncoderSetImmediates_ADDR, JvmDowncallEngine.FunctionShape(result = JvmDowncallEngine.AbiType.Void, arguments = listOf(JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I32, JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I64)), renderPassEncoder?.handler?.rawValue ?: 0L, offset.toInt(), data?.rawValue ?: 0L, size.toLong())
    return
}

private val wgpuRenderPassEncoderSetIndexBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetIndexBuffer") }
actual fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV5PPILL(wgpuRenderPassEncoderSetIndexBuffer_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, format.toInt(), offset.toLong(), size.toLong())
    return
}

private val wgpuRenderPassEncoderSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetLabel") }
actual fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: WGPURenderPassEncoder?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderPassEncoderSetLabel_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuRenderPassEncoderSetPipeline_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetPipeline") }
actual fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: WGPURenderPassEncoder?, pipeline: WGPURenderPipeline?): Unit {
    JvmDowncallEngine.callV2PP(wgpuRenderPassEncoderSetPipeline_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, pipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderSetScissorRect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetScissorRect") }
actual fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: WGPURenderPassEncoder?, x: UInt, y: UInt, width: UInt, height: UInt): Unit {
    JvmDowncallEngine.callV5PIIII(wgpuRenderPassEncoderSetScissorRect_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    return
}

private val wgpuRenderPassEncoderSetStencilReference_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetStencilReference") }
actual fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: WGPURenderPassEncoder?, reference: UInt): Unit {
    JvmDowncallEngine.callV2PI(wgpuRenderPassEncoderSetStencilReference_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, reference.toInt())
    return
}

private val wgpuRenderPassEncoderSetVertexBuffer_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetVertexBuffer") }
actual fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: WGPURenderPassEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    JvmDowncallEngine.callV5PIPLL(wgpuRenderPassEncoderSetVertexBuffer_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, slot.toInt(), buffer?.handler?.rawValue ?: 0L, offset.toLong(), size.toLong())
    return
}

private val wgpuRenderPassEncoderSetViewport_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderSetViewport") }
actual fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: WGPURenderPassEncoder?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit {
    JvmDowncallEngine.callV7PFFFFFF(wgpuRenderPassEncoderSetViewport_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, x, y, width, height, minDepth, maxDepth)
    return
}

private val wgpuRenderPassEncoderAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderAddRef") }
actual fun wgpuRenderPassEncoderAddRef(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderAddRef_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderRelease") }
actual fun wgpuRenderPassEncoderRelease(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderRelease_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPipelineGetBindGroupLayout_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineGetBindGroupLayout") }
actual fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: WGPURenderPipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return JvmDowncallEngine.callP2PI(wgpuRenderPipelineGetBindGroupLayout_ADDR, renderPipeline?.handler?.rawValue ?: 0L, groupIndex.toInt()).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUBindGroupLayout)
}

private val wgpuRenderPipelineSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineSetLabel") }
actual fun wgpuRenderPipelineSetLabel(renderPipeline: WGPURenderPipeline?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuRenderPipelineSetLabel_ADDR, renderPipeline?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuRenderPipelineAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineAddRef") }
actual fun wgpuRenderPipelineAddRef(renderPipeline: WGPURenderPipeline?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPipelineAddRef_ADDR, renderPipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPipelineRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPipelineRelease") }
actual fun wgpuRenderPipelineRelease(renderPipeline: WGPURenderPipeline?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPipelineRelease_ADDR, renderPipeline?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSamplerSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSamplerSetLabel") }
actual fun wgpuSamplerSetLabel(sampler: WGPUSampler?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuSamplerSetLabel_ADDR, sampler?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuSamplerAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSamplerAddRef") }
actual fun wgpuSamplerAddRef(sampler: WGPUSampler?): Unit {
    JvmDowncallEngine.callV1P(wgpuSamplerAddRef_ADDR, sampler?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSamplerRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSamplerRelease") }
actual fun wgpuSamplerRelease(sampler: WGPUSampler?): Unit {
    JvmDowncallEngine.callV1P(wgpuSamplerRelease_ADDR, sampler?.handler?.rawValue ?: 0L)
    return
}

private val wgpuShaderModuleGetCompilationInfo_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleGetCompilationInfo") }
actual fun wgpuShaderModuleGetCompilationInfo(allocator: MemoryAllocator, shaderModule: WGPUShaderModule?, callbackInfo: WGPUCompilationInfoCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(JvmDowncallEngine.callStructReturnWGPUFutureWGPUCompilationInfoCallbackInfo(wgpuShaderModuleGetCompilationInfo_ADDR, allocator, shaderModule?.handler?.rawValue ?: 0L, callbackInfo.handler.rawValue))
}

private val wgpuShaderModuleSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleSetLabel") }
actual fun wgpuShaderModuleSetLabel(shaderModule: WGPUShaderModule?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuShaderModuleSetLabel_ADDR, shaderModule?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuShaderModuleAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleAddRef") }
actual fun wgpuShaderModuleAddRef(shaderModule: WGPUShaderModule?): Unit {
    JvmDowncallEngine.callV1P(wgpuShaderModuleAddRef_ADDR, shaderModule?.handler?.rawValue ?: 0L)
    return
}

private val wgpuShaderModuleRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuShaderModuleRelease") }
actual fun wgpuShaderModuleRelease(shaderModule: WGPUShaderModule?): Unit {
    JvmDowncallEngine.callV1P(wgpuShaderModuleRelease_ADDR, shaderModule?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSupportedFeaturesFreeMembers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSupportedFeaturesFreeMembers") }
actual fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: WGPUSupportedFeatures): Unit {
    JvmDowncallEngine.callStructArgWGPUSupportedFeatures(wgpuSupportedFeaturesFreeMembers_ADDR, supportedFeatures.handler.rawValue)
    return
}

private val wgpuSupportedInstanceFeaturesFreeMembers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSupportedInstanceFeaturesFreeMembers") }
actual fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: WGPUSupportedInstanceFeatures): Unit {
    JvmDowncallEngine.callStructArgWGPUSupportedInstanceFeatures(wgpuSupportedInstanceFeaturesFreeMembers_ADDR, supportedInstanceFeatures.handler.rawValue)
    return
}

private val wgpuSupportedWGSLLanguageFeaturesFreeMembers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSupportedWGSLLanguageFeaturesFreeMembers") }
actual fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: WGPUSupportedWGSLLanguageFeatures): Unit {
    JvmDowncallEngine.callStructArgWGPUSupportedWGSLLanguageFeatures(wgpuSupportedWGSLLanguageFeaturesFreeMembers_ADDR, supportedWGSLLanguageFeatures.handler.rawValue)
    return
}

private val wgpuSurfaceConfigure_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceConfigure") }
actual fun wgpuSurfaceConfigure(surface: WGPUSurface?, config: WGPUSurfaceConfiguration?): Unit {
    JvmDowncallEngine.callV2PP(wgpuSurfaceConfigure_ADDR, surface?.handler?.rawValue ?: 0L, config?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSurfaceGetCapabilities_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceGetCapabilities") }
actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    return (JvmDowncallEngine.callI3PPP(wgpuSurfaceGetCapabilities_ADDR, surface?.handler?.rawValue ?: 0L, adapter?.handler?.rawValue ?: 0L, capabilities?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuSurfaceGetCurrentTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceGetCurrentTexture") }
actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    JvmDowncallEngine.callV2PP(wgpuSurfaceGetCurrentTexture_ADDR, surface?.handler?.rawValue ?: 0L, surfaceTexture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSurfacePresent_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfacePresent") }
actual fun wgpuSurfacePresent(surface: WGPUSurface?): WGPUStatus {
    return (JvmDowncallEngine.callI1P(wgpuSurfacePresent_ADDR, surface?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuSurfaceSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceSetLabel") }
actual fun wgpuSurfaceSetLabel(surface: WGPUSurface?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuSurfaceSetLabel_ADDR, surface?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuSurfaceUnconfigure_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceUnconfigure") }
actual fun wgpuSurfaceUnconfigure(surface: WGPUSurface?): Unit {
    JvmDowncallEngine.callV1P(wgpuSurfaceUnconfigure_ADDR, surface?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSurfaceAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceAddRef") }
actual fun wgpuSurfaceAddRef(surface: WGPUSurface?): Unit {
    JvmDowncallEngine.callV1P(wgpuSurfaceAddRef_ADDR, surface?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSurfaceRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceRelease") }
actual fun wgpuSurfaceRelease(surface: WGPUSurface?): Unit {
    JvmDowncallEngine.callV1P(wgpuSurfaceRelease_ADDR, surface?.handler?.rawValue ?: 0L)
    return
}

private val wgpuSurfaceCapabilitiesFreeMembers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSurfaceCapabilitiesFreeMembers") }
actual fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: WGPUSurfaceCapabilities): Unit {
    JvmDowncallEngine.callStructArgWGPUSurfaceCapabilities(wgpuSurfaceCapabilitiesFreeMembers_ADDR, surfaceCapabilities.handler.rawValue)
    return
}

private val wgpuTextureCreateView_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureCreateView") }
actual fun wgpuTextureCreateView(texture: WGPUTexture?, descriptor: WGPUTextureViewDescriptor?): WGPUTextureView? {
    return JvmDowncallEngine.callP2PP(wgpuTextureCreateView_ADDR, texture?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUTextureView)
}

private val wgpuTextureDestroy_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureDestroy") }
actual fun wgpuTextureDestroy(texture: WGPUTexture?): Unit {
    JvmDowncallEngine.callV1P(wgpuTextureDestroy_ADDR, texture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuTextureGetDepthOrArrayLayers_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetDepthOrArrayLayers") }
actual fun wgpuTextureGetDepthOrArrayLayers(texture: WGPUTexture?): UInt {
    return JvmDowncallEngine.callI1P(wgpuTextureGetDepthOrArrayLayers_ADDR, texture?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuTextureGetDimension_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetDimension") }
actual fun wgpuTextureGetDimension(texture: WGPUTexture?): WGPUTextureDimension {
    return (JvmDowncallEngine.callI1P(wgpuTextureGetDimension_ADDR, texture?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuTextureGetFormat_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetFormat") }
actual fun wgpuTextureGetFormat(texture: WGPUTexture?): WGPUTextureFormat {
    return (JvmDowncallEngine.callI1P(wgpuTextureGetFormat_ADDR, texture?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuTextureGetHeight_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetHeight") }
actual fun wgpuTextureGetHeight(texture: WGPUTexture?): UInt {
    return JvmDowncallEngine.callI1P(wgpuTextureGetHeight_ADDR, texture?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuTextureGetMipLevelCount_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetMipLevelCount") }
actual fun wgpuTextureGetMipLevelCount(texture: WGPUTexture?): UInt {
    return JvmDowncallEngine.callI1P(wgpuTextureGetMipLevelCount_ADDR, texture?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuTextureGetSampleCount_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetSampleCount") }
actual fun wgpuTextureGetSampleCount(texture: WGPUTexture?): UInt {
    return JvmDowncallEngine.callI1P(wgpuTextureGetSampleCount_ADDR, texture?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuTextureGetTextureBindingViewDimension_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetTextureBindingViewDimension") }
actual fun wgpuTextureGetTextureBindingViewDimension(texture: WGPUTexture?): WGPUTextureViewDimension {
    return (JvmDowncallEngine.callI1P(wgpuTextureGetTextureBindingViewDimension_ADDR, texture?.handler?.rawValue ?: 0L).toInt()).toUInt()
}

private val wgpuTextureGetUsage_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetUsage") }
actual fun wgpuTextureGetUsage(texture: WGPUTexture?): ULong {
    return JvmDowncallEngine.callL1P(wgpuTextureGetUsage_ADDR, texture?.handler?.rawValue ?: 0L).toULong()
}

private val wgpuTextureGetWidth_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetWidth") }
actual fun wgpuTextureGetWidth(texture: WGPUTexture?): UInt {
    return JvmDowncallEngine.callI1P(wgpuTextureGetWidth_ADDR, texture?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuTextureSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureSetLabel") }
actual fun wgpuTextureSetLabel(texture: WGPUTexture?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuTextureSetLabel_ADDR, texture?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuTextureAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureAddRef") }
actual fun wgpuTextureAddRef(texture: WGPUTexture?): Unit {
    JvmDowncallEngine.callV1P(wgpuTextureAddRef_ADDR, texture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuTextureRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureRelease") }
actual fun wgpuTextureRelease(texture: WGPUTexture?): Unit {
    JvmDowncallEngine.callV1P(wgpuTextureRelease_ADDR, texture?.handler?.rawValue ?: 0L)
    return
}

private val wgpuTextureViewSetLabel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewSetLabel") }
actual fun wgpuTextureViewSetLabel(textureView: WGPUTextureView?, label: WGPUStringView): Unit {
    JvmDowncallEngine.callStructArgWGPUStringView(wgpuTextureViewSetLabel_ADDR, textureView?.handler?.rawValue ?: 0L, label.handler.rawValue)
    return
}

private val wgpuTextureViewAddRef_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewAddRef") }
actual fun wgpuTextureViewAddRef(textureView: WGPUTextureView?): Unit {
    JvmDowncallEngine.callV1P(wgpuTextureViewAddRef_ADDR, textureView?.handler?.rawValue ?: 0L)
    return
}

private val wgpuTextureViewRelease_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureViewRelease") }
actual fun wgpuTextureViewRelease(textureView: WGPUTextureView?): Unit {
    JvmDowncallEngine.callV1P(wgpuTextureViewRelease_ADDR, textureView?.handler?.rawValue ?: 0L)
    return
}

actual interface WGPUXlibDisplayHandle {
    actual var display: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXlibDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXlibDisplayHandle = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXlibDisplayHandle) -> Unit): ArrayHolder<WGPUXlibDisplayHandle> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUXlibDisplayHandle>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUXlibDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var display: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override var screen: Int
            get() = mem.readInt(8uL)
            set(value) { mem.writeInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUXlibDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var display: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override var screen: Int
            get() = mem.readInt(8uL)
            set(value) { mem.writeInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUXcbDisplayHandle {
    actual var connection: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXcbDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXcbDisplayHandle = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXcbDisplayHandle) -> Unit): ArrayHolder<WGPUXcbDisplayHandle> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUXcbDisplayHandle>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUXcbDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var connection: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override var screen: Int
            get() = mem.readInt(8uL)
            set(value) { mem.writeInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUXcbDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var connection: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override var screen: Int
            get() = mem.readInt(8uL)
            set(value) { mem.writeInt(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUWaylandDisplayHandle {
    actual var display: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUWaylandDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUWaylandDisplayHandle = ByReference(allocator.allocateBuffer(8uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUWaylandDisplayHandle) -> Unit): ArrayHolder<WGPUWaylandDisplayHandle> {
            val buffer = allocator.allocateBuffer(8uL * size)
            val result = ArrayHolder<WGPUWaylandDisplayHandle>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 8L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUWaylandDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 8uL) }
        override var display: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUWaylandDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 8uL) }
        override var display: NativeAddress?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 0uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUNativeDisplayHandle {
    actual var type: WGPUNativeDisplayHandleType
    actual val xlib: WGPUXlibDisplayHandle?
    actual fun setXlib(value: WGPUXlibDisplayHandle)
    actual val xcb: WGPUXcbDisplayHandle?
    actual fun setXcb(value: WGPUXcbDisplayHandle)
    actual val wayland: WGPUWaylandDisplayHandle?
    actual fun setWayland(value: WGPUWaylandDisplayHandle)
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUNativeDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeDisplayHandle = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeDisplayHandle) -> Unit): ArrayHolder<WGPUNativeDisplayHandle> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUNativeDisplayHandle>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUNativeDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var type: WGPUNativeDisplayHandleType
            get() = mem.readUInt(0uL) as WGPUNativeDisplayHandleType
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override val xlib: WGPUXlibDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Xlib) null else WGPUXlibDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xlib
            val bytes = ByteArray(16)
            MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
            mem.writeBytes(bytes, 0u, 8uL, 16uL)
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Xcb) null else WGPUXcbDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xcb
            val bytes = ByteArray(16)
            MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
            mem.writeBytes(bytes, 0u, 8uL, 16uL)
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Wayland) null else WGPUWaylandDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Wayland
            val bytes = ByteArray(8)
            MemoryBuffer(value.handler, 8uL).readBytes(bytes, 0u, 0uL, 8uL)
            mem.writeBytes(bytes, 0u, 8uL, 8uL)
        }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUNativeDisplayHandle {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var type: WGPUNativeDisplayHandleType
            get() = mem.readUInt(0uL) as WGPUNativeDisplayHandleType
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override val xlib: WGPUXlibDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Xlib) null else WGPUXlibDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xlib
            val bytes = ByteArray(16)
            MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
            mem.writeBytes(bytes, 0u, 8uL, 16uL)
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Xcb) null else WGPUXcbDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Xcb
            val bytes = ByteArray(16)
            MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
            mem.writeBytes(bytes, 0u, 8uL, 16uL)
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() = if (type != WGPUNativeDisplayHandleType_Wayland) null else WGPUWaylandDisplayHandle.ByValue(NativeAddress(handle.rawValue + 8L))
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            type = WGPUNativeDisplayHandleType_Wayland
            val bytes = ByteArray(8)
            MemoryBuffer(value.handler, 8uL).readBytes(bytes, 0u, 0uL, 8uL)
            mem.writeBytes(bytes, 0u, 8uL, 8uL)
        }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUInstanceExtras {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceExtras = ByReference(allocator.allocateBuffer(112uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceExtras) -> Unit): ArrayHolder<WGPUInstanceExtras> {
            val buffer = allocator.allocateBuffer(112uL * size)
            val result = ArrayHolder<WGPUInstanceExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 112L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 112uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var backends: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var flags: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = mem.readUInt(32uL) as WGPUDx12Compiler
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = mem.readUInt(36uL) as WGPUGles3MinorVersion
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = mem.readUInt(40uL) as WGPUGLFenceBehaviour
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var dxcPath: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 48L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 48uL, 16uL)
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = mem.readUInt(64uL) as WGPUDxcMaxShaderModel
            set(value) { mem.writeUInt(value.toUInt(), 64uL) }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = mem.readUInt(68uL) as WGPUDx12SwapchainKind
            set(value) { mem.writeUInt(value.toUInt(), 68uL) }
        override var budgetForDeviceCreation: NativeAddress?
            get() = mem.readPointer(72uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 72uL) }
        override var budgetForDeviceLoss: NativeAddress?
            get() = mem.readPointer(80uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 80uL) }
        override var displayHandle: WGPUNativeDisplayHandle
            get() = WGPUNativeDisplayHandle.ByValue(NativeAddress(handle.rawValue + 88L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 88uL, 24uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 112uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var backends: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var flags: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = mem.readUInt(32uL) as WGPUDx12Compiler
            set(value) { mem.writeUInt(value.toUInt(), 32uL) }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = mem.readUInt(36uL) as WGPUGles3MinorVersion
            set(value) { mem.writeUInt(value.toUInt(), 36uL) }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = mem.readUInt(40uL) as WGPUGLFenceBehaviour
            set(value) { mem.writeUInt(value.toUInt(), 40uL) }
        override var dxcPath: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 48L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 48uL, 16uL)
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = mem.readUInt(64uL) as WGPUDxcMaxShaderModel
            set(value) { mem.writeUInt(value.toUInt(), 64uL) }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = mem.readUInt(68uL) as WGPUDx12SwapchainKind
            set(value) { mem.writeUInt(value.toUInt(), 68uL) }
        override var budgetForDeviceCreation: NativeAddress?
            get() = mem.readPointer(72uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 72uL) }
        override var budgetForDeviceLoss: NativeAddress?
            get() = mem.readPointer(80uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 80uL) }
        override var displayHandle: WGPUNativeDisplayHandle
            get() = WGPUNativeDisplayHandle.ByValue(NativeAddress(handle.rawValue + 88L))
            set(value) {
                val bytes = ByteArray(24)
                MemoryBuffer(value.handler, 24uL).readBytes(bytes, 0u, 0uL, 24uL)
                mem.writeBytes(bytes, 0u, 88uL, 24uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUDeviceExtras {
    actual var chain: WGPUChainedStruct
    actual var tracePath: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDeviceExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceExtras = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceExtras) -> Unit): ArrayHolder<WGPUDeviceExtras> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUDeviceExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var tracePath: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUDeviceExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var tracePath: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUNativeLimits {
    actual var chain: WGPUChainedStruct
    actual var maxNonSamplerBindings: UInt
    actual var maxBindingArrayElementsPerShaderStage: UInt
    actual var maxBindingArraySamplerElementsPerShaderStage: UInt
    actual var maxMultiviewViewCount: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUNativeLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeLimits = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeLimits) -> Unit): ArrayHolder<WGPUNativeLimits> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUNativeLimits>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUNativeLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxNonSamplerBindings: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxMultiviewViewCount: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUNativeLimits {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var maxNonSamplerBindings: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = mem.readUInt(24uL)
            set(value) { mem.writeUInt(value, 24uL) }
        override var maxMultiviewViewCount: UInt
            get() = mem.readUInt(28uL)
            set(value) { mem.writeUInt(value, 28uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderDefine {
    actual var name: WGPUStringView
    actual var value: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderDefine = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderDefine = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderDefine) -> Unit): ArrayHolder<WGPUShaderDefine> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUShaderDefine>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderDefine {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var name: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var value: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderDefine {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var name: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var value: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 16L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 16uL, 16uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderSourceGLSL {
    actual var chain: WGPUChainedStruct
    actual var stage: ULong
    actual var code: WGPUStringView
    actual var defineCount: UInt
    actual var defines: WGPUShaderDefine?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceGLSL = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceGLSL = ByReference(allocator.allocateBuffer(56uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceGLSL) -> Unit): ArrayHolder<WGPUShaderSourceGLSL> {
            val buffer = allocator.allocateBuffer(56uL * size)
            val result = ArrayHolder<WGPUShaderSourceGLSL>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 56L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceGLSL {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var stage: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var code: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 24L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 24uL, 16uL)
            }
        override var defineCount: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var defines: WGPUShaderDefine?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderDefine(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderSourceGLSL {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 56uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var stage: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var code: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 24L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 24uL, 16uL)
            }
        override var defineCount: UInt
            get() = mem.readUInt(40uL)
            set(value) { mem.writeUInt(value, 40uL) }
        override var defines: WGPUShaderDefine?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }?.let { WGPUShaderDefine(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 48uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUShaderModuleDescriptorSpirV {
    actual var label: WGPUStringView
    actual var sourceSize: UInt
    actual var source: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptorSpirV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptorSpirV = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptorSpirV) -> Unit): ArrayHolder<WGPUShaderModuleDescriptorSpirV> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUShaderModuleDescriptorSpirV>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderModuleDescriptorSpirV {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var sourceSize: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var source: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUShaderModuleDescriptorSpirV {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var label: WGPUStringView
            get() = WGPUStringView.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var sourceSize: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override var source: NativeAddress?
            get() = mem.readPointer(24uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPURegistryReport {
    actual var numAllocated: ULong
    actual var numKeptFromUser: ULong
    actual var numReleasedFromUser: ULong
    actual var elementSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURegistryReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURegistryReport = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURegistryReport) -> Unit): ArrayHolder<WGPURegistryReport> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPURegistryReport>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPURegistryReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var numAllocated: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var numKeptFromUser: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var numReleasedFromUser: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var elementSize: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPURegistryReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var numAllocated: ULong
            get() = mem.readULong(0uL)
            set(value) { mem.writeULong(value, 0uL) }
        override var numKeptFromUser: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override var numReleasedFromUser: ULong
            get() = mem.readULong(16uL)
            set(value) { mem.writeULong(value, 16uL) }
        override var elementSize: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUHubReport {
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
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUHubReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUHubReport = ByReference(allocator.allocateBuffer(544uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUHubReport) -> Unit): ArrayHolder<WGPUHubReport> {
            val buffer = allocator.allocateBuffer(544uL * size)
            val result = ArrayHolder<WGPUHubReport>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 544L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUHubReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 544uL) }
        override var adapters: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 0uL, 32uL)
            }
        override var devices: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 32uL, 32uL)
            }
        override var queues: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 64L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 64uL, 32uL)
            }
        override var pipelineLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 96uL, 32uL)
            }
        override var shaderModules: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 128L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 128uL, 32uL)
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 160L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 160uL, 32uL)
            }
        override var bindGroups: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 192L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 192uL, 32uL)
            }
        override var commandBuffers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 224L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 224uL, 32uL)
            }
        override var renderBundles: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 256L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 256uL, 32uL)
            }
        override var renderPipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 288L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 288uL, 32uL)
            }
        override var computePipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 320L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 320uL, 32uL)
            }
        override var pipelineCaches: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 352L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 352uL, 32uL)
            }
        override var querySets: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 384L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 384uL, 32uL)
            }
        override var buffers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 416L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 416uL, 32uL)
            }
        override var textures: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 448L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 448uL, 32uL)
            }
        override var textureViews: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 480L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 480uL, 32uL)
            }
        override var samplers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 512L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 512uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUHubReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 544uL) }
        override var adapters: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 0uL, 32uL)
            }
        override var devices: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 32uL, 32uL)
            }
        override var queues: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 64L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 64uL, 32uL)
            }
        override var pipelineLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 96L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 96uL, 32uL)
            }
        override var shaderModules: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 128L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 128uL, 32uL)
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 160L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 160uL, 32uL)
            }
        override var bindGroups: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 192L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 192uL, 32uL)
            }
        override var commandBuffers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 224L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 224uL, 32uL)
            }
        override var renderBundles: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 256L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 256uL, 32uL)
            }
        override var renderPipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 288L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 288uL, 32uL)
            }
        override var computePipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 320L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 320uL, 32uL)
            }
        override var pipelineCaches: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 352L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 352uL, 32uL)
            }
        override var querySets: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 384L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 384uL, 32uL)
            }
        override var buffers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 416L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 416uL, 32uL)
            }
        override var textures: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 448L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 448uL, 32uL)
            }
        override var textureViews: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 480L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 480uL, 32uL)
            }
        override var samplers: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 512L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 512uL, 32uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUGlobalReport {
    actual var surfaces: WGPURegistryReport
    actual var hub: WGPUHubReport
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUGlobalReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUGlobalReport = ByReference(allocator.allocateBuffer(576uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUGlobalReport) -> Unit): ArrayHolder<WGPUGlobalReport> {
            val buffer = allocator.allocateBuffer(576uL * size)
            val result = ArrayHolder<WGPUGlobalReport>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 576L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUGlobalReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 576uL) }
        override var surfaces: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 0uL, 32uL)
            }
        override var hub: WGPUHubReport
            get() = WGPUHubReport.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(544)
                MemoryBuffer(value.handler, 544uL).readBytes(bytes, 0u, 0uL, 544uL)
                mem.writeBytes(bytes, 0u, 32uL, 544uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUGlobalReport {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 576uL) }
        override var surfaces: WGPURegistryReport
            get() = WGPURegistryReport.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(32)
                MemoryBuffer(value.handler, 32uL).readBytes(bytes, 0u, 0uL, 32uL)
                mem.writeBytes(bytes, 0u, 0uL, 32uL)
            }
        override var hub: WGPUHubReport
            get() = WGPUHubReport.ByValue(NativeAddress(handle.rawValue + 32L))
            set(value) {
                val bytes = ByteArray(544)
                MemoryBuffer(value.handler, 544uL).readBytes(bytes, 0u, 0uL, 544uL)
                mem.writeBytes(bytes, 0u, 32uL, 544uL)
            }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUInstanceEnumerateAdapterOptions {
    actual var nextInChain: WGPUChainedStruct?
    actual var backends: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceEnumerateAdapterOptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceEnumerateAdapterOptions = ByReference(allocator.allocateBuffer(16uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceEnumerateAdapterOptions) -> Unit): ArrayHolder<WGPUInstanceEnumerateAdapterOptions> {
            val buffer = allocator.allocateBuffer(16uL * size)
            val result = ArrayHolder<WGPUInstanceEnumerateAdapterOptions>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 16L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceEnumerateAdapterOptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var backends: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUInstanceEnumerateAdapterOptions {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 16uL) }
        override var nextInChain: WGPUChainedStruct?
            get() = mem.readPointer(0uL).takeIf { it.rawValue != 0L }?.let { WGPUChainedStruct(it) }
            set(value) { mem.writePointer(value?.handler ?: NativeAddress(0L), 0uL) }
        override var backends: ULong
            get() = mem.readULong(8uL)
            set(value) { mem.writeULong(value, 8uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupEntryExtras {
    actual var chain: WGPUChainedStruct
    actual var buffers: NativeAddress?
    actual var bufferCount: ULong
    actual var samplers: NativeAddress?
    actual var samplerCount: ULong
    actual var textureViews: NativeAddress?
    actual var textureViewCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntryExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntryExtras = ByReference(allocator.allocateBuffer(64uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupEntryExtras> {
            val buffer = allocator.allocateBuffer(64uL * size)
            val result = ArrayHolder<WGPUBindGroupEntryExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 64L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupEntryExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var buffers: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var bufferCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var samplers: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var samplerCount: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var textureViews: NativeAddress?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 48uL) }
        override var textureViewCount: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupEntryExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 64uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var buffers: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var bufferCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override var samplers: NativeAddress?
            get() = mem.readPointer(32uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 32uL) }
        override var samplerCount: ULong
            get() = mem.readULong(40uL)
            set(value) { mem.writeULong(value, 40uL) }
        override var textureViews: NativeAddress?
            get() = mem.readPointer(48uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 48uL) }
        override var textureViewCount: ULong
            get() = mem.readULong(56uL)
            set(value) { mem.writeULong(value, 56uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUBindGroupLayoutEntryExtras {
    actual var chain: WGPUChainedStruct
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntryExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntryExtras = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntryExtras> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUBindGroupLayoutEntryExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutEntryExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var count: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUBindGroupLayoutEntryExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var count: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUQuerySetDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var pipelineStatistics: NativeAddress?
    actual var pipelineStatisticCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptorExtras = ByReference(allocator.allocateBuffer(32uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptorExtras) -> Unit): ArrayHolder<WGPUQuerySetDescriptorExtras> {
            val buffer = allocator.allocateBuffer(32uL * size)
            val result = ArrayHolder<WGPUQuerySetDescriptorExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 32L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUQuerySetDescriptorExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var pipelineStatistics: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var pipelineStatisticCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUQuerySetDescriptorExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 32uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var pipelineStatistics: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override var pipelineStatisticCount: ULong
            get() = mem.readULong(24uL)
            set(value) { mem.writeULong(value, 24uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceConfigurationExtras {
    actual var chain: WGPUChainedStruct
    actual var desiredMaximumFrameLatency: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfigurationExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfigurationExtras = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfigurationExtras) -> Unit): ArrayHolder<WGPUSurfaceConfigurationExtras> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceConfigurationExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceConfigurationExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var desiredMaximumFrameLatency: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceConfigurationExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var desiredMaximumFrameLatency: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSurfaceSourceSwapChainPanel {
    actual var chain: WGPUChainedStruct
    actual var panelNative: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceSwapChainPanel = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceSwapChainPanel = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceSwapChainPanel) -> Unit): ArrayHolder<WGPUSurfaceSourceSwapChainPanel> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSurfaceSourceSwapChainPanel>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceSwapChainPanel {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var panelNative: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSurfaceSourceSwapChainPanel {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var panelNative: NativeAddress?
            get() = mem.readPointer(16uL).takeIf { it.rawValue != 0L }
            set(value) { mem.writePointer(value ?: NativeAddress(0L), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUPrimitiveStateExtras {
    actual var chain: WGPUChainedStruct
    actual var polygonMode: WGPUPolygonMode
    actual var conservative: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveStateExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveStateExtras = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveStateExtras) -> Unit): ArrayHolder<WGPUPrimitiveStateExtras> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUPrimitiveStateExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUPrimitiveStateExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var polygonMode: WGPUPolygonMode
            get() = mem.readUInt(16uL) as WGPUPolygonMode
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var conservative: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUPrimitiveStateExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var polygonMode: WGPUPolygonMode
            get() = mem.readUInt(16uL) as WGPUPolygonMode
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override var conservative: UInt
            get() = mem.readUInt(20uL)
            set(value) { mem.writeUInt(value, 20uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUImageSubresourceRange {
    actual var aspect: WGPUTextureAspect
    actual var baseMipLevel: UInt
    actual var mipLevelCount: UInt
    actual var baseArrayLayer: UInt
    actual var arrayLayerCount: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUImageSubresourceRange = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUImageSubresourceRange = ByReference(allocator.allocateBuffer(20uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUImageSubresourceRange) -> Unit): ArrayHolder<WGPUImageSubresourceRange> {
            val buffer = allocator.allocateBuffer(20uL * size)
            val result = ArrayHolder<WGPUImageSubresourceRange>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 20L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUImageSubresourceRange {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 20uL) }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(0uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var baseMipLevel: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var baseArrayLayer: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var arrayLayerCount: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUImageSubresourceRange {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 20uL) }
        override var aspect: WGPUTextureAspect
            get() = mem.readUInt(0uL) as WGPUTextureAspect
            set(value) { mem.writeUInt(value.toUInt(), 0uL) }
        override var baseMipLevel: UInt
            get() = mem.readUInt(4uL)
            set(value) { mem.writeUInt(value, 4uL) }
        override var mipLevelCount: UInt
            get() = mem.readUInt(8uL)
            set(value) { mem.writeUInt(value, 8uL) }
        override var baseArrayLayer: UInt
            get() = mem.readUInt(12uL)
            set(value) { mem.writeUInt(value, 12uL) }
        override var arrayLayerCount: UInt
            get() = mem.readUInt(16uL)
            set(value) { mem.writeUInt(value, 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

actual interface WGPUSamplerDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var samplerBorderColor: WGPUSamplerBorderColor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptorExtras = ByReference(allocator.allocateBuffer(24uL).handler)
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptorExtras) -> Unit): ArrayHolder<WGPUSamplerDescriptorExtras> {
            val buffer = allocator.allocateBuffer(24uL * size)
            val result = ArrayHolder<WGPUSamplerDescriptorExtras>(buffer.handler)
            repeat(size.toInt()) { index ->
                provider(index.toUInt(), ByValue(NativeAddress(buffer.handler.rawValue + index.toLong() * 24L)))
            }
            return result
        }
    }
    
    class ByReference(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerDescriptorExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = mem.readUInt(16uL) as WGPUSamplerBorderColor
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
    
    class ByValue(val handle: NativeAddress = NativeAddress(0L)) : WGPUSamplerDescriptorExtras {
        private val mem: MemoryBuffer by lazy { MemoryBuffer(handle, 24uL) }
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByValue(NativeAddress(handle.rawValue + 0L))
            set(value) {
                val bytes = ByteArray(16)
                MemoryBuffer(value.handler, 16uL).readBytes(bytes, 0u, 0uL, 16uL)
                mem.writeBytes(bytes, 0u, 0uL, 16uL)
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = mem.readUInt(16uL) as WGPUSamplerBorderColor
            set(value) { mem.writeUInt(value.toUInt(), 16uL) }
        override val handler: NativeAddress
            get() = handle
    }
}

private val wgpuGenerateReport_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuGenerateReport") }
actual fun wgpuGenerateReport(instance: WGPUInstance?, report: WGPUGlobalReport?): Unit {
    JvmDowncallEngine.callV2PP(wgpuGenerateReport_ADDR, instance?.handler?.rawValue ?: 0L, report?.handler?.rawValue ?: 0L)
    return
}

private val wgpuInstanceEnumerateAdapters_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuInstanceEnumerateAdapters") }
actual fun wgpuInstanceEnumerateAdapters(instance: WGPUInstance?, options: WGPUInstanceEnumerateAdapterOptions?, adapters: NativeAddress?): ULong {
    return JvmDowncallEngine.callL3PPP(wgpuInstanceEnumerateAdapters_ADDR, instance?.handler?.rawValue ?: 0L, options?.handler?.rawValue ?: 0L, adapters?.rawValue ?: 0L).toULong()
}

private val wgpuQueueSubmitForIndex_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueSubmitForIndex") }
actual fun wgpuQueueSubmitForIndex(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): ULong {
    return JvmDowncallEngine.callL3PLP(wgpuQueueSubmitForIndex_ADDR, queue?.handler?.rawValue ?: 0L, commandCount.toLong(), commands?.rawValue ?: 0L).toULong()
}

private val wgpuQueueGetTimestampPeriod_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueGetTimestampPeriod") }
actual fun wgpuQueueGetTimestampPeriod(queue: WGPUQueue?): Float {
    return JvmDowncallEngine.callF1P(wgpuQueueGetTimestampPeriod_ADDR, queue?.handler?.rawValue ?: 0L)
}

private val wgpuDevicePoll_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDevicePoll") }
actual fun wgpuDevicePoll(device: WGPUDevice?, wait: UInt, submissionIndex: NativeAddress?): UInt {
    return JvmDowncallEngine.callI3PIP(wgpuDevicePoll_ADDR, device?.handler?.rawValue ?: 0L, wait.toInt(), submissionIndex?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuDeviceCreateShaderModuleSpirV_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModuleSpirV") }
actual fun wgpuDeviceCreateShaderModuleSpirV(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptorSpirV?): WGPUShaderModule? {
    return JvmDowncallEngine.callP2PP(wgpuDeviceCreateShaderModuleSpirV_ADDR, device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUShaderModule)
}

private val wgpuSetLogCallback_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSetLogCallback") }
actual fun wgpuSetLogCallback(callback: NativeAddress?, userdata: NativeAddress?): Unit {
    JvmDowncallEngine.callV2PP(wgpuSetLogCallback_ADDR, callback?.rawValue ?: 0L, userdata?.rawValue ?: 0L)
    return
}

private val wgpuSetLogLevel_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuSetLogLevel") }
actual fun wgpuSetLogLevel(level: WGPULogLevel): Unit {
    JvmDowncallEngine.callV1I(wgpuSetLogLevel_ADDR, level.toInt())
    return
}

private val wgpuGetVersion_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuGetVersion") }
actual fun wgpuGetVersion(): UInt {
    return JvmDowncallEngine.callI0(wgpuGetVersion_ADDR).toInt().toUInt()
}

private val wgpuDeviceGetNativeMetalDevice_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceGetNativeMetalDevice") }
actual fun wgpuDeviceGetNativeMetalDevice(device: WGPUDevice?): NativeAddress? {
    return JvmDowncallEngine.callP1P(wgpuDeviceGetNativeMetalDevice_ADDR, device?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuQueueGetNativeMetalCommandQueue_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuQueueGetNativeMetalCommandQueue") }
actual fun wgpuQueueGetNativeMetalCommandQueue(queue: WGPUQueue?): NativeAddress? {
    return JvmDowncallEngine.callP1P(wgpuQueueGetNativeMetalCommandQueue_ADDR, queue?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuTextureGetNativeMetalTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuTextureGetNativeMetalTexture") }
actual fun wgpuTextureGetNativeMetalTexture(texture: WGPUTexture?): NativeAddress? {
    return JvmDowncallEngine.callP1P(wgpuTextureGetNativeMetalTexture_ADDR, texture?.handler?.rawValue ?: 0L).takeIf { it != 0L }?.let(::NativeAddress)
}

private val wgpuRenderPassEncoderMultiDrawIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndirect") }
actual fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    JvmDowncallEngine.callV4PPLI(wgpuRenderPassEncoderMultiDrawIndirect_ADDR, encoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, offset.toLong(), count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndexedIndirect_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndexedIndirect") }
actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    JvmDowncallEngine.callV4PPLI(wgpuRenderPassEncoderMultiDrawIndexedIndirect_ADDR, encoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, offset.toLong(), count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndirectCount_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndirectCount") }
actual fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    JvmDowncallEngine.callV6PPLPLI(wgpuRenderPassEncoderMultiDrawIndirectCount_ADDR, encoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, offset.toLong(), count_buffer?.handler?.rawValue ?: 0L, count_buffer_offset.toLong(), max_count.toInt())
    return
}

private val wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderMultiDrawIndexedIndirectCount") }
actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    JvmDowncallEngine.callV6PPLPLI(wgpuRenderPassEncoderMultiDrawIndexedIndirectCount_ADDR, encoder?.handler?.rawValue ?: 0L, buffer?.handler?.rawValue ?: 0L, offset.toLong(), count_buffer?.handler?.rawValue ?: 0L, count_buffer_offset.toLong(), max_count.toInt())
    return
}

private val wgpuComputePassEncoderBeginPipelineStatisticsQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderBeginPipelineStatisticsQuery") }
actual fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV3PPI(wgpuComputePassEncoderBeginPipelineStatisticsQuery_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuComputePassEncoderEndPipelineStatisticsQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderEndPipelineStatisticsQuery") }
actual fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuComputePassEncoderEndPipelineStatisticsQuery_ADDR, computePassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuRenderPassEncoderBeginPipelineStatisticsQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderBeginPipelineStatisticsQuery") }
actual fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV3PPI(wgpuRenderPassEncoderBeginPipelineStatisticsQuery_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderEndPipelineStatisticsQuery_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderEndPipelineStatisticsQuery") }
actual fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    JvmDowncallEngine.callV1P(wgpuRenderPassEncoderEndPipelineStatisticsQuery_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L)
    return
}

private val wgpuComputePassEncoderWriteTimestamp_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuComputePassEncoderWriteTimestamp") }
actual fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV3PPI(wgpuComputePassEncoderWriteTimestamp_ADDR, computePassEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuRenderPassEncoderWriteTimestamp_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuRenderPassEncoderWriteTimestamp") }
actual fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    JvmDowncallEngine.callV3PPI(wgpuRenderPassEncoderWriteTimestamp_ADDR, renderPassEncoder?.handler?.rawValue ?: 0L, querySet?.handler?.rawValue ?: 0L, queryIndex.toInt())
    return
}

private val wgpuDeviceStartGraphicsDebuggerCapture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceStartGraphicsDebuggerCapture") }
actual fun wgpuDeviceStartGraphicsDebuggerCapture(device: WGPUDevice?): UInt {
    return JvmDowncallEngine.callI1P(wgpuDeviceStartGraphicsDebuggerCapture_ADDR, device?.handler?.rawValue ?: 0L).toInt().toUInt()
}

private val wgpuDeviceStopGraphicsDebuggerCapture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceStopGraphicsDebuggerCapture") }
actual fun wgpuDeviceStopGraphicsDebuggerCapture(device: WGPUDevice?): Unit {
    JvmDowncallEngine.callV1P(wgpuDeviceStopGraphicsDebuggerCapture_ADDR, device?.handler?.rawValue ?: 0L)
    return
}

private val wgpuCommandEncoderClearTexture_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuCommandEncoderClearTexture") }
actual fun wgpuCommandEncoderClearTexture(commandEncoder: WGPUCommandEncoder?, texture: WGPUTexture?, range: WGPUImageSubresourceRange?): Unit {
    JvmDowncallEngine.callGeneric(wgpuCommandEncoderClearTexture_ADDR, JvmDowncallEngine.FunctionShape(result = JvmDowncallEngine.AbiType.Void, arguments = listOf(JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.Pointer)), commandEncoder?.handler?.rawValue ?: 0L, texture?.handler?.rawValue ?: 0L, range?.handler?.rawValue ?: 0L)
    return
}

private val wgpuDeviceCreateShaderModuleTrusted_ADDR: Long by lazy { KextractNativeBootstrap.resolve("wgpuDeviceCreateShaderModuleTrusted") }
actual fun wgpuDeviceCreateShaderModuleTrusted(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?, runtimeChecks: ULong): WGPUShaderModule? {
    val _kffiGenericResult = JvmDowncallEngine.callGeneric(wgpuDeviceCreateShaderModuleTrusted_ADDR, JvmDowncallEngine.FunctionShape(result = JvmDowncallEngine.AbiType.Pointer, arguments = listOf(JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.Pointer, JvmDowncallEngine.AbiType.I64)), device?.handler?.rawValue ?: 0L, descriptor?.handler?.rawValue ?: 0L, runtimeChecks.toLong())
    return (_kffiGenericResult as MemorySegment).address().takeIf { it != 0L }?.let(::NativeAddress)?.let(::WGPUShaderModule)
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUProcTrampoline {
    val address: NativeAddress by lazy {
        JvmUpcallEngine.allocateTrampoline(
            dispatcherClass = WGPUProcTrampoline::class.java,
            dispatchMethod = "dispatch",
            dispatchSig = "()V",
        )
    }
    
    @JvmStatic
    fun dispatch(
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUBufferMapCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUMapAsyncStatus,
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    val address: NativeAddress by lazy {
        JvmUpcallEngine.allocateTrampoline(
            dispatcherClass = WGPUCompilationInfoCallbackTrampoline::class.java,
            dispatchMethod = "dispatch",
            dispatchSig = "(IJJJ)V",
        )
    }
    
    @JvmStatic
    fun dispatch(
        status: Int,
        compilationInfo: Long,
        userdata1: Long,
        userdata2: Long,
    ) {
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCompilationInfoCallbackType,
                userdata = userdata2.takeIf { it != 0L }?.let(::NativeAddress),
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCompilationInfoRequestStatus,
                    compilationInfo.takeIf { it != 0L }?.let(::NativeAddress),
                    userdata1.takeIf { it != 0L }?.let(::NativeAddress),
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUCreateComputePipelineAsyncCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) }?.let { WGPUComputePipeline(it) },
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUCreateRenderPipelineAsyncCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) }?.let { WGPURenderPipeline(it) },
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUDeviceLostCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    device.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
                    reason.toUInt() as WGPUDeviceLostReason,
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUPopErrorScopeCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUPopErrorScopeStatus,
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUQueueWorkDoneCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUQueueWorkDoneStatus,
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPURequestAdapterCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestAdapterStatus,
                    adapter.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) }?.let { WGPUAdapter(it) },
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPURequestDeviceCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestDeviceStatus,
                    device.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) }?.let { WGPUDevice(it) },
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPUUncapturedErrorCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata2.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    device.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView(NativeAddress(message.address())),
                    userdata1.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
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
    private val descriptor: FunctionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT, JvmDowncallEngine.structLayout("WGPUStringView"), ValueLayout.ADDRESS)
    private val methodHandle: MethodHandle by lazy {
        MethodHandles.lookup().findStatic(
            WGPULogCallbackTrampoline::class.java,
            "invoke",
            descriptor.toMethodType(),
        )
    }
    val address: NativeAddress by lazy {
        NativeAddress(Linker.nativeLinker().upcallStub(methodHandle, descriptor, Arena.global()).address())
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
                userdata = userdata.takeIf { it != MemorySegment.NULL }?.let { NativeAddress(it.address()) },
            ) { callback ->
                callback.invoke(
                    level.toUInt() as WGPULogLevel,
                    WGPUStringView(NativeAddress(message.address())),
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

internal actual fun wgpuSetLogCallbackCallbackBindingPreflight(): (NativeAddress?, NativeAddress?) -> Unit {
    return { callback, userdata ->
        JvmDowncallEngine.callV2PP(wgpuSetLogCallback_ADDR, callback?.rawValue ?: 0L, userdata?.rawValue ?: 0L)
    }
}
// Layouts des structs par valeur : enregistrés au chargement du fichier
// (classe façade), donc avant tout downcall — les companions de structs
// imbriqués ne sont pas garantis initialisés à ce moment.
// Les listes de champs sont découpées en fonctions : un bloc unique dépasse
// la limite JVM de 64 Ko par méthode pour les gros en-têtes (structs opaques).
private fun __kffiJvmStructFields0_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("data", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("length", JvmDowncallEngine.FieldKind.UINT64, 8L),
)
private fun __kffiJvmRegisterStructLayout0() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUStringView",
        16L, 8L,
        __kffiJvmStructFields0_0(),
    )
}
private fun __kffiJvmStructFields1_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("next", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("sType", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout1() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUChainedStruct",
        16L, 8L,
        __kffiJvmStructFields1_0(),
    )
}
private fun __kffiJvmStructFields2_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout2() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBufferMapCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields2_0(),
    )
}
private fun __kffiJvmStructFields3_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout3() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCompilationInfoCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields3_0(),
    )
}
private fun __kffiJvmStructFields4_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout4() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCreateComputePipelineAsyncCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields4_0(),
    )
}
private fun __kffiJvmStructFields5_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout5() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCreateRenderPipelineAsyncCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields5_0(),
    )
}
private fun __kffiJvmStructFields6_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout6() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUDeviceLostCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields6_0(),
    )
}
private fun __kffiJvmStructFields7_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout7() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUPopErrorScopeCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields7_0(),
    )
}
private fun __kffiJvmStructFields8_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout8() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUQueueWorkDoneCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields8_0(),
    )
}
private fun __kffiJvmStructFields9_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout9() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURequestAdapterCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields9_0(),
    )
}
private fun __kffiJvmStructFields10_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout10() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURequestDeviceCallbackInfo",
        40L, 8L,
        __kffiJvmStructFields10_0(),
    )
}
private fun __kffiJvmStructFields11_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("callback", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("userdata1", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("userdata2", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout11() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUUncapturedErrorCallbackInfo",
        32L, 8L,
        __kffiJvmStructFields11_0(),
    )
}
private fun __kffiJvmStructFields12_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 24L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 40L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 56L),
    JvmDowncallEngine.StructField("backendType", JvmDowncallEngine.FieldKind.UINT32, 72L),
    JvmDowncallEngine.StructField("adapterType", JvmDowncallEngine.FieldKind.UINT32, 76L),
    JvmDowncallEngine.StructField("vendorID", JvmDowncallEngine.FieldKind.UINT32, 80L),
    JvmDowncallEngine.StructField("deviceID", JvmDowncallEngine.FieldKind.UINT32, 84L),
    JvmDowncallEngine.StructField("subgroupMinSize", JvmDowncallEngine.FieldKind.UINT32, 88L),
    JvmDowncallEngine.StructField("subgroupMaxSize", JvmDowncallEngine.FieldKind.UINT32, 92L),
)
private fun __kffiJvmRegisterStructLayout12() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUAdapterInfo",
        96L, 8L,
        __kffiJvmStructFields12_0(),
    )
}
private fun __kffiJvmStructFields13_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("operation", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("srcFactor", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("dstFactor", JvmDowncallEngine.FieldKind.UINT32, 8L),
)
private fun __kffiJvmRegisterStructLayout13() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBlendComponent",
        12L, 4L,
        __kffiJvmStructFields13_0(),
    )
}
private fun __kffiJvmStructFields14_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("type", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("hasDynamicOffset", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("minBindingSize", JvmDowncallEngine.FieldKind.UINT64, 16L),
)
private fun __kffiJvmRegisterStructLayout14() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBufferBindingLayout",
        24L, 8L,
        __kffiJvmStructFields14_0(),
    )
}
private fun __kffiJvmStructFields15_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("usage", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("size", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("mappedAtCreation", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout15() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBufferDescriptor",
        48L, 8L,
        __kffiJvmStructFields15_0(),
    )
}
private fun __kffiJvmStructFields16_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("r", JvmDowncallEngine.FieldKind.FLOAT64, 0L),
    JvmDowncallEngine.StructField("g", JvmDowncallEngine.FieldKind.FLOAT64, 8L),
    JvmDowncallEngine.StructField("b", JvmDowncallEngine.FieldKind.FLOAT64, 16L),
    JvmDowncallEngine.StructField("a", JvmDowncallEngine.FieldKind.FLOAT64, 24L),
)
private fun __kffiJvmRegisterStructLayout16() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUColor",
        32L, 8L,
        __kffiJvmStructFields16_0(),
    )
}
private fun __kffiJvmStructFields17_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout17() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCommandBufferDescriptor",
        24L, 8L,
        __kffiJvmStructFields17_0(),
    )
}
private fun __kffiJvmStructFields18_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout18() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCommandEncoderDescriptor",
        24L, 8L,
        __kffiJvmStructFields18_0(),
    )
}
private fun __kffiJvmStructFields19_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("maxStorageBuffersInVertexStage", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("maxStorageTexturesInVertexStage", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("maxStorageBuffersInFragmentStage", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("maxStorageTexturesInFragmentStage", JvmDowncallEngine.FieldKind.UINT32, 28L),
)
private fun __kffiJvmRegisterStructLayout19() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCompatibilityModeLimits",
        32L, 8L,
        __kffiJvmStructFields19_0(),
    )
}
private fun __kffiJvmStructFields20_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("type", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("lineNum", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("linePos", JvmDowncallEngine.FieldKind.UINT64, 40L),
    JvmDowncallEngine.StructField("offset", JvmDowncallEngine.FieldKind.UINT64, 48L),
    JvmDowncallEngine.StructField("length", JvmDowncallEngine.FieldKind.UINT64, 56L),
)
private fun __kffiJvmRegisterStructLayout20() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCompilationMessage",
        64L, 8L,
        __kffiJvmStructFields20_0(),
    )
}
private fun __kffiJvmStructFields21_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("value", JvmDowncallEngine.FieldKind.FLOAT64, 24L),
)
private fun __kffiJvmRegisterStructLayout21() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUConstantEntry",
        32L, 8L,
        __kffiJvmStructFields21_0(),
    )
}
private fun __kffiJvmStructFields22_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("width", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("height", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("depthOrArrayLayers", JvmDowncallEngine.FieldKind.UINT32, 8L),
)
private fun __kffiJvmRegisterStructLayout22() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUExtent3D",
        12L, 4L,
        __kffiJvmStructFields22_0(),
    )
}
private fun __kffiJvmStructFields23_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("externalTexture", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout23() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUExternalTextureBindingEntry",
        24L, 8L,
        __kffiJvmStructFields23_0(),
    )
}
private fun __kffiJvmStructFields24_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
)
private fun __kffiJvmRegisterStructLayout24() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUExternalTextureBindingLayout",
        16L, 8L,
        __kffiJvmStructFields24_0(),
    )
}
private fun __kffiJvmStructFields25_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("id", JvmDowncallEngine.FieldKind.UINT64, 0L),
)
private fun __kffiJvmRegisterStructLayout25() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUFuture",
        8L, 8L,
        __kffiJvmStructFields25_0(),
    )
}
private fun __kffiJvmStructFields26_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("timedWaitAnyMaxCount", JvmDowncallEngine.FieldKind.UINT64, 8L),
)
private fun __kffiJvmRegisterStructLayout26() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUInstanceLimits",
        16L, 8L,
        __kffiJvmStructFields26_0(),
    )
}
private fun __kffiJvmStructFields27_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("count", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("mask", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("alphaToCoverageEnabled", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout27() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUMultisampleState",
        24L, 8L,
        __kffiJvmStructFields27_0(),
    )
}
private fun __kffiJvmStructFields28_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("x", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("y", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("z", JvmDowncallEngine.FieldKind.UINT32, 8L),
)
private fun __kffiJvmRegisterStructLayout28() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUOrigin3D",
        12L, 4L,
        __kffiJvmStructFields28_0(),
    )
}
private fun __kffiJvmStructFields29_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("querySet", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("beginningOfPassWriteIndex", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("endOfPassWriteIndex", JvmDowncallEngine.FieldKind.UINT32, 20L),
)
private fun __kffiJvmRegisterStructLayout29() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUPassTimestampWrites",
        24L, 8L,
        __kffiJvmStructFields29_0(),
    )
}
private fun __kffiJvmStructFields30_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("bindGroupLayoutCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("bindGroupLayouts", JvmDowncallEngine.FieldKind.POINTER, 32L),
    JvmDowncallEngine.StructField("immediateSize", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout30() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUPipelineLayoutDescriptor",
        48L, 8L,
        __kffiJvmStructFields30_0(),
    )
}
private fun __kffiJvmStructFields31_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("topology", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("stripIndexFormat", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("frontFace", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("cullMode", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("unclippedDepth", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout31() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUPrimitiveState",
        32L, 8L,
        __kffiJvmStructFields31_0(),
    )
}
private fun __kffiJvmStructFields32_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("type", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("count", JvmDowncallEngine.FieldKind.UINT32, 28L),
)
private fun __kffiJvmRegisterStructLayout32() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUQuerySetDescriptor",
        32L, 8L,
        __kffiJvmStructFields32_0(),
    )
}
private fun __kffiJvmStructFields33_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout33() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUQueueDescriptor",
        24L, 8L,
        __kffiJvmStructFields33_0(),
    )
}
private fun __kffiJvmStructFields34_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout34() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderBundleDescriptor",
        24L, 8L,
        __kffiJvmStructFields34_0(),
    )
}
private fun __kffiJvmStructFields35_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("colorFormatCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("colorFormats", JvmDowncallEngine.FieldKind.POINTER, 32L),
    JvmDowncallEngine.StructField("depthStencilFormat", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("sampleCount", JvmDowncallEngine.FieldKind.UINT32, 44L),
    JvmDowncallEngine.StructField("depthReadOnly", JvmDowncallEngine.FieldKind.UINT32, 48L),
    JvmDowncallEngine.StructField("stencilReadOnly", JvmDowncallEngine.FieldKind.UINT32, 52L),
)
private fun __kffiJvmRegisterStructLayout35() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderBundleEncoderDescriptor",
        56L, 8L,
        __kffiJvmStructFields35_0(),
    )
}
private fun __kffiJvmStructFields36_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("view", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("depthLoadOp", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("depthStoreOp", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("depthClearValue", JvmDowncallEngine.FieldKind.FLOAT32, 24L),
    JvmDowncallEngine.StructField("depthReadOnly", JvmDowncallEngine.FieldKind.UINT32, 28L),
    JvmDowncallEngine.StructField("stencilLoadOp", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("stencilStoreOp", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("stencilClearValue", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("stencilReadOnly", JvmDowncallEngine.FieldKind.UINT32, 44L),
)
private fun __kffiJvmRegisterStructLayout36() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderPassDepthStencilAttachment",
        48L, 8L,
        __kffiJvmStructFields36_0(),
    )
}
private fun __kffiJvmStructFields37_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("maxDrawCount", JvmDowncallEngine.FieldKind.UINT64, 16L),
)
private fun __kffiJvmRegisterStructLayout37() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderPassMaxDrawCount",
        24L, 8L,
        __kffiJvmStructFields37_0(),
    )
}
private fun __kffiJvmStructFields38_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("xrCompatible", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout38() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURequestAdapterWebXROptions",
        24L, 8L,
        __kffiJvmStructFields38_0(),
    )
}
private fun __kffiJvmStructFields39_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("type", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout39() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSamplerBindingLayout",
        16L, 8L,
        __kffiJvmStructFields39_0(),
    )
}
private fun __kffiJvmStructFields40_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("addressModeU", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("addressModeV", JvmDowncallEngine.FieldKind.UINT32, 28L),
    JvmDowncallEngine.StructField("addressModeW", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("magFilter", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("minFilter", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("mipmapFilter", JvmDowncallEngine.FieldKind.UINT32, 44L),
    JvmDowncallEngine.StructField("lodMinClamp", JvmDowncallEngine.FieldKind.FLOAT32, 48L),
    JvmDowncallEngine.StructField("lodMaxClamp", JvmDowncallEngine.FieldKind.FLOAT32, 52L),
    JvmDowncallEngine.StructField("compare", JvmDowncallEngine.FieldKind.UINT32, 56L),
    JvmDowncallEngine.StructField("maxAnisotropy", JvmDowncallEngine.FieldKind.UINT16, 60L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 2L),
)
private fun __kffiJvmRegisterStructLayout40() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSamplerDescriptor",
        64L, 8L,
        __kffiJvmStructFields40_0(),
    )
}
private fun __kffiJvmStructFields41_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("codeSize", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("code", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout41() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderSourceSPIRV",
        32L, 8L,
        __kffiJvmStructFields41_0(),
    )
}
private fun __kffiJvmStructFields42_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
)
private fun __kffiJvmRegisterStructLayout42() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderSourceWGSL",
        32L, 8L,
        __kffiJvmStructFields42_0(),
    )
}
private fun __kffiJvmStructFields43_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("compare", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("failOp", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("depthFailOp", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("passOp", JvmDowncallEngine.FieldKind.UINT32, 12L),
)
private fun __kffiJvmRegisterStructLayout43() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUStencilFaceState",
        16L, 4L,
        __kffiJvmStructFields43_0(),
    )
}
private fun __kffiJvmStructFields44_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("access", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("viewDimension", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout44() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUStorageTextureBindingLayout",
        24L, 8L,
        __kffiJvmStructFields44_0(),
    )
}
private fun __kffiJvmStructFields45_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("featureCount", JvmDowncallEngine.FieldKind.UINT64, 0L),
    JvmDowncallEngine.StructField("features", JvmDowncallEngine.FieldKind.POINTER, 8L),
)
private fun __kffiJvmRegisterStructLayout45() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSupportedFeatures",
        16L, 8L,
        __kffiJvmStructFields45_0(),
    )
}
private fun __kffiJvmStructFields46_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("featureCount", JvmDowncallEngine.FieldKind.UINT64, 0L),
    JvmDowncallEngine.StructField("features", JvmDowncallEngine.FieldKind.POINTER, 8L),
)
private fun __kffiJvmRegisterStructLayout46() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSupportedInstanceFeatures",
        16L, 8L,
        __kffiJvmStructFields46_0(),
    )
}
private fun __kffiJvmStructFields47_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("featureCount", JvmDowncallEngine.FieldKind.UINT64, 0L),
    JvmDowncallEngine.StructField("features", JvmDowncallEngine.FieldKind.POINTER, 8L),
)
private fun __kffiJvmRegisterStructLayout47() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSupportedWGSLLanguageFeatures",
        16L, 8L,
        __kffiJvmStructFields47_0(),
    )
}
private fun __kffiJvmStructFields48_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("usages", JvmDowncallEngine.FieldKind.UINT64, 8L),
    JvmDowncallEngine.StructField("formatCount", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("formats", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("presentModeCount", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("presentModes", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("alphaModeCount", JvmDowncallEngine.FieldKind.UINT64, 48L),
    JvmDowncallEngine.StructField("alphaModes", JvmDowncallEngine.FieldKind.POINTER, 56L),
)
private fun __kffiJvmRegisterStructLayout48() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceCapabilities",
        64L, 8L,
        __kffiJvmStructFields48_0(),
    )
}
private fun __kffiJvmStructFields49_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("colorSpace", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("toneMappingMode", JvmDowncallEngine.FieldKind.UINT32, 20L),
)
private fun __kffiJvmRegisterStructLayout49() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceColorManagement",
        24L, 8L,
        __kffiJvmStructFields49_0(),
    )
}
private fun __kffiJvmStructFields50_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("device", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("usage", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("width", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("height", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("viewFormatCount", JvmDowncallEngine.FieldKind.UINT64, 40L),
    JvmDowncallEngine.StructField("viewFormats", JvmDowncallEngine.FieldKind.POINTER, 48L),
    JvmDowncallEngine.StructField("alphaMode", JvmDowncallEngine.FieldKind.UINT32, 56L),
    JvmDowncallEngine.StructField("presentMode", JvmDowncallEngine.FieldKind.UINT32, 60L),
)
private fun __kffiJvmRegisterStructLayout50() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceConfiguration",
        64L, 8L,
        __kffiJvmStructFields50_0(),
    )
}
private fun __kffiJvmStructFields51_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("window", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout51() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceAndroidNativeWindow",
        24L, 8L,
        __kffiJvmStructFields51_0(),
    )
}
private fun __kffiJvmStructFields52_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("layer", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout52() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceMetalLayer",
        24L, 8L,
        __kffiJvmStructFields52_0(),
    )
}
private fun __kffiJvmStructFields53_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("display", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("surface", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout53() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceWaylandSurface",
        32L, 8L,
        __kffiJvmStructFields53_0(),
    )
}
private fun __kffiJvmStructFields54_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("hinstance", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("hwnd", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout54() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceWindowsHWND",
        32L, 8L,
        __kffiJvmStructFields54_0(),
    )
}
private fun __kffiJvmStructFields55_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("connection", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("window", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout55() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceXCBWindow",
        32L, 8L,
        __kffiJvmStructFields55_0(),
    )
}
private fun __kffiJvmStructFields56_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("display", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("window", JvmDowncallEngine.FieldKind.UINT64, 24L),
)
private fun __kffiJvmRegisterStructLayout56() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceXlibWindow",
        32L, 8L,
        __kffiJvmStructFields56_0(),
    )
}
private fun __kffiJvmStructFields57_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("texture", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("status", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout57() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceTexture",
        24L, 8L,
        __kffiJvmStructFields57_0(),
    )
}
private fun __kffiJvmStructFields58_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("offset", JvmDowncallEngine.FieldKind.UINT64, 0L),
    JvmDowncallEngine.StructField("bytesPerRow", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("rowsPerImage", JvmDowncallEngine.FieldKind.UINT32, 12L),
)
private fun __kffiJvmRegisterStructLayout58() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTexelCopyBufferLayout",
        16L, 8L,
        __kffiJvmStructFields58_0(),
    )
}
private fun __kffiJvmStructFields59_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("sampleType", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("viewDimension", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("multisampled", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout59() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureBindingLayout",
        24L, 8L,
        __kffiJvmStructFields59_0(),
    )
}
private fun __kffiJvmStructFields60_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("textureBindingViewDimension", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout60() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureBindingViewDimension",
        24L, 8L,
        __kffiJvmStructFields60_0(),
    )
}
private fun __kffiJvmStructFields61_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("r", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("g", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("b", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("a", JvmDowncallEngine.FieldKind.UINT32, 12L),
)
private fun __kffiJvmRegisterStructLayout61() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureComponentSwizzle",
        16L, 4L,
        __kffiJvmStructFields61_0(),
    )
}
private fun __kffiJvmStructFields62_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("offset", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("shaderLocation", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout62() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUVertexAttribute",
        32L, 8L,
        __kffiJvmStructFields62_0(),
    )
}
private fun __kffiJvmStructFields63_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("binding", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("buffer", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("offset", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("size", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("sampler", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("textureView", JvmDowncallEngine.FieldKind.POINTER, 48L),
)
private fun __kffiJvmRegisterStructLayout63() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupEntry",
        56L, 8L,
        __kffiJvmStructFields63_0(),
    )
}
private fun __kffiJvmStructFields64_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("binding", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("visibility", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("bindingArraySize", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("WGPUBufferBindingLayout", JvmDowncallEngine.FieldKind.STRUCT, 32L),
    JvmDowncallEngine.StructField("WGPUSamplerBindingLayout", JvmDowncallEngine.FieldKind.STRUCT, 56L),
    JvmDowncallEngine.StructField("WGPUTextureBindingLayout", JvmDowncallEngine.FieldKind.STRUCT, 72L),
    JvmDowncallEngine.StructField("WGPUStorageTextureBindingLayout", JvmDowncallEngine.FieldKind.STRUCT, 96L),
)
private fun __kffiJvmRegisterStructLayout64() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupLayoutEntry",
        120L, 8L,
        __kffiJvmStructFields64_0(),
    )
}
private fun __kffiJvmStructFields65_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUBlendComponent", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUBlendComponent", JvmDowncallEngine.FieldKind.STRUCT, 12L),
)
private fun __kffiJvmRegisterStructLayout65() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBlendState",
        24L, 4L,
        __kffiJvmStructFields65_0(),
    )
}
private fun __kffiJvmStructFields66_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("messageCount", JvmDowncallEngine.FieldKind.UINT64, 8L),
    JvmDowncallEngine.StructField("messages", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout66() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUCompilationInfo",
        24L, 8L,
        __kffiJvmStructFields66_0(),
    )
}
private fun __kffiJvmStructFields67_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("timestampWrites", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout67() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUComputePassDescriptor",
        32L, 8L,
        __kffiJvmStructFields67_0(),
    )
}
private fun __kffiJvmStructFields68_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("module", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
    JvmDowncallEngine.StructField("constantCount", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("constants", JvmDowncallEngine.FieldKind.POINTER, 40L),
)
private fun __kffiJvmRegisterStructLayout68() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUComputeState",
        48L, 8L,
        __kffiJvmStructFields68_0(),
    )
}
private fun __kffiJvmStructFields69_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("depthWriteEnabled", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("depthCompare", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("WGPUStencilFaceState", JvmDowncallEngine.FieldKind.STRUCT, 20L),
    JvmDowncallEngine.StructField("WGPUStencilFaceState", JvmDowncallEngine.FieldKind.STRUCT, 36L),
    JvmDowncallEngine.StructField("stencilReadMask", JvmDowncallEngine.FieldKind.UINT32, 52L),
    JvmDowncallEngine.StructField("stencilWriteMask", JvmDowncallEngine.FieldKind.UINT32, 56L),
    JvmDowncallEngine.StructField("depthBias", JvmDowncallEngine.FieldKind.INT32, 60L),
    JvmDowncallEngine.StructField("depthBiasSlopeScale", JvmDowncallEngine.FieldKind.FLOAT32, 64L),
    JvmDowncallEngine.StructField("depthBiasClamp", JvmDowncallEngine.FieldKind.FLOAT32, 68L),
)
private fun __kffiJvmRegisterStructLayout69() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUDepthStencilState",
        72L, 8L,
        __kffiJvmStructFields69_0(),
    )
}
private fun __kffiJvmStructFields70_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUFuture", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("completed", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout70() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUFutureWaitInfo",
        16L, 8L,
        __kffiJvmStructFields70_0(),
    )
}
private fun __kffiJvmStructFields71_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("requiredFeatureCount", JvmDowncallEngine.FieldKind.UINT64, 8L),
    JvmDowncallEngine.StructField("requiredFeatures", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("requiredLimits", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout71() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUInstanceDescriptor",
        32L, 8L,
        __kffiJvmStructFields71_0(),
    )
}
private fun __kffiJvmStructFields72_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("maxTextureDimension1D", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("maxTextureDimension2D", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("maxTextureDimension3D", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("maxTextureArrayLayers", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("maxBindGroups", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("maxBindGroupsPlusVertexBuffers", JvmDowncallEngine.FieldKind.UINT32, 28L),
    JvmDowncallEngine.StructField("maxBindingsPerBindGroup", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("maxDynamicUniformBuffersPerPipelineLayout", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("maxDynamicStorageBuffersPerPipelineLayout", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("maxSampledTexturesPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 44L),
    JvmDowncallEngine.StructField("maxSamplersPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 48L),
    JvmDowncallEngine.StructField("maxStorageBuffersPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 52L),
    JvmDowncallEngine.StructField("maxStorageTexturesPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 56L),
    JvmDowncallEngine.StructField("maxUniformBuffersPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 60L),
    JvmDowncallEngine.StructField("maxUniformBufferBindingSize", JvmDowncallEngine.FieldKind.UINT64, 64L),
    JvmDowncallEngine.StructField("maxStorageBufferBindingSize", JvmDowncallEngine.FieldKind.UINT64, 72L),
    JvmDowncallEngine.StructField("minUniformBufferOffsetAlignment", JvmDowncallEngine.FieldKind.UINT32, 80L),
    JvmDowncallEngine.StructField("minStorageBufferOffsetAlignment", JvmDowncallEngine.FieldKind.UINT32, 84L),
    JvmDowncallEngine.StructField("maxVertexBuffers", JvmDowncallEngine.FieldKind.UINT32, 88L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("maxBufferSize", JvmDowncallEngine.FieldKind.UINT64, 96L),
    JvmDowncallEngine.StructField("maxVertexAttributes", JvmDowncallEngine.FieldKind.UINT32, 104L),
    JvmDowncallEngine.StructField("maxVertexBufferArrayStride", JvmDowncallEngine.FieldKind.UINT32, 108L),
    JvmDowncallEngine.StructField("maxInterStageShaderVariables", JvmDowncallEngine.FieldKind.UINT32, 112L),
    JvmDowncallEngine.StructField("maxColorAttachments", JvmDowncallEngine.FieldKind.UINT32, 116L),
    JvmDowncallEngine.StructField("maxColorAttachmentBytesPerSample", JvmDowncallEngine.FieldKind.UINT32, 120L),
    JvmDowncallEngine.StructField("maxComputeWorkgroupStorageSize", JvmDowncallEngine.FieldKind.UINT32, 124L),
    JvmDowncallEngine.StructField("maxComputeInvocationsPerWorkgroup", JvmDowncallEngine.FieldKind.UINT32, 128L),
    JvmDowncallEngine.StructField("maxComputeWorkgroupSizeX", JvmDowncallEngine.FieldKind.UINT32, 132L),
    JvmDowncallEngine.StructField("maxComputeWorkgroupSizeY", JvmDowncallEngine.FieldKind.UINT32, 136L),
    JvmDowncallEngine.StructField("maxComputeWorkgroupSizeZ", JvmDowncallEngine.FieldKind.UINT32, 140L),
    JvmDowncallEngine.StructField("maxComputeWorkgroupsPerDimension", JvmDowncallEngine.FieldKind.UINT32, 144L),
    JvmDowncallEngine.StructField("maxImmediateSize", JvmDowncallEngine.FieldKind.UINT32, 148L),
)
private fun __kffiJvmRegisterStructLayout72() {
    JvmDowncallEngine.registerStructLayout(
        "WGPULimits",
        152L, 8L,
        __kffiJvmStructFields72_0(),
    )
}
private fun __kffiJvmStructFields73_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("view", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("depthSlice", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("resolveTarget", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("loadOp", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("storeOp", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("WGPUColor", JvmDowncallEngine.FieldKind.STRUCT, 40L),
)
private fun __kffiJvmRegisterStructLayout73() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderPassColorAttachment",
        72L, 8L,
        __kffiJvmStructFields73_0(),
    )
}
private fun __kffiJvmStructFields74_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("featureLevel", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("powerPreference", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("forceFallbackAdapter", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("backendType", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("compatibleSurface", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout74() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURequestAdapterOptions",
        32L, 8L,
        __kffiJvmStructFields74_0(),
    )
}
private fun __kffiJvmStructFields75_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout75() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderModuleDescriptor",
        24L, 8L,
        __kffiJvmStructFields75_0(),
    )
}
private fun __kffiJvmStructFields76_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
)
private fun __kffiJvmRegisterStructLayout76() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceDescriptor",
        24L, 8L,
        __kffiJvmStructFields76_0(),
    )
}
private fun __kffiJvmStructFields77_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUTexelCopyBufferLayout", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("buffer", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout77() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTexelCopyBufferInfo",
        24L, 8L,
        __kffiJvmStructFields77_0(),
    )
}
private fun __kffiJvmStructFields78_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("texture", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("mipLevel", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("WGPUOrigin3D", JvmDowncallEngine.FieldKind.STRUCT, 12L),
    JvmDowncallEngine.StructField("aspect", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout78() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTexelCopyTextureInfo",
        32L, 8L,
        __kffiJvmStructFields78_0(),
    )
}
private fun __kffiJvmStructFields79_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUTextureComponentSwizzle", JvmDowncallEngine.FieldKind.STRUCT, 16L),
)
private fun __kffiJvmRegisterStructLayout79() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureComponentSwizzleDescriptor",
        32L, 8L,
        __kffiJvmStructFields79_0(),
    )
}
private fun __kffiJvmStructFields80_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("usage", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("dimension", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("WGPUExtent3D", JvmDowncallEngine.FieldKind.STRUCT, 36L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 48L),
    JvmDowncallEngine.StructField("mipLevelCount", JvmDowncallEngine.FieldKind.UINT32, 52L),
    JvmDowncallEngine.StructField("sampleCount", JvmDowncallEngine.FieldKind.UINT32, 56L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("viewFormatCount", JvmDowncallEngine.FieldKind.UINT64, 64L),
    JvmDowncallEngine.StructField("viewFormats", JvmDowncallEngine.FieldKind.POINTER, 72L),
)
private fun __kffiJvmRegisterStructLayout80() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureDescriptor",
        80L, 8L,
        __kffiJvmStructFields80_0(),
    )
}
private fun __kffiJvmStructFields81_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("stepMode", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("arrayStride", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("attributeCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("attributes", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout81() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUVertexBufferLayout",
        40L, 8L,
        __kffiJvmStructFields81_0(),
    )
}
private fun __kffiJvmStructFields82_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("layout", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("entryCount", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("entries", JvmDowncallEngine.FieldKind.POINTER, 40L),
)
private fun __kffiJvmRegisterStructLayout82() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupDescriptor",
        48L, 8L,
        __kffiJvmStructFields82_0(),
    )
}
private fun __kffiJvmStructFields83_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("entryCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("entries", JvmDowncallEngine.FieldKind.POINTER, 32L),
)
private fun __kffiJvmRegisterStructLayout83() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupLayoutDescriptor",
        40L, 8L,
        __kffiJvmStructFields83_0(),
    )
}
private fun __kffiJvmStructFields84_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("blend", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("writeMask", JvmDowncallEngine.FieldKind.UINT64, 24L),
)
private fun __kffiJvmRegisterStructLayout84() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUColorTargetState",
        32L, 8L,
        __kffiJvmStructFields84_0(),
    )
}
private fun __kffiJvmStructFields85_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("layout", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("WGPUComputeState", JvmDowncallEngine.FieldKind.STRUCT, 32L),
)
private fun __kffiJvmRegisterStructLayout85() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUComputePipelineDescriptor",
        80L, 8L,
        __kffiJvmStructFields85_0(),
    )
}
private fun __kffiJvmStructFields86_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("requiredFeatureCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("requiredFeatures", JvmDowncallEngine.FieldKind.POINTER, 32L),
    JvmDowncallEngine.StructField("requiredLimits", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("WGPUQueueDescriptor", JvmDowncallEngine.FieldKind.STRUCT, 48L),
    JvmDowncallEngine.StructField("WGPUDeviceLostCallbackInfo", JvmDowncallEngine.FieldKind.STRUCT, 72L),
    JvmDowncallEngine.StructField("WGPUUncapturedErrorCallbackInfo", JvmDowncallEngine.FieldKind.STRUCT, 112L),
)
private fun __kffiJvmRegisterStructLayout86() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUDeviceDescriptor",
        144L, 8L,
        __kffiJvmStructFields86_0(),
    )
}
private fun __kffiJvmStructFields87_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("colorAttachmentCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("colorAttachments", JvmDowncallEngine.FieldKind.POINTER, 32L),
    JvmDowncallEngine.StructField("depthStencilAttachment", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("occlusionQuerySet", JvmDowncallEngine.FieldKind.POINTER, 48L),
    JvmDowncallEngine.StructField("timestampWrites", JvmDowncallEngine.FieldKind.POINTER, 56L),
)
private fun __kffiJvmRegisterStructLayout87() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderPassDescriptor",
        64L, 8L,
        __kffiJvmStructFields87_0(),
    )
}
private fun __kffiJvmStructFields88_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("format", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("dimension", JvmDowncallEngine.FieldKind.UINT32, 28L),
    JvmDowncallEngine.StructField("baseMipLevel", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("mipLevelCount", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("baseArrayLayer", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("arrayLayerCount", JvmDowncallEngine.FieldKind.UINT32, 44L),
    JvmDowncallEngine.StructField("aspect", JvmDowncallEngine.FieldKind.UINT32, 48L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("usage", JvmDowncallEngine.FieldKind.UINT64, 56L),
)
private fun __kffiJvmRegisterStructLayout88() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUTextureViewDescriptor",
        64L, 8L,
        __kffiJvmStructFields88_0(),
    )
}
private fun __kffiJvmStructFields89_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("module", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
    JvmDowncallEngine.StructField("constantCount", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("constants", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("bufferCount", JvmDowncallEngine.FieldKind.UINT64, 48L),
    JvmDowncallEngine.StructField("buffers", JvmDowncallEngine.FieldKind.POINTER, 56L),
)
private fun __kffiJvmRegisterStructLayout89() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUVertexState",
        64L, 8L,
        __kffiJvmStructFields89_0(),
    )
}
private fun __kffiJvmStructFields90_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("module", JvmDowncallEngine.FieldKind.POINTER, 8L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
    JvmDowncallEngine.StructField("constantCount", JvmDowncallEngine.FieldKind.UINT64, 32L),
    JvmDowncallEngine.StructField("constants", JvmDowncallEngine.FieldKind.POINTER, 40L),
    JvmDowncallEngine.StructField("targetCount", JvmDowncallEngine.FieldKind.UINT64, 48L),
    JvmDowncallEngine.StructField("targets", JvmDowncallEngine.FieldKind.POINTER, 56L),
)
private fun __kffiJvmRegisterStructLayout90() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUFragmentState",
        64L, 8L,
        __kffiJvmStructFields90_0(),
    )
}
private fun __kffiJvmStructFields91_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 8L),
    JvmDowncallEngine.StructField("layout", JvmDowncallEngine.FieldKind.POINTER, 24L),
    JvmDowncallEngine.StructField("WGPUVertexState", JvmDowncallEngine.FieldKind.STRUCT, 32L),
    JvmDowncallEngine.StructField("WGPUPrimitiveState", JvmDowncallEngine.FieldKind.STRUCT, 96L),
    JvmDowncallEngine.StructField("depthStencil", JvmDowncallEngine.FieldKind.POINTER, 128L),
    JvmDowncallEngine.StructField("WGPUMultisampleState", JvmDowncallEngine.FieldKind.STRUCT, 136L),
    JvmDowncallEngine.StructField("fragment", JvmDowncallEngine.FieldKind.POINTER, 160L),
)
private fun __kffiJvmRegisterStructLayout91() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURenderPipelineDescriptor",
        168L, 8L,
        __kffiJvmStructFields91_0(),
    )
}
private fun __kffiJvmStructFields92_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("display", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("screen", JvmDowncallEngine.FieldKind.INT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout92() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUXlibDisplayHandle",
        16L, 8L,
        __kffiJvmStructFields92_0(),
    )
}
private fun __kffiJvmStructFields93_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("connection", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("screen", JvmDowncallEngine.FieldKind.INT32, 8L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout93() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUXcbDisplayHandle",
        16L, 8L,
        __kffiJvmStructFields93_0(),
    )
}
private fun __kffiJvmStructFields94_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("display", JvmDowncallEngine.FieldKind.POINTER, 0L),
)
private fun __kffiJvmRegisterStructLayout94() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUWaylandDisplayHandle",
        8L, 8L,
        __kffiJvmStructFields94_0(),
    )
}
private fun __kffiJvmStructFields95_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("backends", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("flags", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("dx12ShaderCompiler", JvmDowncallEngine.FieldKind.UINT32, 32L),
    JvmDowncallEngine.StructField("gles3MinorVersion", JvmDowncallEngine.FieldKind.UINT32, 36L),
    JvmDowncallEngine.StructField("glFenceBehaviour", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 48L),
    JvmDowncallEngine.StructField("dxcMaxShaderModel", JvmDowncallEngine.FieldKind.UINT32, 64L),
    JvmDowncallEngine.StructField("dx12PresentationSystem", JvmDowncallEngine.FieldKind.UINT32, 68L),
    JvmDowncallEngine.StructField("budgetForDeviceCreation", JvmDowncallEngine.FieldKind.POINTER, 72L),
    JvmDowncallEngine.StructField("budgetForDeviceLoss", JvmDowncallEngine.FieldKind.POINTER, 80L),
    JvmDowncallEngine.StructField("WGPUNativeDisplayHandle", JvmDowncallEngine.FieldKind.STRUCT, 88L),
)
private fun __kffiJvmRegisterStructLayout95() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUInstanceExtras",
        112L, 8L,
        __kffiJvmStructFields95_0(),
    )
}
private fun __kffiJvmStructFields96_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
)
private fun __kffiJvmRegisterStructLayout96() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUDeviceExtras",
        32L, 8L,
        __kffiJvmStructFields96_0(),
    )
}
private fun __kffiJvmStructFields97_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("maxNonSamplerBindings", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("maxBindingArrayElementsPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 20L),
    JvmDowncallEngine.StructField("maxBindingArraySamplerElementsPerShaderStage", JvmDowncallEngine.FieldKind.UINT32, 24L),
    JvmDowncallEngine.StructField("maxMultiviewViewCount", JvmDowncallEngine.FieldKind.UINT32, 28L),
)
private fun __kffiJvmRegisterStructLayout97() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUNativeLimits",
        32L, 8L,
        __kffiJvmStructFields97_0(),
    )
}
private fun __kffiJvmStructFields98_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 16L),
)
private fun __kffiJvmRegisterStructLayout98() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderDefine",
        32L, 8L,
        __kffiJvmStructFields98_0(),
    )
}
private fun __kffiJvmStructFields99_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("stage", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 24L),
    JvmDowncallEngine.StructField("defineCount", JvmDowncallEngine.FieldKind.UINT32, 40L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("defines", JvmDowncallEngine.FieldKind.POINTER, 48L),
)
private fun __kffiJvmRegisterStructLayout99() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderSourceGLSL",
        56L, 8L,
        __kffiJvmStructFields99_0(),
    )
}
private fun __kffiJvmStructFields100_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUStringView", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("sourceSize", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
    JvmDowncallEngine.StructField("source", JvmDowncallEngine.FieldKind.POINTER, 24L),
)
private fun __kffiJvmRegisterStructLayout100() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUShaderModuleDescriptorSpirV",
        32L, 8L,
        __kffiJvmStructFields100_0(),
    )
}
private fun __kffiJvmStructFields101_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("numAllocated", JvmDowncallEngine.FieldKind.UINT64, 0L),
    JvmDowncallEngine.StructField("numKeptFromUser", JvmDowncallEngine.FieldKind.UINT64, 8L),
    JvmDowncallEngine.StructField("numReleasedFromUser", JvmDowncallEngine.FieldKind.UINT64, 16L),
    JvmDowncallEngine.StructField("elementSize", JvmDowncallEngine.FieldKind.UINT64, 24L),
)
private fun __kffiJvmRegisterStructLayout101() {
    JvmDowncallEngine.registerStructLayout(
        "WGPURegistryReport",
        32L, 8L,
        __kffiJvmStructFields101_0(),
    )
}
private fun __kffiJvmStructFields102_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 32L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 64L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 96L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 128L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 160L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 192L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 224L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 256L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 288L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 320L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 352L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 384L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 416L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 448L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 480L),
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 512L),
)
private fun __kffiJvmRegisterStructLayout102() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUHubReport",
        544L, 8L,
        __kffiJvmStructFields102_0(),
    )
}
private fun __kffiJvmStructFields103_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPURegistryReport", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("WGPUHubReport", JvmDowncallEngine.FieldKind.STRUCT, 32L),
)
private fun __kffiJvmRegisterStructLayout103() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUGlobalReport",
        576L, 8L,
        __kffiJvmStructFields103_0(),
    )
}
private fun __kffiJvmStructFields104_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("nextInChain", JvmDowncallEngine.FieldKind.POINTER, 0L),
    JvmDowncallEngine.StructField("backends", JvmDowncallEngine.FieldKind.UINT64, 8L),
)
private fun __kffiJvmRegisterStructLayout104() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUInstanceEnumerateAdapterOptions",
        16L, 8L,
        __kffiJvmStructFields104_0(),
    )
}
private fun __kffiJvmStructFields105_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("buffers", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("bufferCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
    JvmDowncallEngine.StructField("samplers", JvmDowncallEngine.FieldKind.POINTER, 32L),
    JvmDowncallEngine.StructField("samplerCount", JvmDowncallEngine.FieldKind.UINT64, 40L),
    JvmDowncallEngine.StructField("textureViews", JvmDowncallEngine.FieldKind.POINTER, 48L),
    JvmDowncallEngine.StructField("textureViewCount", JvmDowncallEngine.FieldKind.UINT64, 56L),
)
private fun __kffiJvmRegisterStructLayout105() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupEntryExtras",
        64L, 8L,
        __kffiJvmStructFields105_0(),
    )
}
private fun __kffiJvmStructFields106_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("count", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout106() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUBindGroupLayoutEntryExtras",
        24L, 8L,
        __kffiJvmStructFields106_0(),
    )
}
private fun __kffiJvmStructFields107_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("pipelineStatistics", JvmDowncallEngine.FieldKind.POINTER, 16L),
    JvmDowncallEngine.StructField("pipelineStatisticCount", JvmDowncallEngine.FieldKind.UINT64, 24L),
)
private fun __kffiJvmRegisterStructLayout107() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUQuerySetDescriptorExtras",
        32L, 8L,
        __kffiJvmStructFields107_0(),
    )
}
private fun __kffiJvmStructFields108_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("desiredMaximumFrameLatency", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout108() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceConfigurationExtras",
        24L, 8L,
        __kffiJvmStructFields108_0(),
    )
}
private fun __kffiJvmStructFields109_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("panelNative", JvmDowncallEngine.FieldKind.POINTER, 16L),
)
private fun __kffiJvmRegisterStructLayout109() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSurfaceSourceSwapChainPanel",
        24L, 8L,
        __kffiJvmStructFields109_0(),
    )
}
private fun __kffiJvmStructFields110_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("polygonMode", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("conservative", JvmDowncallEngine.FieldKind.UINT32, 20L),
)
private fun __kffiJvmRegisterStructLayout110() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUPrimitiveStateExtras",
        24L, 8L,
        __kffiJvmStructFields110_0(),
    )
}
private fun __kffiJvmStructFields111_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("aspect", JvmDowncallEngine.FieldKind.UINT32, 0L),
    JvmDowncallEngine.StructField("baseMipLevel", JvmDowncallEngine.FieldKind.UINT32, 4L),
    JvmDowncallEngine.StructField("mipLevelCount", JvmDowncallEngine.FieldKind.UINT32, 8L),
    JvmDowncallEngine.StructField("baseArrayLayer", JvmDowncallEngine.FieldKind.UINT32, 12L),
    JvmDowncallEngine.StructField("arrayLayerCount", JvmDowncallEngine.FieldKind.UINT32, 16L),
)
private fun __kffiJvmRegisterStructLayout111() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUImageSubresourceRange",
        20L, 4L,
        __kffiJvmStructFields111_0(),
    )
}
private fun __kffiJvmStructFields112_0(): kotlin.collections.List<JvmDowncallEngine.StructField> = listOf(
    JvmDowncallEngine.StructField("WGPUChainedStruct", JvmDowncallEngine.FieldKind.STRUCT, 0L),
    JvmDowncallEngine.StructField("samplerBorderColor", JvmDowncallEngine.FieldKind.UINT32, 16L),
    JvmDowncallEngine.StructField("__pad", JvmDowncallEngine.FieldKind.PADDING, 4L),
)
private fun __kffiJvmRegisterStructLayout112() {
    JvmDowncallEngine.registerStructLayout(
        "WGPUSamplerDescriptorExtras",
        24L, 8L,
        __kffiJvmStructFields112_0(),
    )
}
private val __kffiJvmStructLayouts: Unit = run {
    __kffiJvmRegisterStructLayout0()
    __kffiJvmRegisterStructLayout1()
    __kffiJvmRegisterStructLayout2()
    __kffiJvmRegisterStructLayout3()
    __kffiJvmRegisterStructLayout4()
    __kffiJvmRegisterStructLayout5()
    __kffiJvmRegisterStructLayout6()
    __kffiJvmRegisterStructLayout7()
    __kffiJvmRegisterStructLayout8()
    __kffiJvmRegisterStructLayout9()
    __kffiJvmRegisterStructLayout10()
    __kffiJvmRegisterStructLayout11()
    __kffiJvmRegisterStructLayout12()
    __kffiJvmRegisterStructLayout13()
    __kffiJvmRegisterStructLayout14()
    __kffiJvmRegisterStructLayout15()
    __kffiJvmRegisterStructLayout16()
    __kffiJvmRegisterStructLayout17()
    __kffiJvmRegisterStructLayout18()
    __kffiJvmRegisterStructLayout19()
    __kffiJvmRegisterStructLayout20()
    __kffiJvmRegisterStructLayout21()
    __kffiJvmRegisterStructLayout22()
    __kffiJvmRegisterStructLayout23()
    __kffiJvmRegisterStructLayout24()
    __kffiJvmRegisterStructLayout25()
    __kffiJvmRegisterStructLayout26()
    __kffiJvmRegisterStructLayout27()
    __kffiJvmRegisterStructLayout28()
    __kffiJvmRegisterStructLayout29()
    __kffiJvmRegisterStructLayout30()
    __kffiJvmRegisterStructLayout31()
    __kffiJvmRegisterStructLayout32()
    __kffiJvmRegisterStructLayout33()
    __kffiJvmRegisterStructLayout34()
    __kffiJvmRegisterStructLayout35()
    __kffiJvmRegisterStructLayout36()
    __kffiJvmRegisterStructLayout37()
    __kffiJvmRegisterStructLayout38()
    __kffiJvmRegisterStructLayout39()
    __kffiJvmRegisterStructLayout40()
    __kffiJvmRegisterStructLayout41()
    __kffiJvmRegisterStructLayout42()
    __kffiJvmRegisterStructLayout43()
    __kffiJvmRegisterStructLayout44()
    __kffiJvmRegisterStructLayout45()
    __kffiJvmRegisterStructLayout46()
    __kffiJvmRegisterStructLayout47()
    __kffiJvmRegisterStructLayout48()
    __kffiJvmRegisterStructLayout49()
    __kffiJvmRegisterStructLayout50()
    __kffiJvmRegisterStructLayout51()
    __kffiJvmRegisterStructLayout52()
    __kffiJvmRegisterStructLayout53()
    __kffiJvmRegisterStructLayout54()
    __kffiJvmRegisterStructLayout55()
    __kffiJvmRegisterStructLayout56()
    __kffiJvmRegisterStructLayout57()
    __kffiJvmRegisterStructLayout58()
    __kffiJvmRegisterStructLayout59()
    __kffiJvmRegisterStructLayout60()
    __kffiJvmRegisterStructLayout61()
    __kffiJvmRegisterStructLayout62()
    __kffiJvmRegisterStructLayout63()
    __kffiJvmRegisterStructLayout64()
    __kffiJvmRegisterStructLayout65()
    __kffiJvmRegisterStructLayout66()
    __kffiJvmRegisterStructLayout67()
    __kffiJvmRegisterStructLayout68()
    __kffiJvmRegisterStructLayout69()
    __kffiJvmRegisterStructLayout70()
    __kffiJvmRegisterStructLayout71()
    __kffiJvmRegisterStructLayout72()
    __kffiJvmRegisterStructLayout73()
    __kffiJvmRegisterStructLayout74()
    __kffiJvmRegisterStructLayout75()
    __kffiJvmRegisterStructLayout76()
    __kffiJvmRegisterStructLayout77()
    __kffiJvmRegisterStructLayout78()
    __kffiJvmRegisterStructLayout79()
    __kffiJvmRegisterStructLayout80()
    __kffiJvmRegisterStructLayout81()
    __kffiJvmRegisterStructLayout82()
    __kffiJvmRegisterStructLayout83()
    __kffiJvmRegisterStructLayout84()
    __kffiJvmRegisterStructLayout85()
    __kffiJvmRegisterStructLayout86()
    __kffiJvmRegisterStructLayout87()
    __kffiJvmRegisterStructLayout88()
    __kffiJvmRegisterStructLayout89()
    __kffiJvmRegisterStructLayout90()
    __kffiJvmRegisterStructLayout91()
    __kffiJvmRegisterStructLayout92()
    __kffiJvmRegisterStructLayout93()
    __kffiJvmRegisterStructLayout94()
    __kffiJvmRegisterStructLayout95()
    __kffiJvmRegisterStructLayout96()
    __kffiJvmRegisterStructLayout97()
    __kffiJvmRegisterStructLayout98()
    __kffiJvmRegisterStructLayout99()
    __kffiJvmRegisterStructLayout100()
    __kffiJvmRegisterStructLayout101()
    __kffiJvmRegisterStructLayout102()
    __kffiJvmRegisterStructLayout103()
    __kffiJvmRegisterStructLayout104()
    __kffiJvmRegisterStructLayout105()
    __kffiJvmRegisterStructLayout106()
    __kffiJvmRegisterStructLayout107()
    __kffiJvmRegisterStructLayout108()
    __kffiJvmRegisterStructLayout109()
    __kffiJvmRegisterStructLayout110()
    __kffiJvmRegisterStructLayout111()
    __kffiJvmRegisterStructLayout112()
}

