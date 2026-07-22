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
import io.ygdrasil.kffi.toAddress
import com.sun.jna.Pointer
import com.sun.jna.CallbackReference
import com.sun.jna.Native
import kotlin.OptIn
import kotlin.UnsupportedOperationException
import kotlin.jvm.JvmInline

actual interface WGPUStringView {
    actual var data: CString?
    actual var length: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStringView {
            return ByReference(io.ygdrasil.wgpu.android.WGPUStringView.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUStringView {
            val ref = io.ygdrasil.wgpu.android.WGPUStringView.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStringView) -> Unit): ArrayHolder<WGPUStringView> {
            val ref = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUStringView.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUStringView.ByReference = io.ygdrasil.wgpu.android.WGPUStringView.ByReference(com.sun.jna.Pointer.NULL)) : WGPUStringView {
        override var data: CString?
            get() = handle.data?.let(::CString)
            set(value) { handle.data = value?.handler }
        override var length: ULong
            get() = handle.length.toULong() as ULong
            set(value) { handle.length = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue(com.sun.jna.Pointer.NULL)) : WGPUStringView {
        override var data: CString?
            get() = handle.data?.let(::CString)
            set(value) { handle.data = value?.handler }
        override var length: ULong
            get() = handle.length.toULong() as ULong
            set(value) { handle.length = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUChainedStruct {
            return ByReference(io.ygdrasil.wgpu.android.WGPUChainedStruct.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUChainedStruct {
            val ref = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUChainedStruct) -> Unit): ArrayHolder<WGPUChainedStruct> {
            val ref = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByReference = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByReference(com.sun.jna.Pointer.NULL)) : WGPUChainedStruct {
        override var next: WGPUChainedStruct?
            get() = handle.next?.let { WGPUChainedStruct(it) }
            set(value) { handle.next = value?.handler }
        override var sType: WGPUSType
            get() = handle.sType.toUInt() as WGPUSType
            set(value) { handle.sType = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue(com.sun.jna.Pointer.NULL)) : WGPUChainedStruct {
        override var next: WGPUChainedStruct?
            get() = handle.next?.let { WGPUChainedStruct(it) }
            set(value) { handle.next = value?.handler }
        override var sType: WGPUSType
            get() = handle.sType.toUInt() as WGPUSType
            set(value) { handle.sType = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBufferMapCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferMapCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferMapCallbackInfo) -> Unit): ArrayHolder<WGPUBufferMapCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBufferMapCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBufferMapCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfoCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfoCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfoCallbackInfo) -> Unit): ArrayHolder<WGPUCompilationInfoCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCompilationInfoCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCompilationInfoCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUCreateComputePipelineAsyncCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateComputePipelineAsyncCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateComputePipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCreateComputePipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCreateComputePipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUCreateRenderPipelineAsyncCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateRenderPipelineAsyncCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateRenderPipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUDeviceLostCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceLostCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceLostCallbackInfo) -> Unit): ArrayHolder<WGPUDeviceLostCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUDeviceLostCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUDeviceLostCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUPopErrorScopeCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUPopErrorScopeCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPopErrorScopeCallbackInfo) -> Unit): ArrayHolder<WGPUPopErrorScopeCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUPopErrorScopeCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUPopErrorScopeCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUQueueWorkDoneCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueWorkDoneCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueWorkDoneCallbackInfo) -> Unit): ArrayHolder<WGPUQueueWorkDoneCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUQueueWorkDoneCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUQueueWorkDoneCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterCallbackInfo) -> Unit): ArrayHolder<WGPURequestAdapterCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURequestDeviceCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURequestDeviceCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestDeviceCallbackInfo) -> Unit): ArrayHolder<WGPURequestDeviceCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPURequestDeviceCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPURequestDeviceCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var mode: WGPUCallbackMode
            get() = handle.mode.toUInt() as WGPUCallbackMode
            set(value) { handle.mode = value.toInt() }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUUncapturedErrorCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUUncapturedErrorCallbackInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUUncapturedErrorCallbackInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUUncapturedErrorCallbackInfo) -> Unit): ArrayHolder<WGPUUncapturedErrorCallbackInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByReference = io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUUncapturedErrorCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUUncapturedErrorCallbackInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var callback: NativeAddress?
            get() = handle.callback
            set(value) { handle.callback = value }
        override var userdata1: NativeAddress?
            get() = handle.userdata1
            set(value) { handle.userdata1 = value }
        override var userdata2: NativeAddress?
            get() = handle.userdata2
            set(value) { handle.userdata2 = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUAdapterInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUAdapterInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUAdapterInfo) -> Unit): ArrayHolder<WGPUAdapterInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByReference = io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUAdapterInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var vendor: WGPUStringView
            get() {
                handle.readField("vendor")
                return WGPUStringView.ByValue(handle.vendor)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.vendor.size())
                handle.readField("vendor")
                handle.vendor.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("vendor")
            }
        override var architecture: WGPUStringView
            get() {
                handle.readField("architecture")
                return WGPUStringView.ByValue(handle.architecture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.architecture.size())
                handle.readField("architecture")
                handle.architecture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("architecture")
            }
        override var device: WGPUStringView
            get() {
                handle.readField("device")
                return WGPUStringView.ByValue(handle.device)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.device.size())
                handle.readField("device")
                handle.device.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("device")
            }
        override var description: WGPUStringView
            get() {
                handle.readField("description")
                return WGPUStringView.ByValue(handle.description)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.description.size())
                handle.readField("description")
                handle.description.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("description")
            }
        override var backendType: WGPUBackendType
            get() = handle.backendType.toUInt() as WGPUBackendType
            set(value) { handle.backendType = value.toInt() }
        override var adapterType: WGPUAdapterType
            get() = handle.adapterType.toUInt() as WGPUAdapterType
            set(value) { handle.adapterType = value.toInt() }
        override var vendorID: UInt
            get() = handle.vendorID.toUInt() as UInt
            set(value) { handle.vendorID = value.toInt() }
        override var deviceID: UInt
            get() = handle.deviceID.toUInt() as UInt
            set(value) { handle.deviceID = value.toInt() }
        override var subgroupMinSize: UInt
            get() = handle.subgroupMinSize.toUInt() as UInt
            set(value) { handle.subgroupMinSize = value.toInt() }
        override var subgroupMaxSize: UInt
            get() = handle.subgroupMaxSize.toUInt() as UInt
            set(value) { handle.subgroupMaxSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue = io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUAdapterInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var vendor: WGPUStringView
            get() {
                handle.readField("vendor")
                return WGPUStringView.ByValue(handle.vendor)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.vendor.size())
                handle.readField("vendor")
                handle.vendor.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("vendor")
            }
        override var architecture: WGPUStringView
            get() {
                handle.readField("architecture")
                return WGPUStringView.ByValue(handle.architecture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.architecture.size())
                handle.readField("architecture")
                handle.architecture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("architecture")
            }
        override var device: WGPUStringView
            get() {
                handle.readField("device")
                return WGPUStringView.ByValue(handle.device)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.device.size())
                handle.readField("device")
                handle.device.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("device")
            }
        override var description: WGPUStringView
            get() {
                handle.readField("description")
                return WGPUStringView.ByValue(handle.description)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.description.size())
                handle.readField("description")
                handle.description.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("description")
            }
        override var backendType: WGPUBackendType
            get() = handle.backendType.toUInt() as WGPUBackendType
            set(value) { handle.backendType = value.toInt() }
        override var adapterType: WGPUAdapterType
            get() = handle.adapterType.toUInt() as WGPUAdapterType
            set(value) { handle.adapterType = value.toInt() }
        override var vendorID: UInt
            get() = handle.vendorID.toUInt() as UInt
            set(value) { handle.vendorID = value.toInt() }
        override var deviceID: UInt
            get() = handle.deviceID.toUInt() as UInt
            set(value) { handle.deviceID = value.toInt() }
        override var subgroupMinSize: UInt
            get() = handle.subgroupMinSize.toUInt() as UInt
            set(value) { handle.subgroupMinSize = value.toInt() }
        override var subgroupMaxSize: UInt
            get() = handle.subgroupMaxSize.toUInt() as UInt
            set(value) { handle.subgroupMaxSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUBlendComponent {
    actual var operation: WGPUBlendOperation
    actual var srcFactor: WGPUBlendFactor
    actual var dstFactor: WGPUBlendFactor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendComponent {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBlendComponent.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendComponent {
            val ref = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendComponent) -> Unit): ArrayHolder<WGPUBlendComponent> {
            val ref = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBlendComponent.ByReference = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBlendComponent {
        override var operation: WGPUBlendOperation
            get() = handle.operation.toUInt() as WGPUBlendOperation
            set(value) { handle.operation = value.toInt() }
        override var srcFactor: WGPUBlendFactor
            get() = handle.srcFactor.toUInt() as WGPUBlendFactor
            set(value) { handle.srcFactor = value.toInt() }
        override var dstFactor: WGPUBlendFactor
            get() = handle.dstFactor.toUInt() as WGPUBlendFactor
            set(value) { handle.dstFactor = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBlendComponent {
        override var operation: WGPUBlendOperation
            get() = handle.operation.toUInt() as WGPUBlendOperation
            set(value) { handle.operation = value.toInt() }
        override var srcFactor: WGPUBlendFactor
            get() = handle.srcFactor.toUInt() as WGPUBlendFactor
            set(value) { handle.srcFactor = value.toInt() }
        override var dstFactor: WGPUBlendFactor
            get() = handle.dstFactor.toUInt() as WGPUBlendFactor
            set(value) { handle.dstFactor = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUBufferBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUBufferBindingType
    actual var hasDynamicOffset: UInt
    actual var minBindingSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBufferBindingLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferBindingLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferBindingLayout) -> Unit): ArrayHolder<WGPUBufferBindingLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByReference = io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBufferBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var type: WGPUBufferBindingType
            get() = handle.type.toUInt() as WGPUBufferBindingType
            set(value) { handle.type = value.toInt() }
        override var hasDynamicOffset: UInt
            get() = handle.hasDynamicOffset.toUInt() as UInt
            set(value) { handle.hasDynamicOffset = value.toInt() }
        override var minBindingSize: ULong
            get() = handle.minBindingSize.toULong() as ULong
            set(value) { handle.minBindingSize = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBufferBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var type: WGPUBufferBindingType
            get() = handle.type.toUInt() as WGPUBufferBindingType
            set(value) { handle.type = value.toInt() }
        override var hasDynamicOffset: UInt
            get() = handle.hasDynamicOffset.toUInt() as UInt
            set(value) { handle.hasDynamicOffset = value.toInt() }
        override var minBindingSize: ULong
            get() = handle.minBindingSize.toULong() as ULong
            set(value) { handle.minBindingSize = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBufferDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferDescriptor) -> Unit): ArrayHolder<WGPUBufferDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var size: ULong
            get() = handle.size.toULong() as ULong
            set(value) { handle.size = value.toLong() }
        override var mappedAtCreation: UInt
            get() = handle.mappedAtCreation.toUInt() as UInt
            set(value) { handle.mappedAtCreation = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUBufferDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var size: ULong
            get() = handle.size.toULong() as ULong
            set(value) { handle.size = value.toLong() }
        override var mappedAtCreation: UInt
            get() = handle.mappedAtCreation.toUInt() as UInt
            set(value) { handle.mappedAtCreation = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUColor {
    actual var r: Double
    actual var g: Double
    actual var b: Double
    actual var a: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUColor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUColor {
            val ref = io.ygdrasil.wgpu.android.WGPUColor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColor) -> Unit): ArrayHolder<WGPUColor> {
            val ref = io.ygdrasil.wgpu.android.WGPUColor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUColor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUColor.ByReference = io.ygdrasil.wgpu.android.WGPUColor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUColor {
        override var r: Double
            get() = handle.r as Double
            set(value) { handle.r = value }
        override var g: Double
            get() = handle.g as Double
            set(value) { handle.g = value }
        override var b: Double
            get() = handle.b as Double
            set(value) { handle.b = value }
        override var a: Double
            get() = handle.a as Double
            set(value) { handle.a = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUColor.ByValue = io.ygdrasil.wgpu.android.WGPUColor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUColor {
        override var r: Double
            get() = handle.r as Double
            set(value) { handle.r = value }
        override var g: Double
            get() = handle.g as Double
            set(value) { handle.g = value }
        override var b: Double
            get() = handle.b as Double
            set(value) { handle.b = value }
        override var a: Double
            get() = handle.a as Double
            set(value) { handle.a = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUCommandBufferDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandBufferDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandBufferDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandBufferDescriptor) -> Unit): ArrayHolder<WGPUCommandBufferDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCommandBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUCommandBufferDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCommandBufferDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUCommandEncoderDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandEncoderDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandEncoderDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandEncoderDescriptor) -> Unit): ArrayHolder<WGPUCommandEncoderDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCommandEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUCommandEncoderDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCommandEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUCompatibilityModeLimits {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCompatibilityModeLimits {
            val ref = io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompatibilityModeLimits) -> Unit): ArrayHolder<WGPUCompatibilityModeLimits> {
            val ref = io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByReference = io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCompatibilityModeLimits {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = handle.maxStorageBuffersInVertexStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersInVertexStage = value.toInt() }
        override var maxStorageTexturesInVertexStage: UInt
            get() = handle.maxStorageTexturesInVertexStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesInVertexStage = value.toInt() }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = handle.maxStorageBuffersInFragmentStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersInFragmentStage = value.toInt() }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = handle.maxStorageTexturesInFragmentStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesInFragmentStage = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByValue = io.ygdrasil.wgpu.android.WGPUCompatibilityModeLimits.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCompatibilityModeLimits {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = handle.maxStorageBuffersInVertexStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersInVertexStage = value.toInt() }
        override var maxStorageTexturesInVertexStage: UInt
            get() = handle.maxStorageTexturesInVertexStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesInVertexStage = value.toInt() }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = handle.maxStorageBuffersInFragmentStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersInFragmentStage = value.toInt() }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = handle.maxStorageTexturesInFragmentStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesInFragmentStage = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUCompilationMessage {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationMessage {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationMessage) -> Unit): ArrayHolder<WGPUCompilationMessage> {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByReference = io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCompilationMessage {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var message: WGPUStringView
            get() {
                handle.readField("message")
                return WGPUStringView.ByValue(handle.message)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.message.size())
                handle.readField("message")
                handle.message.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("message")
            }
        override var type: WGPUCompilationMessageType
            get() = handle.type.toUInt() as WGPUCompilationMessageType
            set(value) { handle.type = value.toInt() }
        override var lineNum: ULong
            get() = handle.lineNum.toULong() as ULong
            set(value) { handle.lineNum = value.toLong() }
        override var linePos: ULong
            get() = handle.linePos.toULong() as ULong
            set(value) { handle.linePos = value.toLong() }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var length: ULong
            get() = handle.length.toULong() as ULong
            set(value) { handle.length = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByValue = io.ygdrasil.wgpu.android.WGPUCompilationMessage.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCompilationMessage {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var message: WGPUStringView
            get() {
                handle.readField("message")
                return WGPUStringView.ByValue(handle.message)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.message.size())
                handle.readField("message")
                handle.message.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("message")
            }
        override var type: WGPUCompilationMessageType
            get() = handle.type.toUInt() as WGPUCompilationMessageType
            set(value) { handle.type = value.toInt() }
        override var lineNum: ULong
            get() = handle.lineNum.toULong() as ULong
            set(value) { handle.lineNum = value.toLong() }
        override var linePos: ULong
            get() = handle.linePos.toULong() as ULong
            set(value) { handle.linePos = value.toLong() }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var length: ULong
            get() = handle.length.toULong() as ULong
            set(value) { handle.length = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUConstantEntry {
    actual var nextInChain: WGPUChainedStruct?
    actual var key: WGPUStringView
    actual var value: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUConstantEntry {
            return ByReference(io.ygdrasil.wgpu.android.WGPUConstantEntry.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUConstantEntry {
            val ref = io.ygdrasil.wgpu.android.WGPUConstantEntry.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUConstantEntry) -> Unit): ArrayHolder<WGPUConstantEntry> {
            val ref = io.ygdrasil.wgpu.android.WGPUConstantEntry.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUConstantEntry.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUConstantEntry.ByReference = io.ygdrasil.wgpu.android.WGPUConstantEntry.ByReference(com.sun.jna.Pointer.NULL)) : WGPUConstantEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var key: WGPUStringView
            get() {
                handle.readField("key")
                return WGPUStringView.ByValue(handle.key)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.key.size())
                handle.readField("key")
                handle.key.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("key")
            }
        override var value: Double
            get() = handle.value as Double
            set(value) { handle.value = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUConstantEntry.ByValue = io.ygdrasil.wgpu.android.WGPUConstantEntry.ByValue(com.sun.jna.Pointer.NULL)) : WGPUConstantEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var key: WGPUStringView
            get() {
                handle.readField("key")
                return WGPUStringView.ByValue(handle.key)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.key.size())
                handle.readField("key")
                handle.key.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("key")
            }
        override var value: Double
            get() = handle.value as Double
            set(value) { handle.value = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUExtent3D {
    actual var width: UInt
    actual var height: UInt
    actual var depthOrArrayLayers: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExtent3D {
            return ByReference(io.ygdrasil.wgpu.android.WGPUExtent3D.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUExtent3D {
            val ref = io.ygdrasil.wgpu.android.WGPUExtent3D.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExtent3D) -> Unit): ArrayHolder<WGPUExtent3D> {
            val ref = io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUExtent3D.ByReference = io.ygdrasil.wgpu.android.WGPUExtent3D.ByReference(com.sun.jna.Pointer.NULL)) : WGPUExtent3D {
        override var width: UInt
            get() = handle.width.toUInt() as UInt
            set(value) { handle.width = value.toInt() }
        override var height: UInt
            get() = handle.height.toUInt() as UInt
            set(value) { handle.height = value.toInt() }
        override var depthOrArrayLayers: UInt
            get() = handle.depthOrArrayLayers.toUInt() as UInt
            set(value) { handle.depthOrArrayLayers = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue = io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue(com.sun.jna.Pointer.NULL)) : WGPUExtent3D {
        override var width: UInt
            get() = handle.width.toUInt() as UInt
            set(value) { handle.width = value.toInt() }
        override var height: UInt
            get() = handle.height.toUInt() as UInt
            set(value) { handle.height = value.toInt() }
        override var depthOrArrayLayers: UInt
            get() = handle.depthOrArrayLayers.toUInt() as UInt
            set(value) { handle.depthOrArrayLayers = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUExternalTextureBindingEntry {
    actual var chain: WGPUChainedStruct
    actual var externalTexture: WGPUExternalTexture?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingEntry {
            return ByReference(io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingEntry {
            val ref = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingEntry) -> Unit): ArrayHolder<WGPUExternalTextureBindingEntry> {
            val ref = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByReference = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByReference(com.sun.jna.Pointer.NULL)) : WGPUExternalTextureBindingEntry {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var externalTexture: WGPUExternalTexture?
            get() = handle.externalTexture?.let { WGPUExternalTexture(it) }
            set(value) { handle.externalTexture = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByValue = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingEntry.ByValue(com.sun.jna.Pointer.NULL)) : WGPUExternalTextureBindingEntry {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var externalTexture: WGPUExternalTexture?
            get() = handle.externalTexture?.let { WGPUExternalTexture(it) }
            set(value) { handle.externalTexture = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUExternalTextureBindingLayout {
    actual var chain: WGPUChainedStruct
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingLayout) -> Unit): ArrayHolder<WGPUExternalTextureBindingLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByReference = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUExternalTextureBindingLayout {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUExternalTextureBindingLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUExternalTextureBindingLayout {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUFuture {
    actual var id: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFuture {
            return ByReference(io.ygdrasil.wgpu.android.WGPUFuture.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUFuture {
            val ref = io.ygdrasil.wgpu.android.WGPUFuture.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFuture) -> Unit): ArrayHolder<WGPUFuture> {
            val ref = io.ygdrasil.wgpu.android.WGPUFuture.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUFuture.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUFuture.ByReference = io.ygdrasil.wgpu.android.WGPUFuture.ByReference(com.sun.jna.Pointer.NULL)) : WGPUFuture {
        override var id: ULong
            get() = handle.id.toULong() as ULong
            set(value) { handle.id = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUFuture.ByValue = io.ygdrasil.wgpu.android.WGPUFuture.ByValue(com.sun.jna.Pointer.NULL)) : WGPUFuture {
        override var id: ULong
            get() = handle.id.toULong() as ULong
            set(value) { handle.id = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUInstanceLimits {
    actual var nextInChain: WGPUChainedStruct?
    actual var timedWaitAnyMaxCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceLimits {
            return ByReference(io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceLimits {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceLimits) -> Unit): ArrayHolder<WGPUInstanceLimits> {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByReference = io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByReference(com.sun.jna.Pointer.NULL)) : WGPUInstanceLimits {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var timedWaitAnyMaxCount: ULong
            get() = handle.timedWaitAnyMaxCount.toULong() as ULong
            set(value) { handle.timedWaitAnyMaxCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByValue = io.ygdrasil.wgpu.android.WGPUInstanceLimits.ByValue(com.sun.jna.Pointer.NULL)) : WGPUInstanceLimits {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var timedWaitAnyMaxCount: ULong
            get() = handle.timedWaitAnyMaxCount.toULong() as ULong
            set(value) { handle.timedWaitAnyMaxCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUMultisampleState {
    actual var nextInChain: WGPUChainedStruct?
    actual var count: UInt
    actual var mask: UInt
    actual var alphaToCoverageEnabled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUMultisampleState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUMultisampleState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUMultisampleState {
            val ref = io.ygdrasil.wgpu.android.WGPUMultisampleState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUMultisampleState) -> Unit): ArrayHolder<WGPUMultisampleState> {
            val ref = io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUMultisampleState.ByReference = io.ygdrasil.wgpu.android.WGPUMultisampleState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUMultisampleState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override var mask: UInt
            get() = handle.mask.toUInt() as UInt
            set(value) { handle.mask = value.toInt() }
        override var alphaToCoverageEnabled: UInt
            get() = handle.alphaToCoverageEnabled.toUInt() as UInt
            set(value) { handle.alphaToCoverageEnabled = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue = io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUMultisampleState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override var mask: UInt
            get() = handle.mask.toUInt() as UInt
            set(value) { handle.mask = value.toInt() }
        override var alphaToCoverageEnabled: UInt
            get() = handle.alphaToCoverageEnabled.toUInt() as UInt
            set(value) { handle.alphaToCoverageEnabled = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUOrigin3D {
    actual var x: UInt
    actual var y: UInt
    actual var z: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUOrigin3D {
            return ByReference(io.ygdrasil.wgpu.android.WGPUOrigin3D.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUOrigin3D {
            val ref = io.ygdrasil.wgpu.android.WGPUOrigin3D.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUOrigin3D) -> Unit): ArrayHolder<WGPUOrigin3D> {
            val ref = io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUOrigin3D.ByReference = io.ygdrasil.wgpu.android.WGPUOrigin3D.ByReference(com.sun.jna.Pointer.NULL)) : WGPUOrigin3D {
        override var x: UInt
            get() = handle.x.toUInt() as UInt
            set(value) { handle.x = value.toInt() }
        override var y: UInt
            get() = handle.y.toUInt() as UInt
            set(value) { handle.y = value.toInt() }
        override var z: UInt
            get() = handle.z.toUInt() as UInt
            set(value) { handle.z = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue = io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue(com.sun.jna.Pointer.NULL)) : WGPUOrigin3D {
        override var x: UInt
            get() = handle.x.toUInt() as UInt
            set(value) { handle.x = value.toInt() }
        override var y: UInt
            get() = handle.y.toUInt() as UInt
            set(value) { handle.y = value.toInt() }
        override var z: UInt
            get() = handle.z.toUInt() as UInt
            set(value) { handle.z = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUPassTimestampWrites {
    actual var nextInChain: WGPUChainedStruct?
    actual var querySet: WGPUQuerySet?
    actual var beginningOfPassWriteIndex: UInt
    actual var endOfPassWriteIndex: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPassTimestampWrites {
            return ByReference(io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUPassTimestampWrites {
            val ref = io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPassTimestampWrites) -> Unit): ArrayHolder<WGPUPassTimestampWrites> {
            val ref = io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByReference = io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByReference(com.sun.jna.Pointer.NULL)) : WGPUPassTimestampWrites {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var querySet: WGPUQuerySet?
            get() = handle.querySet?.let { WGPUQuerySet(it) }
            set(value) { handle.querySet = value?.handler }
        override var beginningOfPassWriteIndex: UInt
            get() = handle.beginningOfPassWriteIndex.toUInt() as UInt
            set(value) { handle.beginningOfPassWriteIndex = value.toInt() }
        override var endOfPassWriteIndex: UInt
            get() = handle.endOfPassWriteIndex.toUInt() as UInt
            set(value) { handle.endOfPassWriteIndex = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByValue = io.ygdrasil.wgpu.android.WGPUPassTimestampWrites.ByValue(com.sun.jna.Pointer.NULL)) : WGPUPassTimestampWrites {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var querySet: WGPUQuerySet?
            get() = handle.querySet?.let { WGPUQuerySet(it) }
            set(value) { handle.querySet = value?.handler }
        override var beginningOfPassWriteIndex: UInt
            get() = handle.beginningOfPassWriteIndex.toUInt() as UInt
            set(value) { handle.beginningOfPassWriteIndex = value.toInt() }
        override var endOfPassWriteIndex: UInt
            get() = handle.endOfPassWriteIndex.toUInt() as UInt
            set(value) { handle.endOfPassWriteIndex = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUPipelineLayoutDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUPipelineLayoutDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPipelineLayoutDescriptor) -> Unit): ArrayHolder<WGPUPipelineLayoutDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUPipelineLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var bindGroupLayoutCount: ULong
            get() = handle.bindGroupLayoutCount.toULong() as ULong
            set(value) { handle.bindGroupLayoutCount = value.toLong() }
        override var bindGroupLayouts: NativeAddress?
            get() = handle.bindGroupLayouts
            set(value) { handle.bindGroupLayouts = value }
        override var immediateSize: UInt
            get() = handle.immediateSize.toUInt() as UInt
            set(value) { handle.immediateSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUPipelineLayoutDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUPipelineLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var bindGroupLayoutCount: ULong
            get() = handle.bindGroupLayoutCount.toULong() as ULong
            set(value) { handle.bindGroupLayoutCount = value.toLong() }
        override var bindGroupLayouts: NativeAddress?
            get() = handle.bindGroupLayouts
            set(value) { handle.bindGroupLayouts = value }
        override var immediateSize: UInt
            get() = handle.immediateSize.toUInt() as UInt
            set(value) { handle.immediateSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveState {
            val ref = io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveState) -> Unit): ArrayHolder<WGPUPrimitiveState> {
            val ref = io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByReference = io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUPrimitiveState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var topology: WGPUPrimitiveTopology
            get() = handle.topology.toUInt() as WGPUPrimitiveTopology
            set(value) { handle.topology = value.toInt() }
        override var stripIndexFormat: WGPUIndexFormat
            get() = handle.stripIndexFormat.toUInt() as WGPUIndexFormat
            set(value) { handle.stripIndexFormat = value.toInt() }
        override var frontFace: WGPUFrontFace
            get() = handle.frontFace.toUInt() as WGPUFrontFace
            set(value) { handle.frontFace = value.toInt() }
        override var cullMode: WGPUCullMode
            get() = handle.cullMode.toUInt() as WGPUCullMode
            set(value) { handle.cullMode = value.toInt() }
        override var unclippedDepth: UInt
            get() = handle.unclippedDepth.toUInt() as UInt
            set(value) { handle.unclippedDepth = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue = io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUPrimitiveState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var topology: WGPUPrimitiveTopology
            get() = handle.topology.toUInt() as WGPUPrimitiveTopology
            set(value) { handle.topology = value.toInt() }
        override var stripIndexFormat: WGPUIndexFormat
            get() = handle.stripIndexFormat.toUInt() as WGPUIndexFormat
            set(value) { handle.stripIndexFormat = value.toInt() }
        override var frontFace: WGPUFrontFace
            get() = handle.frontFace.toUInt() as WGPUFrontFace
            set(value) { handle.frontFace = value.toInt() }
        override var cullMode: WGPUCullMode
            get() = handle.cullMode.toUInt() as WGPUCullMode
            set(value) { handle.cullMode = value.toInt() }
        override var unclippedDepth: UInt
            get() = handle.unclippedDepth.toUInt() as UInt
            set(value) { handle.unclippedDepth = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUQuerySetDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var type: WGPUQueryType
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptor) -> Unit): ArrayHolder<WGPUQuerySetDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUQuerySetDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var type: WGPUQueryType
            get() = handle.type.toUInt() as WGPUQueryType
            set(value) { handle.type = value.toInt() }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUQuerySetDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var type: WGPUQueryType
            get() = handle.type.toUInt() as WGPUQueryType
            set(value) { handle.type = value.toInt() }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUQueueDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQueueDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueDescriptor) -> Unit): ArrayHolder<WGPUQueueDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUQueueDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUQueueDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPURenderBundleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleDescriptor) -> Unit): ArrayHolder<WGPURenderBundleDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderBundleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPURenderBundleDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderBundleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleEncoderDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleEncoderDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleEncoderDescriptor) -> Unit): ArrayHolder<WGPURenderBundleEncoderDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderBundleEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var colorFormatCount: ULong
            get() = handle.colorFormatCount.toULong() as ULong
            set(value) { handle.colorFormatCount = value.toLong() }
        override var colorFormats: NativeAddress?
            get() = handle.colorFormats
            set(value) { handle.colorFormats = value }
        override var depthStencilFormat: WGPUTextureFormat
            get() = handle.depthStencilFormat.toUInt() as WGPUTextureFormat
            set(value) { handle.depthStencilFormat = value.toInt() }
        override var sampleCount: UInt
            get() = handle.sampleCount.toUInt() as UInt
            set(value) { handle.sampleCount = value.toInt() }
        override var depthReadOnly: UInt
            get() = handle.depthReadOnly.toUInt() as UInt
            set(value) { handle.depthReadOnly = value.toInt() }
        override var stencilReadOnly: UInt
            get() = handle.stencilReadOnly.toUInt() as UInt
            set(value) { handle.stencilReadOnly = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPURenderBundleEncoderDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderBundleEncoderDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var colorFormatCount: ULong
            get() = handle.colorFormatCount.toULong() as ULong
            set(value) { handle.colorFormatCount = value.toLong() }
        override var colorFormats: NativeAddress?
            get() = handle.colorFormats
            set(value) { handle.colorFormats = value }
        override var depthStencilFormat: WGPUTextureFormat
            get() = handle.depthStencilFormat.toUInt() as WGPUTextureFormat
            set(value) { handle.depthStencilFormat = value.toInt() }
        override var sampleCount: UInt
            get() = handle.sampleCount.toUInt() as UInt
            set(value) { handle.sampleCount = value.toInt() }
        override var depthReadOnly: UInt
            get() = handle.depthReadOnly.toUInt() as UInt
            set(value) { handle.depthReadOnly = value.toInt() }
        override var stencilReadOnly: UInt
            get() = handle.stencilReadOnly.toUInt() as UInt
            set(value) { handle.stencilReadOnly = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDepthStencilAttachment {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDepthStencilAttachment {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDepthStencilAttachment) -> Unit): ArrayHolder<WGPURenderPassDepthStencilAttachment> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByReference = io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderPassDepthStencilAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var view: WGPUTextureView?
            get() = handle.view?.let { WGPUTextureView(it) }
            set(value) { handle.view = value?.handler }
        override var depthLoadOp: WGPULoadOp
            get() = handle.depthLoadOp.toUInt() as WGPULoadOp
            set(value) { handle.depthLoadOp = value.toInt() }
        override var depthStoreOp: WGPUStoreOp
            get() = handle.depthStoreOp.toUInt() as WGPUStoreOp
            set(value) { handle.depthStoreOp = value.toInt() }
        override var depthClearValue: Float
            get() = handle.depthClearValue as Float
            set(value) { handle.depthClearValue = value }
        override var depthReadOnly: UInt
            get() = handle.depthReadOnly.toUInt() as UInt
            set(value) { handle.depthReadOnly = value.toInt() }
        override var stencilLoadOp: WGPULoadOp
            get() = handle.stencilLoadOp.toUInt() as WGPULoadOp
            set(value) { handle.stencilLoadOp = value.toInt() }
        override var stencilStoreOp: WGPUStoreOp
            get() = handle.stencilStoreOp.toUInt() as WGPUStoreOp
            set(value) { handle.stencilStoreOp = value.toInt() }
        override var stencilClearValue: UInt
            get() = handle.stencilClearValue.toUInt() as UInt
            set(value) { handle.stencilClearValue = value.toInt() }
        override var stencilReadOnly: UInt
            get() = handle.stencilReadOnly.toUInt() as UInt
            set(value) { handle.stencilReadOnly = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByValue = io.ygdrasil.wgpu.android.WGPURenderPassDepthStencilAttachment.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderPassDepthStencilAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var view: WGPUTextureView?
            get() = handle.view?.let { WGPUTextureView(it) }
            set(value) { handle.view = value?.handler }
        override var depthLoadOp: WGPULoadOp
            get() = handle.depthLoadOp.toUInt() as WGPULoadOp
            set(value) { handle.depthLoadOp = value.toInt() }
        override var depthStoreOp: WGPUStoreOp
            get() = handle.depthStoreOp.toUInt() as WGPUStoreOp
            set(value) { handle.depthStoreOp = value.toInt() }
        override var depthClearValue: Float
            get() = handle.depthClearValue as Float
            set(value) { handle.depthClearValue = value }
        override var depthReadOnly: UInt
            get() = handle.depthReadOnly.toUInt() as UInt
            set(value) { handle.depthReadOnly = value.toInt() }
        override var stencilLoadOp: WGPULoadOp
            get() = handle.stencilLoadOp.toUInt() as WGPULoadOp
            set(value) { handle.stencilLoadOp = value.toInt() }
        override var stencilStoreOp: WGPUStoreOp
            get() = handle.stencilStoreOp.toUInt() as WGPUStoreOp
            set(value) { handle.stencilStoreOp = value.toInt() }
        override var stencilClearValue: UInt
            get() = handle.stencilClearValue.toUInt() as UInt
            set(value) { handle.stencilClearValue = value.toInt() }
        override var stencilReadOnly: UInt
            get() = handle.stencilReadOnly.toUInt() as UInt
            set(value) { handle.stencilReadOnly = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPURenderPassMaxDrawCount {
    actual var chain: WGPUChainedStruct
    actual var maxDrawCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassMaxDrawCount {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassMaxDrawCount {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassMaxDrawCount) -> Unit): ArrayHolder<WGPURenderPassMaxDrawCount> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByReference = io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderPassMaxDrawCount {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxDrawCount: ULong
            get() = handle.maxDrawCount.toULong() as ULong
            set(value) { handle.maxDrawCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByValue = io.ygdrasil.wgpu.android.WGPURenderPassMaxDrawCount.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderPassMaxDrawCount {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxDrawCount: ULong
            get() = handle.maxDrawCount.toULong() as ULong
            set(value) { handle.maxDrawCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPURequestAdapterWebXROptions {
    actual var chain: WGPUChainedStruct
    actual var xrCompatible: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterWebXROptions {
            return ByReference(io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterWebXROptions {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterWebXROptions) -> Unit): ArrayHolder<WGPURequestAdapterWebXROptions> {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByReference = io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByReference(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterWebXROptions {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var xrCompatible: UInt
            get() = handle.xrCompatible.toUInt() as UInt
            set(value) { handle.xrCompatible = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByValue = io.ygdrasil.wgpu.android.WGPURequestAdapterWebXROptions.ByValue(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterWebXROptions {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var xrCompatible: UInt
            get() = handle.xrCompatible.toUInt() as UInt
            set(value) { handle.xrCompatible = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSamplerBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUSamplerBindingType
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerBindingLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerBindingLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerBindingLayout) -> Unit): ArrayHolder<WGPUSamplerBindingLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByReference = io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSamplerBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var type: WGPUSamplerBindingType
            get() = handle.type.toUInt() as WGPUSamplerBindingType
            set(value) { handle.type = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSamplerBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var type: WGPUSamplerBindingType
            get() = handle.type.toUInt() as WGPUSamplerBindingType
            set(value) { handle.type = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptor) -> Unit): ArrayHolder<WGPUSamplerDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSamplerDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var addressModeU: WGPUAddressMode
            get() = handle.addressModeU.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeU = value.toInt() }
        override var addressModeV: WGPUAddressMode
            get() = handle.addressModeV.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeV = value.toInt() }
        override var addressModeW: WGPUAddressMode
            get() = handle.addressModeW.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeW = value.toInt() }
        override var magFilter: WGPUFilterMode
            get() = handle.magFilter.toUInt() as WGPUFilterMode
            set(value) { handle.magFilter = value.toInt() }
        override var minFilter: WGPUFilterMode
            get() = handle.minFilter.toUInt() as WGPUFilterMode
            set(value) { handle.minFilter = value.toInt() }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = handle.mipmapFilter.toUInt() as WGPUMipmapFilterMode
            set(value) { handle.mipmapFilter = value.toInt() }
        override var lodMinClamp: Float
            get() = handle.lodMinClamp as Float
            set(value) { handle.lodMinClamp = value }
        override var lodMaxClamp: Float
            get() = handle.lodMaxClamp as Float
            set(value) { handle.lodMaxClamp = value }
        override var compare: WGPUCompareFunction
            get() = handle.compare.toUInt() as WGPUCompareFunction
            set(value) { handle.compare = value.toInt() }
        override var maxAnisotropy: UShort
            get() = handle.maxAnisotropy.toUShort() as UShort
            set(value) { handle.maxAnisotropy = value.toShort() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUSamplerDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSamplerDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var addressModeU: WGPUAddressMode
            get() = handle.addressModeU.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeU = value.toInt() }
        override var addressModeV: WGPUAddressMode
            get() = handle.addressModeV.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeV = value.toInt() }
        override var addressModeW: WGPUAddressMode
            get() = handle.addressModeW.toUInt() as WGPUAddressMode
            set(value) { handle.addressModeW = value.toInt() }
        override var magFilter: WGPUFilterMode
            get() = handle.magFilter.toUInt() as WGPUFilterMode
            set(value) { handle.magFilter = value.toInt() }
        override var minFilter: WGPUFilterMode
            get() = handle.minFilter.toUInt() as WGPUFilterMode
            set(value) { handle.minFilter = value.toInt() }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = handle.mipmapFilter.toUInt() as WGPUMipmapFilterMode
            set(value) { handle.mipmapFilter = value.toInt() }
        override var lodMinClamp: Float
            get() = handle.lodMinClamp as Float
            set(value) { handle.lodMinClamp = value }
        override var lodMaxClamp: Float
            get() = handle.lodMaxClamp as Float
            set(value) { handle.lodMaxClamp = value }
        override var compare: WGPUCompareFunction
            get() = handle.compare.toUInt() as WGPUCompareFunction
            set(value) { handle.compare = value.toInt() }
        override var maxAnisotropy: UShort
            get() = handle.maxAnisotropy.toUShort() as UShort
            set(value) { handle.maxAnisotropy = value.toShort() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUShaderSourceSPIRV {
    actual var chain: WGPUChainedStruct
    actual var codeSize: UInt
    actual var code: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceSPIRV {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceSPIRV {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceSPIRV) -> Unit): ArrayHolder<WGPUShaderSourceSPIRV> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByReference = io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceSPIRV {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var codeSize: UInt
            get() = handle.codeSize.toUInt() as UInt
            set(value) { handle.codeSize = value.toInt() }
        override var code: NativeAddress?
            get() = handle.code
            set(value) { handle.code = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByValue = io.ygdrasil.wgpu.android.WGPUShaderSourceSPIRV.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceSPIRV {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var codeSize: UInt
            get() = handle.codeSize.toUInt() as UInt
            set(value) { handle.codeSize = value.toInt() }
        override var code: NativeAddress?
            get() = handle.code
            set(value) { handle.code = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUShaderSourceWGSL {
    actual var chain: WGPUChainedStruct
    actual var code: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceWGSL {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceWGSL {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceWGSL) -> Unit): ArrayHolder<WGPUShaderSourceWGSL> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByReference = io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceWGSL {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var code: WGPUStringView
            get() {
                handle.readField("code")
                return WGPUStringView.ByValue(handle.code)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.code.size())
                handle.readField("code")
                handle.code.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("code")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByValue = io.ygdrasil.wgpu.android.WGPUShaderSourceWGSL.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceWGSL {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var code: WGPUStringView
            get() {
                handle.readField("code")
                return WGPUStringView.ByValue(handle.code)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.code.size())
                handle.readField("code")
                handle.code.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("code")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUStencilFaceState {
    actual var compare: WGPUCompareFunction
    actual var failOp: WGPUStencilOperation
    actual var depthFailOp: WGPUStencilOperation
    actual var passOp: WGPUStencilOperation
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStencilFaceState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUStencilFaceState {
            val ref = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStencilFaceState) -> Unit): ArrayHolder<WGPUStencilFaceState> {
            val ref = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByReference = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUStencilFaceState {
        override var compare: WGPUCompareFunction
            get() = handle.compare.toUInt() as WGPUCompareFunction
            set(value) { handle.compare = value.toInt() }
        override var failOp: WGPUStencilOperation
            get() = handle.failOp.toUInt() as WGPUStencilOperation
            set(value) { handle.failOp = value.toInt() }
        override var depthFailOp: WGPUStencilOperation
            get() = handle.depthFailOp.toUInt() as WGPUStencilOperation
            set(value) { handle.depthFailOp = value.toInt() }
        override var passOp: WGPUStencilOperation
            get() = handle.passOp.toUInt() as WGPUStencilOperation
            set(value) { handle.passOp = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUStencilFaceState {
        override var compare: WGPUCompareFunction
            get() = handle.compare.toUInt() as WGPUCompareFunction
            set(value) { handle.compare = value.toInt() }
        override var failOp: WGPUStencilOperation
            get() = handle.failOp.toUInt() as WGPUStencilOperation
            set(value) { handle.failOp = value.toInt() }
        override var depthFailOp: WGPUStencilOperation
            get() = handle.depthFailOp.toUInt() as WGPUStencilOperation
            set(value) { handle.depthFailOp = value.toInt() }
        override var passOp: WGPUStencilOperation
            get() = handle.passOp.toUInt() as WGPUStencilOperation
            set(value) { handle.passOp = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUStorageTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var access: WGPUStorageTextureAccess
    actual var format: WGPUTextureFormat
    actual var viewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStorageTextureBindingLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUStorageTextureBindingLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStorageTextureBindingLayout) -> Unit): ArrayHolder<WGPUStorageTextureBindingLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByReference = io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUStorageTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var access: WGPUStorageTextureAccess
            get() = handle.access.toUInt() as WGPUStorageTextureAccess
            set(value) { handle.access = value.toInt() }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.viewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.viewDimension = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUStorageTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var access: WGPUStorageTextureAccess
            get() = handle.access.toUInt() as WGPUStorageTextureAccess
            set(value) { handle.access = value.toInt() }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.viewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.viewDimension = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSupportedFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedFeatures {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedFeatures {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedFeatures) -> Unit): ArrayHolder<WGPUSupportedFeatures> {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByReference = io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSupportedFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue = io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSupportedFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSupportedInstanceFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedInstanceFeatures {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedInstanceFeatures {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedInstanceFeatures) -> Unit): ArrayHolder<WGPUSupportedInstanceFeatures> {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByReference = io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSupportedInstanceFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue = io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSupportedInstanceFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSupportedWGSLLanguageFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedWGSLLanguageFeatures {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedWGSLLanguageFeatures {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedWGSLLanguageFeatures) -> Unit): ArrayHolder<WGPUSupportedWGSLLanguageFeatures> {
            val ref = io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByReference = io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSupportedWGSLLanguageFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue = io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSupportedWGSLLanguageFeatures {
        override var featureCount: ULong
            get() = handle.featureCount.toULong() as ULong
            set(value) { handle.featureCount = value.toLong() }
        override var features: NativeAddress?
            get() = handle.features
            set(value) { handle.features = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceCapabilities {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceCapabilities {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceCapabilities) -> Unit): ArrayHolder<WGPUSurfaceCapabilities> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceCapabilities {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var usages: ULong
            get() = handle.usages.toULong() as ULong
            set(value) { handle.usages = value.toLong() }
        override var formatCount: ULong
            get() = handle.formatCount.toULong() as ULong
            set(value) { handle.formatCount = value.toLong() }
        override var formats: NativeAddress?
            get() = handle.formats
            set(value) { handle.formats = value }
        override var presentModeCount: ULong
            get() = handle.presentModeCount.toULong() as ULong
            set(value) { handle.presentModeCount = value.toLong() }
        override var presentModes: NativeAddress?
            get() = handle.presentModes
            set(value) { handle.presentModes = value }
        override var alphaModeCount: ULong
            get() = handle.alphaModeCount.toULong() as ULong
            set(value) { handle.alphaModeCount = value.toLong() }
        override var alphaModes: NativeAddress?
            get() = handle.alphaModes
            set(value) { handle.alphaModes = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceCapabilities {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var usages: ULong
            get() = handle.usages.toULong() as ULong
            set(value) { handle.usages = value.toLong() }
        override var formatCount: ULong
            get() = handle.formatCount.toULong() as ULong
            set(value) { handle.formatCount = value.toLong() }
        override var formats: NativeAddress?
            get() = handle.formats
            set(value) { handle.formats = value }
        override var presentModeCount: ULong
            get() = handle.presentModeCount.toULong() as ULong
            set(value) { handle.presentModeCount = value.toLong() }
        override var presentModes: NativeAddress?
            get() = handle.presentModes
            set(value) { handle.presentModes = value }
        override var alphaModeCount: ULong
            get() = handle.alphaModeCount.toULong() as ULong
            set(value) { handle.alphaModeCount = value.toLong() }
        override var alphaModes: NativeAddress?
            get() = handle.alphaModes
            set(value) { handle.alphaModes = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceColorManagement {
    actual var chain: WGPUChainedStruct
    actual var colorSpace: WGPUPredefinedColorSpace
    actual var toneMappingMode: WGPUToneMappingMode
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceColorManagement {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceColorManagement {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceColorManagement) -> Unit): ArrayHolder<WGPUSurfaceColorManagement> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceColorManagement {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = handle.colorSpace.toUInt() as WGPUPredefinedColorSpace
            set(value) { handle.colorSpace = value.toInt() }
        override var toneMappingMode: WGPUToneMappingMode
            get() = handle.toneMappingMode.toUInt() as WGPUToneMappingMode
            set(value) { handle.toneMappingMode = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceColorManagement.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceColorManagement {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = handle.colorSpace.toUInt() as WGPUPredefinedColorSpace
            set(value) { handle.colorSpace = value.toInt() }
        override var toneMappingMode: WGPUToneMappingMode
            get() = handle.toneMappingMode.toUInt() as WGPUToneMappingMode
            set(value) { handle.toneMappingMode = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfiguration {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfiguration {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfiguration) -> Unit): ArrayHolder<WGPUSurfaceConfiguration> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceConfiguration {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var device: WGPUDevice?
            get() = handle.device?.let { WGPUDevice(it) }
            set(value) { handle.device = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var width: UInt
            get() = handle.width.toUInt() as UInt
            set(value) { handle.width = value.toInt() }
        override var height: UInt
            get() = handle.height.toUInt() as UInt
            set(value) { handle.height = value.toInt() }
        override var viewFormatCount: ULong
            get() = handle.viewFormatCount.toULong() as ULong
            set(value) { handle.viewFormatCount = value.toLong() }
        override var viewFormats: NativeAddress?
            get() = handle.viewFormats
            set(value) { handle.viewFormats = value }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = handle.alphaMode.toUInt() as WGPUCompositeAlphaMode
            set(value) { handle.alphaMode = value.toInt() }
        override var presentMode: WGPUPresentMode
            get() = handle.presentMode.toUInt() as WGPUPresentMode
            set(value) { handle.presentMode = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceConfiguration.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceConfiguration {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var device: WGPUDevice?
            get() = handle.device?.let { WGPUDevice(it) }
            set(value) { handle.device = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var width: UInt
            get() = handle.width.toUInt() as UInt
            set(value) { handle.width = value.toInt() }
        override var height: UInt
            get() = handle.height.toUInt() as UInt
            set(value) { handle.height = value.toInt() }
        override var viewFormatCount: ULong
            get() = handle.viewFormatCount.toULong() as ULong
            set(value) { handle.viewFormatCount = value.toLong() }
        override var viewFormats: NativeAddress?
            get() = handle.viewFormats
            set(value) { handle.viewFormats = value }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = handle.alphaMode.toUInt() as WGPUCompositeAlphaMode
            set(value) { handle.alphaMode = value.toInt() }
        override var presentMode: WGPUPresentMode
            get() = handle.presentMode.toUInt() as WGPUPresentMode
            set(value) { handle.presentMode = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceAndroidNativeWindow {
    actual var chain: WGPUChainedStruct
    actual var window: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceAndroidNativeWindow {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceAndroidNativeWindow {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceAndroidNativeWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceAndroidNativeWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var window: NativeAddress?
            get() = handle.window
            set(value) { handle.window = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceAndroidNativeWindow.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceAndroidNativeWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var window: NativeAddress?
            get() = handle.window
            set(value) { handle.window = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceMetalLayer {
    actual var chain: WGPUChainedStruct
    actual var layer: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceMetalLayer {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceMetalLayer {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceMetalLayer) -> Unit): ArrayHolder<WGPUSurfaceSourceMetalLayer> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceMetalLayer {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var layer: NativeAddress?
            get() = handle.layer
            set(value) { handle.layer = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceMetalLayer.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceMetalLayer {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var layer: NativeAddress?
            get() = handle.layer
            set(value) { handle.layer = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceWaylandSurface {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var surface: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWaylandSurface {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWaylandSurface {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWaylandSurface) -> Unit): ArrayHolder<WGPUSurfaceSourceWaylandSurface> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceWaylandSurface {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var surface: NativeAddress?
            get() = handle.surface
            set(value) { handle.surface = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWaylandSurface.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceWaylandSurface {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var surface: NativeAddress?
            get() = handle.surface
            set(value) { handle.surface = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceWindowsHWND {
    actual var chain: WGPUChainedStruct
    actual var hinstance: NativeAddress?
    actual var hwnd: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWindowsHWND {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWindowsHWND {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWindowsHWND) -> Unit): ArrayHolder<WGPUSurfaceSourceWindowsHWND> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceWindowsHWND {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var hinstance: NativeAddress?
            get() = handle.hinstance
            set(value) { handle.hinstance = value }
        override var hwnd: NativeAddress?
            get() = handle.hwnd
            set(value) { handle.hwnd = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceWindowsHWND.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceWindowsHWND {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var hinstance: NativeAddress?
            get() = handle.hinstance
            set(value) { handle.hinstance = value }
        override var hwnd: NativeAddress?
            get() = handle.hwnd
            set(value) { handle.hwnd = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceXCBWindow {
    actual var chain: WGPUChainedStruct
    actual var connection: NativeAddress?
    actual var window: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXCBWindow {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXCBWindow {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXCBWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXCBWindow> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceXCBWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var connection: NativeAddress?
            get() = handle.connection
            set(value) { handle.connection = value }
        override var window: UInt
            get() = handle.window.toUInt() as UInt
            set(value) { handle.window = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXCBWindow.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceXCBWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var connection: NativeAddress?
            get() = handle.connection
            set(value) { handle.connection = value }
        override var window: UInt
            get() = handle.window.toUInt() as UInt
            set(value) { handle.window = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceXlibWindow {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var window: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXlibWindow {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXlibWindow {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXlibWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXlibWindow> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceXlibWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var window: ULong
            get() = handle.window.toULong() as ULong
            set(value) { handle.window = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceXlibWindow.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceXlibWindow {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var window: ULong
            get() = handle.window.toULong() as ULong
            set(value) { handle.window = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceTexture {
    actual var nextInChain: WGPUChainedStruct?
    actual var texture: WGPUTexture?
    actual var status: WGPUSurfaceGetCurrentTextureStatus
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceTexture {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceTexture {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceTexture) -> Unit): ArrayHolder<WGPUSurfaceTexture> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceTexture {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var texture: WGPUTexture?
            get() = handle.texture?.let { WGPUTexture(it) }
            set(value) { handle.texture = value?.handler }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = handle.status.toUInt() as WGPUSurfaceGetCurrentTextureStatus
            set(value) { handle.status = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceTexture.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceTexture {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var texture: WGPUTexture?
            get() = handle.texture?.let { WGPUTexture(it) }
            set(value) { handle.texture = value?.handler }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = handle.status.toUInt() as WGPUSurfaceGetCurrentTextureStatus
            set(value) { handle.status = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTexelCopyBufferLayout {
    actual var offset: ULong
    actual var bytesPerRow: UInt
    actual var rowsPerImage: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferLayout) -> Unit): ArrayHolder<WGPUTexelCopyBufferLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByReference = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyBufferLayout {
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var bytesPerRow: UInt
            get() = handle.bytesPerRow.toUInt() as UInt
            set(value) { handle.bytesPerRow = value.toInt() }
        override var rowsPerImage: UInt
            get() = handle.rowsPerImage.toUInt() as UInt
            set(value) { handle.rowsPerImage = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyBufferLayout {
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var bytesPerRow: UInt
            get() = handle.bytesPerRow.toUInt() as UInt
            set(value) { handle.bytesPerRow = value.toInt() }
        override var rowsPerImage: UInt
            get() = handle.rowsPerImage.toUInt() as UInt
            set(value) { handle.rowsPerImage = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var sampleType: WGPUTextureSampleType
    actual var viewDimension: WGPUTextureViewDimension
    actual var multisampled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingLayout) -> Unit): ArrayHolder<WGPUTextureBindingLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByReference = io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var sampleType: WGPUTextureSampleType
            get() = handle.sampleType.toUInt() as WGPUTextureSampleType
            set(value) { handle.sampleType = value.toInt() }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.viewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.viewDimension = value.toInt() }
        override var multisampled: UInt
            get() = handle.multisampled.toUInt() as UInt
            set(value) { handle.multisampled = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureBindingLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var sampleType: WGPUTextureSampleType
            get() = handle.sampleType.toUInt() as WGPUTextureSampleType
            set(value) { handle.sampleType = value.toInt() }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.viewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.viewDimension = value.toInt() }
        override var multisampled: UInt
            get() = handle.multisampled.toUInt() as UInt
            set(value) { handle.multisampled = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTextureBindingViewDimension {
    actual var chain: WGPUChainedStruct
    actual var textureBindingViewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingViewDimension {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingViewDimension {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingViewDimension) -> Unit): ArrayHolder<WGPUTextureBindingViewDimension> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByReference = io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureBindingViewDimension {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = handle.textureBindingViewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.textureBindingViewDimension = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByValue = io.ygdrasil.wgpu.android.WGPUTextureBindingViewDimension.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureBindingViewDimension {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = handle.textureBindingViewDimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.textureBindingViewDimension = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTextureComponentSwizzle {
    actual var r: WGPUComponentSwizzle
    actual var g: WGPUComponentSwizzle
    actual var b: WGPUComponentSwizzle
    actual var a: WGPUComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzle {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzle {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzle) -> Unit): ArrayHolder<WGPUTextureComponentSwizzle> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByReference = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureComponentSwizzle {
        override var r: WGPUComponentSwizzle
            get() = handle.r.toUInt() as WGPUComponentSwizzle
            set(value) { handle.r = value.toInt() }
        override var g: WGPUComponentSwizzle
            get() = handle.g.toUInt() as WGPUComponentSwizzle
            set(value) { handle.g = value.toInt() }
        override var b: WGPUComponentSwizzle
            get() = handle.b.toUInt() as WGPUComponentSwizzle
            set(value) { handle.b = value.toInt() }
        override var a: WGPUComponentSwizzle
            get() = handle.a.toUInt() as WGPUComponentSwizzle
            set(value) { handle.a = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureComponentSwizzle {
        override var r: WGPUComponentSwizzle
            get() = handle.r.toUInt() as WGPUComponentSwizzle
            set(value) { handle.r = value.toInt() }
        override var g: WGPUComponentSwizzle
            get() = handle.g.toUInt() as WGPUComponentSwizzle
            set(value) { handle.g = value.toInt() }
        override var b: WGPUComponentSwizzle
            get() = handle.b.toUInt() as WGPUComponentSwizzle
            set(value) { handle.b = value.toInt() }
        override var a: WGPUComponentSwizzle
            get() = handle.a.toUInt() as WGPUComponentSwizzle
            set(value) { handle.a = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUVertexAttribute {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUVertexFormat
    actual var offset: ULong
    actual var shaderLocation: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUVertexAttribute {
            return ByReference(io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexAttribute {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexAttribute) -> Unit): ArrayHolder<WGPUVertexAttribute> {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByReference = io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByReference(com.sun.jna.Pointer.NULL)) : WGPUVertexAttribute {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUVertexFormat
            get() = handle.format.toUInt() as WGPUVertexFormat
            set(value) { handle.format = value.toInt() }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var shaderLocation: UInt
            get() = handle.shaderLocation.toUInt() as UInt
            set(value) { handle.shaderLocation = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByValue = io.ygdrasil.wgpu.android.WGPUVertexAttribute.ByValue(com.sun.jna.Pointer.NULL)) : WGPUVertexAttribute {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUVertexFormat
            get() = handle.format.toUInt() as WGPUVertexFormat
            set(value) { handle.format = value.toInt() }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var shaderLocation: UInt
            get() = handle.shaderLocation.toUInt() as UInt
            set(value) { handle.shaderLocation = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntry {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntry {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntry) -> Unit): ArrayHolder<WGPUBindGroupEntry> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var binding: UInt
            get() = handle.binding.toUInt() as UInt
            set(value) { handle.binding = value.toInt() }
        override var buffer: WGPUBuffer?
            get() = handle.buffer?.let { WGPUBuffer(it) }
            set(value) { handle.buffer = value?.handler }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var size: ULong
            get() = handle.size.toULong() as ULong
            set(value) { handle.size = value.toLong() }
        override var sampler: WGPUSampler?
            get() = handle.sampler?.let { WGPUSampler(it) }
            set(value) { handle.sampler = value?.handler }
        override var textureView: WGPUTextureView?
            get() = handle.textureView?.let { WGPUTextureView(it) }
            set(value) { handle.textureView = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupEntry.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var binding: UInt
            get() = handle.binding.toUInt() as UInt
            set(value) { handle.binding = value.toInt() }
        override var buffer: WGPUBuffer?
            get() = handle.buffer?.let { WGPUBuffer(it) }
            set(value) { handle.buffer = value?.handler }
        override var offset: ULong
            get() = handle.offset.toULong() as ULong
            set(value) { handle.offset = value.toLong() }
        override var size: ULong
            get() = handle.size.toULong() as ULong
            set(value) { handle.size = value.toLong() }
        override var sampler: WGPUSampler?
            get() = handle.sampler?.let { WGPUSampler(it) }
            set(value) { handle.sampler = value?.handler }
        override var textureView: WGPUTextureView?
            get() = handle.textureView?.let { WGPUTextureView(it) }
            set(value) { handle.textureView = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntry {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntry {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntry) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntry> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var binding: UInt
            get() = handle.binding.toUInt() as UInt
            set(value) { handle.binding = value.toInt() }
        override var visibility: ULong
            get() = handle.visibility.toULong() as ULong
            set(value) { handle.visibility = value.toLong() }
        override var bindingArraySize: UInt
            get() = handle.bindingArraySize.toUInt() as UInt
            set(value) { handle.bindingArraySize = value.toInt() }
        override var buffer: WGPUBufferBindingLayout
            get() {
                handle.readField("buffer")
                return WGPUBufferBindingLayout.ByValue(handle.buffer)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.buffer.size())
                handle.readField("buffer")
                handle.buffer.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("buffer")
            }
        override var sampler: WGPUSamplerBindingLayout
            get() {
                handle.readField("sampler")
                return WGPUSamplerBindingLayout.ByValue(handle.sampler)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.sampler.size())
                handle.readField("sampler")
                handle.sampler.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("sampler")
            }
        override var texture: WGPUTextureBindingLayout
            get() {
                handle.readField("texture")
                return WGPUTextureBindingLayout.ByValue(handle.texture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.texture.size())
                handle.readField("texture")
                handle.texture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("texture")
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() {
                handle.readField("storageTexture")
                return WGPUStorageTextureBindingLayout.ByValue(handle.storageTexture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.storageTexture.size())
                handle.readField("storageTexture")
                handle.storageTexture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("storageTexture")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntry.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutEntry {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var binding: UInt
            get() = handle.binding.toUInt() as UInt
            set(value) { handle.binding = value.toInt() }
        override var visibility: ULong
            get() = handle.visibility.toULong() as ULong
            set(value) { handle.visibility = value.toLong() }
        override var bindingArraySize: UInt
            get() = handle.bindingArraySize.toUInt() as UInt
            set(value) { handle.bindingArraySize = value.toInt() }
        override var buffer: WGPUBufferBindingLayout
            get() {
                handle.readField("buffer")
                return WGPUBufferBindingLayout.ByValue(handle.buffer)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.buffer.size())
                handle.readField("buffer")
                handle.buffer.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("buffer")
            }
        override var sampler: WGPUSamplerBindingLayout
            get() {
                handle.readField("sampler")
                return WGPUSamplerBindingLayout.ByValue(handle.sampler)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.sampler.size())
                handle.readField("sampler")
                handle.sampler.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("sampler")
            }
        override var texture: WGPUTextureBindingLayout
            get() {
                handle.readField("texture")
                return WGPUTextureBindingLayout.ByValue(handle.texture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.texture.size())
                handle.readField("texture")
                handle.texture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("texture")
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() {
                handle.readField("storageTexture")
                return WGPUStorageTextureBindingLayout.ByValue(handle.storageTexture)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.storageTexture.size())
                handle.readField("storageTexture")
                handle.storageTexture.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("storageTexture")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUBlendState {
    actual var color: WGPUBlendComponent
    actual var alpha: WGPUBlendComponent
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBlendState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendState {
            val ref = io.ygdrasil.wgpu.android.WGPUBlendState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendState) -> Unit): ArrayHolder<WGPUBlendState> {
            val ref = io.ygdrasil.wgpu.android.WGPUBlendState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBlendState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBlendState.ByReference = io.ygdrasil.wgpu.android.WGPUBlendState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBlendState {
        override var color: WGPUBlendComponent
            get() {
                handle.readField("color")
                return WGPUBlendComponent.ByValue(handle.color)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.color.size())
                handle.readField("color")
                handle.color.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("color")
            }
        override var alpha: WGPUBlendComponent
            get() {
                handle.readField("alpha")
                return WGPUBlendComponent.ByValue(handle.alpha)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.alpha.size())
                handle.readField("alpha")
                handle.alpha.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("alpha")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBlendState.ByValue = io.ygdrasil.wgpu.android.WGPUBlendState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBlendState {
        override var color: WGPUBlendComponent
            get() {
                handle.readField("color")
                return WGPUBlendComponent.ByValue(handle.color)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.color.size())
                handle.readField("color")
                handle.color.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("color")
            }
        override var alpha: WGPUBlendComponent
            get() {
                handle.readField("alpha")
                return WGPUBlendComponent.ByValue(handle.alpha)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.alpha.size())
                handle.readField("alpha")
                handle.alpha.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("alpha")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUCompilationInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var messageCount: ULong
    actual var messages: WGPUCompilationMessage?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfo) -> Unit): ArrayHolder<WGPUCompilationInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByReference = io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUCompilationInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var messageCount: ULong
            get() = handle.messageCount.toULong() as ULong
            set(value) { handle.messageCount = value.toLong() }
        override var messages: WGPUCompilationMessage?
            get() = handle.messages?.let { WGPUCompilationMessage(it) }
            set(value) { handle.messages = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByValue = io.ygdrasil.wgpu.android.WGPUCompilationInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUCompilationInfo {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var messageCount: ULong
            get() = handle.messageCount.toULong() as ULong
            set(value) { handle.messageCount = value.toLong() }
        override var messages: WGPUCompilationMessage?
            get() = handle.messages?.let { WGPUCompilationMessage(it) }
            set(value) { handle.messages = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUComputePassDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePassDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePassDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePassDescriptor) -> Unit): ArrayHolder<WGPUComputePassDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUComputePassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.timestampWrites?.let { WGPUPassTimestampWrites(it) }
            set(value) { handle.timestampWrites = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUComputePassDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUComputePassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.timestampWrites?.let { WGPUPassTimestampWrites(it) }
            set(value) { handle.timestampWrites = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUComputeState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUComputeState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUComputeState {
            val ref = io.ygdrasil.wgpu.android.WGPUComputeState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputeState) -> Unit): ArrayHolder<WGPUComputeState> {
            val ref = io.ygdrasil.wgpu.android.WGPUComputeState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUComputeState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUComputeState.ByReference = io.ygdrasil.wgpu.android.WGPUComputeState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUComputeState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUComputeState.ByValue = io.ygdrasil.wgpu.android.WGPUComputeState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUComputeState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUDepthStencilState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUDepthStencilState {
            val ref = io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDepthStencilState) -> Unit): ArrayHolder<WGPUDepthStencilState> {
            val ref = io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByReference = io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUDepthStencilState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = handle.depthWriteEnabled.toUInt() as WGPUOptionalBool
            set(value) { handle.depthWriteEnabled = value.toInt() }
        override var depthCompare: WGPUCompareFunction
            get() = handle.depthCompare.toUInt() as WGPUCompareFunction
            set(value) { handle.depthCompare = value.toInt() }
        override var stencilFront: WGPUStencilFaceState
            get() {
                handle.readField("stencilFront")
                return WGPUStencilFaceState.ByValue(handle.stencilFront)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.stencilFront.size())
                handle.readField("stencilFront")
                handle.stencilFront.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("stencilFront")
            }
        override var stencilBack: WGPUStencilFaceState
            get() {
                handle.readField("stencilBack")
                return WGPUStencilFaceState.ByValue(handle.stencilBack)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.stencilBack.size())
                handle.readField("stencilBack")
                handle.stencilBack.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("stencilBack")
            }
        override var stencilReadMask: UInt
            get() = handle.stencilReadMask.toUInt() as UInt
            set(value) { handle.stencilReadMask = value.toInt() }
        override var stencilWriteMask: UInt
            get() = handle.stencilWriteMask.toUInt() as UInt
            set(value) { handle.stencilWriteMask = value.toInt() }
        override var depthBias: Int
            get() = handle.depthBias as Int
            set(value) { handle.depthBias = value }
        override var depthBiasSlopeScale: Float
            get() = handle.depthBiasSlopeScale as Float
            set(value) { handle.depthBiasSlopeScale = value }
        override var depthBiasClamp: Float
            get() = handle.depthBiasClamp as Float
            set(value) { handle.depthBiasClamp = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByValue = io.ygdrasil.wgpu.android.WGPUDepthStencilState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUDepthStencilState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = handle.depthWriteEnabled.toUInt() as WGPUOptionalBool
            set(value) { handle.depthWriteEnabled = value.toInt() }
        override var depthCompare: WGPUCompareFunction
            get() = handle.depthCompare.toUInt() as WGPUCompareFunction
            set(value) { handle.depthCompare = value.toInt() }
        override var stencilFront: WGPUStencilFaceState
            get() {
                handle.readField("stencilFront")
                return WGPUStencilFaceState.ByValue(handle.stencilFront)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.stencilFront.size())
                handle.readField("stencilFront")
                handle.stencilFront.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("stencilFront")
            }
        override var stencilBack: WGPUStencilFaceState
            get() {
                handle.readField("stencilBack")
                return WGPUStencilFaceState.ByValue(handle.stencilBack)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.stencilBack.size())
                handle.readField("stencilBack")
                handle.stencilBack.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("stencilBack")
            }
        override var stencilReadMask: UInt
            get() = handle.stencilReadMask.toUInt() as UInt
            set(value) { handle.stencilReadMask = value.toInt() }
        override var stencilWriteMask: UInt
            get() = handle.stencilWriteMask.toUInt() as UInt
            set(value) { handle.stencilWriteMask = value.toInt() }
        override var depthBias: Int
            get() = handle.depthBias as Int
            set(value) { handle.depthBias = value }
        override var depthBiasSlopeScale: Float
            get() = handle.depthBiasSlopeScale as Float
            set(value) { handle.depthBiasSlopeScale = value }
        override var depthBiasClamp: Float
            get() = handle.depthBiasClamp as Float
            set(value) { handle.depthBiasClamp = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUFutureWaitInfo {
    actual var future: WGPUFuture
    actual var completed: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFutureWaitInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUFutureWaitInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFutureWaitInfo) -> Unit): ArrayHolder<WGPUFutureWaitInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByReference = io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUFutureWaitInfo {
        override var future: WGPUFuture
            get() {
                handle.readField("future")
                return WGPUFuture.ByValue(handle.future)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.future.size())
                handle.readField("future")
                handle.future.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("future")
            }
        override var completed: UInt
            get() = handle.completed.toUInt() as UInt
            set(value) { handle.completed = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByValue = io.ygdrasil.wgpu.android.WGPUFutureWaitInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUFutureWaitInfo {
        override var future: WGPUFuture
            get() {
                handle.readField("future")
                return WGPUFuture.ByValue(handle.future)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.future.size())
                handle.readField("future")
                handle.future.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("future")
            }
        override var completed: UInt
            get() = handle.completed.toUInt() as UInt
            set(value) { handle.completed = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUInstanceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPUInstanceLimits?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceDescriptor) -> Unit): ArrayHolder<WGPUInstanceDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUInstanceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var requiredFeatureCount: ULong
            get() = handle.requiredFeatureCount.toULong() as ULong
            set(value) { handle.requiredFeatureCount = value.toLong() }
        override var requiredFeatures: NativeAddress?
            get() = handle.requiredFeatures
            set(value) { handle.requiredFeatures = value }
        override var requiredLimits: WGPUInstanceLimits?
            get() = handle.requiredLimits?.let { WGPUInstanceLimits(it) }
            set(value) { handle.requiredLimits = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUInstanceDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUInstanceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var requiredFeatureCount: ULong
            get() = handle.requiredFeatureCount.toULong() as ULong
            set(value) { handle.requiredFeatureCount = value.toLong() }
        override var requiredFeatures: NativeAddress?
            get() = handle.requiredFeatures
            set(value) { handle.requiredFeatures = value }
        override var requiredLimits: WGPUInstanceLimits?
            get() = handle.requiredLimits?.let { WGPUInstanceLimits(it) }
            set(value) { handle.requiredLimits = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPULimits {
            return ByReference(io.ygdrasil.wgpu.android.WGPULimits.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPULimits {
            val ref = io.ygdrasil.wgpu.android.WGPULimits.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPULimits) -> Unit): ArrayHolder<WGPULimits> {
            val ref = io.ygdrasil.wgpu.android.WGPULimits.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPULimits.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPULimits.ByReference = io.ygdrasil.wgpu.android.WGPULimits.ByReference(com.sun.jna.Pointer.NULL)) : WGPULimits {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var maxTextureDimension1D: UInt
            get() = handle.maxTextureDimension1D.toUInt() as UInt
            set(value) { handle.maxTextureDimension1D = value.toInt() }
        override var maxTextureDimension2D: UInt
            get() = handle.maxTextureDimension2D.toUInt() as UInt
            set(value) { handle.maxTextureDimension2D = value.toInt() }
        override var maxTextureDimension3D: UInt
            get() = handle.maxTextureDimension3D.toUInt() as UInt
            set(value) { handle.maxTextureDimension3D = value.toInt() }
        override var maxTextureArrayLayers: UInt
            get() = handle.maxTextureArrayLayers.toUInt() as UInt
            set(value) { handle.maxTextureArrayLayers = value.toInt() }
        override var maxBindGroups: UInt
            get() = handle.maxBindGroups.toUInt() as UInt
            set(value) { handle.maxBindGroups = value.toInt() }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = handle.maxBindGroupsPlusVertexBuffers.toUInt() as UInt
            set(value) { handle.maxBindGroupsPlusVertexBuffers = value.toInt() }
        override var maxBindingsPerBindGroup: UInt
            get() = handle.maxBindingsPerBindGroup.toUInt() as UInt
            set(value) { handle.maxBindingsPerBindGroup = value.toInt() }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = handle.maxDynamicUniformBuffersPerPipelineLayout.toUInt() as UInt
            set(value) { handle.maxDynamicUniformBuffersPerPipelineLayout = value.toInt() }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = handle.maxDynamicStorageBuffersPerPipelineLayout.toUInt() as UInt
            set(value) { handle.maxDynamicStorageBuffersPerPipelineLayout = value.toInt() }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = handle.maxSampledTexturesPerShaderStage.toUInt() as UInt
            set(value) { handle.maxSampledTexturesPerShaderStage = value.toInt() }
        override var maxSamplersPerShaderStage: UInt
            get() = handle.maxSamplersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxSamplersPerShaderStage = value.toInt() }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = handle.maxStorageBuffersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersPerShaderStage = value.toInt() }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = handle.maxStorageTexturesPerShaderStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesPerShaderStage = value.toInt() }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = handle.maxUniformBuffersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxUniformBuffersPerShaderStage = value.toInt() }
        override var maxUniformBufferBindingSize: ULong
            get() = handle.maxUniformBufferBindingSize.toULong() as ULong
            set(value) { handle.maxUniformBufferBindingSize = value.toLong() }
        override var maxStorageBufferBindingSize: ULong
            get() = handle.maxStorageBufferBindingSize.toULong() as ULong
            set(value) { handle.maxStorageBufferBindingSize = value.toLong() }
        override var minUniformBufferOffsetAlignment: UInt
            get() = handle.minUniformBufferOffsetAlignment.toUInt() as UInt
            set(value) { handle.minUniformBufferOffsetAlignment = value.toInt() }
        override var minStorageBufferOffsetAlignment: UInt
            get() = handle.minStorageBufferOffsetAlignment.toUInt() as UInt
            set(value) { handle.minStorageBufferOffsetAlignment = value.toInt() }
        override var maxVertexBuffers: UInt
            get() = handle.maxVertexBuffers.toUInt() as UInt
            set(value) { handle.maxVertexBuffers = value.toInt() }
        override var maxBufferSize: ULong
            get() = handle.maxBufferSize.toULong() as ULong
            set(value) { handle.maxBufferSize = value.toLong() }
        override var maxVertexAttributes: UInt
            get() = handle.maxVertexAttributes.toUInt() as UInt
            set(value) { handle.maxVertexAttributes = value.toInt() }
        override var maxVertexBufferArrayStride: UInt
            get() = handle.maxVertexBufferArrayStride.toUInt() as UInt
            set(value) { handle.maxVertexBufferArrayStride = value.toInt() }
        override var maxInterStageShaderVariables: UInt
            get() = handle.maxInterStageShaderVariables.toUInt() as UInt
            set(value) { handle.maxInterStageShaderVariables = value.toInt() }
        override var maxColorAttachments: UInt
            get() = handle.maxColorAttachments.toUInt() as UInt
            set(value) { handle.maxColorAttachments = value.toInt() }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = handle.maxColorAttachmentBytesPerSample.toUInt() as UInt
            set(value) { handle.maxColorAttachmentBytesPerSample = value.toInt() }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = handle.maxComputeWorkgroupStorageSize.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupStorageSize = value.toInt() }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = handle.maxComputeInvocationsPerWorkgroup.toUInt() as UInt
            set(value) { handle.maxComputeInvocationsPerWorkgroup = value.toInt() }
        override var maxComputeWorkgroupSizeX: UInt
            get() = handle.maxComputeWorkgroupSizeX.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeX = value.toInt() }
        override var maxComputeWorkgroupSizeY: UInt
            get() = handle.maxComputeWorkgroupSizeY.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeY = value.toInt() }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = handle.maxComputeWorkgroupSizeZ.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeZ = value.toInt() }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = handle.maxComputeWorkgroupsPerDimension.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupsPerDimension = value.toInt() }
        override var maxImmediateSize: UInt
            get() = handle.maxImmediateSize.toUInt() as UInt
            set(value) { handle.maxImmediateSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPULimits.ByValue = io.ygdrasil.wgpu.android.WGPULimits.ByValue(com.sun.jna.Pointer.NULL)) : WGPULimits {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var maxTextureDimension1D: UInt
            get() = handle.maxTextureDimension1D.toUInt() as UInt
            set(value) { handle.maxTextureDimension1D = value.toInt() }
        override var maxTextureDimension2D: UInt
            get() = handle.maxTextureDimension2D.toUInt() as UInt
            set(value) { handle.maxTextureDimension2D = value.toInt() }
        override var maxTextureDimension3D: UInt
            get() = handle.maxTextureDimension3D.toUInt() as UInt
            set(value) { handle.maxTextureDimension3D = value.toInt() }
        override var maxTextureArrayLayers: UInt
            get() = handle.maxTextureArrayLayers.toUInt() as UInt
            set(value) { handle.maxTextureArrayLayers = value.toInt() }
        override var maxBindGroups: UInt
            get() = handle.maxBindGroups.toUInt() as UInt
            set(value) { handle.maxBindGroups = value.toInt() }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = handle.maxBindGroupsPlusVertexBuffers.toUInt() as UInt
            set(value) { handle.maxBindGroupsPlusVertexBuffers = value.toInt() }
        override var maxBindingsPerBindGroup: UInt
            get() = handle.maxBindingsPerBindGroup.toUInt() as UInt
            set(value) { handle.maxBindingsPerBindGroup = value.toInt() }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = handle.maxDynamicUniformBuffersPerPipelineLayout.toUInt() as UInt
            set(value) { handle.maxDynamicUniformBuffersPerPipelineLayout = value.toInt() }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = handle.maxDynamicStorageBuffersPerPipelineLayout.toUInt() as UInt
            set(value) { handle.maxDynamicStorageBuffersPerPipelineLayout = value.toInt() }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = handle.maxSampledTexturesPerShaderStage.toUInt() as UInt
            set(value) { handle.maxSampledTexturesPerShaderStage = value.toInt() }
        override var maxSamplersPerShaderStage: UInt
            get() = handle.maxSamplersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxSamplersPerShaderStage = value.toInt() }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = handle.maxStorageBuffersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxStorageBuffersPerShaderStage = value.toInt() }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = handle.maxStorageTexturesPerShaderStage.toUInt() as UInt
            set(value) { handle.maxStorageTexturesPerShaderStage = value.toInt() }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = handle.maxUniformBuffersPerShaderStage.toUInt() as UInt
            set(value) { handle.maxUniformBuffersPerShaderStage = value.toInt() }
        override var maxUniformBufferBindingSize: ULong
            get() = handle.maxUniformBufferBindingSize.toULong() as ULong
            set(value) { handle.maxUniformBufferBindingSize = value.toLong() }
        override var maxStorageBufferBindingSize: ULong
            get() = handle.maxStorageBufferBindingSize.toULong() as ULong
            set(value) { handle.maxStorageBufferBindingSize = value.toLong() }
        override var minUniformBufferOffsetAlignment: UInt
            get() = handle.minUniformBufferOffsetAlignment.toUInt() as UInt
            set(value) { handle.minUniformBufferOffsetAlignment = value.toInt() }
        override var minStorageBufferOffsetAlignment: UInt
            get() = handle.minStorageBufferOffsetAlignment.toUInt() as UInt
            set(value) { handle.minStorageBufferOffsetAlignment = value.toInt() }
        override var maxVertexBuffers: UInt
            get() = handle.maxVertexBuffers.toUInt() as UInt
            set(value) { handle.maxVertexBuffers = value.toInt() }
        override var maxBufferSize: ULong
            get() = handle.maxBufferSize.toULong() as ULong
            set(value) { handle.maxBufferSize = value.toLong() }
        override var maxVertexAttributes: UInt
            get() = handle.maxVertexAttributes.toUInt() as UInt
            set(value) { handle.maxVertexAttributes = value.toInt() }
        override var maxVertexBufferArrayStride: UInt
            get() = handle.maxVertexBufferArrayStride.toUInt() as UInt
            set(value) { handle.maxVertexBufferArrayStride = value.toInt() }
        override var maxInterStageShaderVariables: UInt
            get() = handle.maxInterStageShaderVariables.toUInt() as UInt
            set(value) { handle.maxInterStageShaderVariables = value.toInt() }
        override var maxColorAttachments: UInt
            get() = handle.maxColorAttachments.toUInt() as UInt
            set(value) { handle.maxColorAttachments = value.toInt() }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = handle.maxColorAttachmentBytesPerSample.toUInt() as UInt
            set(value) { handle.maxColorAttachmentBytesPerSample = value.toInt() }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = handle.maxComputeWorkgroupStorageSize.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupStorageSize = value.toInt() }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = handle.maxComputeInvocationsPerWorkgroup.toUInt() as UInt
            set(value) { handle.maxComputeInvocationsPerWorkgroup = value.toInt() }
        override var maxComputeWorkgroupSizeX: UInt
            get() = handle.maxComputeWorkgroupSizeX.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeX = value.toInt() }
        override var maxComputeWorkgroupSizeY: UInt
            get() = handle.maxComputeWorkgroupSizeY.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeY = value.toInt() }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = handle.maxComputeWorkgroupSizeZ.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupSizeZ = value.toInt() }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = handle.maxComputeWorkgroupsPerDimension.toUInt() as UInt
            set(value) { handle.maxComputeWorkgroupsPerDimension = value.toInt() }
        override var maxImmediateSize: UInt
            get() = handle.maxImmediateSize.toUInt() as UInt
            set(value) { handle.maxImmediateSize = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURenderPassColorAttachment {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassColorAttachment {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassColorAttachment) -> Unit): ArrayHolder<WGPURenderPassColorAttachment> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByReference = io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderPassColorAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var view: WGPUTextureView?
            get() = handle.view?.let { WGPUTextureView(it) }
            set(value) { handle.view = value?.handler }
        override var depthSlice: UInt
            get() = handle.depthSlice.toUInt() as UInt
            set(value) { handle.depthSlice = value.toInt() }
        override var resolveTarget: WGPUTextureView?
            get() = handle.resolveTarget?.let { WGPUTextureView(it) }
            set(value) { handle.resolveTarget = value?.handler }
        override var loadOp: WGPULoadOp
            get() = handle.loadOp.toUInt() as WGPULoadOp
            set(value) { handle.loadOp = value.toInt() }
        override var storeOp: WGPUStoreOp
            get() = handle.storeOp.toUInt() as WGPUStoreOp
            set(value) { handle.storeOp = value.toInt() }
        override var clearValue: WGPUColor
            get() {
                handle.readField("clearValue")
                return WGPUColor.ByValue(handle.clearValue)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.clearValue.size())
                handle.readField("clearValue")
                handle.clearValue.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("clearValue")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByValue = io.ygdrasil.wgpu.android.WGPURenderPassColorAttachment.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderPassColorAttachment {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var view: WGPUTextureView?
            get() = handle.view?.let { WGPUTextureView(it) }
            set(value) { handle.view = value?.handler }
        override var depthSlice: UInt
            get() = handle.depthSlice.toUInt() as UInt
            set(value) { handle.depthSlice = value.toInt() }
        override var resolveTarget: WGPUTextureView?
            get() = handle.resolveTarget?.let { WGPUTextureView(it) }
            set(value) { handle.resolveTarget = value?.handler }
        override var loadOp: WGPULoadOp
            get() = handle.loadOp.toUInt() as WGPULoadOp
            set(value) { handle.loadOp = value.toInt() }
        override var storeOp: WGPUStoreOp
            get() = handle.storeOp.toUInt() as WGPUStoreOp
            set(value) { handle.storeOp = value.toInt() }
        override var clearValue: WGPUColor
            get() {
                handle.readField("clearValue")
                return WGPUColor.ByValue(handle.clearValue)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.clearValue.size())
                handle.readField("clearValue")
                handle.clearValue.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("clearValue")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterOptions {
            return ByReference(io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterOptions {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterOptions) -> Unit): ArrayHolder<WGPURequestAdapterOptions> {
            val ref = io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByReference = io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByReference(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var featureLevel: WGPUFeatureLevel
            get() = handle.featureLevel.toUInt() as WGPUFeatureLevel
            set(value) { handle.featureLevel = value.toInt() }
        override var powerPreference: WGPUPowerPreference
            get() = handle.powerPreference.toUInt() as WGPUPowerPreference
            set(value) { handle.powerPreference = value.toInt() }
        override var forceFallbackAdapter: UInt
            get() = handle.forceFallbackAdapter.toUInt() as UInt
            set(value) { handle.forceFallbackAdapter = value.toInt() }
        override var backendType: WGPUBackendType
            get() = handle.backendType.toUInt() as WGPUBackendType
            set(value) { handle.backendType = value.toInt() }
        override var compatibleSurface: WGPUSurface?
            get() = handle.compatibleSurface?.let { WGPUSurface(it) }
            set(value) { handle.compatibleSurface = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByValue = io.ygdrasil.wgpu.android.WGPURequestAdapterOptions.ByValue(com.sun.jna.Pointer.NULL)) : WGPURequestAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var featureLevel: WGPUFeatureLevel
            get() = handle.featureLevel.toUInt() as WGPUFeatureLevel
            set(value) { handle.featureLevel = value.toInt() }
        override var powerPreference: WGPUPowerPreference
            get() = handle.powerPreference.toUInt() as WGPUPowerPreference
            set(value) { handle.powerPreference = value.toInt() }
        override var forceFallbackAdapter: UInt
            get() = handle.forceFallbackAdapter.toUInt() as UInt
            set(value) { handle.forceFallbackAdapter = value.toInt() }
        override var backendType: WGPUBackendType
            get() = handle.backendType.toUInt() as WGPUBackendType
            set(value) { handle.backendType = value.toInt() }
        override var compatibleSurface: WGPUSurface?
            get() = handle.compatibleSurface?.let { WGPUSurface(it) }
            set(value) { handle.compatibleSurface = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUShaderModuleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptor) -> Unit): ArrayHolder<WGPUShaderModuleDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderModuleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderModuleDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceDescriptor) -> Unit): ArrayHolder<WGPUSurfaceDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTexelCopyBufferInfo {
    actual var layout: WGPUTexelCopyBufferLayout
    actual var buffer: WGPUBuffer?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferInfo) -> Unit): ArrayHolder<WGPUTexelCopyBufferInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByReference = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyBufferInfo {
        override var layout: WGPUTexelCopyBufferLayout
            get() {
                handle.readField("layout")
                return WGPUTexelCopyBufferLayout.ByValue(handle.layout)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.layout.size())
                handle.readField("layout")
                handle.layout.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("layout")
            }
        override var buffer: WGPUBuffer?
            get() = handle.buffer?.let { WGPUBuffer(it) }
            set(value) { handle.buffer = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByValue = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyBufferInfo {
        override var layout: WGPUTexelCopyBufferLayout
            get() {
                handle.readField("layout")
                return WGPUTexelCopyBufferLayout.ByValue(handle.layout)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.layout.size())
                handle.readField("layout")
                handle.layout.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("layout")
            }
        override var buffer: WGPUBuffer?
            get() = handle.buffer?.let { WGPUBuffer(it) }
            set(value) { handle.buffer = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTexelCopyTextureInfo {
    actual var texture: WGPUTexture?
    actual var mipLevel: UInt
    actual var origin: WGPUOrigin3D
    actual var aspect: WGPUTextureAspect
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyTextureInfo {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyTextureInfo {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyTextureInfo) -> Unit): ArrayHolder<WGPUTexelCopyTextureInfo> {
            val ref = io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByReference = io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyTextureInfo {
        override var texture: WGPUTexture?
            get() = handle.texture?.let { WGPUTexture(it) }
            set(value) { handle.texture = value?.handler }
        override var mipLevel: UInt
            get() = handle.mipLevel.toUInt() as UInt
            set(value) { handle.mipLevel = value.toInt() }
        override var origin: WGPUOrigin3D
            get() {
                handle.readField("origin")
                return WGPUOrigin3D.ByValue(handle.origin)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.origin.size())
                handle.readField("origin")
                handle.origin.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("origin")
            }
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByValue = io.ygdrasil.wgpu.android.WGPUTexelCopyTextureInfo.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTexelCopyTextureInfo {
        override var texture: WGPUTexture?
            get() = handle.texture?.let { WGPUTexture(it) }
            set(value) { handle.texture = value?.handler }
        override var mipLevel: UInt
            get() = handle.mipLevel.toUInt() as UInt
            set(value) { handle.mipLevel = value.toInt() }
        override var origin: WGPUOrigin3D
            get() {
                handle.readField("origin")
                return WGPUOrigin3D.ByValue(handle.origin)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.origin.size())
                handle.readField("origin")
                handle.origin.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("origin")
            }
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUTextureComponentSwizzleDescriptor {
    actual var chain: WGPUChainedStruct
    actual var swizzle: WGPUTextureComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzleDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzleDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzleDescriptor) -> Unit): ArrayHolder<WGPUTextureComponentSwizzleDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureComponentSwizzleDescriptor {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() {
                handle.readField("swizzle")
                return WGPUTextureComponentSwizzle.ByValue(handle.swizzle)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.swizzle.size())
                handle.readField("swizzle")
                handle.swizzle.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("swizzle")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzleDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureComponentSwizzleDescriptor {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() {
                handle.readField("swizzle")
                return WGPUTextureComponentSwizzle.ByValue(handle.swizzle)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.swizzle.size())
                handle.readField("swizzle")
                handle.swizzle.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("swizzle")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUTextureDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureDescriptor) -> Unit): ArrayHolder<WGPUTextureDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var dimension: WGPUTextureDimension
            get() = handle.dimension.toUInt() as WGPUTextureDimension
            set(value) { handle.dimension = value.toInt() }
        override var size: WGPUExtent3D
            get() {
                handle.readField("size")
                return WGPUExtent3D.ByValue(handle.size)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.size.size())
                handle.readField("size")
                handle.size.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("size")
            }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var sampleCount: UInt
            get() = handle.sampleCount.toUInt() as UInt
            set(value) { handle.sampleCount = value.toInt() }
        override var viewFormatCount: ULong
            get() = handle.viewFormatCount.toULong() as ULong
            set(value) { handle.viewFormatCount = value.toLong() }
        override var viewFormats: NativeAddress?
            get() = handle.viewFormats
            set(value) { handle.viewFormats = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUTextureDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override var dimension: WGPUTextureDimension
            get() = handle.dimension.toUInt() as WGPUTextureDimension
            set(value) { handle.dimension = value.toInt() }
        override var size: WGPUExtent3D
            get() {
                handle.readField("size")
                return WGPUExtent3D.ByValue(handle.size)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.size.size())
                handle.readField("size")
                handle.size.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("size")
            }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var sampleCount: UInt
            get() = handle.sampleCount.toUInt() as UInt
            set(value) { handle.sampleCount = value.toInt() }
        override var viewFormatCount: ULong
            get() = handle.viewFormatCount.toULong() as ULong
            set(value) { handle.viewFormatCount = value.toLong() }
        override var viewFormats: NativeAddress?
            get() = handle.viewFormats
            set(value) { handle.viewFormats = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUVertexBufferLayout {
            return ByReference(io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexBufferLayout {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexBufferLayout) -> Unit): ArrayHolder<WGPUVertexBufferLayout> {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByReference = io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByReference(com.sun.jna.Pointer.NULL)) : WGPUVertexBufferLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var stepMode: WGPUVertexStepMode
            get() = handle.stepMode.toUInt() as WGPUVertexStepMode
            set(value) { handle.stepMode = value.toInt() }
        override var arrayStride: ULong
            get() = handle.arrayStride.toULong() as ULong
            set(value) { handle.arrayStride = value.toLong() }
        override var attributeCount: ULong
            get() = handle.attributeCount.toULong() as ULong
            set(value) { handle.attributeCount = value.toLong() }
        override var attributes: WGPUVertexAttribute?
            get() = handle.attributes?.let { WGPUVertexAttribute(it) }
            set(value) { handle.attributes = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByValue = io.ygdrasil.wgpu.android.WGPUVertexBufferLayout.ByValue(com.sun.jna.Pointer.NULL)) : WGPUVertexBufferLayout {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var stepMode: WGPUVertexStepMode
            get() = handle.stepMode.toUInt() as WGPUVertexStepMode
            set(value) { handle.stepMode = value.toInt() }
        override var arrayStride: ULong
            get() = handle.arrayStride.toULong() as ULong
            set(value) { handle.arrayStride = value.toLong() }
        override var attributeCount: ULong
            get() = handle.attributeCount.toULong() as ULong
            set(value) { handle.attributeCount = value.toLong() }
        override var attributes: WGPUVertexAttribute?
            get() = handle.attributes?.let { WGPUVertexAttribute(it) }
            set(value) { handle.attributes = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupDescriptor) -> Unit): ArrayHolder<WGPUBindGroupDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUBindGroupLayout?
            get() = handle.layout?.let { WGPUBindGroupLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var entryCount: ULong
            get() = handle.entryCount.toULong() as ULong
            set(value) { handle.entryCount = value.toLong() }
        override var entries: WGPUBindGroupEntry?
            get() = handle.entries?.let { WGPUBindGroupEntry(it) }
            set(value) { handle.entries = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUBindGroupLayout?
            get() = handle.layout?.let { WGPUBindGroupLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var entryCount: ULong
            get() = handle.entryCount.toULong() as ULong
            set(value) { handle.entryCount = value.toLong() }
        override var entries: WGPUBindGroupEntry?
            get() = handle.entries?.let { WGPUBindGroupEntry(it) }
            set(value) { handle.entries = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUBindGroupLayoutDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupLayoutEntry?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutDescriptor) -> Unit): ArrayHolder<WGPUBindGroupLayoutDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var entryCount: ULong
            get() = handle.entryCount.toULong() as ULong
            set(value) { handle.entryCount = value.toLong() }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = handle.entries?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) { handle.entries = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var entryCount: ULong
            get() = handle.entryCount.toULong() as ULong
            set(value) { handle.entryCount = value.toLong() }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = handle.entries?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) { handle.entries = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUColorTargetState {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUTextureFormat
    actual var blend: WGPUBlendState?
    actual var writeMask: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColorTargetState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUColorTargetState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUColorTargetState {
            val ref = io.ygdrasil.wgpu.android.WGPUColorTargetState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColorTargetState) -> Unit): ArrayHolder<WGPUColorTargetState> {
            val ref = io.ygdrasil.wgpu.android.WGPUColorTargetState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUColorTargetState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUColorTargetState.ByReference = io.ygdrasil.wgpu.android.WGPUColorTargetState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUColorTargetState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var blend: WGPUBlendState?
            get() = handle.blend?.let { WGPUBlendState(it) }
            set(value) { handle.blend = value?.handler }
        override var writeMask: ULong
            get() = handle.writeMask.toULong() as ULong
            set(value) { handle.writeMask = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUColorTargetState.ByValue = io.ygdrasil.wgpu.android.WGPUColorTargetState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUColorTargetState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var blend: WGPUBlendState?
            get() = handle.blend?.let { WGPUBlendState(it) }
            set(value) { handle.blend = value?.handler }
        override var writeMask: ULong
            get() = handle.writeMask.toULong() as ULong
            set(value) { handle.writeMask = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUComputePipelineDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var compute: WGPUComputeState
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePipelineDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePipelineDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePipelineDescriptor) -> Unit): ArrayHolder<WGPUComputePipelineDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUComputePipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUPipelineLayout?
            get() = handle.layout?.let { WGPUPipelineLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var compute: WGPUComputeState
            get() {
                handle.readField("compute")
                return WGPUComputeState.ByValue(handle.compute)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.compute.size())
                handle.readField("compute")
                handle.compute.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("compute")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUComputePipelineDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUComputePipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUPipelineLayout?
            get() = handle.layout?.let { WGPUPipelineLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var compute: WGPUComputeState
            get() {
                handle.readField("compute")
                return WGPUComputeState.ByValue(handle.compute)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.compute.size())
                handle.readField("compute")
                handle.compute.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("compute")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUDeviceDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceDescriptor) -> Unit): ArrayHolder<WGPUDeviceDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUDeviceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var requiredFeatureCount: ULong
            get() = handle.requiredFeatureCount.toULong() as ULong
            set(value) { handle.requiredFeatureCount = value.toLong() }
        override var requiredFeatures: NativeAddress?
            get() = handle.requiredFeatures
            set(value) { handle.requiredFeatures = value }
        override var requiredLimits: WGPULimits?
            get() = handle.requiredLimits?.let { WGPULimits(it) }
            set(value) { handle.requiredLimits = value?.handler }
        override var defaultQueue: WGPUQueueDescriptor
            get() {
                handle.readField("defaultQueue")
                return WGPUQueueDescriptor.ByValue(handle.defaultQueue)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.defaultQueue.size())
                handle.readField("defaultQueue")
                handle.defaultQueue.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("defaultQueue")
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() {
                handle.readField("deviceLostCallbackInfo")
                return WGPUDeviceLostCallbackInfo.ByValue(handle.deviceLostCallbackInfo)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.deviceLostCallbackInfo.size())
                handle.readField("deviceLostCallbackInfo")
                handle.deviceLostCallbackInfo.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("deviceLostCallbackInfo")
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() {
                handle.readField("uncapturedErrorCallbackInfo")
                return WGPUUncapturedErrorCallbackInfo.ByValue(handle.uncapturedErrorCallbackInfo)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.uncapturedErrorCallbackInfo.size())
                handle.readField("uncapturedErrorCallbackInfo")
                handle.uncapturedErrorCallbackInfo.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("uncapturedErrorCallbackInfo")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUDeviceDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUDeviceDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var requiredFeatureCount: ULong
            get() = handle.requiredFeatureCount.toULong() as ULong
            set(value) { handle.requiredFeatureCount = value.toLong() }
        override var requiredFeatures: NativeAddress?
            get() = handle.requiredFeatures
            set(value) { handle.requiredFeatures = value }
        override var requiredLimits: WGPULimits?
            get() = handle.requiredLimits?.let { WGPULimits(it) }
            set(value) { handle.requiredLimits = value?.handler }
        override var defaultQueue: WGPUQueueDescriptor
            get() {
                handle.readField("defaultQueue")
                return WGPUQueueDescriptor.ByValue(handle.defaultQueue)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.defaultQueue.size())
                handle.readField("defaultQueue")
                handle.defaultQueue.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("defaultQueue")
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() {
                handle.readField("deviceLostCallbackInfo")
                return WGPUDeviceLostCallbackInfo.ByValue(handle.deviceLostCallbackInfo)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.deviceLostCallbackInfo.size())
                handle.readField("deviceLostCallbackInfo")
                handle.deviceLostCallbackInfo.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("deviceLostCallbackInfo")
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() {
                handle.readField("uncapturedErrorCallbackInfo")
                return WGPUUncapturedErrorCallbackInfo.ByValue(handle.uncapturedErrorCallbackInfo)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.uncapturedErrorCallbackInfo.size())
                handle.readField("uncapturedErrorCallbackInfo")
                handle.uncapturedErrorCallbackInfo.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("uncapturedErrorCallbackInfo")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURenderPassDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDescriptor) -> Unit): ArrayHolder<WGPURenderPassDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderPassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var colorAttachmentCount: ULong
            get() = handle.colorAttachmentCount.toULong() as ULong
            set(value) { handle.colorAttachmentCount = value.toLong() }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = handle.colorAttachments?.let { WGPURenderPassColorAttachment(it) }
            set(value) { handle.colorAttachments = value?.handler }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = handle.depthStencilAttachment?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) { handle.depthStencilAttachment = value?.handler }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = handle.occlusionQuerySet?.let { WGPUQuerySet(it) }
            set(value) { handle.occlusionQuerySet = value?.handler }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.timestampWrites?.let { WGPUPassTimestampWrites(it) }
            set(value) { handle.timestampWrites = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPURenderPassDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderPassDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var colorAttachmentCount: ULong
            get() = handle.colorAttachmentCount.toULong() as ULong
            set(value) { handle.colorAttachmentCount = value.toLong() }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = handle.colorAttachments?.let { WGPURenderPassColorAttachment(it) }
            set(value) { handle.colorAttachments = value?.handler }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = handle.depthStencilAttachment?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) { handle.depthStencilAttachment = value?.handler }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = handle.occlusionQuerySet?.let { WGPUQuerySet(it) }
            set(value) { handle.occlusionQuerySet = value?.handler }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.timestampWrites?.let { WGPUPassTimestampWrites(it) }
            set(value) { handle.timestampWrites = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUTextureViewDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureViewDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureViewDescriptor) -> Unit): ArrayHolder<WGPUTextureViewDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPUTextureViewDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var dimension: WGPUTextureViewDimension
            get() = handle.dimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.dimension = value.toInt() }
        override var baseMipLevel: UInt
            get() = handle.baseMipLevel.toUInt() as UInt
            set(value) { handle.baseMipLevel = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var baseArrayLayer: UInt
            get() = handle.baseArrayLayer.toUInt() as UInt
            set(value) { handle.baseArrayLayer = value.toInt() }
        override var arrayLayerCount: UInt
            get() = handle.arrayLayerCount.toUInt() as UInt
            set(value) { handle.arrayLayerCount = value.toInt() }
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUTextureViewDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPUTextureViewDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var format: WGPUTextureFormat
            get() = handle.format.toUInt() as WGPUTextureFormat
            set(value) { handle.format = value.toInt() }
        override var dimension: WGPUTextureViewDimension
            get() = handle.dimension.toUInt() as WGPUTextureViewDimension
            set(value) { handle.dimension = value.toInt() }
        override var baseMipLevel: UInt
            get() = handle.baseMipLevel.toUInt() as UInt
            set(value) { handle.baseMipLevel = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var baseArrayLayer: UInt
            get() = handle.baseArrayLayer.toUInt() as UInt
            set(value) { handle.baseArrayLayer = value.toInt() }
        override var arrayLayerCount: UInt
            get() = handle.arrayLayerCount.toUInt() as UInt
            set(value) { handle.arrayLayerCount = value.toInt() }
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override var usage: ULong
            get() = handle.usage.toULong() as ULong
            set(value) { handle.usage = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUVertexState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUVertexState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexState {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexState) -> Unit): ArrayHolder<WGPUVertexState> {
            val ref = io.ygdrasil.wgpu.android.WGPUVertexState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUVertexState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUVertexState.ByReference = io.ygdrasil.wgpu.android.WGPUVertexState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUVertexState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override var bufferCount: ULong
            get() = handle.bufferCount.toULong() as ULong
            set(value) { handle.bufferCount = value.toLong() }
        override var buffers: WGPUVertexBufferLayout?
            get() = handle.buffers?.let { WGPUVertexBufferLayout(it) }
            set(value) { handle.buffers = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUVertexState.ByValue = io.ygdrasil.wgpu.android.WGPUVertexState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUVertexState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override var bufferCount: ULong
            get() = handle.bufferCount.toULong() as ULong
            set(value) { handle.bufferCount = value.toLong() }
        override var buffers: WGPUVertexBufferLayout?
            get() = handle.buffers?.let { WGPUVertexBufferLayout(it) }
            set(value) { handle.buffers = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUFragmentState {
            return ByReference(io.ygdrasil.wgpu.android.WGPUFragmentState.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUFragmentState {
            val ref = io.ygdrasil.wgpu.android.WGPUFragmentState.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFragmentState) -> Unit): ArrayHolder<WGPUFragmentState> {
            val ref = io.ygdrasil.wgpu.android.WGPUFragmentState.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUFragmentState.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUFragmentState.ByReference = io.ygdrasil.wgpu.android.WGPUFragmentState.ByReference(com.sun.jna.Pointer.NULL)) : WGPUFragmentState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override var targetCount: ULong
            get() = handle.targetCount.toULong() as ULong
            set(value) { handle.targetCount = value.toLong() }
        override var targets: WGPUColorTargetState?
            get() = handle.targets?.let { WGPUColorTargetState(it) }
            set(value) { handle.targets = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUFragmentState.ByValue = io.ygdrasil.wgpu.android.WGPUFragmentState.ByValue(com.sun.jna.Pointer.NULL)) : WGPUFragmentState {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var module: WGPUShaderModule?
            get() = handle.module?.let { WGPUShaderModule(it) }
            set(value) { handle.module = value?.handler }
        override var entryPoint: WGPUStringView
            get() {
                handle.readField("entryPoint")
                return WGPUStringView.ByValue(handle.entryPoint)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.entryPoint.size())
                handle.readField("entryPoint")
                handle.entryPoint.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("entryPoint")
            }
        override var constantCount: ULong
            get() = handle.constantCount.toULong() as ULong
            set(value) { handle.constantCount = value.toLong() }
        override var constants: WGPUConstantEntry?
            get() = handle.constants?.let { WGPUConstantEntry(it) }
            set(value) { handle.constants = value?.handler }
        override var targetCount: ULong
            get() = handle.targetCount.toULong() as ULong
            set(value) { handle.targetCount = value.toLong() }
        override var targets: WGPUColorTargetState?
            get() = handle.targets?.let { WGPUColorTargetState(it) }
            set(value) { handle.targets = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPURenderPipelineDescriptor {
            return ByReference(io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPipelineDescriptor {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPipelineDescriptor) -> Unit): ArrayHolder<WGPURenderPipelineDescriptor> {
            val ref = io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByReference = io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByReference(com.sun.jna.Pointer.NULL)) : WGPURenderPipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUPipelineLayout?
            get() = handle.layout?.let { WGPUPipelineLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var vertex: WGPUVertexState
            get() {
                handle.readField("vertex")
                return WGPUVertexState.ByValue(handle.vertex)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.vertex.size())
                handle.readField("vertex")
                handle.vertex.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("vertex")
            }
        override var primitive: WGPUPrimitiveState
            get() {
                handle.readField("primitive")
                return WGPUPrimitiveState.ByValue(handle.primitive)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.primitive.size())
                handle.readField("primitive")
                handle.primitive.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("primitive")
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = handle.depthStencil?.let { WGPUDepthStencilState(it) }
            set(value) { handle.depthStencil = value?.handler }
        override var multisample: WGPUMultisampleState
            get() {
                handle.readField("multisample")
                return WGPUMultisampleState.ByValue(handle.multisample)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.multisample.size())
                handle.readField("multisample")
                handle.multisample.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("multisample")
            }
        override var fragment: WGPUFragmentState?
            get() = handle.fragment?.let { WGPUFragmentState(it) }
            set(value) { handle.fragment = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPURenderPipelineDescriptor.ByValue(com.sun.jna.Pointer.NULL)) : WGPURenderPipelineDescriptor {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var layout: WGPUPipelineLayout?
            get() = handle.layout?.let { WGPUPipelineLayout(it) }
            set(value) { handle.layout = value?.handler }
        override var vertex: WGPUVertexState
            get() {
                handle.readField("vertex")
                return WGPUVertexState.ByValue(handle.vertex)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.vertex.size())
                handle.readField("vertex")
                handle.vertex.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("vertex")
            }
        override var primitive: WGPUPrimitiveState
            get() {
                handle.readField("primitive")
                return WGPUPrimitiveState.ByValue(handle.primitive)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.primitive.size())
                handle.readField("primitive")
                handle.primitive.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("primitive")
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = handle.depthStencil?.let { WGPUDepthStencilState(it) }
            set(value) { handle.depthStencil = value?.handler }
        override var multisample: WGPUMultisampleState
            get() {
                handle.readField("multisample")
                return WGPUMultisampleState.ByValue(handle.multisample)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.multisample.size())
                handle.readField("multisample")
                handle.multisample.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("multisample")
            }
        override var fragment: WGPUFragmentState?
            get() = handle.fragment?.let { WGPUFragmentState(it) }
            set(value) { handle.fragment = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual fun wgpuCreateInstance(descriptor: WGPUInstanceDescriptor?): WGPUInstance? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCreateInstance(descriptor?.handler)?.let { WGPUInstance(it) }
}

actual fun wgpuGetInstanceFeatures(features: WGPUSupportedInstanceFeatures?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuGetInstanceFeatures(features?.handler)
    return
}

actual fun wgpuGetInstanceLimits(limits: WGPUInstanceLimits?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuGetInstanceLimits(limits?.handler)).toUInt()
}

actual fun wgpuHasInstanceFeature(feature: WGPUInstanceFeatureName): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuHasInstanceFeature(feature.toInt()).toUInt()
}

actual fun wgpuGetProcAddress(procName: WGPUStringView): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuGetProcAddress(io.ygdrasil.wgpu.android.WGPUStringView.ByValue(procName.handler).apply { read() })
}

actual fun wgpuAdapterGetFeatures(adapter: WGPUAdapter?, features: WGPUSupportedFeatures?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterGetFeatures(adapter?.handler, features?.handler)
    return
}

actual fun wgpuAdapterGetInfo(adapter: WGPUAdapter?, info: WGPUAdapterInfo?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterGetInfo(adapter?.handler, info?.handler)).toUInt()
}

actual fun wgpuAdapterGetLimits(adapter: WGPUAdapter?, limits: WGPULimits?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterGetLimits(adapter?.handler, limits?.handler)).toUInt()
}

actual fun wgpuAdapterHasFeature(adapter: WGPUAdapter?, feature: WGPUFeatureName): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterHasFeature(adapter?.handler, feature.toInt()).toUInt()
}

actual fun wgpuAdapterRequestDevice(adapter: WGPUAdapter?, descriptor: WGPUDeviceDescriptor?, callbackInfo: WGPURequestDeviceCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterRequestDevice(adapter?.handler, descriptor?.handler, io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuAdapterAddRef(adapter: WGPUAdapter?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterAddRef(adapter?.handler)
    return
}

actual fun wgpuAdapterRelease(adapter: WGPUAdapter?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterRelease(adapter?.handler)
    return
}

actual fun wgpuAdapterInfoFreeMembers(adapterInfo: WGPUAdapterInfo): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuAdapterInfoFreeMembers(io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue(adapterInfo.handler).apply { read() })
    return
}

actual fun wgpuBindGroupSetLabel(bindGroup: WGPUBindGroup?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupSetLabel(bindGroup?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuBindGroupAddRef(bindGroup: WGPUBindGroup?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupAddRef(bindGroup?.handler)
    return
}

actual fun wgpuBindGroupRelease(bindGroup: WGPUBindGroup?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupRelease(bindGroup?.handler)
    return
}

actual fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: WGPUBindGroupLayout?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupLayoutSetLabel(bindGroupLayout?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuBindGroupLayoutAddRef(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupLayoutAddRef(bindGroupLayout?.handler)
    return
}

actual fun wgpuBindGroupLayoutRelease(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBindGroupLayoutRelease(bindGroupLayout?.handler)
    return
}

actual fun wgpuBufferDestroy(buffer: WGPUBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferDestroy(buffer?.handler)
    return
}

actual fun wgpuBufferGetConstMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferGetConstMappedRange(buffer?.handler, offset.toLong(), size.toLong())
}

actual fun wgpuBufferGetMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferGetMappedRange(buffer?.handler, offset.toLong(), size.toLong())
}

actual fun wgpuBufferGetMapState(buffer: WGPUBuffer?): WGPUBufferMapState {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferGetMapState(buffer?.handler)).toUInt()
}

actual fun wgpuBufferGetSize(buffer: WGPUBuffer?): ULong {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferGetSize(buffer?.handler).toULong()
}

actual fun wgpuBufferGetUsage(buffer: WGPUBuffer?): ULong {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferGetUsage(buffer?.handler).toULong()
}

actual fun wgpuBufferMapAsync(buffer: WGPUBuffer?, mode: ULong, offset: ULong, size: ULong, callbackInfo: WGPUBufferMapCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferMapAsync(buffer?.handler, mode.toLong(), offset.toLong(), size.toLong(), io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuBufferReadMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferReadMappedRange(buffer?.handler, offset.toLong(), data, size.toLong())).toUInt()
}

actual fun wgpuBufferSetLabel(buffer: WGPUBuffer?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferSetLabel(buffer?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuBufferUnmap(buffer: WGPUBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferUnmap(buffer?.handler)
    return
}

actual fun wgpuBufferWriteMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferWriteMappedRange(buffer?.handler, offset.toLong(), data, size.toLong())).toUInt()
}

actual fun wgpuBufferAddRef(buffer: WGPUBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferAddRef(buffer?.handler)
    return
}

actual fun wgpuBufferRelease(buffer: WGPUBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuBufferRelease(buffer?.handler)
    return
}

actual fun wgpuCommandBufferSetLabel(commandBuffer: WGPUCommandBuffer?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandBufferSetLabel(commandBuffer?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuCommandBufferAddRef(commandBuffer: WGPUCommandBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandBufferAddRef(commandBuffer?.handler)
    return
}

actual fun wgpuCommandBufferRelease(commandBuffer: WGPUCommandBuffer?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandBufferRelease(commandBuffer?.handler)
    return
}

actual fun wgpuCommandEncoderBeginComputePass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUComputePassDescriptor?): WGPUComputePassEncoder? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderBeginComputePass(commandEncoder?.handler, descriptor?.handler)?.let { WGPUComputePassEncoder(it) }
}

actual fun wgpuCommandEncoderBeginRenderPass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPURenderPassDescriptor?): WGPURenderPassEncoder? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderBeginRenderPass(commandEncoder?.handler, descriptor?.handler)?.let { WGPURenderPassEncoder(it) }
}

actual fun wgpuCommandEncoderClearBuffer(commandEncoder: WGPUCommandEncoder?, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderClearBuffer(commandEncoder?.handler, buffer?.handler, offset.toLong(), size.toLong())
    return
}

actual fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUBuffer?, sourceOffset: ULong, destination: WGPUBuffer?, destinationOffset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderCopyBufferToBuffer(commandEncoder?.handler, source?.handler, sourceOffset.toLong(), destination?.handler, destinationOffset.toLong(), size.toLong())
    return
}

actual fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyBufferInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderCopyBufferToTexture(commandEncoder?.handler, source?.handler, destination?.handler, copySize?.handler)
    return
}

actual fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyBufferInfo?, copySize: WGPUExtent3D?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderCopyTextureToBuffer(commandEncoder?.handler, source?.handler, destination?.handler, copySize?.handler)
    return
}

actual fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderCopyTextureToTexture(commandEncoder?.handler, source?.handler, destination?.handler, copySize?.handler)
    return
}

actual fun wgpuCommandEncoderFinish(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUCommandBufferDescriptor?): WGPUCommandBuffer? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderFinish(commandEncoder?.handler, descriptor?.handler)?.let { WGPUCommandBuffer(it) }
}

actual fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: WGPUCommandEncoder?, markerLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderInsertDebugMarker(commandEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(markerLabel.handler).apply { read() })
    return
}

actual fun wgpuCommandEncoderPopDebugGroup(commandEncoder: WGPUCommandEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderPopDebugGroup(commandEncoder?.handler)
    return
}

actual fun wgpuCommandEncoderPushDebugGroup(commandEncoder: WGPUCommandEncoder?, groupLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderPushDebugGroup(commandEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(groupLabel.handler).apply { read() })
    return
}

actual fun wgpuCommandEncoderResolveQuerySet(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, firstQuery: UInt, queryCount: UInt, destination: WGPUBuffer?, destinationOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderResolveQuerySet(commandEncoder?.handler, querySet?.handler, firstQuery.toInt(), queryCount.toInt(), destination?.handler, destinationOffset.toLong())
    return
}

actual fun wgpuCommandEncoderSetLabel(commandEncoder: WGPUCommandEncoder?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderSetLabel(commandEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuCommandEncoderWriteTimestamp(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderWriteTimestamp(commandEncoder?.handler, querySet?.handler, queryIndex.toInt())
    return
}

actual fun wgpuCommandEncoderAddRef(commandEncoder: WGPUCommandEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderAddRef(commandEncoder?.handler)
    return
}

actual fun wgpuCommandEncoderRelease(commandEncoder: WGPUCommandEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderRelease(commandEncoder?.handler)
    return
}

actual fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: WGPUComputePassEncoder?, workgroupCountX: UInt, workgroupCountY: UInt, workgroupCountZ: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder?.handler, workgroupCountX.toInt(), workgroupCountY.toInt(), workgroupCountZ.toInt())
    return
}

actual fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: WGPUComputePassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder?.handler, indirectBuffer?.handler, indirectOffset.toLong())
    return
}

actual fun wgpuComputePassEncoderEnd(computePassEncoder: WGPUComputePassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderEnd(computePassEncoder?.handler)
    return
}

actual fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: WGPUComputePassEncoder?, markerLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderInsertDebugMarker(computePassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(markerLabel.handler).apply { read() })
    return
}

actual fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: WGPUComputePassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderPopDebugGroup(computePassEncoder?.handler)
    return
}

actual fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: WGPUComputePassEncoder?, groupLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderPushDebugGroup(computePassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(groupLabel.handler).apply { read() })
    return
}

actual fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: WGPUComputePassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderSetBindGroup(computePassEncoder?.handler, groupIndex.toInt(), group?.handler, dynamicOffsetCount.toLong(), dynamicOffsets)
    return
}

actual fun wgpuComputePassEncoderSetImmediates(computePassEncoder: WGPUComputePassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderSetImmediates(computePassEncoder?.handler, offset.toInt(), data, size.toLong())
    return
}

actual fun wgpuComputePassEncoderSetLabel(computePassEncoder: WGPUComputePassEncoder?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderSetLabel(computePassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuComputePassEncoderSetPipeline(computePassEncoder: WGPUComputePassEncoder?, pipeline: WGPUComputePipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderSetPipeline(computePassEncoder?.handler, pipeline?.handler)
    return
}

actual fun wgpuComputePassEncoderAddRef(computePassEncoder: WGPUComputePassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderAddRef(computePassEncoder?.handler)
    return
}

actual fun wgpuComputePassEncoderRelease(computePassEncoder: WGPUComputePassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderRelease(computePassEncoder?.handler)
    return
}

actual fun wgpuComputePipelineGetBindGroupLayout(computePipeline: WGPUComputePipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePipelineGetBindGroupLayout(computePipeline?.handler, groupIndex.toInt())?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuComputePipelineSetLabel(computePipeline: WGPUComputePipeline?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePipelineSetLabel(computePipeline?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuComputePipelineAddRef(computePipeline: WGPUComputePipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePipelineAddRef(computePipeline?.handler)
    return
}

actual fun wgpuComputePipelineRelease(computePipeline: WGPUComputePipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePipelineRelease(computePipeline?.handler)
    return
}

actual fun wgpuDeviceCreateBindGroup(device: WGPUDevice?, descriptor: WGPUBindGroupDescriptor?): WGPUBindGroup? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateBindGroup(device?.handler, descriptor?.handler)?.let { WGPUBindGroup(it) }
}

actual fun wgpuDeviceCreateBindGroupLayout(device: WGPUDevice?, descriptor: WGPUBindGroupLayoutDescriptor?): WGPUBindGroupLayout? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateBindGroupLayout(device?.handler, descriptor?.handler)?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuDeviceCreateBuffer(device: WGPUDevice?, descriptor: WGPUBufferDescriptor?): WGPUBuffer? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateBuffer(device?.handler, descriptor?.handler)?.let { WGPUBuffer(it) }
}

actual fun wgpuDeviceCreateCommandEncoder(device: WGPUDevice?, descriptor: WGPUCommandEncoderDescriptor?): WGPUCommandEncoder? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateCommandEncoder(device?.handler, descriptor?.handler)?.let { WGPUCommandEncoder(it) }
}

actual fun wgpuDeviceCreateComputePipeline(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?): WGPUComputePipeline? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateComputePipeline(device?.handler, descriptor?.handler)?.let { WGPUComputePipeline(it) }
}

actual fun wgpuDeviceCreateComputePipelineAsync(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?, callbackInfo: WGPUCreateComputePipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateComputePipelineAsync(device?.handler, descriptor?.handler, io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuDeviceCreatePipelineLayout(device: WGPUDevice?, descriptor: WGPUPipelineLayoutDescriptor?): WGPUPipelineLayout? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreatePipelineLayout(device?.handler, descriptor?.handler)?.let { WGPUPipelineLayout(it) }
}

actual fun wgpuDeviceCreateQuerySet(device: WGPUDevice?, descriptor: WGPUQuerySetDescriptor?): WGPUQuerySet? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateQuerySet(device?.handler, descriptor?.handler)?.let { WGPUQuerySet(it) }
}

actual fun wgpuDeviceCreateRenderBundleEncoder(device: WGPUDevice?, descriptor: WGPURenderBundleEncoderDescriptor?): WGPURenderBundleEncoder? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateRenderBundleEncoder(device?.handler, descriptor?.handler)?.let { WGPURenderBundleEncoder(it) }
}

actual fun wgpuDeviceCreateRenderPipeline(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?): WGPURenderPipeline? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateRenderPipeline(device?.handler, descriptor?.handler)?.let { WGPURenderPipeline(it) }
}

actual fun wgpuDeviceCreateRenderPipelineAsync(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?, callbackInfo: WGPUCreateRenderPipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateRenderPipelineAsync(device?.handler, descriptor?.handler, io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuDeviceCreateSampler(device: WGPUDevice?, descriptor: WGPUSamplerDescriptor?): WGPUSampler? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateSampler(device?.handler, descriptor?.handler)?.let { WGPUSampler(it) }
}

actual fun wgpuDeviceCreateShaderModule(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?): WGPUShaderModule? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateShaderModule(device?.handler, descriptor?.handler)?.let { WGPUShaderModule(it) }
}

actual fun wgpuDeviceCreateTexture(device: WGPUDevice?, descriptor: WGPUTextureDescriptor?): WGPUTexture? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateTexture(device?.handler, descriptor?.handler)?.let { WGPUTexture(it) }
}

actual fun wgpuDeviceDestroy(device: WGPUDevice?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceDestroy(device?.handler)
    return
}

actual fun wgpuDeviceGetAdapterInfo(device: WGPUDevice?, adapterInfo: WGPUAdapterInfo?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetAdapterInfo(device?.handler, adapterInfo?.handler)).toUInt()
}

actual fun wgpuDeviceGetFeatures(device: WGPUDevice?, features: WGPUSupportedFeatures?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetFeatures(device?.handler, features?.handler)
    return
}

actual fun wgpuDeviceGetLimits(device: WGPUDevice?, limits: WGPULimits?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetLimits(device?.handler, limits?.handler)).toUInt()
}

actual fun wgpuDeviceGetLostFuture(device: WGPUDevice?): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetLostFuture(device?.handler))
}

actual fun wgpuDeviceGetQueue(device: WGPUDevice?): WGPUQueue? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetQueue(device?.handler)?.let { WGPUQueue(it) }
}

actual fun wgpuDeviceHasFeature(device: WGPUDevice?, feature: WGPUFeatureName): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceHasFeature(device?.handler, feature.toInt()).toUInt()
}

actual fun wgpuDevicePopErrorScope(device: WGPUDevice?, callbackInfo: WGPUPopErrorScopeCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDevicePopErrorScope(device?.handler, io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuDevicePushErrorScope(device: WGPUDevice?, filter: WGPUErrorFilter): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDevicePushErrorScope(device?.handler, filter.toInt())
    return
}

actual fun wgpuDeviceSetLabel(device: WGPUDevice?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceSetLabel(device?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuDeviceAddRef(device: WGPUDevice?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceAddRef(device?.handler)
    return
}

actual fun wgpuDeviceRelease(device: WGPUDevice?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceRelease(device?.handler)
    return
}

actual fun wgpuExternalTextureSetLabel(externalTexture: WGPUExternalTexture?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuExternalTextureSetLabel(externalTexture?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuExternalTextureAddRef(externalTexture: WGPUExternalTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuExternalTextureAddRef(externalTexture?.handler)
    return
}

actual fun wgpuExternalTextureRelease(externalTexture: WGPUExternalTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuExternalTextureRelease(externalTexture?.handler)
    return
}

actual fun wgpuInstanceCreateSurface(instance: WGPUInstance?, descriptor: WGPUSurfaceDescriptor?): WGPUSurface? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceCreateSurface(instance?.handler, descriptor?.handler)?.let { WGPUSurface(it) }
}

actual fun wgpuInstanceGetWGSLLanguageFeatures(instance: WGPUInstance?, features: WGPUSupportedWGSLLanguageFeatures?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceGetWGSLLanguageFeatures(instance?.handler, features?.handler)
    return
}

actual fun wgpuInstanceHasWGSLLanguageFeature(instance: WGPUInstance?, feature: WGPUWGSLLanguageFeatureName): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceHasWGSLLanguageFeature(instance?.handler, feature.toInt()).toUInt()
}

actual fun wgpuInstanceProcessEvents(instance: WGPUInstance?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceProcessEvents(instance?.handler)
    return
}

actual fun wgpuInstanceRequestAdapter(instance: WGPUInstance?, options: WGPURequestAdapterOptions?, callbackInfo: WGPURequestAdapterCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceRequestAdapter(instance?.handler, options?.handler, io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuInstanceWaitAny(instance: WGPUInstance?, futureCount: ULong, futures: WGPUFutureWaitInfo?, timeoutNS: ULong): WGPUWaitStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceWaitAny(instance?.handler, futureCount.toLong(), futures?.handler, timeoutNS.toLong())).toUInt()
}

actual fun wgpuInstanceAddRef(instance: WGPUInstance?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceAddRef(instance?.handler)
    return
}

actual fun wgpuInstanceRelease(instance: WGPUInstance?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceRelease(instance?.handler)
    return
}

actual fun wgpuPipelineLayoutSetLabel(pipelineLayout: WGPUPipelineLayout?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuPipelineLayoutSetLabel(pipelineLayout?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuPipelineLayoutAddRef(pipelineLayout: WGPUPipelineLayout?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuPipelineLayoutAddRef(pipelineLayout?.handler)
    return
}

actual fun wgpuPipelineLayoutRelease(pipelineLayout: WGPUPipelineLayout?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuPipelineLayoutRelease(pipelineLayout?.handler)
    return
}

actual fun wgpuQuerySetDestroy(querySet: WGPUQuerySet?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetDestroy(querySet?.handler)
    return
}

actual fun wgpuQuerySetGetCount(querySet: WGPUQuerySet?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetGetCount(querySet?.handler).toUInt()
}

actual fun wgpuQuerySetGetType(querySet: WGPUQuerySet?): WGPUQueryType {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetGetType(querySet?.handler)).toUInt()
}

actual fun wgpuQuerySetSetLabel(querySet: WGPUQuerySet?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetSetLabel(querySet?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuQuerySetAddRef(querySet: WGPUQuerySet?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetAddRef(querySet?.handler)
    return
}

actual fun wgpuQuerySetRelease(querySet: WGPUQuerySet?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQuerySetRelease(querySet?.handler)
    return
}

actual fun wgpuQueueOnSubmittedWorkDone(queue: WGPUQueue?, callbackInfo: WGPUQueueWorkDoneCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueOnSubmittedWorkDone(queue?.handler, io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuQueueSetLabel(queue: WGPUQueue?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueSetLabel(queue?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuQueueSubmit(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueSubmit(queue?.handler, commandCount.toLong(), commands)
    return
}

actual fun wgpuQueueWriteBuffer(queue: WGPUQueue?, buffer: WGPUBuffer?, bufferOffset: ULong, data: NativeAddress?, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueWriteBuffer(queue?.handler, buffer?.handler, bufferOffset.toLong(), data, size.toLong())
    return
}

actual fun wgpuQueueWriteTexture(queue: WGPUQueue?, destination: WGPUTexelCopyTextureInfo?, data: NativeAddress?, dataSize: ULong, dataLayout: WGPUTexelCopyBufferLayout?, writeSize: WGPUExtent3D?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueWriteTexture(queue?.handler, destination?.handler, data, dataSize.toLong(), dataLayout?.handler, writeSize?.handler)
    return
}

actual fun wgpuQueueAddRef(queue: WGPUQueue?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueAddRef(queue?.handler)
    return
}

actual fun wgpuQueueRelease(queue: WGPUQueue?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueRelease(queue?.handler)
    return
}

actual fun wgpuRenderBundleSetLabel(renderBundle: WGPURenderBundle?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleSetLabel(renderBundle?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuRenderBundleAddRef(renderBundle: WGPURenderBundle?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleAddRef(renderBundle?.handler)
    return
}

actual fun wgpuRenderBundleRelease(renderBundle: WGPURenderBundle?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleRelease(renderBundle?.handler)
    return
}

actual fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: WGPURenderBundleEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderDraw(renderBundleEncoder?.handler, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

actual fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: WGPURenderBundleEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder?.handler, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

actual fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder?.handler, indirectBuffer?.handler, indirectOffset.toLong())
    return
}

actual fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder?.handler, indirectBuffer?.handler, indirectOffset.toLong())
    return
}

actual fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: WGPURenderBundleEncoder?, descriptor: WGPURenderBundleDescriptor?): WGPURenderBundle? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderFinish(renderBundleEncoder?.handler, descriptor?.handler)?.let { WGPURenderBundle(it) }
}

actual fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: WGPURenderBundleEncoder?, markerLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(markerLabel.handler).apply { read() })
    return
}

actual fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder?.handler)
    return
}

actual fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(groupLabel.handler).apply { read() })
    return
}

actual fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder?.handler, groupIndex.toInt(), group?.handler, dynamicOffsetCount.toLong(), dynamicOffsets)
    return
}

actual fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: WGPURenderBundleEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder?.handler, offset.toInt(), data, size.toLong())
    return
}

actual fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder?.handler, buffer?.handler, format.toInt(), offset.toLong(), size.toLong())
    return
}

actual fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: WGPURenderBundleEncoder?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetLabel(renderBundleEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: WGPURenderBundleEncoder?, pipeline: WGPURenderPipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder?.handler, pipeline?.handler)
    return
}

actual fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder?.handler, slot.toInt(), buffer?.handler, offset.toLong(), size.toLong())
    return
}

actual fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderAddRef(renderBundleEncoder?.handler)
    return
}

actual fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderBundleEncoderRelease(renderBundleEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder?.handler, queryIndex.toInt())
    return
}

actual fun wgpuRenderPassEncoderDraw(renderPassEncoder: WGPURenderPassEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderDraw(renderPassEncoder?.handler, vertexCount.toInt(), instanceCount.toInt(), firstVertex.toInt(), firstInstance.toInt())
    return
}

actual fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: WGPURenderPassEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderDrawIndexed(renderPassEncoder?.handler, indexCount.toInt(), instanceCount.toInt(), firstIndex.toInt(), baseVertex, firstInstance.toInt())
    return
}

actual fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder?.handler, indirectBuffer?.handler, indirectOffset.toLong())
    return
}

actual fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderDrawIndirect(renderPassEncoder?.handler, indirectBuffer?.handler, indirectOffset.toLong())
    return
}

actual fun wgpuRenderPassEncoderEnd(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderEnd(renderPassEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: WGPURenderPassEncoder?, bundleCount: ULong, bundles: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderExecuteBundles(renderPassEncoder?.handler, bundleCount.toLong(), bundles)
    return
}

actual fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: WGPURenderPassEncoder?, markerLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(markerLabel.handler).apply { read() })
    return
}

actual fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: WGPURenderPassEncoder?, groupLabel: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(groupLabel.handler).apply { read() })
    return
}

actual fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: WGPURenderPassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetBindGroup(renderPassEncoder?.handler, groupIndex.toInt(), group?.handler, dynamicOffsetCount.toLong(), dynamicOffsets)
    return
}

actual fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: WGPURenderPassEncoder?, color: WGPUColor?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder?.handler, color?.handler)
    return
}

actual fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: WGPURenderPassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetImmediates(renderPassEncoder?.handler, offset.toInt(), data, size.toLong())
    return
}

actual fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder?.handler, buffer?.handler, format.toInt(), offset.toLong(), size.toLong())
    return
}

actual fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: WGPURenderPassEncoder?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetLabel(renderPassEncoder?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: WGPURenderPassEncoder?, pipeline: WGPURenderPipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetPipeline(renderPassEncoder?.handler, pipeline?.handler)
    return
}

actual fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: WGPURenderPassEncoder?, x: UInt, y: UInt, width: UInt, height: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetScissorRect(renderPassEncoder?.handler, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    return
}

actual fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: WGPURenderPassEncoder?, reference: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetStencilReference(renderPassEncoder?.handler, reference.toInt())
    return
}

actual fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: WGPURenderPassEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder?.handler, slot.toInt(), buffer?.handler, offset.toLong(), size.toLong())
    return
}

actual fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: WGPURenderPassEncoder?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderSetViewport(renderPassEncoder?.handler, x, y, width, height, minDepth, maxDepth)
    return
}

actual fun wgpuRenderPassEncoderAddRef(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderAddRef(renderPassEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderRelease(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderRelease(renderPassEncoder?.handler)
    return
}

actual fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: WGPURenderPipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPipelineGetBindGroupLayout(renderPipeline?.handler, groupIndex.toInt())?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuRenderPipelineSetLabel(renderPipeline: WGPURenderPipeline?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPipelineSetLabel(renderPipeline?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuRenderPipelineAddRef(renderPipeline: WGPURenderPipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPipelineAddRef(renderPipeline?.handler)
    return
}

actual fun wgpuRenderPipelineRelease(renderPipeline: WGPURenderPipeline?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPipelineRelease(renderPipeline?.handler)
    return
}

actual fun wgpuSamplerSetLabel(sampler: WGPUSampler?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSamplerSetLabel(sampler?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuSamplerAddRef(sampler: WGPUSampler?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSamplerAddRef(sampler?.handler)
    return
}

actual fun wgpuSamplerRelease(sampler: WGPUSampler?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSamplerRelease(sampler?.handler)
    return
}

actual fun wgpuShaderModuleGetCompilationInfo(shaderModule: WGPUShaderModule?, callbackInfo: WGPUCompilationInfoCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuShaderModuleGetCompilationInfo(shaderModule?.handler, io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue(callbackInfo.handler).apply { read() }))
}

actual fun wgpuShaderModuleSetLabel(shaderModule: WGPUShaderModule?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuShaderModuleSetLabel(shaderModule?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuShaderModuleAddRef(shaderModule: WGPUShaderModule?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuShaderModuleAddRef(shaderModule?.handler)
    return
}

actual fun wgpuShaderModuleRelease(shaderModule: WGPUShaderModule?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuShaderModuleRelease(shaderModule?.handler)
    return
}

actual fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: WGPUSupportedFeatures): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSupportedFeaturesFreeMembers(io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue(supportedFeatures.handler).apply { read() })
    return
}

actual fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: WGPUSupportedInstanceFeatures): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSupportedInstanceFeaturesFreeMembers(io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue(supportedInstanceFeatures.handler).apply { read() })
    return
}

actual fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: WGPUSupportedWGSLLanguageFeatures): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSupportedWGSLLanguageFeaturesFreeMembers(io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue(supportedWGSLLanguageFeatures.handler).apply { read() })
    return
}

actual fun wgpuSurfaceConfigure(surface: WGPUSurface?, config: WGPUSurfaceConfiguration?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceConfigure(surface?.handler, config?.handler)
    return
}

actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    val status = io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCapabilities(surface?.handler, adapter?.handler, capabilities?.handler)
    when (capabilities) {
        is WGPUSurfaceCapabilities.ByReference -> capabilities.handle.read()
        is WGPUSurfaceCapabilities.ByValue -> capabilities.handle.read()
        null -> Unit
    }
    return status.toUInt()
}

actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceGetCurrentTexture(surface?.handler, surfaceTexture?.handler)
    when (surfaceTexture) {
        is WGPUSurfaceTexture.ByReference -> surfaceTexture.handle.read()
        is WGPUSurfaceTexture.ByValue -> surfaceTexture.handle.read()
        null -> Unit
    }
    return
}

actual fun wgpuSurfacePresent(surface: WGPUSurface?): WGPUStatus {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfacePresent(surface?.handler)).toUInt()
}

actual fun wgpuSurfaceSetLabel(surface: WGPUSurface?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceSetLabel(surface?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuSurfaceUnconfigure(surface: WGPUSurface?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceUnconfigure(surface?.handler)
    return
}

actual fun wgpuSurfaceAddRef(surface: WGPUSurface?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceAddRef(surface?.handler)
    return
}

actual fun wgpuSurfaceRelease(surface: WGPUSurface?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceRelease(surface?.handler)
    return
}

actual fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: WGPUSurfaceCapabilities): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSurfaceCapabilitiesFreeMembers(io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue(surfaceCapabilities.handler).apply { read() })
    return
}

actual fun wgpuTextureCreateView(texture: WGPUTexture?, descriptor: WGPUTextureViewDescriptor?): WGPUTextureView? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureCreateView(texture?.handler, descriptor?.handler)?.let { WGPUTextureView(it) }
}

actual fun wgpuTextureDestroy(texture: WGPUTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureDestroy(texture?.handler)
    return
}

actual fun wgpuTextureGetDepthOrArrayLayers(texture: WGPUTexture?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetDepthOrArrayLayers(texture?.handler).toUInt()
}

actual fun wgpuTextureGetDimension(texture: WGPUTexture?): WGPUTextureDimension {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetDimension(texture?.handler)).toUInt()
}

actual fun wgpuTextureGetFormat(texture: WGPUTexture?): WGPUTextureFormat {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetFormat(texture?.handler)).toUInt()
}

actual fun wgpuTextureGetHeight(texture: WGPUTexture?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetHeight(texture?.handler).toUInt()
}

actual fun wgpuTextureGetMipLevelCount(texture: WGPUTexture?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetMipLevelCount(texture?.handler).toUInt()
}

actual fun wgpuTextureGetSampleCount(texture: WGPUTexture?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetSampleCount(texture?.handler).toUInt()
}

actual fun wgpuTextureGetTextureBindingViewDimension(texture: WGPUTexture?): WGPUTextureViewDimension {
    return (io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetTextureBindingViewDimension(texture?.handler)).toUInt()
}

actual fun wgpuTextureGetUsage(texture: WGPUTexture?): ULong {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetUsage(texture?.handler).toULong()
}

actual fun wgpuTextureGetWidth(texture: WGPUTexture?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetWidth(texture?.handler).toUInt()
}

actual fun wgpuTextureSetLabel(texture: WGPUTexture?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureSetLabel(texture?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuTextureAddRef(texture: WGPUTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureAddRef(texture?.handler)
    return
}

actual fun wgpuTextureRelease(texture: WGPUTexture?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureRelease(texture?.handler)
    return
}

actual fun wgpuTextureViewSetLabel(textureView: WGPUTextureView?, label: WGPUStringView): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureViewSetLabel(textureView?.handler, io.ygdrasil.wgpu.android.WGPUStringView.ByValue(label.handler).apply { read() })
    return
}

actual fun wgpuTextureViewAddRef(textureView: WGPUTextureView?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureViewAddRef(textureView?.handler)
    return
}

actual fun wgpuTextureViewRelease(textureView: WGPUTextureView?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureViewRelease(textureView?.handler)
    return
}

actual interface WGPUXlibDisplayHandle {
    actual var display: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXlibDisplayHandle {
            return ByReference(io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUXlibDisplayHandle {
            val ref = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXlibDisplayHandle) -> Unit): ArrayHolder<WGPUXlibDisplayHandle> {
            val ref = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByReference = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByReference(com.sun.jna.Pointer.NULL)) : WGPUXlibDisplayHandle {
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var screen: Int
            get() = handle.screen as Int
            set(value) { handle.screen = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue(com.sun.jna.Pointer.NULL)) : WGPUXlibDisplayHandle {
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override var screen: Int
            get() = handle.screen as Int
            set(value) { handle.screen = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUXcbDisplayHandle {
    actual var connection: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXcbDisplayHandle {
            return ByReference(io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUXcbDisplayHandle {
            val ref = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXcbDisplayHandle) -> Unit): ArrayHolder<WGPUXcbDisplayHandle> {
            val ref = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByReference = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByReference(com.sun.jna.Pointer.NULL)) : WGPUXcbDisplayHandle {
        override var connection: NativeAddress?
            get() = handle.connection
            set(value) { handle.connection = value }
        override var screen: Int
            get() = handle.screen as Int
            set(value) { handle.screen = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue(com.sun.jna.Pointer.NULL)) : WGPUXcbDisplayHandle {
        override var connection: NativeAddress?
            get() = handle.connection
            set(value) { handle.connection = value }
        override var screen: Int
            get() = handle.screen as Int
            set(value) { handle.screen = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUWaylandDisplayHandle {
    actual var display: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUWaylandDisplayHandle {
            return ByReference(io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUWaylandDisplayHandle {
            val ref = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUWaylandDisplayHandle) -> Unit): ArrayHolder<WGPUWaylandDisplayHandle> {
            val ref = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByReference = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByReference(com.sun.jna.Pointer.NULL)) : WGPUWaylandDisplayHandle {
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue(com.sun.jna.Pointer.NULL)) : WGPUWaylandDisplayHandle {
        override var display: NativeAddress?
            get() = handle.display
            set(value) { handle.display = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUNativeDisplayHandle = ByReference(io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByReference(address))
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeDisplayHandle {
            val ref = io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeDisplayHandle) -> Unit): ArrayHolder<WGPUNativeDisplayHandle> {
            val ref = io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct -> provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue)) }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByReference = io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByReference(com.sun.jna.Pointer.NULL)) : WGPUNativeDisplayHandle {
        override var type: WGPUNativeDisplayHandleType
            get() { handle.read(); return handle.type.toUInt() as WGPUNativeDisplayHandleType }
            set(value) { handle.type = value.toInt(); handle.write() }
        override val xlib: WGPUXlibDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Xlib) return null
                handle.data.readField("xlib")
                return WGPUXlibDisplayHandle.ByValue(handle.data.xlib)
            }
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Xlib.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.xlib = copy
            handle.data.writeField("xlib")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Xcb) return null
                handle.data.readField("xcb")
                return WGPUXcbDisplayHandle.ByValue(handle.data.xcb)
            }
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Xcb.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.xcb = copy
            handle.data.writeField("xcb")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Wayland) return null
                handle.data.readField("wayland")
                return WGPUWaylandDisplayHandle.ByValue(handle.data.wayland)
            }
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Wayland.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.wayland = copy
            handle.data.writeField("wayland")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val handler: NativeAddress
            get() { handle.write(); return handle.pointer }
    }
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue = io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue(com.sun.jna.Pointer.NULL)) : WGPUNativeDisplayHandle {
        override var type: WGPUNativeDisplayHandleType
            get() { handle.read(); return handle.type.toUInt() as WGPUNativeDisplayHandleType }
            set(value) { handle.type = value.toInt(); handle.write() }
        override val xlib: WGPUXlibDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Xlib) return null
                handle.data.readField("xlib")
                return WGPUXlibDisplayHandle.ByValue(handle.data.xlib)
            }
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Xlib.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUXlibDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.xlib = copy
            handle.data.writeField("xlib")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Xcb) return null
                handle.data.readField("xcb")
                return WGPUXcbDisplayHandle.ByValue(handle.data.xcb)
            }
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Xcb.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUXcbDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.xcb = copy
            handle.data.writeField("xcb")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() {
                handle.read()
                if (type != WGPUNativeDisplayHandleType_Wayland) return null
                handle.data.readField("wayland")
                return WGPUWaylandDisplayHandle.ByValue(handle.data.wayland)
            }
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            handle.type = WGPUNativeDisplayHandleType_Wayland.toInt()
            val copy = io.ygdrasil.wgpu.android.WGPUWaylandDisplayHandle.ByValue(value.handler)
            copy.read()
            handle.data.wayland = copy
            handle.data.writeField("wayland")
            handle.writeField("type")
            handle.writeField("data")
        }
        override val handler: NativeAddress
            get() { handle.write(); return handle.pointer }
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
        actual operator fun invoke(address: NativeAddress): WGPUInstanceExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceExtras) -> Unit): ArrayHolder<WGPUInstanceExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByReference = io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUInstanceExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var backends: ULong
            get() = handle.backends.toULong() as ULong
            set(value) { handle.backends = value.toLong() }
        override var flags: ULong
            get() = handle.flags.toULong() as ULong
            set(value) { handle.flags = value.toLong() }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = handle.dx12ShaderCompiler.toUInt() as WGPUDx12Compiler
            set(value) { handle.dx12ShaderCompiler = value.toInt() }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = handle.gles3MinorVersion.toUInt() as WGPUGles3MinorVersion
            set(value) { handle.gles3MinorVersion = value.toInt() }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = handle.glFenceBehaviour.toUInt() as WGPUGLFenceBehaviour
            set(value) { handle.glFenceBehaviour = value.toInt() }
        override var dxcPath: WGPUStringView
            get() {
                handle.readField("dxcPath")
                return WGPUStringView.ByValue(handle.dxcPath)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.dxcPath.size())
                handle.readField("dxcPath")
                handle.dxcPath.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("dxcPath")
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = handle.dxcMaxShaderModel.toUInt() as WGPUDxcMaxShaderModel
            set(value) { handle.dxcMaxShaderModel = value.toInt() }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = handle.dx12PresentationSystem.toUInt() as WGPUDx12SwapchainKind
            set(value) { handle.dx12PresentationSystem = value.toInt() }
        override var budgetForDeviceCreation: NativeAddress?
            get() = handle.budgetForDeviceCreation
            set(value) { handle.budgetForDeviceCreation = value }
        override var budgetForDeviceLoss: NativeAddress?
            get() = handle.budgetForDeviceLoss
            set(value) { handle.budgetForDeviceLoss = value }
        override var displayHandle: WGPUNativeDisplayHandle
            get() {
                handle.readField("displayHandle")
                return WGPUNativeDisplayHandle.ByValue(handle.displayHandle)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.displayHandle.size())
                handle.readField("displayHandle")
                handle.displayHandle.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("displayHandle")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByValue = io.ygdrasil.wgpu.android.WGPUInstanceExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUInstanceExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var backends: ULong
            get() = handle.backends.toULong() as ULong
            set(value) { handle.backends = value.toLong() }
        override var flags: ULong
            get() = handle.flags.toULong() as ULong
            set(value) { handle.flags = value.toLong() }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = handle.dx12ShaderCompiler.toUInt() as WGPUDx12Compiler
            set(value) { handle.dx12ShaderCompiler = value.toInt() }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = handle.gles3MinorVersion.toUInt() as WGPUGles3MinorVersion
            set(value) { handle.gles3MinorVersion = value.toInt() }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = handle.glFenceBehaviour.toUInt() as WGPUGLFenceBehaviour
            set(value) { handle.glFenceBehaviour = value.toInt() }
        override var dxcPath: WGPUStringView
            get() {
                handle.readField("dxcPath")
                return WGPUStringView.ByValue(handle.dxcPath)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.dxcPath.size())
                handle.readField("dxcPath")
                handle.dxcPath.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("dxcPath")
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = handle.dxcMaxShaderModel.toUInt() as WGPUDxcMaxShaderModel
            set(value) { handle.dxcMaxShaderModel = value.toInt() }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = handle.dx12PresentationSystem.toUInt() as WGPUDx12SwapchainKind
            set(value) { handle.dx12PresentationSystem = value.toInt() }
        override var budgetForDeviceCreation: NativeAddress?
            get() = handle.budgetForDeviceCreation
            set(value) { handle.budgetForDeviceCreation = value }
        override var budgetForDeviceLoss: NativeAddress?
            get() = handle.budgetForDeviceLoss
            set(value) { handle.budgetForDeviceLoss = value }
        override var displayHandle: WGPUNativeDisplayHandle
            get() {
                handle.readField("displayHandle")
                return WGPUNativeDisplayHandle.ByValue(handle.displayHandle)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.displayHandle.size())
                handle.readField("displayHandle")
                handle.displayHandle.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("displayHandle")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUDeviceExtras {
    actual var chain: WGPUChainedStruct
    actual var tracePath: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDeviceExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceExtras) -> Unit): ArrayHolder<WGPUDeviceExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByReference = io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUDeviceExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var tracePath: WGPUStringView
            get() {
                handle.readField("tracePath")
                return WGPUStringView.ByValue(handle.tracePath)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.tracePath.size())
                handle.readField("tracePath")
                handle.tracePath.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("tracePath")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByValue = io.ygdrasil.wgpu.android.WGPUDeviceExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUDeviceExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var tracePath: WGPUStringView
            get() {
                handle.readField("tracePath")
                return WGPUStringView.ByValue(handle.tracePath)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.tracePath.size())
                handle.readField("tracePath")
                handle.tracePath.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("tracePath")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUNativeLimits {
            return ByReference(io.ygdrasil.wgpu.android.WGPUNativeLimits.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeLimits {
            val ref = io.ygdrasil.wgpu.android.WGPUNativeLimits.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeLimits) -> Unit): ArrayHolder<WGPUNativeLimits> {
            val ref = io.ygdrasil.wgpu.android.WGPUNativeLimits.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUNativeLimits.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUNativeLimits.ByReference = io.ygdrasil.wgpu.android.WGPUNativeLimits.ByReference(com.sun.jna.Pointer.NULL)) : WGPUNativeLimits {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxNonSamplerBindings: UInt
            get() = handle.maxNonSamplerBindings.toUInt() as UInt
            set(value) { handle.maxNonSamplerBindings = value.toInt() }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = handle.maxBindingArrayElementsPerShaderStage.toUInt() as UInt
            set(value) { handle.maxBindingArrayElementsPerShaderStage = value.toInt() }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = handle.maxBindingArraySamplerElementsPerShaderStage.toUInt() as UInt
            set(value) { handle.maxBindingArraySamplerElementsPerShaderStage = value.toInt() }
        override var maxMultiviewViewCount: UInt
            get() = handle.maxMultiviewViewCount.toUInt() as UInt
            set(value) { handle.maxMultiviewViewCount = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUNativeLimits.ByValue = io.ygdrasil.wgpu.android.WGPUNativeLimits.ByValue(com.sun.jna.Pointer.NULL)) : WGPUNativeLimits {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var maxNonSamplerBindings: UInt
            get() = handle.maxNonSamplerBindings.toUInt() as UInt
            set(value) { handle.maxNonSamplerBindings = value.toInt() }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = handle.maxBindingArrayElementsPerShaderStage.toUInt() as UInt
            set(value) { handle.maxBindingArrayElementsPerShaderStage = value.toInt() }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = handle.maxBindingArraySamplerElementsPerShaderStage.toUInt() as UInt
            set(value) { handle.maxBindingArraySamplerElementsPerShaderStage = value.toInt() }
        override var maxMultiviewViewCount: UInt
            get() = handle.maxMultiviewViewCount.toUInt() as UInt
            set(value) { handle.maxMultiviewViewCount = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUShaderDefine {
    actual var name: WGPUStringView
    actual var value: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderDefine {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderDefine.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderDefine {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderDefine.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderDefine) -> Unit): ArrayHolder<WGPUShaderDefine> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderDefine.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderDefine.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderDefine.ByReference = io.ygdrasil.wgpu.android.WGPUShaderDefine.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderDefine {
        override var name: WGPUStringView
            get() {
                handle.readField("name")
                return WGPUStringView.ByValue(handle.name)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.name.size())
                handle.readField("name")
                handle.name.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("name")
            }
        override var value: WGPUStringView
            get() {
                handle.readField("value")
                return WGPUStringView.ByValue(handle.value)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.value.size())
                handle.readField("value")
                handle.value.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("value")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderDefine.ByValue = io.ygdrasil.wgpu.android.WGPUShaderDefine.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderDefine {
        override var name: WGPUStringView
            get() {
                handle.readField("name")
                return WGPUStringView.ByValue(handle.name)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.name.size())
                handle.readField("name")
                handle.name.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("name")
            }
        override var value: WGPUStringView
            get() {
                handle.readField("value")
                return WGPUStringView.ByValue(handle.value)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.value.size())
                handle.readField("value")
                handle.value.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("value")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceGLSL {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceGLSL {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceGLSL) -> Unit): ArrayHolder<WGPUShaderSourceGLSL> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByReference = io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceGLSL {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var stage: ULong
            get() = handle.stage.toULong() as ULong
            set(value) { handle.stage = value.toLong() }
        override var code: WGPUStringView
            get() {
                handle.readField("code")
                return WGPUStringView.ByValue(handle.code)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.code.size())
                handle.readField("code")
                handle.code.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("code")
            }
        override var defineCount: UInt
            get() = handle.defineCount.toUInt() as UInt
            set(value) { handle.defineCount = value.toInt() }
        override var defines: WGPUShaderDefine?
            get() = handle.defines?.let { WGPUShaderDefine(it) }
            set(value) { handle.defines = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByValue = io.ygdrasil.wgpu.android.WGPUShaderSourceGLSL.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderSourceGLSL {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var stage: ULong
            get() = handle.stage.toULong() as ULong
            set(value) { handle.stage = value.toLong() }
        override var code: WGPUStringView
            get() {
                handle.readField("code")
                return WGPUStringView.ByValue(handle.code)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.code.size())
                handle.readField("code")
                handle.code.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("code")
            }
        override var defineCount: UInt
            get() = handle.defineCount.toUInt() as UInt
            set(value) { handle.defineCount = value.toInt() }
        override var defines: WGPUShaderDefine?
            get() = handle.defines?.let { WGPUShaderDefine(it) }
            set(value) { handle.defines = value?.handler }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUShaderModuleDescriptorSpirV {
    actual var label: WGPUStringView
    actual var sourceSize: UInt
    actual var source: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptorSpirV {
            return ByReference(io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptorSpirV {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptorSpirV) -> Unit): ArrayHolder<WGPUShaderModuleDescriptorSpirV> {
            val ref = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByReference = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByReference(com.sun.jna.Pointer.NULL)) : WGPUShaderModuleDescriptorSpirV {
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var sourceSize: UInt
            get() = handle.sourceSize.toUInt() as UInt
            set(value) { handle.sourceSize = value.toInt() }
        override var source: NativeAddress?
            get() = handle.source
            set(value) { handle.source = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByValue = io.ygdrasil.wgpu.android.WGPUShaderModuleDescriptorSpirV.ByValue(com.sun.jna.Pointer.NULL)) : WGPUShaderModuleDescriptorSpirV {
        override var label: WGPUStringView
            get() {
                handle.readField("label")
                return WGPUStringView.ByValue(handle.label)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.label.size())
                handle.readField("label")
                handle.label.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("label")
            }
        override var sourceSize: UInt
            get() = handle.sourceSize.toUInt() as UInt
            set(value) { handle.sourceSize = value.toInt() }
        override var source: NativeAddress?
            get() = handle.source
            set(value) { handle.source = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPURegistryReport {
    actual var numAllocated: ULong
    actual var numKeptFromUser: ULong
    actual var numReleasedFromUser: ULong
    actual var elementSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURegistryReport {
            return ByReference(io.ygdrasil.wgpu.android.WGPURegistryReport.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPURegistryReport {
            val ref = io.ygdrasil.wgpu.android.WGPURegistryReport.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURegistryReport) -> Unit): ArrayHolder<WGPURegistryReport> {
            val ref = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPURegistryReport.ByReference = io.ygdrasil.wgpu.android.WGPURegistryReport.ByReference(com.sun.jna.Pointer.NULL)) : WGPURegistryReport {
        override var numAllocated: ULong
            get() = handle.numAllocated.toULong() as ULong
            set(value) { handle.numAllocated = value.toLong() }
        override var numKeptFromUser: ULong
            get() = handle.numKeptFromUser.toULong() as ULong
            set(value) { handle.numKeptFromUser = value.toLong() }
        override var numReleasedFromUser: ULong
            get() = handle.numReleasedFromUser.toULong() as ULong
            set(value) { handle.numReleasedFromUser = value.toLong() }
        override var elementSize: ULong
            get() = handle.elementSize.toULong() as ULong
            set(value) { handle.elementSize = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue(com.sun.jna.Pointer.NULL)) : WGPURegistryReport {
        override var numAllocated: ULong
            get() = handle.numAllocated.toULong() as ULong
            set(value) { handle.numAllocated = value.toLong() }
        override var numKeptFromUser: ULong
            get() = handle.numKeptFromUser.toULong() as ULong
            set(value) { handle.numKeptFromUser = value.toLong() }
        override var numReleasedFromUser: ULong
            get() = handle.numReleasedFromUser.toULong() as ULong
            set(value) { handle.numReleasedFromUser = value.toLong() }
        override var elementSize: ULong
            get() = handle.elementSize.toULong() as ULong
            set(value) { handle.elementSize = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUHubReport {
            return ByReference(io.ygdrasil.wgpu.android.WGPUHubReport.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUHubReport {
            val ref = io.ygdrasil.wgpu.android.WGPUHubReport.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUHubReport) -> Unit): ArrayHolder<WGPUHubReport> {
            val ref = io.ygdrasil.wgpu.android.WGPUHubReport.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUHubReport.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUHubReport.ByReference = io.ygdrasil.wgpu.android.WGPUHubReport.ByReference(com.sun.jna.Pointer.NULL)) : WGPUHubReport {
        override var adapters: WGPURegistryReport
            get() {
                handle.readField("adapters")
                return WGPURegistryReport.ByValue(handle.adapters)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.adapters.size())
                handle.readField("adapters")
                handle.adapters.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("adapters")
            }
        override var devices: WGPURegistryReport
            get() {
                handle.readField("devices")
                return WGPURegistryReport.ByValue(handle.devices)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.devices.size())
                handle.readField("devices")
                handle.devices.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("devices")
            }
        override var queues: WGPURegistryReport
            get() {
                handle.readField("queues")
                return WGPURegistryReport.ByValue(handle.queues)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.queues.size())
                handle.readField("queues")
                handle.queues.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("queues")
            }
        override var pipelineLayouts: WGPURegistryReport
            get() {
                handle.readField("pipelineLayouts")
                return WGPURegistryReport.ByValue(handle.pipelineLayouts)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.pipelineLayouts.size())
                handle.readField("pipelineLayouts")
                handle.pipelineLayouts.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("pipelineLayouts")
            }
        override var shaderModules: WGPURegistryReport
            get() {
                handle.readField("shaderModules")
                return WGPURegistryReport.ByValue(handle.shaderModules)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.shaderModules.size())
                handle.readField("shaderModules")
                handle.shaderModules.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("shaderModules")
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() {
                handle.readField("bindGroupLayouts")
                return WGPURegistryReport.ByValue(handle.bindGroupLayouts)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.bindGroupLayouts.size())
                handle.readField("bindGroupLayouts")
                handle.bindGroupLayouts.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("bindGroupLayouts")
            }
        override var bindGroups: WGPURegistryReport
            get() {
                handle.readField("bindGroups")
                return WGPURegistryReport.ByValue(handle.bindGroups)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.bindGroups.size())
                handle.readField("bindGroups")
                handle.bindGroups.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("bindGroups")
            }
        override var commandBuffers: WGPURegistryReport
            get() {
                handle.readField("commandBuffers")
                return WGPURegistryReport.ByValue(handle.commandBuffers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.commandBuffers.size())
                handle.readField("commandBuffers")
                handle.commandBuffers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("commandBuffers")
            }
        override var renderBundles: WGPURegistryReport
            get() {
                handle.readField("renderBundles")
                return WGPURegistryReport.ByValue(handle.renderBundles)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.renderBundles.size())
                handle.readField("renderBundles")
                handle.renderBundles.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("renderBundles")
            }
        override var renderPipelines: WGPURegistryReport
            get() {
                handle.readField("renderPipelines")
                return WGPURegistryReport.ByValue(handle.renderPipelines)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.renderPipelines.size())
                handle.readField("renderPipelines")
                handle.renderPipelines.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("renderPipelines")
            }
        override var computePipelines: WGPURegistryReport
            get() {
                handle.readField("computePipelines")
                return WGPURegistryReport.ByValue(handle.computePipelines)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.computePipelines.size())
                handle.readField("computePipelines")
                handle.computePipelines.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("computePipelines")
            }
        override var pipelineCaches: WGPURegistryReport
            get() {
                handle.readField("pipelineCaches")
                return WGPURegistryReport.ByValue(handle.pipelineCaches)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.pipelineCaches.size())
                handle.readField("pipelineCaches")
                handle.pipelineCaches.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("pipelineCaches")
            }
        override var querySets: WGPURegistryReport
            get() {
                handle.readField("querySets")
                return WGPURegistryReport.ByValue(handle.querySets)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.querySets.size())
                handle.readField("querySets")
                handle.querySets.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("querySets")
            }
        override var buffers: WGPURegistryReport
            get() {
                handle.readField("buffers")
                return WGPURegistryReport.ByValue(handle.buffers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.buffers.size())
                handle.readField("buffers")
                handle.buffers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("buffers")
            }
        override var textures: WGPURegistryReport
            get() {
                handle.readField("textures")
                return WGPURegistryReport.ByValue(handle.textures)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.textures.size())
                handle.readField("textures")
                handle.textures.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("textures")
            }
        override var textureViews: WGPURegistryReport
            get() {
                handle.readField("textureViews")
                return WGPURegistryReport.ByValue(handle.textureViews)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.textureViews.size())
                handle.readField("textureViews")
                handle.textureViews.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("textureViews")
            }
        override var samplers: WGPURegistryReport
            get() {
                handle.readField("samplers")
                return WGPURegistryReport.ByValue(handle.samplers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.samplers.size())
                handle.readField("samplers")
                handle.samplers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("samplers")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUHubReport.ByValue = io.ygdrasil.wgpu.android.WGPUHubReport.ByValue(com.sun.jna.Pointer.NULL)) : WGPUHubReport {
        override var adapters: WGPURegistryReport
            get() {
                handle.readField("adapters")
                return WGPURegistryReport.ByValue(handle.adapters)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.adapters.size())
                handle.readField("adapters")
                handle.adapters.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("adapters")
            }
        override var devices: WGPURegistryReport
            get() {
                handle.readField("devices")
                return WGPURegistryReport.ByValue(handle.devices)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.devices.size())
                handle.readField("devices")
                handle.devices.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("devices")
            }
        override var queues: WGPURegistryReport
            get() {
                handle.readField("queues")
                return WGPURegistryReport.ByValue(handle.queues)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.queues.size())
                handle.readField("queues")
                handle.queues.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("queues")
            }
        override var pipelineLayouts: WGPURegistryReport
            get() {
                handle.readField("pipelineLayouts")
                return WGPURegistryReport.ByValue(handle.pipelineLayouts)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.pipelineLayouts.size())
                handle.readField("pipelineLayouts")
                handle.pipelineLayouts.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("pipelineLayouts")
            }
        override var shaderModules: WGPURegistryReport
            get() {
                handle.readField("shaderModules")
                return WGPURegistryReport.ByValue(handle.shaderModules)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.shaderModules.size())
                handle.readField("shaderModules")
                handle.shaderModules.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("shaderModules")
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() {
                handle.readField("bindGroupLayouts")
                return WGPURegistryReport.ByValue(handle.bindGroupLayouts)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.bindGroupLayouts.size())
                handle.readField("bindGroupLayouts")
                handle.bindGroupLayouts.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("bindGroupLayouts")
            }
        override var bindGroups: WGPURegistryReport
            get() {
                handle.readField("bindGroups")
                return WGPURegistryReport.ByValue(handle.bindGroups)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.bindGroups.size())
                handle.readField("bindGroups")
                handle.bindGroups.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("bindGroups")
            }
        override var commandBuffers: WGPURegistryReport
            get() {
                handle.readField("commandBuffers")
                return WGPURegistryReport.ByValue(handle.commandBuffers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.commandBuffers.size())
                handle.readField("commandBuffers")
                handle.commandBuffers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("commandBuffers")
            }
        override var renderBundles: WGPURegistryReport
            get() {
                handle.readField("renderBundles")
                return WGPURegistryReport.ByValue(handle.renderBundles)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.renderBundles.size())
                handle.readField("renderBundles")
                handle.renderBundles.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("renderBundles")
            }
        override var renderPipelines: WGPURegistryReport
            get() {
                handle.readField("renderPipelines")
                return WGPURegistryReport.ByValue(handle.renderPipelines)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.renderPipelines.size())
                handle.readField("renderPipelines")
                handle.renderPipelines.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("renderPipelines")
            }
        override var computePipelines: WGPURegistryReport
            get() {
                handle.readField("computePipelines")
                return WGPURegistryReport.ByValue(handle.computePipelines)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.computePipelines.size())
                handle.readField("computePipelines")
                handle.computePipelines.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("computePipelines")
            }
        override var pipelineCaches: WGPURegistryReport
            get() {
                handle.readField("pipelineCaches")
                return WGPURegistryReport.ByValue(handle.pipelineCaches)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.pipelineCaches.size())
                handle.readField("pipelineCaches")
                handle.pipelineCaches.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("pipelineCaches")
            }
        override var querySets: WGPURegistryReport
            get() {
                handle.readField("querySets")
                return WGPURegistryReport.ByValue(handle.querySets)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.querySets.size())
                handle.readField("querySets")
                handle.querySets.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("querySets")
            }
        override var buffers: WGPURegistryReport
            get() {
                handle.readField("buffers")
                return WGPURegistryReport.ByValue(handle.buffers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.buffers.size())
                handle.readField("buffers")
                handle.buffers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("buffers")
            }
        override var textures: WGPURegistryReport
            get() {
                handle.readField("textures")
                return WGPURegistryReport.ByValue(handle.textures)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.textures.size())
                handle.readField("textures")
                handle.textures.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("textures")
            }
        override var textureViews: WGPURegistryReport
            get() {
                handle.readField("textureViews")
                return WGPURegistryReport.ByValue(handle.textureViews)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.textureViews.size())
                handle.readField("textureViews")
                handle.textureViews.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("textureViews")
            }
        override var samplers: WGPURegistryReport
            get() {
                handle.readField("samplers")
                return WGPURegistryReport.ByValue(handle.samplers)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.samplers.size())
                handle.readField("samplers")
                handle.samplers.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("samplers")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUGlobalReport {
    actual var surfaces: WGPURegistryReport
    actual var hub: WGPUHubReport
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUGlobalReport {
            return ByReference(io.ygdrasil.wgpu.android.WGPUGlobalReport.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUGlobalReport {
            val ref = io.ygdrasil.wgpu.android.WGPUGlobalReport.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUGlobalReport) -> Unit): ArrayHolder<WGPUGlobalReport> {
            val ref = io.ygdrasil.wgpu.android.WGPUGlobalReport.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUGlobalReport.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUGlobalReport.ByReference = io.ygdrasil.wgpu.android.WGPUGlobalReport.ByReference(com.sun.jna.Pointer.NULL)) : WGPUGlobalReport {
        override var surfaces: WGPURegistryReport
            get() {
                handle.readField("surfaces")
                return WGPURegistryReport.ByValue(handle.surfaces)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.surfaces.size())
                handle.readField("surfaces")
                handle.surfaces.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("surfaces")
            }
        override var hub: WGPUHubReport
            get() {
                handle.readField("hub")
                return WGPUHubReport.ByValue(handle.hub)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.hub.size())
                handle.readField("hub")
                handle.hub.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("hub")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUGlobalReport.ByValue = io.ygdrasil.wgpu.android.WGPUGlobalReport.ByValue(com.sun.jna.Pointer.NULL)) : WGPUGlobalReport {
        override var surfaces: WGPURegistryReport
            get() {
                handle.readField("surfaces")
                return WGPURegistryReport.ByValue(handle.surfaces)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.surfaces.size())
                handle.readField("surfaces")
                handle.surfaces.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("surfaces")
            }
        override var hub: WGPUHubReport
            get() {
                handle.readField("hub")
                return WGPUHubReport.ByValue(handle.hub)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.hub.size())
                handle.readField("hub")
                handle.hub.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("hub")
            }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUInstanceEnumerateAdapterOptions {
    actual var nextInChain: WGPUChainedStruct?
    actual var backends: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceEnumerateAdapterOptions {
            return ByReference(io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceEnumerateAdapterOptions {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceEnumerateAdapterOptions) -> Unit): ArrayHolder<WGPUInstanceEnumerateAdapterOptions> {
            val ref = io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByReference = io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByReference(com.sun.jna.Pointer.NULL)) : WGPUInstanceEnumerateAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var backends: ULong
            get() = handle.backends.toULong() as ULong
            set(value) { handle.backends = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByValue = io.ygdrasil.wgpu.android.WGPUInstanceEnumerateAdapterOptions.ByValue(com.sun.jna.Pointer.NULL)) : WGPUInstanceEnumerateAdapterOptions {
        override var nextInChain: WGPUChainedStruct?
            get() = handle.nextInChain?.let { WGPUChainedStruct(it) }
            set(value) { handle.nextInChain = value?.handler }
        override var backends: ULong
            get() = handle.backends.toULong() as ULong
            set(value) { handle.backends = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupEntryExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntryExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupEntryExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupEntryExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var buffers: NativeAddress?
            get() = handle.buffers
            set(value) { handle.buffers = value }
        override var bufferCount: ULong
            get() = handle.bufferCount.toULong() as ULong
            set(value) { handle.bufferCount = value.toLong() }
        override var samplers: NativeAddress?
            get() = handle.samplers
            set(value) { handle.samplers = value }
        override var samplerCount: ULong
            get() = handle.samplerCount.toULong() as ULong
            set(value) { handle.samplerCount = value.toLong() }
        override var textureViews: NativeAddress?
            get() = handle.textureViews
            set(value) { handle.textureViews = value }
        override var textureViewCount: ULong
            get() = handle.textureViewCount.toULong() as ULong
            set(value) { handle.textureViewCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupEntryExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupEntryExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var buffers: NativeAddress?
            get() = handle.buffers
            set(value) { handle.buffers = value }
        override var bufferCount: ULong
            get() = handle.bufferCount.toULong() as ULong
            set(value) { handle.bufferCount = value.toLong() }
        override var samplers: NativeAddress?
            get() = handle.samplers
            set(value) { handle.samplers = value }
        override var samplerCount: ULong
            get() = handle.samplerCount.toULong() as ULong
            set(value) { handle.samplerCount = value.toLong() }
        override var textureViews: NativeAddress?
            get() = handle.textureViews
            set(value) { handle.textureViews = value }
        override var textureViewCount: ULong
            get() = handle.textureViewCount.toULong() as ULong
            set(value) { handle.textureViewCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUBindGroupLayoutEntryExtras {
    actual var chain: WGPUChainedStruct
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntryExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntryExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntryExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByReference = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutEntryExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByValue = io.ygdrasil.wgpu.android.WGPUBindGroupLayoutEntryExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUBindGroupLayoutEntryExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var count: UInt
            get() = handle.count.toUInt() as UInt
            set(value) { handle.count = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUQuerySetDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var pipelineStatistics: NativeAddress?
    actual var pipelineStatisticCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptorExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptorExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptorExtras) -> Unit): ArrayHolder<WGPUQuerySetDescriptorExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByReference = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUQuerySetDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var pipelineStatistics: NativeAddress?
            get() = handle.pipelineStatistics
            set(value) { handle.pipelineStatistics = value }
        override var pipelineStatisticCount: ULong
            get() = handle.pipelineStatisticCount.toULong() as ULong
            set(value) { handle.pipelineStatisticCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByValue = io.ygdrasil.wgpu.android.WGPUQuerySetDescriptorExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUQuerySetDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var pipelineStatistics: NativeAddress?
            get() = handle.pipelineStatistics
            set(value) { handle.pipelineStatistics = value }
        override var pipelineStatisticCount: ULong
            get() = handle.pipelineStatisticCount.toULong() as ULong
            set(value) { handle.pipelineStatisticCount = value.toLong() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceConfigurationExtras {
    actual var chain: WGPUChainedStruct
    actual var desiredMaximumFrameLatency: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfigurationExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfigurationExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfigurationExtras) -> Unit): ArrayHolder<WGPUSurfaceConfigurationExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceConfigurationExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var desiredMaximumFrameLatency: UInt
            get() = handle.desiredMaximumFrameLatency.toUInt() as UInt
            set(value) { handle.desiredMaximumFrameLatency = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceConfigurationExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceConfigurationExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var desiredMaximumFrameLatency: UInt
            get() = handle.desiredMaximumFrameLatency.toUInt() as UInt
            set(value) { handle.desiredMaximumFrameLatency = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSurfaceSourceSwapChainPanel {
    actual var chain: WGPUChainedStruct
    actual var panelNative: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceSwapChainPanel {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceSwapChainPanel {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceSwapChainPanel) -> Unit): ArrayHolder<WGPUSurfaceSourceSwapChainPanel> {
            val ref = io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByReference = io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceSwapChainPanel {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var panelNative: NativeAddress?
            get() = handle.panelNative
            set(value) { handle.panelNative = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByValue = io.ygdrasil.wgpu.android.WGPUSurfaceSourceSwapChainPanel.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSurfaceSourceSwapChainPanel {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var panelNative: NativeAddress?
            get() = handle.panelNative
            set(value) { handle.panelNative = value }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUPrimitiveStateExtras {
    actual var chain: WGPUChainedStruct
    actual var polygonMode: WGPUPolygonMode
    actual var conservative: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveStateExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveStateExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveStateExtras) -> Unit): ArrayHolder<WGPUPrimitiveStateExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByReference = io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUPrimitiveStateExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var polygonMode: WGPUPolygonMode
            get() = handle.polygonMode.toUInt() as WGPUPolygonMode
            set(value) { handle.polygonMode = value.toInt() }
        override var conservative: UInt
            get() = handle.conservative.toUInt() as UInt
            set(value) { handle.conservative = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByValue = io.ygdrasil.wgpu.android.WGPUPrimitiveStateExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUPrimitiveStateExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var polygonMode: WGPUPolygonMode
            get() = handle.polygonMode.toUInt() as WGPUPolygonMode
            set(value) { handle.polygonMode = value.toInt() }
        override var conservative: UInt
            get() = handle.conservative.toUInt() as UInt
            set(value) { handle.conservative = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
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
        actual operator fun invoke(address: NativeAddress): WGPUImageSubresourceRange {
            return ByReference(io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUImageSubresourceRange {
            val ref = io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUImageSubresourceRange) -> Unit): ArrayHolder<WGPUImageSubresourceRange> {
            val ref = io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByReference = io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByReference(com.sun.jna.Pointer.NULL)) : WGPUImageSubresourceRange {
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override var baseMipLevel: UInt
            get() = handle.baseMipLevel.toUInt() as UInt
            set(value) { handle.baseMipLevel = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var baseArrayLayer: UInt
            get() = handle.baseArrayLayer.toUInt() as UInt
            set(value) { handle.baseArrayLayer = value.toInt() }
        override var arrayLayerCount: UInt
            get() = handle.arrayLayerCount.toUInt() as UInt
            set(value) { handle.arrayLayerCount = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByValue = io.ygdrasil.wgpu.android.WGPUImageSubresourceRange.ByValue(com.sun.jna.Pointer.NULL)) : WGPUImageSubresourceRange {
        override var aspect: WGPUTextureAspect
            get() = handle.aspect.toUInt() as WGPUTextureAspect
            set(value) { handle.aspect = value.toInt() }
        override var baseMipLevel: UInt
            get() = handle.baseMipLevel.toUInt() as UInt
            set(value) { handle.baseMipLevel = value.toInt() }
        override var mipLevelCount: UInt
            get() = handle.mipLevelCount.toUInt() as UInt
            set(value) { handle.mipLevelCount = value.toInt() }
        override var baseArrayLayer: UInt
            get() = handle.baseArrayLayer.toUInt() as UInt
            set(value) { handle.baseArrayLayer = value.toInt() }
        override var arrayLayerCount: UInt
            get() = handle.arrayLayerCount.toUInt() as UInt
            set(value) { handle.arrayLayerCount = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual interface WGPUSamplerDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var samplerBorderColor: WGPUSamplerBorderColor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptorExtras {
            return ByReference(io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByReference(address))
        }
        
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptorExtras {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByReference()
            allocator.register(ref)
            return ByReference(ref)
        }
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptorExtras) -> Unit): ArrayHolder<WGPUSamplerDescriptorExtras> {
            val ref = io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByValue()
            val array = ref.toArray(size.toInt())
            array.forEachIndexed { index, struct ->
                provider(index.toUInt(), ByValue(struct as io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByValue))
            }
            val pointer = if (size == 0u) com.sun.jna.Pointer.NULL else array.first().pointer
            return ArrayHolder(pointer)
        }
    }
    
    class ByReference(val handle: io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByReference = io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByReference(com.sun.jna.Pointer.NULL)) : WGPUSamplerDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = handle.samplerBorderColor.toUInt() as WGPUSamplerBorderColor
            set(value) { handle.samplerBorderColor = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
    
    class ByValue(val handle: io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByValue = io.ygdrasil.wgpu.android.WGPUSamplerDescriptorExtras.ByValue(com.sun.jna.Pointer.NULL)) : WGPUSamplerDescriptorExtras {
        override var chain: WGPUChainedStruct
            get() {
                handle.readField("chain")
                return WGPUChainedStruct.ByValue(handle.chain)
            }
            set(value) {
                val bytes = value.handler.getByteArray(0, handle.chain.size())
                handle.readField("chain")
                handle.chain.pointer.write(0, bytes, 0, bytes.size)
                handle.readField("chain")
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = handle.samplerBorderColor.toUInt() as WGPUSamplerBorderColor
            set(value) { handle.samplerBorderColor = value.toInt() }
        override val handler: NativeAddress
            get() {
                handle.write()
                return handle.pointer
            }
    }
}

actual fun wgpuGenerateReport(instance: WGPUInstance?, report: WGPUGlobalReport?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuGenerateReport(instance?.handler, report?.handler)
    return
}

actual fun wgpuInstanceEnumerateAdapters(instance: WGPUInstance?, options: WGPUInstanceEnumerateAdapterOptions?, adapters: NativeAddress?): ULong {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuInstanceEnumerateAdapters(instance?.handler, options?.handler, adapters).toULong()
}

actual fun wgpuQueueSubmitForIndex(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): ULong {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueSubmitForIndex(queue?.handler, commandCount.toLong(), commands).toULong()
}

actual fun wgpuQueueGetTimestampPeriod(queue: WGPUQueue?): Float {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueGetTimestampPeriod(queue?.handler)
}

actual fun wgpuDevicePoll(device: WGPUDevice?, wait: UInt, submissionIndex: NativeAddress?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDevicePoll(device?.handler, wait.toInt(), submissionIndex).toUInt()
}

actual fun wgpuDeviceCreateShaderModuleSpirV(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptorSpirV?): WGPUShaderModule? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateShaderModuleSpirV(device?.handler, descriptor?.handler)?.let { WGPUShaderModule(it) }
}

actual fun wgpuSetLogCallback(callback: NativeAddress?, userdata: NativeAddress?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSetLogCallback(callback, userdata)
    return
}

actual fun wgpuSetLogLevel(level: WGPULogLevel): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSetLogLevel(level.toInt())
    return
}

actual fun wgpuGetVersion(): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuGetVersion().toUInt()
}

actual fun wgpuDeviceGetNativeMetalDevice(device: WGPUDevice?): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceGetNativeMetalDevice(device?.handler)
}

actual fun wgpuQueueGetNativeMetalCommandQueue(queue: WGPUQueue?): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuQueueGetNativeMetalCommandQueue(queue?.handler)
}

actual fun wgpuTextureGetNativeMetalTexture(texture: WGPUTexture?): NativeAddress? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuTextureGetNativeMetalTexture(texture?.handler)
}

actual fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderMultiDrawIndirect(encoder?.handler, buffer?.handler, offset.toLong(), count.toInt())
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder?.handler, buffer?.handler, offset.toLong(), count.toInt())
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderMultiDrawIndirectCount(encoder?.handler, buffer?.handler, offset.toLong(), count_buffer?.handler, count_buffer_offset.toLong(), max_count.toInt())
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder?.handler, buffer?.handler, offset.toLong(), count_buffer?.handler, count_buffer_offset.toLong(), max_count.toInt())
    return
}

actual fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder?.handler, querySet?.handler, queryIndex.toInt())
    return
}

actual fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder?.handler)
    return
}

actual fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder?.handler, querySet?.handler, queryIndex.toInt())
    return
}

actual fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder?.handler)
    return
}

actual fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuComputePassEncoderWriteTimestamp(computePassEncoder?.handler, querySet?.handler, queryIndex.toInt())
    return
}

actual fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder?.handler, querySet?.handler, queryIndex.toInt())
    return
}

actual fun wgpuDeviceStartGraphicsDebuggerCapture(device: WGPUDevice?): UInt {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceStartGraphicsDebuggerCapture(device?.handler).toUInt()
}

actual fun wgpuDeviceStopGraphicsDebuggerCapture(device: WGPUDevice?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceStopGraphicsDebuggerCapture(device?.handler)
    return
}

actual fun wgpuCommandEncoderClearTexture(commandEncoder: WGPUCommandEncoder?, texture: WGPUTexture?, range: WGPUImageSubresourceRange?): Unit {
    io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuCommandEncoderClearTexture(commandEncoder?.handler, texture?.handler, range?.handler)
    return
}

actual fun wgpuDeviceCreateShaderModuleTrusted(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?, runtimeChecks: ULong): WGPUShaderModule? {
    return io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuDeviceCreateShaderModuleTrusted(device?.handler, descriptor?.handler, runtimeChecks.toLong())?.let { WGPUShaderModule(it) }
}

private fun interface WGPUProcJna : com.sun.jna.Callback {
    fun invoke(
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUProcTrampoline {
    private val callback: WGPUProcJna = WGPUProcJna {
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
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUBufferMapCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUBufferMapCallbackTrampoline {
    private val callback: WGPUBufferMapCallbackJna = WGPUBufferMapCallbackJna { status, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUBufferMapCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUMapAsyncStatus,
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUCompilationInfoCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        compilationInfo: Pointer?,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUCompilationInfoCallbackTrampoline {
    private val callback: WGPUCompilationInfoCallbackJna = WGPUCompilationInfoCallbackJna { status, compilationInfo, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCompilationInfoCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCompilationInfoRequestStatus,
                    compilationInfo,
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUCreateComputePipelineAsyncCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        pipeline: Pointer?,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUCreateComputePipelineAsyncCallbackTrampoline {
    private val callback: WGPUCreateComputePipelineAsyncCallbackJna = WGPUCreateComputePipelineAsyncCallbackJna { status, pipeline, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCreateComputePipelineAsyncCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline?.let { WGPUComputePipeline(it) },
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUCreateRenderPipelineAsyncCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        pipeline: Pointer?,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUCreateRenderPipelineAsyncCallbackTrampoline {
    private val callback: WGPUCreateRenderPipelineAsyncCallbackJna = WGPUCreateRenderPipelineAsyncCallbackJna { status, pipeline, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUCreateRenderPipelineAsyncCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUCreatePipelineAsyncStatus,
                    pipeline?.let { WGPURenderPipeline(it) },
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUDeviceLostCallbackJna : com.sun.jna.Callback {
    fun invoke(
        device: Pointer?,
        reason: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUDeviceLostCallbackTrampoline {
    private val callback: WGPUDeviceLostCallbackJna = WGPUDeviceLostCallbackJna { device, reason, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUDeviceLostCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    device,
                    reason.toUInt() as WGPUDeviceLostReason,
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUPopErrorScopeCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        type: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUPopErrorScopeCallbackTrampoline {
    private val callback: WGPUPopErrorScopeCallbackJna = WGPUPopErrorScopeCallbackJna { status, type, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUPopErrorScopeCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUPopErrorScopeStatus,
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUQueueWorkDoneCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUQueueWorkDoneCallbackTrampoline {
    private val callback: WGPUQueueWorkDoneCallbackJna = WGPUQueueWorkDoneCallbackJna { status, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUQueueWorkDoneCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPUQueueWorkDoneStatus,
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPURequestAdapterCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        adapter: Pointer?,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPURequestAdapterCallbackTrampoline {
    private val callback: WGPURequestAdapterCallbackJna = WGPURequestAdapterCallbackJna { status, adapter, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPURequestAdapterCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestAdapterStatus,
                    adapter?.let { WGPUAdapter(it) },
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPURequestDeviceCallbackJna : com.sun.jna.Callback {
    fun invoke(
        status: Int,
        device: Pointer?,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPURequestDeviceCallbackTrampoline {
    private val callback: WGPURequestDeviceCallbackJna = WGPURequestDeviceCallbackJna { status, device, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPURequestDeviceCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    status.toUInt() as WGPURequestDeviceStatus,
                    device?.let { WGPUDevice(it) },
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPUUncapturedErrorCallbackJna : com.sun.jna.Callback {
    fun invoke(
        device: Pointer?,
        type: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata1: Pointer?,
        userdata2: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPUUncapturedErrorCallbackTrampoline {
    private val callback: WGPUUncapturedErrorCallbackJna = WGPUUncapturedErrorCallbackJna { device, type, message, userdata1, userdata2 ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPUUncapturedErrorCallbackType,
                userdata = userdata2,
            ) { callback ->
                callback.invoke(
                    device,
                    type.toUInt() as WGPUErrorType,
                    WGPUStringView.ByValue(message),
                    userdata1,
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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

private fun interface WGPULogCallbackJna : com.sun.jna.Callback {
    fun invoke(
        level: Int,
        message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue,
        userdata: Pointer?,
    )
}

@OptIn(CallbackRuntimeApi::class)
private object WGPULogCallbackTrampoline {
    private val callback: WGPULogCallbackJna = WGPULogCallbackJna { level, message, userdata ->
        try {
            CallbackRuntime.dispatchSafely(
                type = WGPULogCallbackType,
                userdata = userdata,
            ) { callback ->
                callback.invoke(
                    level.toUInt() as WGPULogLevel,
                    WGPUStringView.ByValue(message),
                )
            }
        } catch (failure: Throwable) {
            CallbackRuntime.reportUnroutedFailure(failure)
        }
    }
    val address: NativeAddress by lazy {
        CallbackReference.getFunctionPointer(callback)
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
        io.ygdrasil.wgpu.android.wgpu_hLibraryInstance.wgpuSetLogCallback(
            callback,
            userdata,
        )
    }
}
