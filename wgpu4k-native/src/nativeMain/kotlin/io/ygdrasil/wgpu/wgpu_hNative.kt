@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

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
import io.ygdrasil.kffi.toCString
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.CValue
import kotlinx.cinterop.DoubleVar
import kotlinx.cinterop.FloatVar
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.LongVar
import kotlinx.cinterop.ShortVar
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.cValue
import kotlinx.cinterop.get
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.useContents
import kotlin.OptIn

actual interface WGPUStringView {
    actual var data: CString?
    actual var length: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStringView = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStringView =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUStringView>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStringView) -> Unit): ArrayHolder<WGPUStringView> {
            val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUStringView>) : WGPUStringView {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var data: CString?
            get() = handle.useContents { this.data?.let { CString(NativeAddress(it)) } }
            set(value) { error("Setters not supported on ByValue") }
        override var length: ULong
            get() = handle.useContents { this.length }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUStringView {
        private val struct: webgpu.native.WGPUStringView
            get() = handler.pointer.reinterpret<webgpu.native.WGPUStringView>().pointed
        
        override var data: CString?
            get() = struct.data?.let { CString(NativeAddress(it)) }
            set(value) { struct.data = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var length: ULong
            get() = struct.length
            set(value) { struct.length = value }
    }
}

fun WGPUStringView.toCValue(): CValue<webgpu.native.WGPUStringView> = cValue {
    this.data = this@toCValue.data?.handler?.pointer?.takeIf { this@toCValue.data?.handler?.rawValue != 0L }?.reinterpret()
    this.length = this@toCValue.length
}

actual value class WGPUAdapter actual constructor(actual val handler: NativeAddress)

actual value class WGPUBindGroup actual constructor(actual val handler: NativeAddress)

actual value class WGPUBindGroupLayout actual constructor(actual val handler: NativeAddress)

actual value class WGPUBuffer actual constructor(actual val handler: NativeAddress)

actual value class WGPUCommandBuffer actual constructor(actual val handler: NativeAddress)

actual value class WGPUCommandEncoder actual constructor(actual val handler: NativeAddress)

actual value class WGPUComputePassEncoder actual constructor(actual val handler: NativeAddress)

actual value class WGPUComputePipeline actual constructor(actual val handler: NativeAddress)

actual value class WGPUDevice actual constructor(actual val handler: NativeAddress)

actual value class WGPUExternalTexture actual constructor(actual val handler: NativeAddress)

actual value class WGPUInstance actual constructor(actual val handler: NativeAddress)

actual value class WGPUPipelineLayout actual constructor(actual val handler: NativeAddress)

actual value class WGPUQuerySet actual constructor(actual val handler: NativeAddress)

actual value class WGPUQueue actual constructor(actual val handler: NativeAddress)

actual value class WGPURenderBundle actual constructor(actual val handler: NativeAddress)

actual value class WGPURenderBundleEncoder actual constructor(actual val handler: NativeAddress)

actual value class WGPURenderPassEncoder actual constructor(actual val handler: NativeAddress)

actual value class WGPURenderPipeline actual constructor(actual val handler: NativeAddress)

actual value class WGPUSampler actual constructor(actual val handler: NativeAddress)

actual value class WGPUShaderModule actual constructor(actual val handler: NativeAddress)

actual value class WGPUSurface actual constructor(actual val handler: NativeAddress)

actual value class WGPUTexture actual constructor(actual val handler: NativeAddress)

actual value class WGPUTextureView actual constructor(actual val handler: NativeAddress)

actual interface WGPUChainedStruct {
    actual var next: WGPUChainedStruct?
    actual var sType: WGPUSType
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUChainedStruct = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUChainedStruct =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUChainedStruct>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUChainedStruct) -> Unit): ArrayHolder<WGPUChainedStruct> {
            val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUChainedStruct>) : WGPUChainedStruct {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var next: WGPUChainedStruct?
            get() = handle.useContents { this.next?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var sType: WGPUSType
            get() = handle.useContents { this.sType as WGPUSType }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUChainedStruct {
        private val struct: webgpu.native.WGPUChainedStruct
            get() = handler.pointer.reinterpret<webgpu.native.WGPUChainedStruct>().pointed
        
        override var next: WGPUChainedStruct?
            get() = struct.next?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.next = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var sType: WGPUSType
            get() = struct.sType as WGPUSType
            set(value) { struct.sType = value }
    }
}

fun WGPUChainedStruct.toCValue(): CValue<webgpu.native.WGPUChainedStruct> = cValue {
    this.next = this@toCValue.next?.handler?.pointer?.takeIf { this@toCValue.next?.handler?.rawValue != 0L }?.reinterpret()
    this.sType = this@toCValue.sType
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferMapCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBufferMapCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferMapCallbackInfo) -> Unit): ArrayHolder<WGPUBufferMapCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUBufferMapCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBufferMapCallbackInfo>) : WGPUBufferMapCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBufferMapCallbackInfo {
        private val struct: webgpu.native.WGPUBufferMapCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBufferMapCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUBufferMapCallbackInfo.toCValue(): CValue<webgpu.native.WGPUBufferMapCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfoCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCompilationInfoCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfoCallbackInfo) -> Unit): ArrayHolder<WGPUCompilationInfoCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUCompilationInfoCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCompilationInfoCallbackInfo>) : WGPUCompilationInfoCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCompilationInfoCallbackInfo {
        private val struct: webgpu.native.WGPUCompilationInfoCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCompilationInfoCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUCompilationInfoCallbackInfo.toCValue(): CValue<webgpu.native.WGPUCompilationInfoCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateComputePipelineAsyncCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateComputePipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo>) : WGPUCreateComputePipelineAsyncCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCreateComputePipelineAsyncCallbackInfo {
        private val struct: webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUCreateComputePipelineAsyncCallbackInfo.toCValue(): CValue<webgpu.native.WGPUCreateComputePipelineAsyncCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUCreateRenderPipelineAsyncCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateRenderPipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo>) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCreateRenderPipelineAsyncCallbackInfo {
        private val struct: webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUCreateRenderPipelineAsyncCallbackInfo.toCValue(): CValue<webgpu.native.WGPUCreateRenderPipelineAsyncCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceLostCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUDeviceLostCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceLostCallbackInfo) -> Unit): ArrayHolder<WGPUDeviceLostCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUDeviceLostCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUDeviceLostCallbackInfo>) : WGPUDeviceLostCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUDeviceLostCallbackInfo {
        private val struct: webgpu.native.WGPUDeviceLostCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUDeviceLostCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUDeviceLostCallbackInfo.toCValue(): CValue<webgpu.native.WGPUDeviceLostCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUPopErrorScopeCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUPopErrorScopeCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPopErrorScopeCallbackInfo) -> Unit): ArrayHolder<WGPUPopErrorScopeCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUPopErrorScopeCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUPopErrorScopeCallbackInfo>) : WGPUPopErrorScopeCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUPopErrorScopeCallbackInfo {
        private val struct: webgpu.native.WGPUPopErrorScopeCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUPopErrorScopeCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUPopErrorScopeCallbackInfo.toCValue(): CValue<webgpu.native.WGPUPopErrorScopeCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueWorkDoneCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUQueueWorkDoneCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueWorkDoneCallbackInfo) -> Unit): ArrayHolder<WGPUQueueWorkDoneCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUQueueWorkDoneCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUQueueWorkDoneCallbackInfo>) : WGPUQueueWorkDoneCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUQueueWorkDoneCallbackInfo {
        private val struct: webgpu.native.WGPUQueueWorkDoneCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUQueueWorkDoneCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUQueueWorkDoneCallbackInfo.toCValue(): CValue<webgpu.native.WGPUQueueWorkDoneCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURequestAdapterCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterCallbackInfo) -> Unit): ArrayHolder<WGPURequestAdapterCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPURequestAdapterCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURequestAdapterCallbackInfo>) : WGPURequestAdapterCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURequestAdapterCallbackInfo {
        private val struct: webgpu.native.WGPURequestAdapterCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPURequestAdapterCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPURequestAdapterCallbackInfo.toCValue(): CValue<webgpu.native.WGPURequestAdapterCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPURequestDeviceCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURequestDeviceCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestDeviceCallbackInfo) -> Unit): ArrayHolder<WGPURequestDeviceCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPURequestDeviceCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURequestDeviceCallbackInfo>) : WGPURequestDeviceCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mode: WGPUCallbackMode
            get() = handle.useContents { this.mode as WGPUCallbackMode }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURequestDeviceCallbackInfo {
        private val struct: webgpu.native.WGPURequestDeviceCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPURequestDeviceCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mode: WGPUCallbackMode
            get() = struct.mode as WGPUCallbackMode
            set(value) { struct.mode = value }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPURequestDeviceCallbackInfo.toCValue(): CValue<webgpu.native.WGPURequestDeviceCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.mode = this@toCValue.mode
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUUncapturedErrorCallbackInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var callback: NativeAddress?
    actual var userdata1: NativeAddress?
    actual var userdata2: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUUncapturedErrorCallbackInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUUncapturedErrorCallbackInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUUncapturedErrorCallbackInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUUncapturedErrorCallbackInfo) -> Unit): ArrayHolder<WGPUUncapturedErrorCallbackInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUUncapturedErrorCallbackInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUUncapturedErrorCallbackInfo>) : WGPUUncapturedErrorCallbackInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var callback: NativeAddress?
            get() = handle.useContents { this.callback?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata1: NativeAddress?
            get() = handle.useContents { this.userdata1?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var userdata2: NativeAddress?
            get() = handle.useContents { this.userdata2?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUUncapturedErrorCallbackInfo {
        private val struct: webgpu.native.WGPUUncapturedErrorCallbackInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUUncapturedErrorCallbackInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var callback: NativeAddress?
            get() = struct.callback?.let(::NativeAddress)
            set(value) { struct.callback = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata1: NativeAddress?
            get() = struct.userdata1?.let(::NativeAddress)
            set(value) { struct.userdata1 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var userdata2: NativeAddress?
            get() = struct.userdata2?.let(::NativeAddress)
            set(value) { struct.userdata2 = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUUncapturedErrorCallbackInfo.toCValue(): CValue<webgpu.native.WGPUUncapturedErrorCallbackInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.callback = this@toCValue.callback?.pointer?.takeIf { this@toCValue.callback?.rawValue != 0L }?.reinterpret()
    this.userdata1 = this@toCValue.userdata1?.pointer?.takeIf { this@toCValue.userdata1?.rawValue != 0L }?.reinterpret()
    this.userdata2 = this@toCValue.userdata2?.pointer?.takeIf { this@toCValue.userdata2?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUAdapterInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUAdapterInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUAdapterInfo) -> Unit): ArrayHolder<WGPUAdapterInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUAdapterInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUAdapterInfo>) : WGPUAdapterInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var vendor: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.vendor.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var architecture: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.architecture.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var device: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.device.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var description: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.description.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var backendType: WGPUBackendType
            get() = handle.useContents { this.backendType as WGPUBackendType }
            set(value) { error("Setters not supported on ByValue") }
        override var adapterType: WGPUAdapterType
            get() = handle.useContents { this.adapterType as WGPUAdapterType }
            set(value) { error("Setters not supported on ByValue") }
        override var vendorID: UInt
            get() = handle.useContents { this.vendorID }
            set(value) { error("Setters not supported on ByValue") }
        override var deviceID: UInt
            get() = handle.useContents { this.deviceID }
            set(value) { error("Setters not supported on ByValue") }
        override var subgroupMinSize: UInt
            get() = handle.useContents { this.subgroupMinSize }
            set(value) { error("Setters not supported on ByValue") }
        override var subgroupMaxSize: UInt
            get() = handle.useContents { this.subgroupMaxSize }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUAdapterInfo {
        private val struct: webgpu.native.WGPUAdapterInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUAdapterInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var vendor: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.vendor.ptr))
            set(value) {
                val destBytes = struct.vendor.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var architecture: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.architecture.ptr))
            set(value) {
                val destBytes = struct.architecture.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var device: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.device.ptr))
            set(value) {
                val destBytes = struct.device.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var description: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.description.ptr))
            set(value) {
                val destBytes = struct.description.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var backendType: WGPUBackendType
            get() = struct.backendType as WGPUBackendType
            set(value) { struct.backendType = value }
        override var adapterType: WGPUAdapterType
            get() = struct.adapterType as WGPUAdapterType
            set(value) { struct.adapterType = value }
        override var vendorID: UInt
            get() = struct.vendorID
            set(value) { struct.vendorID = value }
        override var deviceID: UInt
            get() = struct.deviceID
            set(value) { struct.deviceID = value }
        override var subgroupMinSize: UInt
            get() = struct.subgroupMinSize
            set(value) { struct.subgroupMinSize = value }
        override var subgroupMaxSize: UInt
            get() = struct.subgroupMaxSize
            set(value) { struct.subgroupMaxSize = value }
    }
}

fun WGPUAdapterInfo.toCValue(): CValue<webgpu.native.WGPUAdapterInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_vendor = this.vendor.ptr.reinterpret<ByteVar>()
    val src_vendor = this@toCValue.vendor.handler.pointer.reinterpret<ByteVar>()
    val size_vendor = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_vendor) {
        dest_vendor[i.toInt()] = src_vendor[i.toInt()]
    }
    val dest_architecture = this.architecture.ptr.reinterpret<ByteVar>()
    val src_architecture = this@toCValue.architecture.handler.pointer.reinterpret<ByteVar>()
    val size_architecture = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_architecture) {
        dest_architecture[i.toInt()] = src_architecture[i.toInt()]
    }
    val dest_device = this.device.ptr.reinterpret<ByteVar>()
    val src_device = this@toCValue.device.handler.pointer.reinterpret<ByteVar>()
    val size_device = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_device) {
        dest_device[i.toInt()] = src_device[i.toInt()]
    }
    val dest_description = this.description.ptr.reinterpret<ByteVar>()
    val src_description = this@toCValue.description.handler.pointer.reinterpret<ByteVar>()
    val size_description = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_description) {
        dest_description[i.toInt()] = src_description[i.toInt()]
    }
    this.backendType = this@toCValue.backendType
    this.adapterType = this@toCValue.adapterType
    this.vendorID = this@toCValue.vendorID
    this.deviceID = this@toCValue.deviceID
    this.subgroupMinSize = this@toCValue.subgroupMinSize
    this.subgroupMaxSize = this@toCValue.subgroupMaxSize
}

actual interface WGPUBlendComponent {
    actual var operation: WGPUBlendOperation
    actual var srcFactor: WGPUBlendFactor
    actual var dstFactor: WGPUBlendFactor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendComponent = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendComponent =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBlendComponent>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendComponent) -> Unit): ArrayHolder<WGPUBlendComponent> {
            val byteSize = sizeOf<webgpu.native.WGPUBlendComponent>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBlendComponent>) : WGPUBlendComponent {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var operation: WGPUBlendOperation
            get() = handle.useContents { this.operation as WGPUBlendOperation }
            set(value) { error("Setters not supported on ByValue") }
        override var srcFactor: WGPUBlendFactor
            get() = handle.useContents { this.srcFactor as WGPUBlendFactor }
            set(value) { error("Setters not supported on ByValue") }
        override var dstFactor: WGPUBlendFactor
            get() = handle.useContents { this.dstFactor as WGPUBlendFactor }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBlendComponent {
        private val struct: webgpu.native.WGPUBlendComponent
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBlendComponent>().pointed
        
        override var operation: WGPUBlendOperation
            get() = struct.operation as WGPUBlendOperation
            set(value) { struct.operation = value }
        override var srcFactor: WGPUBlendFactor
            get() = struct.srcFactor as WGPUBlendFactor
            set(value) { struct.srcFactor = value }
        override var dstFactor: WGPUBlendFactor
            get() = struct.dstFactor as WGPUBlendFactor
            set(value) { struct.dstFactor = value }
    }
}

fun WGPUBlendComponent.toCValue(): CValue<webgpu.native.WGPUBlendComponent> = cValue {
    this.operation = this@toCValue.operation
    this.srcFactor = this@toCValue.srcFactor
    this.dstFactor = this@toCValue.dstFactor
}

actual interface WGPUBufferBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUBufferBindingType
    actual var hasDynamicOffset: UInt
    actual var minBindingSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBufferBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferBindingLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBufferBindingLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferBindingLayout) -> Unit): ArrayHolder<WGPUBufferBindingLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUBufferBindingLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBufferBindingLayout>) : WGPUBufferBindingLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var type: WGPUBufferBindingType
            get() = handle.useContents { this.type as WGPUBufferBindingType }
            set(value) { error("Setters not supported on ByValue") }
        override var hasDynamicOffset: UInt
            get() = handle.useContents { this.hasDynamicOffset }
            set(value) { error("Setters not supported on ByValue") }
        override var minBindingSize: ULong
            get() = handle.useContents { this.minBindingSize }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBufferBindingLayout {
        private val struct: webgpu.native.WGPUBufferBindingLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBufferBindingLayout>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var type: WGPUBufferBindingType
            get() = struct.type as WGPUBufferBindingType
            set(value) { struct.type = value }
        override var hasDynamicOffset: UInt
            get() = struct.hasDynamicOffset
            set(value) { struct.hasDynamicOffset = value }
        override var minBindingSize: ULong
            get() = struct.minBindingSize
            set(value) { struct.minBindingSize = value }
    }
}

fun WGPUBufferBindingLayout.toCValue(): CValue<webgpu.native.WGPUBufferBindingLayout> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.type = this@toCValue.type
    this.hasDynamicOffset = this@toCValue.hasDynamicOffset
    this.minBindingSize = this@toCValue.minBindingSize
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBufferDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBufferDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferDescriptor) -> Unit): ArrayHolder<WGPUBufferDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUBufferDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBufferDescriptor>) : WGPUBufferDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var usage: ULong
            get() = handle.useContents { this.usage }
            set(value) { error("Setters not supported on ByValue") }
        override var size: ULong
            get() = handle.useContents { this.size }
            set(value) { error("Setters not supported on ByValue") }
        override var mappedAtCreation: UInt
            get() = handle.useContents { this.mappedAtCreation }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBufferDescriptor {
        private val struct: webgpu.native.WGPUBufferDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBufferDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var usage: ULong
            get() = struct.usage
            set(value) { struct.usage = value }
        override var size: ULong
            get() = struct.size
            set(value) { struct.size = value }
        override var mappedAtCreation: UInt
            get() = struct.mappedAtCreation
            set(value) { struct.mappedAtCreation = value }
    }
}

fun WGPUBufferDescriptor.toCValue(): CValue<webgpu.native.WGPUBufferDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.usage = this@toCValue.usage
    this.size = this@toCValue.size
    this.mappedAtCreation = this@toCValue.mappedAtCreation
}

actual interface WGPUColor {
    actual var r: Double
    actual var g: Double
    actual var b: Double
    actual var a: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUColor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColor) -> Unit): ArrayHolder<WGPUColor> {
            val byteSize = sizeOf<webgpu.native.WGPUColor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUColor>) : WGPUColor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var r: Double
            get() = handle.useContents { this.r }
            set(value) { error("Setters not supported on ByValue") }
        override var g: Double
            get() = handle.useContents { this.g }
            set(value) { error("Setters not supported on ByValue") }
        override var b: Double
            get() = handle.useContents { this.b }
            set(value) { error("Setters not supported on ByValue") }
        override var a: Double
            get() = handle.useContents { this.a }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUColor {
        private val struct: webgpu.native.WGPUColor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUColor>().pointed
        
        override var r: Double
            get() = struct.r
            set(value) { struct.r = value }
        override var g: Double
            get() = struct.g
            set(value) { struct.g = value }
        override var b: Double
            get() = struct.b
            set(value) { struct.b = value }
        override var a: Double
            get() = struct.a
            set(value) { struct.a = value }
    }
}

fun WGPUColor.toCValue(): CValue<webgpu.native.WGPUColor> = cValue {
    this.r = this@toCValue.r
    this.g = this@toCValue.g
    this.b = this@toCValue.b
    this.a = this@toCValue.a
}

actual interface WGPUCommandBufferDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandBufferDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandBufferDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCommandBufferDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandBufferDescriptor) -> Unit): ArrayHolder<WGPUCommandBufferDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUCommandBufferDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCommandBufferDescriptor>) : WGPUCommandBufferDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCommandBufferDescriptor {
        private val struct: webgpu.native.WGPUCommandBufferDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCommandBufferDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUCommandBufferDescriptor.toCValue(): CValue<webgpu.native.WGPUCommandBufferDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
}

actual interface WGPUCommandEncoderDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCommandEncoderDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCommandEncoderDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCommandEncoderDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandEncoderDescriptor) -> Unit): ArrayHolder<WGPUCommandEncoderDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUCommandEncoderDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCommandEncoderDescriptor>) : WGPUCommandEncoderDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCommandEncoderDescriptor {
        private val struct: webgpu.native.WGPUCommandEncoderDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCommandEncoderDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUCommandEncoderDescriptor.toCValue(): CValue<webgpu.native.WGPUCommandEncoderDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUCompatibilityModeLimits =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCompatibilityModeLimits>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompatibilityModeLimits) -> Unit): ArrayHolder<WGPUCompatibilityModeLimits> {
            val byteSize = sizeOf<webgpu.native.WGPUCompatibilityModeLimits>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCompatibilityModeLimits>) : WGPUCompatibilityModeLimits {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageBuffersInVertexStage: UInt
            get() = handle.useContents { this.maxStorageBuffersInVertexStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageTexturesInVertexStage: UInt
            get() = handle.useContents { this.maxStorageTexturesInVertexStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = handle.useContents { this.maxStorageBuffersInFragmentStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = handle.useContents { this.maxStorageTexturesInFragmentStage }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCompatibilityModeLimits {
        private val struct: webgpu.native.WGPUCompatibilityModeLimits
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCompatibilityModeLimits>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var maxStorageBuffersInVertexStage: UInt
            get() = struct.maxStorageBuffersInVertexStage
            set(value) { struct.maxStorageBuffersInVertexStage = value }
        override var maxStorageTexturesInVertexStage: UInt
            get() = struct.maxStorageTexturesInVertexStage
            set(value) { struct.maxStorageTexturesInVertexStage = value }
        override var maxStorageBuffersInFragmentStage: UInt
            get() = struct.maxStorageBuffersInFragmentStage
            set(value) { struct.maxStorageBuffersInFragmentStage = value }
        override var maxStorageTexturesInFragmentStage: UInt
            get() = struct.maxStorageTexturesInFragmentStage
            set(value) { struct.maxStorageTexturesInFragmentStage = value }
    }
}

fun WGPUCompatibilityModeLimits.toCValue(): CValue<webgpu.native.WGPUCompatibilityModeLimits> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.maxStorageBuffersInVertexStage = this@toCValue.maxStorageBuffersInVertexStage
    this.maxStorageTexturesInVertexStage = this@toCValue.maxStorageTexturesInVertexStage
    this.maxStorageBuffersInFragmentStage = this@toCValue.maxStorageBuffersInFragmentStage
    this.maxStorageTexturesInFragmentStage = this@toCValue.maxStorageTexturesInFragmentStage
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
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationMessage =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCompilationMessage>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationMessage) -> Unit): ArrayHolder<WGPUCompilationMessage> {
            val byteSize = sizeOf<webgpu.native.WGPUCompilationMessage>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCompilationMessage>) : WGPUCompilationMessage {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var message: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.message.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var type: WGPUCompilationMessageType
            get() = handle.useContents { this.type as WGPUCompilationMessageType }
            set(value) { error("Setters not supported on ByValue") }
        override var lineNum: ULong
            get() = handle.useContents { this.lineNum }
            set(value) { error("Setters not supported on ByValue") }
        override var linePos: ULong
            get() = handle.useContents { this.linePos }
            set(value) { error("Setters not supported on ByValue") }
        override var offset: ULong
            get() = handle.useContents { this.offset }
            set(value) { error("Setters not supported on ByValue") }
        override var length: ULong
            get() = handle.useContents { this.length }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCompilationMessage {
        private val struct: webgpu.native.WGPUCompilationMessage
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCompilationMessage>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var message: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.message.ptr))
            set(value) {
                val destBytes = struct.message.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var type: WGPUCompilationMessageType
            get() = struct.type as WGPUCompilationMessageType
            set(value) { struct.type = value }
        override var lineNum: ULong
            get() = struct.lineNum
            set(value) { struct.lineNum = value }
        override var linePos: ULong
            get() = struct.linePos
            set(value) { struct.linePos = value }
        override var offset: ULong
            get() = struct.offset
            set(value) { struct.offset = value }
        override var length: ULong
            get() = struct.length
            set(value) { struct.length = value }
    }
}

fun WGPUCompilationMessage.toCValue(): CValue<webgpu.native.WGPUCompilationMessage> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_message = this.message.ptr.reinterpret<ByteVar>()
    val src_message = this@toCValue.message.handler.pointer.reinterpret<ByteVar>()
    val size_message = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_message) {
        dest_message[i.toInt()] = src_message[i.toInt()]
    }
    this.type = this@toCValue.type
    this.lineNum = this@toCValue.lineNum
    this.linePos = this@toCValue.linePos
    this.offset = this@toCValue.offset
    this.length = this@toCValue.length
}

actual interface WGPUConstantEntry {
    actual var nextInChain: WGPUChainedStruct?
    actual var key: WGPUStringView
    actual var value: Double
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUConstantEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUConstantEntry =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUConstantEntry>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUConstantEntry) -> Unit): ArrayHolder<WGPUConstantEntry> {
            val byteSize = sizeOf<webgpu.native.WGPUConstantEntry>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUConstantEntry>) : WGPUConstantEntry {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var key: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.key.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var value: Double
            get() = handle.useContents { this.value }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUConstantEntry {
        private val struct: webgpu.native.WGPUConstantEntry
            get() = handler.pointer.reinterpret<webgpu.native.WGPUConstantEntry>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var key: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.key.ptr))
            set(value) {
                val destBytes = struct.key.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var value: Double
            get() = struct.value
            set(value) { struct.value = value }
    }
}

fun WGPUConstantEntry.toCValue(): CValue<webgpu.native.WGPUConstantEntry> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_key = this.key.ptr.reinterpret<ByteVar>()
    val src_key = this@toCValue.key.handler.pointer.reinterpret<ByteVar>()
    val size_key = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_key) {
        dest_key[i.toInt()] = src_key[i.toInt()]
    }
    this.value = this@toCValue.value
}

actual interface WGPUExtent3D {
    actual var width: UInt
    actual var height: UInt
    actual var depthOrArrayLayers: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExtent3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExtent3D =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUExtent3D>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExtent3D) -> Unit): ArrayHolder<WGPUExtent3D> {
            val byteSize = sizeOf<webgpu.native.WGPUExtent3D>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUExtent3D>) : WGPUExtent3D {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var width: UInt
            get() = handle.useContents { this.width }
            set(value) { error("Setters not supported on ByValue") }
        override var height: UInt
            get() = handle.useContents { this.height }
            set(value) { error("Setters not supported on ByValue") }
        override var depthOrArrayLayers: UInt
            get() = handle.useContents { this.depthOrArrayLayers }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUExtent3D {
        private val struct: webgpu.native.WGPUExtent3D
            get() = handler.pointer.reinterpret<webgpu.native.WGPUExtent3D>().pointed
        
        override var width: UInt
            get() = struct.width
            set(value) { struct.width = value }
        override var height: UInt
            get() = struct.height
            set(value) { struct.height = value }
        override var depthOrArrayLayers: UInt
            get() = struct.depthOrArrayLayers
            set(value) { struct.depthOrArrayLayers = value }
    }
}

fun WGPUExtent3D.toCValue(): CValue<webgpu.native.WGPUExtent3D> = cValue {
    this.width = this@toCValue.width
    this.height = this@toCValue.height
    this.depthOrArrayLayers = this@toCValue.depthOrArrayLayers
}

actual interface WGPUExternalTextureBindingEntry {
    actual var chain: WGPUChainedStruct
    actual var externalTexture: WGPUExternalTexture?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingEntry = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingEntry =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUExternalTextureBindingEntry>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingEntry) -> Unit): ArrayHolder<WGPUExternalTextureBindingEntry> {
            val byteSize = sizeOf<webgpu.native.WGPUExternalTextureBindingEntry>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUExternalTextureBindingEntry>) : WGPUExternalTextureBindingEntry {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var externalTexture: WGPUExternalTexture?
            get() = handle.useContents { this.externalTexture?.let(::NativeAddress)?.let { WGPUExternalTexture(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUExternalTextureBindingEntry {
        private val struct: webgpu.native.WGPUExternalTextureBindingEntry
            get() = handler.pointer.reinterpret<webgpu.native.WGPUExternalTextureBindingEntry>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var externalTexture: WGPUExternalTexture?
            get() = struct.externalTexture?.let(::NativeAddress)?.let { WGPUExternalTexture(it) }
            set(value) { struct.externalTexture = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUExternalTextureBindingEntry.toCValue(): CValue<webgpu.native.WGPUExternalTextureBindingEntry> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.externalTexture = this@toCValue.externalTexture?.handler?.pointer?.takeIf { this@toCValue.externalTexture?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUExternalTextureBindingLayout {
    actual var chain: WGPUChainedStruct
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUExternalTextureBindingLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingLayout) -> Unit): ArrayHolder<WGPUExternalTextureBindingLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUExternalTextureBindingLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUExternalTextureBindingLayout>) : WGPUExternalTextureBindingLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUExternalTextureBindingLayout {
        private val struct: webgpu.native.WGPUExternalTextureBindingLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUExternalTextureBindingLayout>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUExternalTextureBindingLayout.toCValue(): CValue<webgpu.native.WGPUExternalTextureBindingLayout> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
}

actual interface WGPUFuture {
    actual var id: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFuture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFuture =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUFuture>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFuture) -> Unit): ArrayHolder<WGPUFuture> {
            val byteSize = sizeOf<webgpu.native.WGPUFuture>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUFuture>) : WGPUFuture {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var id: ULong
            get() = handle.useContents { this.id }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUFuture {
        private val struct: webgpu.native.WGPUFuture
            get() = handler.pointer.reinterpret<webgpu.native.WGPUFuture>().pointed
        
        override var id: ULong
            get() = struct.id
            set(value) { struct.id = value }
    }
}

fun WGPUFuture.toCValue(): CValue<webgpu.native.WGPUFuture> = cValue {
    this.id = this@toCValue.id
}

actual interface WGPUInstanceLimits {
    actual var nextInChain: WGPUChainedStruct?
    actual var timedWaitAnyMaxCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceLimits = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceLimits =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUInstanceLimits>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceLimits) -> Unit): ArrayHolder<WGPUInstanceLimits> {
            val byteSize = sizeOf<webgpu.native.WGPUInstanceLimits>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUInstanceLimits>) : WGPUInstanceLimits {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var timedWaitAnyMaxCount: ULong
            get() = handle.useContents { this.timedWaitAnyMaxCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUInstanceLimits {
        private val struct: webgpu.native.WGPUInstanceLimits
            get() = handler.pointer.reinterpret<webgpu.native.WGPUInstanceLimits>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var timedWaitAnyMaxCount: ULong
            get() = struct.timedWaitAnyMaxCount
            set(value) { struct.timedWaitAnyMaxCount = value }
    }
}

fun WGPUInstanceLimits.toCValue(): CValue<webgpu.native.WGPUInstanceLimits> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.timedWaitAnyMaxCount = this@toCValue.timedWaitAnyMaxCount
}

actual interface WGPUMultisampleState {
    actual var nextInChain: WGPUChainedStruct?
    actual var count: UInt
    actual var mask: UInt
    actual var alphaToCoverageEnabled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUMultisampleState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUMultisampleState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUMultisampleState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUMultisampleState) -> Unit): ArrayHolder<WGPUMultisampleState> {
            val byteSize = sizeOf<webgpu.native.WGPUMultisampleState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUMultisampleState>) : WGPUMultisampleState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var count: UInt
            get() = handle.useContents { this.count }
            set(value) { error("Setters not supported on ByValue") }
        override var mask: UInt
            get() = handle.useContents { this.mask }
            set(value) { error("Setters not supported on ByValue") }
        override var alphaToCoverageEnabled: UInt
            get() = handle.useContents { this.alphaToCoverageEnabled }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUMultisampleState {
        private val struct: webgpu.native.WGPUMultisampleState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUMultisampleState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var count: UInt
            get() = struct.count
            set(value) { struct.count = value }
        override var mask: UInt
            get() = struct.mask
            set(value) { struct.mask = value }
        override var alphaToCoverageEnabled: UInt
            get() = struct.alphaToCoverageEnabled
            set(value) { struct.alphaToCoverageEnabled = value }
    }
}

fun WGPUMultisampleState.toCValue(): CValue<webgpu.native.WGPUMultisampleState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.count = this@toCValue.count
    this.mask = this@toCValue.mask
    this.alphaToCoverageEnabled = this@toCValue.alphaToCoverageEnabled
}

actual interface WGPUOrigin3D {
    actual var x: UInt
    actual var y: UInt
    actual var z: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUOrigin3D = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUOrigin3D =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUOrigin3D>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUOrigin3D) -> Unit): ArrayHolder<WGPUOrigin3D> {
            val byteSize = sizeOf<webgpu.native.WGPUOrigin3D>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUOrigin3D>) : WGPUOrigin3D {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var x: UInt
            get() = handle.useContents { this.x }
            set(value) { error("Setters not supported on ByValue") }
        override var y: UInt
            get() = handle.useContents { this.y }
            set(value) { error("Setters not supported on ByValue") }
        override var z: UInt
            get() = handle.useContents { this.z }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUOrigin3D {
        private val struct: webgpu.native.WGPUOrigin3D
            get() = handler.pointer.reinterpret<webgpu.native.WGPUOrigin3D>().pointed
        
        override var x: UInt
            get() = struct.x
            set(value) { struct.x = value }
        override var y: UInt
            get() = struct.y
            set(value) { struct.y = value }
        override var z: UInt
            get() = struct.z
            set(value) { struct.z = value }
    }
}

fun WGPUOrigin3D.toCValue(): CValue<webgpu.native.WGPUOrigin3D> = cValue {
    this.x = this@toCValue.x
    this.y = this@toCValue.y
    this.z = this@toCValue.z
}

actual interface WGPUPassTimestampWrites {
    actual var nextInChain: WGPUChainedStruct?
    actual var querySet: WGPUQuerySet?
    actual var beginningOfPassWriteIndex: UInt
    actual var endOfPassWriteIndex: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPassTimestampWrites = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPassTimestampWrites =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUPassTimestampWrites>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPassTimestampWrites) -> Unit): ArrayHolder<WGPUPassTimestampWrites> {
            val byteSize = sizeOf<webgpu.native.WGPUPassTimestampWrites>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUPassTimestampWrites>) : WGPUPassTimestampWrites {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var querySet: WGPUQuerySet?
            get() = handle.useContents { this.querySet?.let(::NativeAddress)?.let { WGPUQuerySet(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var beginningOfPassWriteIndex: UInt
            get() = handle.useContents { this.beginningOfPassWriteIndex }
            set(value) { error("Setters not supported on ByValue") }
        override var endOfPassWriteIndex: UInt
            get() = handle.useContents { this.endOfPassWriteIndex }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUPassTimestampWrites {
        private val struct: webgpu.native.WGPUPassTimestampWrites
            get() = handler.pointer.reinterpret<webgpu.native.WGPUPassTimestampWrites>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var querySet: WGPUQuerySet?
            get() = struct.querySet?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
            set(value) { struct.querySet = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var beginningOfPassWriteIndex: UInt
            get() = struct.beginningOfPassWriteIndex
            set(value) { struct.beginningOfPassWriteIndex = value }
        override var endOfPassWriteIndex: UInt
            get() = struct.endOfPassWriteIndex
            set(value) { struct.endOfPassWriteIndex = value }
    }
}

fun WGPUPassTimestampWrites.toCValue(): CValue<webgpu.native.WGPUPassTimestampWrites> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.querySet = this@toCValue.querySet?.handler?.pointer?.takeIf { this@toCValue.querySet?.handler?.rawValue != 0L }?.reinterpret()
    this.beginningOfPassWriteIndex = this@toCValue.beginningOfPassWriteIndex
    this.endOfPassWriteIndex = this@toCValue.endOfPassWriteIndex
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
        actual fun allocate(allocator: MemoryAllocator): WGPUPipelineLayoutDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUPipelineLayoutDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPipelineLayoutDescriptor) -> Unit): ArrayHolder<WGPUPipelineLayoutDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUPipelineLayoutDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUPipelineLayoutDescriptor>) : WGPUPipelineLayoutDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var bindGroupLayoutCount: ULong
            get() = handle.useContents { this.bindGroupLayoutCount }
            set(value) { error("Setters not supported on ByValue") }
        override var bindGroupLayouts: NativeAddress?
            get() = handle.useContents { this.bindGroupLayouts?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var immediateSize: UInt
            get() = handle.useContents { this.immediateSize }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUPipelineLayoutDescriptor {
        private val struct: webgpu.native.WGPUPipelineLayoutDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUPipelineLayoutDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var bindGroupLayoutCount: ULong
            get() = struct.bindGroupLayoutCount
            set(value) { struct.bindGroupLayoutCount = value }
        override var bindGroupLayouts: NativeAddress?
            get() = struct.bindGroupLayouts?.let(::NativeAddress)
            set(value) { struct.bindGroupLayouts = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var immediateSize: UInt
            get() = struct.immediateSize
            set(value) { struct.immediateSize = value }
    }
}

fun WGPUPipelineLayoutDescriptor.toCValue(): CValue<webgpu.native.WGPUPipelineLayoutDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.bindGroupLayoutCount = this@toCValue.bindGroupLayoutCount
    this.bindGroupLayouts = this@toCValue.bindGroupLayouts?.pointer?.takeIf { this@toCValue.bindGroupLayouts?.rawValue != 0L }?.reinterpret()
    this.immediateSize = this@toCValue.immediateSize
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
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUPrimitiveState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveState) -> Unit): ArrayHolder<WGPUPrimitiveState> {
            val byteSize = sizeOf<webgpu.native.WGPUPrimitiveState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUPrimitiveState>) : WGPUPrimitiveState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var topology: WGPUPrimitiveTopology
            get() = handle.useContents { this.topology as WGPUPrimitiveTopology }
            set(value) { error("Setters not supported on ByValue") }
        override var stripIndexFormat: WGPUIndexFormat
            get() = handle.useContents { this.stripIndexFormat as WGPUIndexFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var frontFace: WGPUFrontFace
            get() = handle.useContents { this.frontFace as WGPUFrontFace }
            set(value) { error("Setters not supported on ByValue") }
        override var cullMode: WGPUCullMode
            get() = handle.useContents { this.cullMode as WGPUCullMode }
            set(value) { error("Setters not supported on ByValue") }
        override var unclippedDepth: UInt
            get() = handle.useContents { this.unclippedDepth }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUPrimitiveState {
        private val struct: webgpu.native.WGPUPrimitiveState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUPrimitiveState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var topology: WGPUPrimitiveTopology
            get() = struct.topology as WGPUPrimitiveTopology
            set(value) { struct.topology = value }
        override var stripIndexFormat: WGPUIndexFormat
            get() = struct.stripIndexFormat as WGPUIndexFormat
            set(value) { struct.stripIndexFormat = value }
        override var frontFace: WGPUFrontFace
            get() = struct.frontFace as WGPUFrontFace
            set(value) { struct.frontFace = value }
        override var cullMode: WGPUCullMode
            get() = struct.cullMode as WGPUCullMode
            set(value) { struct.cullMode = value }
        override var unclippedDepth: UInt
            get() = struct.unclippedDepth
            set(value) { struct.unclippedDepth = value }
    }
}

fun WGPUPrimitiveState.toCValue(): CValue<webgpu.native.WGPUPrimitiveState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.topology = this@toCValue.topology
    this.stripIndexFormat = this@toCValue.stripIndexFormat
    this.frontFace = this@toCValue.frontFace
    this.cullMode = this@toCValue.cullMode
    this.unclippedDepth = this@toCValue.unclippedDepth
}

actual interface WGPUQuerySetDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var type: WGPUQueryType
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUQuerySetDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptor) -> Unit): ArrayHolder<WGPUQuerySetDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUQuerySetDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUQuerySetDescriptor>) : WGPUQuerySetDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var type: WGPUQueryType
            get() = handle.useContents { this.type as WGPUQueryType }
            set(value) { error("Setters not supported on ByValue") }
        override var count: UInt
            get() = handle.useContents { this.count }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUQuerySetDescriptor {
        private val struct: webgpu.native.WGPUQuerySetDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUQuerySetDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var type: WGPUQueryType
            get() = struct.type as WGPUQueryType
            set(value) { struct.type = value }
        override var count: UInt
            get() = struct.count
            set(value) { struct.count = value }
    }
}

fun WGPUQuerySetDescriptor.toCValue(): CValue<webgpu.native.WGPUQuerySetDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.type = this@toCValue.type
    this.count = this@toCValue.count
}

actual interface WGPUQueueDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQueueDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQueueDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUQueueDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueDescriptor) -> Unit): ArrayHolder<WGPUQueueDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUQueueDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUQueueDescriptor>) : WGPUQueueDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUQueueDescriptor {
        private val struct: webgpu.native.WGPUQueueDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUQueueDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUQueueDescriptor.toCValue(): CValue<webgpu.native.WGPUQueueDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
}

actual interface WGPURenderBundleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderBundleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderBundleDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleDescriptor) -> Unit): ArrayHolder<WGPURenderBundleDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPURenderBundleDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderBundleDescriptor>) : WGPURenderBundleDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderBundleDescriptor {
        private val struct: webgpu.native.WGPURenderBundleDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderBundleDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPURenderBundleDescriptor.toCValue(): CValue<webgpu.native.WGPURenderBundleDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPURenderBundleEncoderDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderBundleEncoderDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleEncoderDescriptor) -> Unit): ArrayHolder<WGPURenderBundleEncoderDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPURenderBundleEncoderDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderBundleEncoderDescriptor>) : WGPURenderBundleEncoderDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var colorFormatCount: ULong
            get() = handle.useContents { this.colorFormatCount }
            set(value) { error("Setters not supported on ByValue") }
        override var colorFormats: NativeAddress?
            get() = handle.useContents { this.colorFormats?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var depthStencilFormat: WGPUTextureFormat
            get() = handle.useContents { this.depthStencilFormat as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var sampleCount: UInt
            get() = handle.useContents { this.sampleCount }
            set(value) { error("Setters not supported on ByValue") }
        override var depthReadOnly: UInt
            get() = handle.useContents { this.depthReadOnly }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilReadOnly: UInt
            get() = handle.useContents { this.stencilReadOnly }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderBundleEncoderDescriptor {
        private val struct: webgpu.native.WGPURenderBundleEncoderDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderBundleEncoderDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var colorFormatCount: ULong
            get() = struct.colorFormatCount
            set(value) { struct.colorFormatCount = value }
        override var colorFormats: NativeAddress?
            get() = struct.colorFormats?.let(::NativeAddress)
            set(value) { struct.colorFormats = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var depthStencilFormat: WGPUTextureFormat
            get() = struct.depthStencilFormat as WGPUTextureFormat
            set(value) { struct.depthStencilFormat = value }
        override var sampleCount: UInt
            get() = struct.sampleCount
            set(value) { struct.sampleCount = value }
        override var depthReadOnly: UInt
            get() = struct.depthReadOnly
            set(value) { struct.depthReadOnly = value }
        override var stencilReadOnly: UInt
            get() = struct.stencilReadOnly
            set(value) { struct.stencilReadOnly = value }
    }
}

fun WGPURenderBundleEncoderDescriptor.toCValue(): CValue<webgpu.native.WGPURenderBundleEncoderDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.colorFormatCount = this@toCValue.colorFormatCount
    this.colorFormats = this@toCValue.colorFormats?.pointer?.takeIf { this@toCValue.colorFormats?.rawValue != 0L }?.reinterpret()
    this.depthStencilFormat = this@toCValue.depthStencilFormat
    this.sampleCount = this@toCValue.sampleCount
    this.depthReadOnly = this@toCValue.depthReadOnly
    this.stencilReadOnly = this@toCValue.stencilReadOnly
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
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDepthStencilAttachment =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderPassDepthStencilAttachment>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDepthStencilAttachment) -> Unit): ArrayHolder<WGPURenderPassDepthStencilAttachment> {
            val byteSize = sizeOf<webgpu.native.WGPURenderPassDepthStencilAttachment>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderPassDepthStencilAttachment>) : WGPURenderPassDepthStencilAttachment {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var view: WGPUTextureView?
            get() = handle.useContents { this.view?.let(::NativeAddress)?.let { WGPUTextureView(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var depthLoadOp: WGPULoadOp
            get() = handle.useContents { this.depthLoadOp as WGPULoadOp }
            set(value) { error("Setters not supported on ByValue") }
        override var depthStoreOp: WGPUStoreOp
            get() = handle.useContents { this.depthStoreOp as WGPUStoreOp }
            set(value) { error("Setters not supported on ByValue") }
        override var depthClearValue: Float
            get() = handle.useContents { this.depthClearValue }
            set(value) { error("Setters not supported on ByValue") }
        override var depthReadOnly: UInt
            get() = handle.useContents { this.depthReadOnly }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilLoadOp: WGPULoadOp
            get() = handle.useContents { this.stencilLoadOp as WGPULoadOp }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilStoreOp: WGPUStoreOp
            get() = handle.useContents { this.stencilStoreOp as WGPUStoreOp }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilClearValue: UInt
            get() = handle.useContents { this.stencilClearValue }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilReadOnly: UInt
            get() = handle.useContents { this.stencilReadOnly }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderPassDepthStencilAttachment {
        private val struct: webgpu.native.WGPURenderPassDepthStencilAttachment
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderPassDepthStencilAttachment>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var view: WGPUTextureView?
            get() = struct.view?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) { struct.view = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var depthLoadOp: WGPULoadOp
            get() = struct.depthLoadOp as WGPULoadOp
            set(value) { struct.depthLoadOp = value }
        override var depthStoreOp: WGPUStoreOp
            get() = struct.depthStoreOp as WGPUStoreOp
            set(value) { struct.depthStoreOp = value }
        override var depthClearValue: Float
            get() = struct.depthClearValue
            set(value) { struct.depthClearValue = value }
        override var depthReadOnly: UInt
            get() = struct.depthReadOnly
            set(value) { struct.depthReadOnly = value }
        override var stencilLoadOp: WGPULoadOp
            get() = struct.stencilLoadOp as WGPULoadOp
            set(value) { struct.stencilLoadOp = value }
        override var stencilStoreOp: WGPUStoreOp
            get() = struct.stencilStoreOp as WGPUStoreOp
            set(value) { struct.stencilStoreOp = value }
        override var stencilClearValue: UInt
            get() = struct.stencilClearValue
            set(value) { struct.stencilClearValue = value }
        override var stencilReadOnly: UInt
            get() = struct.stencilReadOnly
            set(value) { struct.stencilReadOnly = value }
    }
}

fun WGPURenderPassDepthStencilAttachment.toCValue(): CValue<webgpu.native.WGPURenderPassDepthStencilAttachment> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.view = this@toCValue.view?.handler?.pointer?.takeIf { this@toCValue.view?.handler?.rawValue != 0L }?.reinterpret()
    this.depthLoadOp = this@toCValue.depthLoadOp
    this.depthStoreOp = this@toCValue.depthStoreOp
    this.depthClearValue = this@toCValue.depthClearValue
    this.depthReadOnly = this@toCValue.depthReadOnly
    this.stencilLoadOp = this@toCValue.stencilLoadOp
    this.stencilStoreOp = this@toCValue.stencilStoreOp
    this.stencilClearValue = this@toCValue.stencilClearValue
    this.stencilReadOnly = this@toCValue.stencilReadOnly
}

actual interface WGPURenderPassMaxDrawCount {
    actual var chain: WGPUChainedStruct
    actual var maxDrawCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURenderPassMaxDrawCount = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassMaxDrawCount =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderPassMaxDrawCount>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassMaxDrawCount) -> Unit): ArrayHolder<WGPURenderPassMaxDrawCount> {
            val byteSize = sizeOf<webgpu.native.WGPURenderPassMaxDrawCount>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderPassMaxDrawCount>) : WGPURenderPassMaxDrawCount {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var maxDrawCount: ULong
            get() = handle.useContents { this.maxDrawCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderPassMaxDrawCount {
        private val struct: webgpu.native.WGPURenderPassMaxDrawCount
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderPassMaxDrawCount>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var maxDrawCount: ULong
            get() = struct.maxDrawCount
            set(value) { struct.maxDrawCount = value }
    }
}

fun WGPURenderPassMaxDrawCount.toCValue(): CValue<webgpu.native.WGPURenderPassMaxDrawCount> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.maxDrawCount = this@toCValue.maxDrawCount
}

actual interface WGPURequestAdapterWebXROptions {
    actual var chain: WGPUChainedStruct
    actual var xrCompatible: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURequestAdapterWebXROptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterWebXROptions =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURequestAdapterWebXROptions>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterWebXROptions) -> Unit): ArrayHolder<WGPURequestAdapterWebXROptions> {
            val byteSize = sizeOf<webgpu.native.WGPURequestAdapterWebXROptions>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURequestAdapterWebXROptions>) : WGPURequestAdapterWebXROptions {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var xrCompatible: UInt
            get() = handle.useContents { this.xrCompatible }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURequestAdapterWebXROptions {
        private val struct: webgpu.native.WGPURequestAdapterWebXROptions
            get() = handler.pointer.reinterpret<webgpu.native.WGPURequestAdapterWebXROptions>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var xrCompatible: UInt
            get() = struct.xrCompatible
            set(value) { struct.xrCompatible = value }
    }
}

fun WGPURequestAdapterWebXROptions.toCValue(): CValue<webgpu.native.WGPURequestAdapterWebXROptions> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.xrCompatible = this@toCValue.xrCompatible
}

actual interface WGPUSamplerBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var type: WGPUSamplerBindingType
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerBindingLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSamplerBindingLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerBindingLayout) -> Unit): ArrayHolder<WGPUSamplerBindingLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUSamplerBindingLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSamplerBindingLayout>) : WGPUSamplerBindingLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var type: WGPUSamplerBindingType
            get() = handle.useContents { this.type as WGPUSamplerBindingType }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSamplerBindingLayout {
        private val struct: webgpu.native.WGPUSamplerBindingLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSamplerBindingLayout>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var type: WGPUSamplerBindingType
            get() = struct.type as WGPUSamplerBindingType
            set(value) { struct.type = value }
    }
}

fun WGPUSamplerBindingLayout.toCValue(): CValue<webgpu.native.WGPUSamplerBindingLayout> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.type = this@toCValue.type
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
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSamplerDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptor) -> Unit): ArrayHolder<WGPUSamplerDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUSamplerDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSamplerDescriptor>) : WGPUSamplerDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var addressModeU: WGPUAddressMode
            get() = handle.useContents { this.addressModeU as WGPUAddressMode }
            set(value) { error("Setters not supported on ByValue") }
        override var addressModeV: WGPUAddressMode
            get() = handle.useContents { this.addressModeV as WGPUAddressMode }
            set(value) { error("Setters not supported on ByValue") }
        override var addressModeW: WGPUAddressMode
            get() = handle.useContents { this.addressModeW as WGPUAddressMode }
            set(value) { error("Setters not supported on ByValue") }
        override var magFilter: WGPUFilterMode
            get() = handle.useContents { this.magFilter as WGPUFilterMode }
            set(value) { error("Setters not supported on ByValue") }
        override var minFilter: WGPUFilterMode
            get() = handle.useContents { this.minFilter as WGPUFilterMode }
            set(value) { error("Setters not supported on ByValue") }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = handle.useContents { this.mipmapFilter as WGPUMipmapFilterMode }
            set(value) { error("Setters not supported on ByValue") }
        override var lodMinClamp: Float
            get() = handle.useContents { this.lodMinClamp }
            set(value) { error("Setters not supported on ByValue") }
        override var lodMaxClamp: Float
            get() = handle.useContents { this.lodMaxClamp }
            set(value) { error("Setters not supported on ByValue") }
        override var compare: WGPUCompareFunction
            get() = handle.useContents { this.compare as WGPUCompareFunction }
            set(value) { error("Setters not supported on ByValue") }
        override var maxAnisotropy: UShort
            get() = handle.useContents { this.maxAnisotropy }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSamplerDescriptor {
        private val struct: webgpu.native.WGPUSamplerDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSamplerDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var addressModeU: WGPUAddressMode
            get() = struct.addressModeU as WGPUAddressMode
            set(value) { struct.addressModeU = value }
        override var addressModeV: WGPUAddressMode
            get() = struct.addressModeV as WGPUAddressMode
            set(value) { struct.addressModeV = value }
        override var addressModeW: WGPUAddressMode
            get() = struct.addressModeW as WGPUAddressMode
            set(value) { struct.addressModeW = value }
        override var magFilter: WGPUFilterMode
            get() = struct.magFilter as WGPUFilterMode
            set(value) { struct.magFilter = value }
        override var minFilter: WGPUFilterMode
            get() = struct.minFilter as WGPUFilterMode
            set(value) { struct.minFilter = value }
        override var mipmapFilter: WGPUMipmapFilterMode
            get() = struct.mipmapFilter as WGPUMipmapFilterMode
            set(value) { struct.mipmapFilter = value }
        override var lodMinClamp: Float
            get() = struct.lodMinClamp
            set(value) { struct.lodMinClamp = value }
        override var lodMaxClamp: Float
            get() = struct.lodMaxClamp
            set(value) { struct.lodMaxClamp = value }
        override var compare: WGPUCompareFunction
            get() = struct.compare as WGPUCompareFunction
            set(value) { struct.compare = value }
        override var maxAnisotropy: UShort
            get() = struct.maxAnisotropy
            set(value) { struct.maxAnisotropy = value }
    }
}

fun WGPUSamplerDescriptor.toCValue(): CValue<webgpu.native.WGPUSamplerDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.addressModeU = this@toCValue.addressModeU
    this.addressModeV = this@toCValue.addressModeV
    this.addressModeW = this@toCValue.addressModeW
    this.magFilter = this@toCValue.magFilter
    this.minFilter = this@toCValue.minFilter
    this.mipmapFilter = this@toCValue.mipmapFilter
    this.lodMinClamp = this@toCValue.lodMinClamp
    this.lodMaxClamp = this@toCValue.lodMaxClamp
    this.compare = this@toCValue.compare
    this.maxAnisotropy = this@toCValue.maxAnisotropy
}

actual interface WGPUShaderSourceSPIRV {
    actual var chain: WGPUChainedStruct
    actual var codeSize: UInt
    actual var code: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceSPIRV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceSPIRV =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderSourceSPIRV>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceSPIRV) -> Unit): ArrayHolder<WGPUShaderSourceSPIRV> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderSourceSPIRV>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderSourceSPIRV>) : WGPUShaderSourceSPIRV {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var codeSize: UInt
            get() = handle.useContents { this.codeSize }
            set(value) { error("Setters not supported on ByValue") }
        override var code: NativeAddress?
            get() = handle.useContents { this.code?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderSourceSPIRV {
        private val struct: webgpu.native.WGPUShaderSourceSPIRV
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderSourceSPIRV>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var codeSize: UInt
            get() = struct.codeSize
            set(value) { struct.codeSize = value }
        override var code: NativeAddress?
            get() = struct.code?.let(::NativeAddress)
            set(value) { struct.code = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUShaderSourceSPIRV.toCValue(): CValue<webgpu.native.WGPUShaderSourceSPIRV> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.codeSize = this@toCValue.codeSize
    this.code = this@toCValue.code?.pointer?.takeIf { this@toCValue.code?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUShaderSourceWGSL {
    actual var chain: WGPUChainedStruct
    actual var code: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderSourceWGSL = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceWGSL =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderSourceWGSL>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceWGSL) -> Unit): ArrayHolder<WGPUShaderSourceWGSL> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderSourceWGSL>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderSourceWGSL>) : WGPUShaderSourceWGSL {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var code: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.code.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderSourceWGSL {
        private val struct: webgpu.native.WGPUShaderSourceWGSL
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderSourceWGSL>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var code: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.code.ptr))
            set(value) {
                val destBytes = struct.code.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUShaderSourceWGSL.toCValue(): CValue<webgpu.native.WGPUShaderSourceWGSL> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    val dest_code = this.code.ptr.reinterpret<ByteVar>()
    val src_code = this@toCValue.code.handler.pointer.reinterpret<ByteVar>()
    val size_code = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_code) {
        dest_code[i.toInt()] = src_code[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUStencilFaceState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStencilFaceState) -> Unit): ArrayHolder<WGPUStencilFaceState> {
            val byteSize = sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUStencilFaceState>) : WGPUStencilFaceState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var compare: WGPUCompareFunction
            get() = handle.useContents { this.compare as WGPUCompareFunction }
            set(value) { error("Setters not supported on ByValue") }
        override var failOp: WGPUStencilOperation
            get() = handle.useContents { this.failOp as WGPUStencilOperation }
            set(value) { error("Setters not supported on ByValue") }
        override var depthFailOp: WGPUStencilOperation
            get() = handle.useContents { this.depthFailOp as WGPUStencilOperation }
            set(value) { error("Setters not supported on ByValue") }
        override var passOp: WGPUStencilOperation
            get() = handle.useContents { this.passOp as WGPUStencilOperation }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUStencilFaceState {
        private val struct: webgpu.native.WGPUStencilFaceState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUStencilFaceState>().pointed
        
        override var compare: WGPUCompareFunction
            get() = struct.compare as WGPUCompareFunction
            set(value) { struct.compare = value }
        override var failOp: WGPUStencilOperation
            get() = struct.failOp as WGPUStencilOperation
            set(value) { struct.failOp = value }
        override var depthFailOp: WGPUStencilOperation
            get() = struct.depthFailOp as WGPUStencilOperation
            set(value) { struct.depthFailOp = value }
        override var passOp: WGPUStencilOperation
            get() = struct.passOp as WGPUStencilOperation
            set(value) { struct.passOp = value }
    }
}

fun WGPUStencilFaceState.toCValue(): CValue<webgpu.native.WGPUStencilFaceState> = cValue {
    this.compare = this@toCValue.compare
    this.failOp = this@toCValue.failOp
    this.depthFailOp = this@toCValue.depthFailOp
    this.passOp = this@toCValue.passOp
}

actual interface WGPUStorageTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var access: WGPUStorageTextureAccess
    actual var format: WGPUTextureFormat
    actual var viewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUStorageTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUStorageTextureBindingLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUStorageTextureBindingLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStorageTextureBindingLayout) -> Unit): ArrayHolder<WGPUStorageTextureBindingLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUStorageTextureBindingLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUStorageTextureBindingLayout>) : WGPUStorageTextureBindingLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var access: WGPUStorageTextureAccess
            get() = handle.useContents { this.access as WGPUStorageTextureAccess }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.useContents { this.viewDimension as WGPUTextureViewDimension }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUStorageTextureBindingLayout {
        private val struct: webgpu.native.WGPUStorageTextureBindingLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUStorageTextureBindingLayout>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var access: WGPUStorageTextureAccess
            get() = struct.access as WGPUStorageTextureAccess
            set(value) { struct.access = value }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var viewDimension: WGPUTextureViewDimension
            get() = struct.viewDimension as WGPUTextureViewDimension
            set(value) { struct.viewDimension = value }
    }
}

fun WGPUStorageTextureBindingLayout.toCValue(): CValue<webgpu.native.WGPUStorageTextureBindingLayout> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.access = this@toCValue.access
    this.format = this@toCValue.format
    this.viewDimension = this@toCValue.viewDimension
}

actual interface WGPUSupportedFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedFeatures =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSupportedFeatures>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedFeatures) -> Unit): ArrayHolder<WGPUSupportedFeatures> {
            val byteSize = sizeOf<webgpu.native.WGPUSupportedFeatures>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSupportedFeatures>) : WGPUSupportedFeatures {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var featureCount: ULong
            get() = handle.useContents { this.featureCount }
            set(value) { error("Setters not supported on ByValue") }
        override var features: NativeAddress?
            get() = handle.useContents { this.features?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSupportedFeatures {
        private val struct: webgpu.native.WGPUSupportedFeatures
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSupportedFeatures>().pointed
        
        override var featureCount: ULong
            get() = struct.featureCount
            set(value) { struct.featureCount = value }
        override var features: NativeAddress?
            get() = struct.features?.let(::NativeAddress)
            set(value) { struct.features = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSupportedFeatures.toCValue(): CValue<webgpu.native.WGPUSupportedFeatures> = cValue {
    this.featureCount = this@toCValue.featureCount
    this.features = this@toCValue.features?.pointer?.takeIf { this@toCValue.features?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSupportedInstanceFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedInstanceFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedInstanceFeatures =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSupportedInstanceFeatures>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedInstanceFeatures) -> Unit): ArrayHolder<WGPUSupportedInstanceFeatures> {
            val byteSize = sizeOf<webgpu.native.WGPUSupportedInstanceFeatures>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSupportedInstanceFeatures>) : WGPUSupportedInstanceFeatures {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var featureCount: ULong
            get() = handle.useContents { this.featureCount }
            set(value) { error("Setters not supported on ByValue") }
        override var features: NativeAddress?
            get() = handle.useContents { this.features?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSupportedInstanceFeatures {
        private val struct: webgpu.native.WGPUSupportedInstanceFeatures
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSupportedInstanceFeatures>().pointed
        
        override var featureCount: ULong
            get() = struct.featureCount
            set(value) { struct.featureCount = value }
        override var features: NativeAddress?
            get() = struct.features?.let(::NativeAddress)
            set(value) { struct.features = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSupportedInstanceFeatures.toCValue(): CValue<webgpu.native.WGPUSupportedInstanceFeatures> = cValue {
    this.featureCount = this@toCValue.featureCount
    this.features = this@toCValue.features?.pointer?.takeIf { this@toCValue.features?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSupportedWGSLLanguageFeatures {
    actual var featureCount: ULong
    actual var features: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSupportedWGSLLanguageFeatures = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSupportedWGSLLanguageFeatures =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSupportedWGSLLanguageFeatures>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedWGSLLanguageFeatures) -> Unit): ArrayHolder<WGPUSupportedWGSLLanguageFeatures> {
            val byteSize = sizeOf<webgpu.native.WGPUSupportedWGSLLanguageFeatures>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSupportedWGSLLanguageFeatures>) : WGPUSupportedWGSLLanguageFeatures {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var featureCount: ULong
            get() = handle.useContents { this.featureCount }
            set(value) { error("Setters not supported on ByValue") }
        override var features: NativeAddress?
            get() = handle.useContents { this.features?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSupportedWGSLLanguageFeatures {
        private val struct: webgpu.native.WGPUSupportedWGSLLanguageFeatures
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSupportedWGSLLanguageFeatures>().pointed
        
        override var featureCount: ULong
            get() = struct.featureCount
            set(value) { struct.featureCount = value }
        override var features: NativeAddress?
            get() = struct.features?.let(::NativeAddress)
            set(value) { struct.features = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSupportedWGSLLanguageFeatures.toCValue(): CValue<webgpu.native.WGPUSupportedWGSLLanguageFeatures> = cValue {
    this.featureCount = this@toCValue.featureCount
    this.features = this@toCValue.features?.pointer?.takeIf { this@toCValue.features?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceCapabilities =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceCapabilities>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceCapabilities) -> Unit): ArrayHolder<WGPUSurfaceCapabilities> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceCapabilities>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceCapabilities>) : WGPUSurfaceCapabilities {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var usages: ULong
            get() = handle.useContents { this.usages }
            set(value) { error("Setters not supported on ByValue") }
        override var formatCount: ULong
            get() = handle.useContents { this.formatCount }
            set(value) { error("Setters not supported on ByValue") }
        override var formats: NativeAddress?
            get() = handle.useContents { this.formats?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var presentModeCount: ULong
            get() = handle.useContents { this.presentModeCount }
            set(value) { error("Setters not supported on ByValue") }
        override var presentModes: NativeAddress?
            get() = handle.useContents { this.presentModes?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var alphaModeCount: ULong
            get() = handle.useContents { this.alphaModeCount }
            set(value) { error("Setters not supported on ByValue") }
        override var alphaModes: NativeAddress?
            get() = handle.useContents { this.alphaModes?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceCapabilities {
        private val struct: webgpu.native.WGPUSurfaceCapabilities
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceCapabilities>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var usages: ULong
            get() = struct.usages
            set(value) { struct.usages = value }
        override var formatCount: ULong
            get() = struct.formatCount
            set(value) { struct.formatCount = value }
        override var formats: NativeAddress?
            get() = struct.formats?.let(::NativeAddress)
            set(value) { struct.formats = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var presentModeCount: ULong
            get() = struct.presentModeCount
            set(value) { struct.presentModeCount = value }
        override var presentModes: NativeAddress?
            get() = struct.presentModes?.let(::NativeAddress)
            set(value) { struct.presentModes = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var alphaModeCount: ULong
            get() = struct.alphaModeCount
            set(value) { struct.alphaModeCount = value }
        override var alphaModes: NativeAddress?
            get() = struct.alphaModes?.let(::NativeAddress)
            set(value) { struct.alphaModes = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceCapabilities.toCValue(): CValue<webgpu.native.WGPUSurfaceCapabilities> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.usages = this@toCValue.usages
    this.formatCount = this@toCValue.formatCount
    this.formats = this@toCValue.formats?.pointer?.takeIf { this@toCValue.formats?.rawValue != 0L }?.reinterpret()
    this.presentModeCount = this@toCValue.presentModeCount
    this.presentModes = this@toCValue.presentModes?.pointer?.takeIf { this@toCValue.presentModes?.rawValue != 0L }?.reinterpret()
    this.alphaModeCount = this@toCValue.alphaModeCount
    this.alphaModes = this@toCValue.alphaModes?.pointer?.takeIf { this@toCValue.alphaModes?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSurfaceColorManagement {
    actual var chain: WGPUChainedStruct
    actual var colorSpace: WGPUPredefinedColorSpace
    actual var toneMappingMode: WGPUToneMappingMode
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceColorManagement = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceColorManagement =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceColorManagement>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceColorManagement) -> Unit): ArrayHolder<WGPUSurfaceColorManagement> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceColorManagement>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceColorManagement>) : WGPUSurfaceColorManagement {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = handle.useContents { this.colorSpace as WGPUPredefinedColorSpace }
            set(value) { error("Setters not supported on ByValue") }
        override var toneMappingMode: WGPUToneMappingMode
            get() = handle.useContents { this.toneMappingMode as WGPUToneMappingMode }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceColorManagement {
        private val struct: webgpu.native.WGPUSurfaceColorManagement
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceColorManagement>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var colorSpace: WGPUPredefinedColorSpace
            get() = struct.colorSpace as WGPUPredefinedColorSpace
            set(value) { struct.colorSpace = value }
        override var toneMappingMode: WGPUToneMappingMode
            get() = struct.toneMappingMode as WGPUToneMappingMode
            set(value) { struct.toneMappingMode = value }
    }
}

fun WGPUSurfaceColorManagement.toCValue(): CValue<webgpu.native.WGPUSurfaceColorManagement> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.colorSpace = this@toCValue.colorSpace
    this.toneMappingMode = this@toCValue.toneMappingMode
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
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfiguration =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceConfiguration>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfiguration) -> Unit): ArrayHolder<WGPUSurfaceConfiguration> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceConfiguration>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceConfiguration>) : WGPUSurfaceConfiguration {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var device: WGPUDevice?
            get() = handle.useContents { this.device?.let(::NativeAddress)?.let { WGPUDevice(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var usage: ULong
            get() = handle.useContents { this.usage }
            set(value) { error("Setters not supported on ByValue") }
        override var width: UInt
            get() = handle.useContents { this.width }
            set(value) { error("Setters not supported on ByValue") }
        override var height: UInt
            get() = handle.useContents { this.height }
            set(value) { error("Setters not supported on ByValue") }
        override var viewFormatCount: ULong
            get() = handle.useContents { this.viewFormatCount }
            set(value) { error("Setters not supported on ByValue") }
        override var viewFormats: NativeAddress?
            get() = handle.useContents { this.viewFormats?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = handle.useContents { this.alphaMode as WGPUCompositeAlphaMode }
            set(value) { error("Setters not supported on ByValue") }
        override var presentMode: WGPUPresentMode
            get() = handle.useContents { this.presentMode as WGPUPresentMode }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceConfiguration {
        private val struct: webgpu.native.WGPUSurfaceConfiguration
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceConfiguration>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var device: WGPUDevice?
            get() = struct.device?.let(::NativeAddress)?.let { WGPUDevice(it) }
            set(value) { struct.device = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var usage: ULong
            get() = struct.usage
            set(value) { struct.usage = value }
        override var width: UInt
            get() = struct.width
            set(value) { struct.width = value }
        override var height: UInt
            get() = struct.height
            set(value) { struct.height = value }
        override var viewFormatCount: ULong
            get() = struct.viewFormatCount
            set(value) { struct.viewFormatCount = value }
        override var viewFormats: NativeAddress?
            get() = struct.viewFormats?.let(::NativeAddress)
            set(value) { struct.viewFormats = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var alphaMode: WGPUCompositeAlphaMode
            get() = struct.alphaMode as WGPUCompositeAlphaMode
            set(value) { struct.alphaMode = value }
        override var presentMode: WGPUPresentMode
            get() = struct.presentMode as WGPUPresentMode
            set(value) { struct.presentMode = value }
    }
}

fun WGPUSurfaceConfiguration.toCValue(): CValue<webgpu.native.WGPUSurfaceConfiguration> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.device = this@toCValue.device?.handler?.pointer?.takeIf { this@toCValue.device?.handler?.rawValue != 0L }?.reinterpret()
    this.format = this@toCValue.format
    this.usage = this@toCValue.usage
    this.width = this@toCValue.width
    this.height = this@toCValue.height
    this.viewFormatCount = this@toCValue.viewFormatCount
    this.viewFormats = this@toCValue.viewFormats?.pointer?.takeIf { this@toCValue.viewFormats?.rawValue != 0L }?.reinterpret()
    this.alphaMode = this@toCValue.alphaMode
    this.presentMode = this@toCValue.presentMode
}

actual interface WGPUSurfaceSourceAndroidNativeWindow {
    actual var chain: WGPUChainedStruct
    actual var window: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceAndroidNativeWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceAndroidNativeWindow =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceAndroidNativeWindow>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceAndroidNativeWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceAndroidNativeWindow>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceAndroidNativeWindow>) : WGPUSurfaceSourceAndroidNativeWindow {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var window: NativeAddress?
            get() = handle.useContents { this.window?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceAndroidNativeWindow {
        private val struct: webgpu.native.WGPUSurfaceSourceAndroidNativeWindow
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceAndroidNativeWindow>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var window: NativeAddress?
            get() = struct.window?.let(::NativeAddress)
            set(value) { struct.window = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceSourceAndroidNativeWindow.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceAndroidNativeWindow> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.window = this@toCValue.window?.pointer?.takeIf { this@toCValue.window?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSurfaceSourceMetalLayer {
    actual var chain: WGPUChainedStruct
    actual var layer: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceMetalLayer = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceMetalLayer =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceMetalLayer>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceMetalLayer) -> Unit): ArrayHolder<WGPUSurfaceSourceMetalLayer> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceMetalLayer>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceMetalLayer>) : WGPUSurfaceSourceMetalLayer {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var layer: NativeAddress?
            get() = handle.useContents { this.layer?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceMetalLayer {
        private val struct: webgpu.native.WGPUSurfaceSourceMetalLayer
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceMetalLayer>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var layer: NativeAddress?
            get() = struct.layer?.let(::NativeAddress)
            set(value) { struct.layer = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceSourceMetalLayer.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceMetalLayer> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.layer = this@toCValue.layer?.pointer?.takeIf { this@toCValue.layer?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSurfaceSourceWaylandSurface {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var surface: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWaylandSurface = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWaylandSurface =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceWaylandSurface>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWaylandSurface) -> Unit): ArrayHolder<WGPUSurfaceSourceWaylandSurface> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceWaylandSurface>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceWaylandSurface>) : WGPUSurfaceSourceWaylandSurface {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var display: NativeAddress?
            get() = handle.useContents { this.display?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var surface: NativeAddress?
            get() = handle.useContents { this.surface?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceWaylandSurface {
        private val struct: webgpu.native.WGPUSurfaceSourceWaylandSurface
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceWaylandSurface>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var display: NativeAddress?
            get() = struct.display?.let(::NativeAddress)
            set(value) { struct.display = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var surface: NativeAddress?
            get() = struct.surface?.let(::NativeAddress)
            set(value) { struct.surface = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceSourceWaylandSurface.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceWaylandSurface> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.display = this@toCValue.display?.pointer?.takeIf { this@toCValue.display?.rawValue != 0L }?.reinterpret()
    this.surface = this@toCValue.surface?.pointer?.takeIf { this@toCValue.surface?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSurfaceSourceWindowsHWND {
    actual var chain: WGPUChainedStruct
    actual var hinstance: NativeAddress?
    actual var hwnd: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWindowsHWND = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWindowsHWND =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceWindowsHWND>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWindowsHWND) -> Unit): ArrayHolder<WGPUSurfaceSourceWindowsHWND> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceWindowsHWND>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceWindowsHWND>) : WGPUSurfaceSourceWindowsHWND {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var hinstance: NativeAddress?
            get() = handle.useContents { this.hinstance?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var hwnd: NativeAddress?
            get() = handle.useContents { this.hwnd?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceWindowsHWND {
        private val struct: webgpu.native.WGPUSurfaceSourceWindowsHWND
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceWindowsHWND>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var hinstance: NativeAddress?
            get() = struct.hinstance?.let(::NativeAddress)
            set(value) { struct.hinstance = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var hwnd: NativeAddress?
            get() = struct.hwnd?.let(::NativeAddress)
            set(value) { struct.hwnd = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceSourceWindowsHWND.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceWindowsHWND> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.hinstance = this@toCValue.hinstance?.pointer?.takeIf { this@toCValue.hinstance?.rawValue != 0L }?.reinterpret()
    this.hwnd = this@toCValue.hwnd?.pointer?.takeIf { this@toCValue.hwnd?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUSurfaceSourceXCBWindow {
    actual var chain: WGPUChainedStruct
    actual var connection: NativeAddress?
    actual var window: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXCBWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXCBWindow =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceXCBWindow>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXCBWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXCBWindow> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceXCBWindow>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceXCBWindow>) : WGPUSurfaceSourceXCBWindow {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var connection: NativeAddress?
            get() = handle.useContents { this.connection?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var window: UInt
            get() = handle.useContents { this.window }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceXCBWindow {
        private val struct: webgpu.native.WGPUSurfaceSourceXCBWindow
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceXCBWindow>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var connection: NativeAddress?
            get() = struct.connection?.let(::NativeAddress)
            set(value) { struct.connection = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var window: UInt
            get() = struct.window
            set(value) { struct.window = value }
    }
}

fun WGPUSurfaceSourceXCBWindow.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceXCBWindow> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.connection = this@toCValue.connection?.pointer?.takeIf { this@toCValue.connection?.rawValue != 0L }?.reinterpret()
    this.window = this@toCValue.window
}

actual interface WGPUSurfaceSourceXlibWindow {
    actual var chain: WGPUChainedStruct
    actual var display: NativeAddress?
    actual var window: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXlibWindow = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXlibWindow =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceXlibWindow>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXlibWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXlibWindow> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceXlibWindow>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceXlibWindow>) : WGPUSurfaceSourceXlibWindow {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var display: NativeAddress?
            get() = handle.useContents { this.display?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var window: ULong
            get() = handle.useContents { this.window }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceXlibWindow {
        private val struct: webgpu.native.WGPUSurfaceSourceXlibWindow
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceXlibWindow>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var display: NativeAddress?
            get() = struct.display?.let(::NativeAddress)
            set(value) { struct.display = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var window: ULong
            get() = struct.window
            set(value) { struct.window = value }
    }
}

fun WGPUSurfaceSourceXlibWindow.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceXlibWindow> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.display = this@toCValue.display?.pointer?.takeIf { this@toCValue.display?.rawValue != 0L }?.reinterpret()
    this.window = this@toCValue.window
}

actual interface WGPUSurfaceTexture {
    actual var nextInChain: WGPUChainedStruct?
    actual var texture: WGPUTexture?
    actual var status: WGPUSurfaceGetCurrentTextureStatus
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceTexture = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceTexture =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceTexture>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceTexture) -> Unit): ArrayHolder<WGPUSurfaceTexture> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceTexture>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceTexture>) : WGPUSurfaceTexture {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var texture: WGPUTexture?
            get() = handle.useContents { this.texture?.let(::NativeAddress)?.let { WGPUTexture(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = handle.useContents { this.status as WGPUSurfaceGetCurrentTextureStatus }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceTexture {
        private val struct: webgpu.native.WGPUSurfaceTexture
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceTexture>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var texture: WGPUTexture?
            get() = struct.texture?.let(::NativeAddress)?.let { WGPUTexture(it) }
            set(value) { struct.texture = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var status: WGPUSurfaceGetCurrentTextureStatus
            get() = struct.status as WGPUSurfaceGetCurrentTextureStatus
            set(value) { struct.status = value }
    }
}

fun WGPUSurfaceTexture.toCValue(): CValue<webgpu.native.WGPUSurfaceTexture> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.texture = this@toCValue.texture?.handler?.pointer?.takeIf { this@toCValue.texture?.handler?.rawValue != 0L }?.reinterpret()
    this.status = this@toCValue.status
}

actual interface WGPUTexelCopyBufferLayout {
    actual var offset: ULong
    actual var bytesPerRow: UInt
    actual var rowsPerImage: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTexelCopyBufferLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferLayout) -> Unit): ArrayHolder<WGPUTexelCopyBufferLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUTexelCopyBufferLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTexelCopyBufferLayout>) : WGPUTexelCopyBufferLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var offset: ULong
            get() = handle.useContents { this.offset }
            set(value) { error("Setters not supported on ByValue") }
        override var bytesPerRow: UInt
            get() = handle.useContents { this.bytesPerRow }
            set(value) { error("Setters not supported on ByValue") }
        override var rowsPerImage: UInt
            get() = handle.useContents { this.rowsPerImage }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTexelCopyBufferLayout {
        private val struct: webgpu.native.WGPUTexelCopyBufferLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTexelCopyBufferLayout>().pointed
        
        override var offset: ULong
            get() = struct.offset
            set(value) { struct.offset = value }
        override var bytesPerRow: UInt
            get() = struct.bytesPerRow
            set(value) { struct.bytesPerRow = value }
        override var rowsPerImage: UInt
            get() = struct.rowsPerImage
            set(value) { struct.rowsPerImage = value }
    }
}

fun WGPUTexelCopyBufferLayout.toCValue(): CValue<webgpu.native.WGPUTexelCopyBufferLayout> = cValue {
    this.offset = this@toCValue.offset
    this.bytesPerRow = this@toCValue.bytesPerRow
    this.rowsPerImage = this@toCValue.rowsPerImage
}

actual interface WGPUTextureBindingLayout {
    actual var nextInChain: WGPUChainedStruct?
    actual var sampleType: WGPUTextureSampleType
    actual var viewDimension: WGPUTextureViewDimension
    actual var multisampled: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingLayout = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureBindingLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingLayout) -> Unit): ArrayHolder<WGPUTextureBindingLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureBindingLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureBindingLayout>) : WGPUTextureBindingLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var sampleType: WGPUTextureSampleType
            get() = handle.useContents { this.sampleType as WGPUTextureSampleType }
            set(value) { error("Setters not supported on ByValue") }
        override var viewDimension: WGPUTextureViewDimension
            get() = handle.useContents { this.viewDimension as WGPUTextureViewDimension }
            set(value) { error("Setters not supported on ByValue") }
        override var multisampled: UInt
            get() = handle.useContents { this.multisampled }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureBindingLayout {
        private val struct: webgpu.native.WGPUTextureBindingLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureBindingLayout>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var sampleType: WGPUTextureSampleType
            get() = struct.sampleType as WGPUTextureSampleType
            set(value) { struct.sampleType = value }
        override var viewDimension: WGPUTextureViewDimension
            get() = struct.viewDimension as WGPUTextureViewDimension
            set(value) { struct.viewDimension = value }
        override var multisampled: UInt
            get() = struct.multisampled
            set(value) { struct.multisampled = value }
    }
}

fun WGPUTextureBindingLayout.toCValue(): CValue<webgpu.native.WGPUTextureBindingLayout> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.sampleType = this@toCValue.sampleType
    this.viewDimension = this@toCValue.viewDimension
    this.multisampled = this@toCValue.multisampled
}

actual interface WGPUTextureBindingViewDimension {
    actual var chain: WGPUChainedStruct
    actual var textureBindingViewDimension: WGPUTextureViewDimension
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureBindingViewDimension = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureBindingViewDimension =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureBindingViewDimension>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingViewDimension) -> Unit): ArrayHolder<WGPUTextureBindingViewDimension> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureBindingViewDimension>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureBindingViewDimension>) : WGPUTextureBindingViewDimension {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = handle.useContents { this.textureBindingViewDimension as WGPUTextureViewDimension }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureBindingViewDimension {
        private val struct: webgpu.native.WGPUTextureBindingViewDimension
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureBindingViewDimension>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var textureBindingViewDimension: WGPUTextureViewDimension
            get() = struct.textureBindingViewDimension as WGPUTextureViewDimension
            set(value) { struct.textureBindingViewDimension = value }
    }
}

fun WGPUTextureBindingViewDimension.toCValue(): CValue<webgpu.native.WGPUTextureBindingViewDimension> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.textureBindingViewDimension = this@toCValue.textureBindingViewDimension
}

actual interface WGPUTextureComponentSwizzle {
    actual var r: WGPUComponentSwizzle
    actual var g: WGPUComponentSwizzle
    actual var b: WGPUComponentSwizzle
    actual var a: WGPUComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzle =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureComponentSwizzle>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzle) -> Unit): ArrayHolder<WGPUTextureComponentSwizzle> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureComponentSwizzle>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureComponentSwizzle>) : WGPUTextureComponentSwizzle {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var r: WGPUComponentSwizzle
            get() = handle.useContents { this.r as WGPUComponentSwizzle }
            set(value) { error("Setters not supported on ByValue") }
        override var g: WGPUComponentSwizzle
            get() = handle.useContents { this.g as WGPUComponentSwizzle }
            set(value) { error("Setters not supported on ByValue") }
        override var b: WGPUComponentSwizzle
            get() = handle.useContents { this.b as WGPUComponentSwizzle }
            set(value) { error("Setters not supported on ByValue") }
        override var a: WGPUComponentSwizzle
            get() = handle.useContents { this.a as WGPUComponentSwizzle }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureComponentSwizzle {
        private val struct: webgpu.native.WGPUTextureComponentSwizzle
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureComponentSwizzle>().pointed
        
        override var r: WGPUComponentSwizzle
            get() = struct.r as WGPUComponentSwizzle
            set(value) { struct.r = value }
        override var g: WGPUComponentSwizzle
            get() = struct.g as WGPUComponentSwizzle
            set(value) { struct.g = value }
        override var b: WGPUComponentSwizzle
            get() = struct.b as WGPUComponentSwizzle
            set(value) { struct.b = value }
        override var a: WGPUComponentSwizzle
            get() = struct.a as WGPUComponentSwizzle
            set(value) { struct.a = value }
    }
}

fun WGPUTextureComponentSwizzle.toCValue(): CValue<webgpu.native.WGPUTextureComponentSwizzle> = cValue {
    this.r = this@toCValue.r
    this.g = this@toCValue.g
    this.b = this@toCValue.b
    this.a = this@toCValue.a
}

actual interface WGPUVertexAttribute {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUVertexFormat
    actual var offset: ULong
    actual var shaderLocation: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUVertexAttribute = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexAttribute =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUVertexAttribute>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexAttribute) -> Unit): ArrayHolder<WGPUVertexAttribute> {
            val byteSize = sizeOf<webgpu.native.WGPUVertexAttribute>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUVertexAttribute>) : WGPUVertexAttribute {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUVertexFormat
            get() = handle.useContents { this.format as WGPUVertexFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var offset: ULong
            get() = handle.useContents { this.offset }
            set(value) { error("Setters not supported on ByValue") }
        override var shaderLocation: UInt
            get() = handle.useContents { this.shaderLocation }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUVertexAttribute {
        private val struct: webgpu.native.WGPUVertexAttribute
            get() = handler.pointer.reinterpret<webgpu.native.WGPUVertexAttribute>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var format: WGPUVertexFormat
            get() = struct.format as WGPUVertexFormat
            set(value) { struct.format = value }
        override var offset: ULong
            get() = struct.offset
            set(value) { struct.offset = value }
        override var shaderLocation: UInt
            get() = struct.shaderLocation
            set(value) { struct.shaderLocation = value }
    }
}

fun WGPUVertexAttribute.toCValue(): CValue<webgpu.native.WGPUVertexAttribute> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.format = this@toCValue.format
    this.offset = this@toCValue.offset
    this.shaderLocation = this@toCValue.shaderLocation
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntry =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupEntry>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntry) -> Unit): ArrayHolder<WGPUBindGroupEntry> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupEntry>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupEntry>) : WGPUBindGroupEntry {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var binding: UInt
            get() = handle.useContents { this.binding }
            set(value) { error("Setters not supported on ByValue") }
        override var buffer: WGPUBuffer?
            get() = handle.useContents { this.buffer?.let(::NativeAddress)?.let { WGPUBuffer(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var offset: ULong
            get() = handle.useContents { this.offset }
            set(value) { error("Setters not supported on ByValue") }
        override var size: ULong
            get() = handle.useContents { this.size }
            set(value) { error("Setters not supported on ByValue") }
        override var sampler: WGPUSampler?
            get() = handle.useContents { this.sampler?.let(::NativeAddress)?.let { WGPUSampler(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var textureView: WGPUTextureView?
            get() = handle.useContents { this.textureView?.let(::NativeAddress)?.let { WGPUTextureView(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupEntry {
        private val struct: webgpu.native.WGPUBindGroupEntry
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupEntry>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var binding: UInt
            get() = struct.binding
            set(value) { struct.binding = value }
        override var buffer: WGPUBuffer?
            get() = struct.buffer?.let(::NativeAddress)?.let { WGPUBuffer(it) }
            set(value) { struct.buffer = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var offset: ULong
            get() = struct.offset
            set(value) { struct.offset = value }
        override var size: ULong
            get() = struct.size
            set(value) { struct.size = value }
        override var sampler: WGPUSampler?
            get() = struct.sampler?.let(::NativeAddress)?.let { WGPUSampler(it) }
            set(value) { struct.sampler = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var textureView: WGPUTextureView?
            get() = struct.textureView?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) { struct.textureView = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUBindGroupEntry.toCValue(): CValue<webgpu.native.WGPUBindGroupEntry> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.binding = this@toCValue.binding
    this.buffer = this@toCValue.buffer?.handler?.pointer?.takeIf { this@toCValue.buffer?.handler?.rawValue != 0L }?.reinterpret()
    this.offset = this@toCValue.offset
    this.size = this@toCValue.size
    this.sampler = this@toCValue.sampler?.handler?.pointer?.takeIf { this@toCValue.sampler?.handler?.rawValue != 0L }?.reinterpret()
    this.textureView = this@toCValue.textureView?.handler?.pointer?.takeIf { this@toCValue.textureView?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntry =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupLayoutEntry>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntry) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntry> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupLayoutEntry>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupLayoutEntry>) : WGPUBindGroupLayoutEntry {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var binding: UInt
            get() = handle.useContents { this.binding }
            set(value) { error("Setters not supported on ByValue") }
        override var visibility: ULong
            get() = handle.useContents { this.visibility }
            set(value) { error("Setters not supported on ByValue") }
        override var bindingArraySize: UInt
            get() = handle.useContents { this.bindingArraySize }
            set(value) { error("Setters not supported on ByValue") }
        override var buffer: WGPUBufferBindingLayout
            get() = handle.useContents { WGPUBufferBindingLayout.ByReference(NativeAddress(this.buffer.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var sampler: WGPUSamplerBindingLayout
            get() = handle.useContents { WGPUSamplerBindingLayout.ByReference(NativeAddress(this.sampler.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var texture: WGPUTextureBindingLayout
            get() = handle.useContents { WGPUTextureBindingLayout.ByReference(NativeAddress(this.texture.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() = handle.useContents { WGPUStorageTextureBindingLayout.ByReference(NativeAddress(this.storageTexture.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutEntry {
        private val struct: webgpu.native.WGPUBindGroupLayoutEntry
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupLayoutEntry>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var binding: UInt
            get() = struct.binding
            set(value) { struct.binding = value }
        override var visibility: ULong
            get() = struct.visibility
            set(value) { struct.visibility = value }
        override var bindingArraySize: UInt
            get() = struct.bindingArraySize
            set(value) { struct.bindingArraySize = value }
        override var buffer: WGPUBufferBindingLayout
            get() = WGPUBufferBindingLayout.ByReference(NativeAddress(struct.buffer.ptr))
            set(value) {
                val destBytes = struct.buffer.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUBufferBindingLayout>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var sampler: WGPUSamplerBindingLayout
            get() = WGPUSamplerBindingLayout.ByReference(NativeAddress(struct.sampler.ptr))
            set(value) {
                val destBytes = struct.sampler.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUSamplerBindingLayout>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var texture: WGPUTextureBindingLayout
            get() = WGPUTextureBindingLayout.ByReference(NativeAddress(struct.texture.ptr))
            set(value) {
                val destBytes = struct.texture.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUTextureBindingLayout>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var storageTexture: WGPUStorageTextureBindingLayout
            get() = WGPUStorageTextureBindingLayout.ByReference(NativeAddress(struct.storageTexture.ptr))
            set(value) {
                val destBytes = struct.storageTexture.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStorageTextureBindingLayout>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUBindGroupLayoutEntry.toCValue(): CValue<webgpu.native.WGPUBindGroupLayoutEntry> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.binding = this@toCValue.binding
    this.visibility = this@toCValue.visibility
    this.bindingArraySize = this@toCValue.bindingArraySize
    val dest_buffer = this.buffer.ptr.reinterpret<ByteVar>()
    val src_buffer = this@toCValue.buffer.handler.pointer.reinterpret<ByteVar>()
    val size_buffer = sizeOf<webgpu.native.WGPUBufferBindingLayout>().toLong()
    for (i in 0L until size_buffer) {
        dest_buffer[i.toInt()] = src_buffer[i.toInt()]
    }
    val dest_sampler = this.sampler.ptr.reinterpret<ByteVar>()
    val src_sampler = this@toCValue.sampler.handler.pointer.reinterpret<ByteVar>()
    val size_sampler = sizeOf<webgpu.native.WGPUSamplerBindingLayout>().toLong()
    for (i in 0L until size_sampler) {
        dest_sampler[i.toInt()] = src_sampler[i.toInt()]
    }
    val dest_texture = this.texture.ptr.reinterpret<ByteVar>()
    val src_texture = this@toCValue.texture.handler.pointer.reinterpret<ByteVar>()
    val size_texture = sizeOf<webgpu.native.WGPUTextureBindingLayout>().toLong()
    for (i in 0L until size_texture) {
        dest_texture[i.toInt()] = src_texture[i.toInt()]
    }
    val dest_storageTexture = this.storageTexture.ptr.reinterpret<ByteVar>()
    val src_storageTexture = this@toCValue.storageTexture.handler.pointer.reinterpret<ByteVar>()
    val size_storageTexture = sizeOf<webgpu.native.WGPUStorageTextureBindingLayout>().toLong()
    for (i in 0L until size_storageTexture) {
        dest_storageTexture[i.toInt()] = src_storageTexture[i.toInt()]
    }
}

actual interface WGPUBlendState {
    actual var color: WGPUBlendComponent
    actual var alpha: WGPUBlendComponent
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBlendState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBlendState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBlendState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendState) -> Unit): ArrayHolder<WGPUBlendState> {
            val byteSize = sizeOf<webgpu.native.WGPUBlendState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBlendState>) : WGPUBlendState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var color: WGPUBlendComponent
            get() = handle.useContents { WGPUBlendComponent.ByReference(NativeAddress(this.color.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var alpha: WGPUBlendComponent
            get() = handle.useContents { WGPUBlendComponent.ByReference(NativeAddress(this.alpha.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBlendState {
        private val struct: webgpu.native.WGPUBlendState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBlendState>().pointed
        
        override var color: WGPUBlendComponent
            get() = WGPUBlendComponent.ByReference(NativeAddress(struct.color.ptr))
            set(value) {
                val destBytes = struct.color.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUBlendComponent>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var alpha: WGPUBlendComponent
            get() = WGPUBlendComponent.ByReference(NativeAddress(struct.alpha.ptr))
            set(value) {
                val destBytes = struct.alpha.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUBlendComponent>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUBlendState.toCValue(): CValue<webgpu.native.WGPUBlendState> = cValue {
    val dest_color = this.color.ptr.reinterpret<ByteVar>()
    val src_color = this@toCValue.color.handler.pointer.reinterpret<ByteVar>()
    val size_color = sizeOf<webgpu.native.WGPUBlendComponent>().toLong()
    for (i in 0L until size_color) {
        dest_color[i.toInt()] = src_color[i.toInt()]
    }
    val dest_alpha = this.alpha.ptr.reinterpret<ByteVar>()
    val src_alpha = this@toCValue.alpha.handler.pointer.reinterpret<ByteVar>()
    val size_alpha = sizeOf<webgpu.native.WGPUBlendComponent>().toLong()
    for (i in 0L until size_alpha) {
        dest_alpha[i.toInt()] = src_alpha[i.toInt()]
    }
}

actual interface WGPUCompilationInfo {
    actual var nextInChain: WGPUChainedStruct?
    actual var messageCount: ULong
    actual var messages: WGPUCompilationMessage?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUCompilationInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUCompilationInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUCompilationInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfo) -> Unit): ArrayHolder<WGPUCompilationInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUCompilationInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUCompilationInfo>) : WGPUCompilationInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var messageCount: ULong
            get() = handle.useContents { this.messageCount }
            set(value) { error("Setters not supported on ByValue") }
        override var messages: WGPUCompilationMessage?
            get() = handle.useContents { this.messages?.let(::NativeAddress)?.let { WGPUCompilationMessage(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUCompilationInfo {
        private val struct: webgpu.native.WGPUCompilationInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUCompilationInfo>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var messageCount: ULong
            get() = struct.messageCount
            set(value) { struct.messageCount = value }
        override var messages: WGPUCompilationMessage?
            get() = struct.messages?.let(::NativeAddress)?.let { WGPUCompilationMessage(it) }
            set(value) { struct.messages = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUCompilationInfo.toCValue(): CValue<webgpu.native.WGPUCompilationInfo> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.messageCount = this@toCValue.messageCount
    this.messages = this@toCValue.messages?.handler?.pointer?.takeIf { this@toCValue.messages?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUComputePassDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var timestampWrites: WGPUPassTimestampWrites?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePassDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePassDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUComputePassDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePassDescriptor) -> Unit): ArrayHolder<WGPUComputePassDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUComputePassDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUComputePassDescriptor>) : WGPUComputePassDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.useContents { this.timestampWrites?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUComputePassDescriptor {
        private val struct: webgpu.native.WGPUComputePassDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUComputePassDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = struct.timestampWrites?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) }
            set(value) { struct.timestampWrites = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUComputePassDescriptor.toCValue(): CValue<webgpu.native.WGPUComputePassDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.timestampWrites = this@toCValue.timestampWrites?.handler?.pointer?.takeIf { this@toCValue.timestampWrites?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUComputeState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUComputeState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputeState) -> Unit): ArrayHolder<WGPUComputeState> {
            val byteSize = sizeOf<webgpu.native.WGPUComputeState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUComputeState>) : WGPUComputeState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var module: WGPUShaderModule?
            get() = handle.useContents { this.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var entryPoint: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.entryPoint.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var constantCount: ULong
            get() = handle.useContents { this.constantCount }
            set(value) { error("Setters not supported on ByValue") }
        override var constants: WGPUConstantEntry?
            get() = handle.useContents { this.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUComputeState {
        private val struct: webgpu.native.WGPUComputeState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUComputeState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var module: WGPUShaderModule?
            get() = struct.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) { struct.module = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.entryPoint.ptr))
            set(value) {
                val destBytes = struct.entryPoint.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var constantCount: ULong
            get() = struct.constantCount
            set(value) { struct.constantCount = value }
        override var constants: WGPUConstantEntry?
            get() = struct.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) { struct.constants = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUComputeState.toCValue(): CValue<webgpu.native.WGPUComputeState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.module = this@toCValue.module?.handler?.pointer?.takeIf { this@toCValue.module?.handler?.rawValue != 0L }?.reinterpret()
    val dest_entryPoint = this.entryPoint.ptr.reinterpret<ByteVar>()
    val src_entryPoint = this@toCValue.entryPoint.handler.pointer.reinterpret<ByteVar>()
    val size_entryPoint = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_entryPoint) {
        dest_entryPoint[i.toInt()] = src_entryPoint[i.toInt()]
    }
    this.constantCount = this@toCValue.constantCount
    this.constants = this@toCValue.constants?.handler?.pointer?.takeIf { this@toCValue.constants?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUDepthStencilState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUDepthStencilState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDepthStencilState) -> Unit): ArrayHolder<WGPUDepthStencilState> {
            val byteSize = sizeOf<webgpu.native.WGPUDepthStencilState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUDepthStencilState>) : WGPUDepthStencilState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = handle.useContents { this.depthWriteEnabled as WGPUOptionalBool }
            set(value) { error("Setters not supported on ByValue") }
        override var depthCompare: WGPUCompareFunction
            get() = handle.useContents { this.depthCompare as WGPUCompareFunction }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilFront: WGPUStencilFaceState
            get() = handle.useContents { WGPUStencilFaceState.ByReference(NativeAddress(this.stencilFront.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilBack: WGPUStencilFaceState
            get() = handle.useContents { WGPUStencilFaceState.ByReference(NativeAddress(this.stencilBack.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilReadMask: UInt
            get() = handle.useContents { this.stencilReadMask }
            set(value) { error("Setters not supported on ByValue") }
        override var stencilWriteMask: UInt
            get() = handle.useContents { this.stencilWriteMask }
            set(value) { error("Setters not supported on ByValue") }
        override var depthBias: Int
            get() = handle.useContents { this.depthBias }
            set(value) { error("Setters not supported on ByValue") }
        override var depthBiasSlopeScale: Float
            get() = handle.useContents { this.depthBiasSlopeScale }
            set(value) { error("Setters not supported on ByValue") }
        override var depthBiasClamp: Float
            get() = handle.useContents { this.depthBiasClamp }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUDepthStencilState {
        private val struct: webgpu.native.WGPUDepthStencilState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUDepthStencilState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var depthWriteEnabled: WGPUOptionalBool
            get() = struct.depthWriteEnabled as WGPUOptionalBool
            set(value) { struct.depthWriteEnabled = value }
        override var depthCompare: WGPUCompareFunction
            get() = struct.depthCompare as WGPUCompareFunction
            set(value) { struct.depthCompare = value }
        override var stencilFront: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByReference(NativeAddress(struct.stencilFront.ptr))
            set(value) {
                val destBytes = struct.stencilFront.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var stencilBack: WGPUStencilFaceState
            get() = WGPUStencilFaceState.ByReference(NativeAddress(struct.stencilBack.ptr))
            set(value) {
                val destBytes = struct.stencilBack.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var stencilReadMask: UInt
            get() = struct.stencilReadMask
            set(value) { struct.stencilReadMask = value }
        override var stencilWriteMask: UInt
            get() = struct.stencilWriteMask
            set(value) { struct.stencilWriteMask = value }
        override var depthBias: Int
            get() = struct.depthBias
            set(value) { struct.depthBias = value }
        override var depthBiasSlopeScale: Float
            get() = struct.depthBiasSlopeScale
            set(value) { struct.depthBiasSlopeScale = value }
        override var depthBiasClamp: Float
            get() = struct.depthBiasClamp
            set(value) { struct.depthBiasClamp = value }
    }
}

fun WGPUDepthStencilState.toCValue(): CValue<webgpu.native.WGPUDepthStencilState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.format = this@toCValue.format
    this.depthWriteEnabled = this@toCValue.depthWriteEnabled
    this.depthCompare = this@toCValue.depthCompare
    val dest_stencilFront = this.stencilFront.ptr.reinterpret<ByteVar>()
    val src_stencilFront = this@toCValue.stencilFront.handler.pointer.reinterpret<ByteVar>()
    val size_stencilFront = sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()
    for (i in 0L until size_stencilFront) {
        dest_stencilFront[i.toInt()] = src_stencilFront[i.toInt()]
    }
    val dest_stencilBack = this.stencilBack.ptr.reinterpret<ByteVar>()
    val src_stencilBack = this@toCValue.stencilBack.handler.pointer.reinterpret<ByteVar>()
    val size_stencilBack = sizeOf<webgpu.native.WGPUStencilFaceState>().toLong()
    for (i in 0L until size_stencilBack) {
        dest_stencilBack[i.toInt()] = src_stencilBack[i.toInt()]
    }
    this.stencilReadMask = this@toCValue.stencilReadMask
    this.stencilWriteMask = this@toCValue.stencilWriteMask
    this.depthBias = this@toCValue.depthBias
    this.depthBiasSlopeScale = this@toCValue.depthBiasSlopeScale
    this.depthBiasClamp = this@toCValue.depthBiasClamp
}

actual interface WGPUFutureWaitInfo {
    actual var future: WGPUFuture
    actual var completed: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUFutureWaitInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUFutureWaitInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUFutureWaitInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFutureWaitInfo) -> Unit): ArrayHolder<WGPUFutureWaitInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUFutureWaitInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUFutureWaitInfo>) : WGPUFutureWaitInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var future: WGPUFuture
            get() = handle.useContents { WGPUFuture.ByReference(NativeAddress(this.future.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var completed: UInt
            get() = handle.useContents { this.completed }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUFutureWaitInfo {
        private val struct: webgpu.native.WGPUFutureWaitInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUFutureWaitInfo>().pointed
        
        override var future: WGPUFuture
            get() = WGPUFuture.ByReference(NativeAddress(struct.future.ptr))
            set(value) {
                val destBytes = struct.future.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUFuture>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var completed: UInt
            get() = struct.completed
            set(value) { struct.completed = value }
    }
}

fun WGPUFutureWaitInfo.toCValue(): CValue<webgpu.native.WGPUFutureWaitInfo> = cValue {
    val dest_future = this.future.ptr.reinterpret<ByteVar>()
    val src_future = this@toCValue.future.handler.pointer.reinterpret<ByteVar>()
    val size_future = sizeOf<webgpu.native.WGPUFuture>().toLong()
    for (i in 0L until size_future) {
        dest_future[i.toInt()] = src_future[i.toInt()]
    }
    this.completed = this@toCValue.completed
}

actual interface WGPUInstanceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var requiredFeatureCount: ULong
    actual var requiredFeatures: NativeAddress?
    actual var requiredLimits: WGPUInstanceLimits?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUInstanceDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceDescriptor) -> Unit): ArrayHolder<WGPUInstanceDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUInstanceDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUInstanceDescriptor>) : WGPUInstanceDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredFeatureCount: ULong
            get() = handle.useContents { this.requiredFeatureCount }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredFeatures: NativeAddress?
            get() = handle.useContents { this.requiredFeatures?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredLimits: WGPUInstanceLimits?
            get() = handle.useContents { this.requiredLimits?.let(::NativeAddress)?.let { WGPUInstanceLimits(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUInstanceDescriptor {
        private val struct: webgpu.native.WGPUInstanceDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUInstanceDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var requiredFeatureCount: ULong
            get() = struct.requiredFeatureCount
            set(value) { struct.requiredFeatureCount = value }
        override var requiredFeatures: NativeAddress?
            get() = struct.requiredFeatures?.let(::NativeAddress)
            set(value) { struct.requiredFeatures = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var requiredLimits: WGPUInstanceLimits?
            get() = struct.requiredLimits?.let(::NativeAddress)?.let { WGPUInstanceLimits(it) }
            set(value) { struct.requiredLimits = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUInstanceDescriptor.toCValue(): CValue<webgpu.native.WGPUInstanceDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.requiredFeatureCount = this@toCValue.requiredFeatureCount
    this.requiredFeatures = this@toCValue.requiredFeatures?.pointer?.takeIf { this@toCValue.requiredFeatures?.rawValue != 0L }?.reinterpret()
    this.requiredLimits = this@toCValue.requiredLimits?.handler?.pointer?.takeIf { this@toCValue.requiredLimits?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPULimits =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPULimits>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPULimits) -> Unit): ArrayHolder<WGPULimits> {
            val byteSize = sizeOf<webgpu.native.WGPULimits>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPULimits>) : WGPULimits {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var maxTextureDimension1D: UInt
            get() = handle.useContents { this.maxTextureDimension1D }
            set(value) { error("Setters not supported on ByValue") }
        override var maxTextureDimension2D: UInt
            get() = handle.useContents { this.maxTextureDimension2D }
            set(value) { error("Setters not supported on ByValue") }
        override var maxTextureDimension3D: UInt
            get() = handle.useContents { this.maxTextureDimension3D }
            set(value) { error("Setters not supported on ByValue") }
        override var maxTextureArrayLayers: UInt
            get() = handle.useContents { this.maxTextureArrayLayers }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBindGroups: UInt
            get() = handle.useContents { this.maxBindGroups }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = handle.useContents { this.maxBindGroupsPlusVertexBuffers }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBindingsPerBindGroup: UInt
            get() = handle.useContents { this.maxBindingsPerBindGroup }
            set(value) { error("Setters not supported on ByValue") }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = handle.useContents { this.maxDynamicUniformBuffersPerPipelineLayout }
            set(value) { error("Setters not supported on ByValue") }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = handle.useContents { this.maxDynamicStorageBuffersPerPipelineLayout }
            set(value) { error("Setters not supported on ByValue") }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = handle.useContents { this.maxSampledTexturesPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxSamplersPerShaderStage: UInt
            get() = handle.useContents { this.maxSamplersPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = handle.useContents { this.maxStorageBuffersPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = handle.useContents { this.maxStorageTexturesPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = handle.useContents { this.maxUniformBuffersPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxUniformBufferBindingSize: ULong
            get() = handle.useContents { this.maxUniformBufferBindingSize }
            set(value) { error("Setters not supported on ByValue") }
        override var maxStorageBufferBindingSize: ULong
            get() = handle.useContents { this.maxStorageBufferBindingSize }
            set(value) { error("Setters not supported on ByValue") }
        override var minUniformBufferOffsetAlignment: UInt
            get() = handle.useContents { this.minUniformBufferOffsetAlignment }
            set(value) { error("Setters not supported on ByValue") }
        override var minStorageBufferOffsetAlignment: UInt
            get() = handle.useContents { this.minStorageBufferOffsetAlignment }
            set(value) { error("Setters not supported on ByValue") }
        override var maxVertexBuffers: UInt
            get() = handle.useContents { this.maxVertexBuffers }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBufferSize: ULong
            get() = handle.useContents { this.maxBufferSize }
            set(value) { error("Setters not supported on ByValue") }
        override var maxVertexAttributes: UInt
            get() = handle.useContents { this.maxVertexAttributes }
            set(value) { error("Setters not supported on ByValue") }
        override var maxVertexBufferArrayStride: UInt
            get() = handle.useContents { this.maxVertexBufferArrayStride }
            set(value) { error("Setters not supported on ByValue") }
        override var maxInterStageShaderVariables: UInt
            get() = handle.useContents { this.maxInterStageShaderVariables }
            set(value) { error("Setters not supported on ByValue") }
        override var maxColorAttachments: UInt
            get() = handle.useContents { this.maxColorAttachments }
            set(value) { error("Setters not supported on ByValue") }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = handle.useContents { this.maxColorAttachmentBytesPerSample }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = handle.useContents { this.maxComputeWorkgroupStorageSize }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = handle.useContents { this.maxComputeInvocationsPerWorkgroup }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeWorkgroupSizeX: UInt
            get() = handle.useContents { this.maxComputeWorkgroupSizeX }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeWorkgroupSizeY: UInt
            get() = handle.useContents { this.maxComputeWorkgroupSizeY }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = handle.useContents { this.maxComputeWorkgroupSizeZ }
            set(value) { error("Setters not supported on ByValue") }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = handle.useContents { this.maxComputeWorkgroupsPerDimension }
            set(value) { error("Setters not supported on ByValue") }
        override var maxImmediateSize: UInt
            get() = handle.useContents { this.maxImmediateSize }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPULimits {
        private val struct: webgpu.native.WGPULimits
            get() = handler.pointer.reinterpret<webgpu.native.WGPULimits>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var maxTextureDimension1D: UInt
            get() = struct.maxTextureDimension1D
            set(value) { struct.maxTextureDimension1D = value }
        override var maxTextureDimension2D: UInt
            get() = struct.maxTextureDimension2D
            set(value) { struct.maxTextureDimension2D = value }
        override var maxTextureDimension3D: UInt
            get() = struct.maxTextureDimension3D
            set(value) { struct.maxTextureDimension3D = value }
        override var maxTextureArrayLayers: UInt
            get() = struct.maxTextureArrayLayers
            set(value) { struct.maxTextureArrayLayers = value }
        override var maxBindGroups: UInt
            get() = struct.maxBindGroups
            set(value) { struct.maxBindGroups = value }
        override var maxBindGroupsPlusVertexBuffers: UInt
            get() = struct.maxBindGroupsPlusVertexBuffers
            set(value) { struct.maxBindGroupsPlusVertexBuffers = value }
        override var maxBindingsPerBindGroup: UInt
            get() = struct.maxBindingsPerBindGroup
            set(value) { struct.maxBindingsPerBindGroup = value }
        override var maxDynamicUniformBuffersPerPipelineLayout: UInt
            get() = struct.maxDynamicUniformBuffersPerPipelineLayout
            set(value) { struct.maxDynamicUniformBuffersPerPipelineLayout = value }
        override var maxDynamicStorageBuffersPerPipelineLayout: UInt
            get() = struct.maxDynamicStorageBuffersPerPipelineLayout
            set(value) { struct.maxDynamicStorageBuffersPerPipelineLayout = value }
        override var maxSampledTexturesPerShaderStage: UInt
            get() = struct.maxSampledTexturesPerShaderStage
            set(value) { struct.maxSampledTexturesPerShaderStage = value }
        override var maxSamplersPerShaderStage: UInt
            get() = struct.maxSamplersPerShaderStage
            set(value) { struct.maxSamplersPerShaderStage = value }
        override var maxStorageBuffersPerShaderStage: UInt
            get() = struct.maxStorageBuffersPerShaderStage
            set(value) { struct.maxStorageBuffersPerShaderStage = value }
        override var maxStorageTexturesPerShaderStage: UInt
            get() = struct.maxStorageTexturesPerShaderStage
            set(value) { struct.maxStorageTexturesPerShaderStage = value }
        override var maxUniformBuffersPerShaderStage: UInt
            get() = struct.maxUniformBuffersPerShaderStage
            set(value) { struct.maxUniformBuffersPerShaderStage = value }
        override var maxUniformBufferBindingSize: ULong
            get() = struct.maxUniformBufferBindingSize
            set(value) { struct.maxUniformBufferBindingSize = value }
        override var maxStorageBufferBindingSize: ULong
            get() = struct.maxStorageBufferBindingSize
            set(value) { struct.maxStorageBufferBindingSize = value }
        override var minUniformBufferOffsetAlignment: UInt
            get() = struct.minUniformBufferOffsetAlignment
            set(value) { struct.minUniformBufferOffsetAlignment = value }
        override var minStorageBufferOffsetAlignment: UInt
            get() = struct.minStorageBufferOffsetAlignment
            set(value) { struct.minStorageBufferOffsetAlignment = value }
        override var maxVertexBuffers: UInt
            get() = struct.maxVertexBuffers
            set(value) { struct.maxVertexBuffers = value }
        override var maxBufferSize: ULong
            get() = struct.maxBufferSize
            set(value) { struct.maxBufferSize = value }
        override var maxVertexAttributes: UInt
            get() = struct.maxVertexAttributes
            set(value) { struct.maxVertexAttributes = value }
        override var maxVertexBufferArrayStride: UInt
            get() = struct.maxVertexBufferArrayStride
            set(value) { struct.maxVertexBufferArrayStride = value }
        override var maxInterStageShaderVariables: UInt
            get() = struct.maxInterStageShaderVariables
            set(value) { struct.maxInterStageShaderVariables = value }
        override var maxColorAttachments: UInt
            get() = struct.maxColorAttachments
            set(value) { struct.maxColorAttachments = value }
        override var maxColorAttachmentBytesPerSample: UInt
            get() = struct.maxColorAttachmentBytesPerSample
            set(value) { struct.maxColorAttachmentBytesPerSample = value }
        override var maxComputeWorkgroupStorageSize: UInt
            get() = struct.maxComputeWorkgroupStorageSize
            set(value) { struct.maxComputeWorkgroupStorageSize = value }
        override var maxComputeInvocationsPerWorkgroup: UInt
            get() = struct.maxComputeInvocationsPerWorkgroup
            set(value) { struct.maxComputeInvocationsPerWorkgroup = value }
        override var maxComputeWorkgroupSizeX: UInt
            get() = struct.maxComputeWorkgroupSizeX
            set(value) { struct.maxComputeWorkgroupSizeX = value }
        override var maxComputeWorkgroupSizeY: UInt
            get() = struct.maxComputeWorkgroupSizeY
            set(value) { struct.maxComputeWorkgroupSizeY = value }
        override var maxComputeWorkgroupSizeZ: UInt
            get() = struct.maxComputeWorkgroupSizeZ
            set(value) { struct.maxComputeWorkgroupSizeZ = value }
        override var maxComputeWorkgroupsPerDimension: UInt
            get() = struct.maxComputeWorkgroupsPerDimension
            set(value) { struct.maxComputeWorkgroupsPerDimension = value }
        override var maxImmediateSize: UInt
            get() = struct.maxImmediateSize
            set(value) { struct.maxImmediateSize = value }
    }
}

fun WGPULimits.toCValue(): CValue<webgpu.native.WGPULimits> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.maxTextureDimension1D = this@toCValue.maxTextureDimension1D
    this.maxTextureDimension2D = this@toCValue.maxTextureDimension2D
    this.maxTextureDimension3D = this@toCValue.maxTextureDimension3D
    this.maxTextureArrayLayers = this@toCValue.maxTextureArrayLayers
    this.maxBindGroups = this@toCValue.maxBindGroups
    this.maxBindGroupsPlusVertexBuffers = this@toCValue.maxBindGroupsPlusVertexBuffers
    this.maxBindingsPerBindGroup = this@toCValue.maxBindingsPerBindGroup
    this.maxDynamicUniformBuffersPerPipelineLayout = this@toCValue.maxDynamicUniformBuffersPerPipelineLayout
    this.maxDynamicStorageBuffersPerPipelineLayout = this@toCValue.maxDynamicStorageBuffersPerPipelineLayout
    this.maxSampledTexturesPerShaderStage = this@toCValue.maxSampledTexturesPerShaderStage
    this.maxSamplersPerShaderStage = this@toCValue.maxSamplersPerShaderStage
    this.maxStorageBuffersPerShaderStage = this@toCValue.maxStorageBuffersPerShaderStage
    this.maxStorageTexturesPerShaderStage = this@toCValue.maxStorageTexturesPerShaderStage
    this.maxUniformBuffersPerShaderStage = this@toCValue.maxUniformBuffersPerShaderStage
    this.maxUniformBufferBindingSize = this@toCValue.maxUniformBufferBindingSize
    this.maxStorageBufferBindingSize = this@toCValue.maxStorageBufferBindingSize
    this.minUniformBufferOffsetAlignment = this@toCValue.minUniformBufferOffsetAlignment
    this.minStorageBufferOffsetAlignment = this@toCValue.minStorageBufferOffsetAlignment
    this.maxVertexBuffers = this@toCValue.maxVertexBuffers
    this.maxBufferSize = this@toCValue.maxBufferSize
    this.maxVertexAttributes = this@toCValue.maxVertexAttributes
    this.maxVertexBufferArrayStride = this@toCValue.maxVertexBufferArrayStride
    this.maxInterStageShaderVariables = this@toCValue.maxInterStageShaderVariables
    this.maxColorAttachments = this@toCValue.maxColorAttachments
    this.maxColorAttachmentBytesPerSample = this@toCValue.maxColorAttachmentBytesPerSample
    this.maxComputeWorkgroupStorageSize = this@toCValue.maxComputeWorkgroupStorageSize
    this.maxComputeInvocationsPerWorkgroup = this@toCValue.maxComputeInvocationsPerWorkgroup
    this.maxComputeWorkgroupSizeX = this@toCValue.maxComputeWorkgroupSizeX
    this.maxComputeWorkgroupSizeY = this@toCValue.maxComputeWorkgroupSizeY
    this.maxComputeWorkgroupSizeZ = this@toCValue.maxComputeWorkgroupSizeZ
    this.maxComputeWorkgroupsPerDimension = this@toCValue.maxComputeWorkgroupsPerDimension
    this.maxImmediateSize = this@toCValue.maxImmediateSize
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
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassColorAttachment =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderPassColorAttachment>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassColorAttachment) -> Unit): ArrayHolder<WGPURenderPassColorAttachment> {
            val byteSize = sizeOf<webgpu.native.WGPURenderPassColorAttachment>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderPassColorAttachment>) : WGPURenderPassColorAttachment {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var view: WGPUTextureView?
            get() = handle.useContents { this.view?.let(::NativeAddress)?.let { WGPUTextureView(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var depthSlice: UInt
            get() = handle.useContents { this.depthSlice }
            set(value) { error("Setters not supported on ByValue") }
        override var resolveTarget: WGPUTextureView?
            get() = handle.useContents { this.resolveTarget?.let(::NativeAddress)?.let { WGPUTextureView(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var loadOp: WGPULoadOp
            get() = handle.useContents { this.loadOp as WGPULoadOp }
            set(value) { error("Setters not supported on ByValue") }
        override var storeOp: WGPUStoreOp
            get() = handle.useContents { this.storeOp as WGPUStoreOp }
            set(value) { error("Setters not supported on ByValue") }
        override var clearValue: WGPUColor
            get() = handle.useContents { WGPUColor.ByReference(NativeAddress(this.clearValue.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderPassColorAttachment {
        private val struct: webgpu.native.WGPURenderPassColorAttachment
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderPassColorAttachment>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var view: WGPUTextureView?
            get() = struct.view?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) { struct.view = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var depthSlice: UInt
            get() = struct.depthSlice
            set(value) { struct.depthSlice = value }
        override var resolveTarget: WGPUTextureView?
            get() = struct.resolveTarget?.let(::NativeAddress)?.let { WGPUTextureView(it) }
            set(value) { struct.resolveTarget = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var loadOp: WGPULoadOp
            get() = struct.loadOp as WGPULoadOp
            set(value) { struct.loadOp = value }
        override var storeOp: WGPUStoreOp
            get() = struct.storeOp as WGPUStoreOp
            set(value) { struct.storeOp = value }
        override var clearValue: WGPUColor
            get() = WGPUColor.ByReference(NativeAddress(struct.clearValue.ptr))
            set(value) {
                val destBytes = struct.clearValue.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUColor>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPURenderPassColorAttachment.toCValue(): CValue<webgpu.native.WGPURenderPassColorAttachment> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.view = this@toCValue.view?.handler?.pointer?.takeIf { this@toCValue.view?.handler?.rawValue != 0L }?.reinterpret()
    this.depthSlice = this@toCValue.depthSlice
    this.resolveTarget = this@toCValue.resolveTarget?.handler?.pointer?.takeIf { this@toCValue.resolveTarget?.handler?.rawValue != 0L }?.reinterpret()
    this.loadOp = this@toCValue.loadOp
    this.storeOp = this@toCValue.storeOp
    val dest_clearValue = this.clearValue.ptr.reinterpret<ByteVar>()
    val src_clearValue = this@toCValue.clearValue.handler.pointer.reinterpret<ByteVar>()
    val size_clearValue = sizeOf<webgpu.native.WGPUColor>().toLong()
    for (i in 0L until size_clearValue) {
        dest_clearValue[i.toInt()] = src_clearValue[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPURequestAdapterOptions =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURequestAdapterOptions>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterOptions) -> Unit): ArrayHolder<WGPURequestAdapterOptions> {
            val byteSize = sizeOf<webgpu.native.WGPURequestAdapterOptions>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURequestAdapterOptions>) : WGPURequestAdapterOptions {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var featureLevel: WGPUFeatureLevel
            get() = handle.useContents { this.featureLevel as WGPUFeatureLevel }
            set(value) { error("Setters not supported on ByValue") }
        override var powerPreference: WGPUPowerPreference
            get() = handle.useContents { this.powerPreference as WGPUPowerPreference }
            set(value) { error("Setters not supported on ByValue") }
        override var forceFallbackAdapter: UInt
            get() = handle.useContents { this.forceFallbackAdapter }
            set(value) { error("Setters not supported on ByValue") }
        override var backendType: WGPUBackendType
            get() = handle.useContents { this.backendType as WGPUBackendType }
            set(value) { error("Setters not supported on ByValue") }
        override var compatibleSurface: WGPUSurface?
            get() = handle.useContents { this.compatibleSurface?.let(::NativeAddress)?.let { WGPUSurface(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURequestAdapterOptions {
        private val struct: webgpu.native.WGPURequestAdapterOptions
            get() = handler.pointer.reinterpret<webgpu.native.WGPURequestAdapterOptions>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var featureLevel: WGPUFeatureLevel
            get() = struct.featureLevel as WGPUFeatureLevel
            set(value) { struct.featureLevel = value }
        override var powerPreference: WGPUPowerPreference
            get() = struct.powerPreference as WGPUPowerPreference
            set(value) { struct.powerPreference = value }
        override var forceFallbackAdapter: UInt
            get() = struct.forceFallbackAdapter
            set(value) { struct.forceFallbackAdapter = value }
        override var backendType: WGPUBackendType
            get() = struct.backendType as WGPUBackendType
            set(value) { struct.backendType = value }
        override var compatibleSurface: WGPUSurface?
            get() = struct.compatibleSurface?.let(::NativeAddress)?.let { WGPUSurface(it) }
            set(value) { struct.compatibleSurface = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPURequestAdapterOptions.toCValue(): CValue<webgpu.native.WGPURequestAdapterOptions> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.featureLevel = this@toCValue.featureLevel
    this.powerPreference = this@toCValue.powerPreference
    this.forceFallbackAdapter = this@toCValue.forceFallbackAdapter
    this.backendType = this@toCValue.backendType
    this.compatibleSurface = this@toCValue.compatibleSurface?.handler?.pointer?.takeIf { this@toCValue.compatibleSurface?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUShaderModuleDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderModuleDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptor) -> Unit): ArrayHolder<WGPUShaderModuleDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderModuleDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderModuleDescriptor>) : WGPUShaderModuleDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderModuleDescriptor {
        private val struct: webgpu.native.WGPUShaderModuleDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderModuleDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUShaderModuleDescriptor.toCValue(): CValue<webgpu.native.WGPUShaderModuleDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
}

actual interface WGPUSurfaceDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceDescriptor) -> Unit): ArrayHolder<WGPUSurfaceDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceDescriptor>) : WGPUSurfaceDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceDescriptor {
        private val struct: webgpu.native.WGPUSurfaceDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUSurfaceDescriptor.toCValue(): CValue<webgpu.native.WGPUSurfaceDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
}

actual interface WGPUTexelCopyBufferInfo {
    actual var layout: WGPUTexelCopyBufferLayout
    actual var buffer: WGPUBuffer?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTexelCopyBufferInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferInfo) -> Unit): ArrayHolder<WGPUTexelCopyBufferInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUTexelCopyBufferInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTexelCopyBufferInfo>) : WGPUTexelCopyBufferInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var layout: WGPUTexelCopyBufferLayout
            get() = handle.useContents { WGPUTexelCopyBufferLayout.ByReference(NativeAddress(this.layout.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var buffer: WGPUBuffer?
            get() = handle.useContents { this.buffer?.let(::NativeAddress)?.let { WGPUBuffer(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTexelCopyBufferInfo {
        private val struct: webgpu.native.WGPUTexelCopyBufferInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTexelCopyBufferInfo>().pointed
        
        override var layout: WGPUTexelCopyBufferLayout
            get() = WGPUTexelCopyBufferLayout.ByReference(NativeAddress(struct.layout.ptr))
            set(value) {
                val destBytes = struct.layout.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUTexelCopyBufferLayout>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var buffer: WGPUBuffer?
            get() = struct.buffer?.let(::NativeAddress)?.let { WGPUBuffer(it) }
            set(value) { struct.buffer = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUTexelCopyBufferInfo.toCValue(): CValue<webgpu.native.WGPUTexelCopyBufferInfo> = cValue {
    val dest_layout = this.layout.ptr.reinterpret<ByteVar>()
    val src_layout = this@toCValue.layout.handler.pointer.reinterpret<ByteVar>()
    val size_layout = sizeOf<webgpu.native.WGPUTexelCopyBufferLayout>().toLong()
    for (i in 0L until size_layout) {
        dest_layout[i.toInt()] = src_layout[i.toInt()]
    }
    this.buffer = this@toCValue.buffer?.handler?.pointer?.takeIf { this@toCValue.buffer?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUTexelCopyTextureInfo {
    actual var texture: WGPUTexture?
    actual var mipLevel: UInt
    actual var origin: WGPUOrigin3D
    actual var aspect: WGPUTextureAspect
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTexelCopyTextureInfo = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTexelCopyTextureInfo =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTexelCopyTextureInfo>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyTextureInfo) -> Unit): ArrayHolder<WGPUTexelCopyTextureInfo> {
            val byteSize = sizeOf<webgpu.native.WGPUTexelCopyTextureInfo>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTexelCopyTextureInfo>) : WGPUTexelCopyTextureInfo {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var texture: WGPUTexture?
            get() = handle.useContents { this.texture?.let(::NativeAddress)?.let { WGPUTexture(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var mipLevel: UInt
            get() = handle.useContents { this.mipLevel }
            set(value) { error("Setters not supported on ByValue") }
        override var origin: WGPUOrigin3D
            get() = handle.useContents { WGPUOrigin3D.ByReference(NativeAddress(this.origin.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var aspect: WGPUTextureAspect
            get() = handle.useContents { this.aspect as WGPUTextureAspect }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTexelCopyTextureInfo {
        private val struct: webgpu.native.WGPUTexelCopyTextureInfo
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTexelCopyTextureInfo>().pointed
        
        override var texture: WGPUTexture?
            get() = struct.texture?.let(::NativeAddress)?.let { WGPUTexture(it) }
            set(value) { struct.texture = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var mipLevel: UInt
            get() = struct.mipLevel
            set(value) { struct.mipLevel = value }
        override var origin: WGPUOrigin3D
            get() = WGPUOrigin3D.ByReference(NativeAddress(struct.origin.ptr))
            set(value) {
                val destBytes = struct.origin.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUOrigin3D>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var aspect: WGPUTextureAspect
            get() = struct.aspect as WGPUTextureAspect
            set(value) { struct.aspect = value }
    }
}

fun WGPUTexelCopyTextureInfo.toCValue(): CValue<webgpu.native.WGPUTexelCopyTextureInfo> = cValue {
    this.texture = this@toCValue.texture?.handler?.pointer?.takeIf { this@toCValue.texture?.handler?.rawValue != 0L }?.reinterpret()
    this.mipLevel = this@toCValue.mipLevel
    val dest_origin = this.origin.ptr.reinterpret<ByteVar>()
    val src_origin = this@toCValue.origin.handler.pointer.reinterpret<ByteVar>()
    val size_origin = sizeOf<webgpu.native.WGPUOrigin3D>().toLong()
    for (i in 0L until size_origin) {
        dest_origin[i.toInt()] = src_origin[i.toInt()]
    }
    this.aspect = this@toCValue.aspect
}

actual interface WGPUTextureComponentSwizzleDescriptor {
    actual var chain: WGPUChainedStruct
    actual var swizzle: WGPUTextureComponentSwizzle
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzleDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzleDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureComponentSwizzleDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzleDescriptor) -> Unit): ArrayHolder<WGPUTextureComponentSwizzleDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureComponentSwizzleDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureComponentSwizzleDescriptor>) : WGPUTextureComponentSwizzleDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var swizzle: WGPUTextureComponentSwizzle
            get() = handle.useContents { WGPUTextureComponentSwizzle.ByReference(NativeAddress(this.swizzle.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureComponentSwizzleDescriptor {
        private val struct: webgpu.native.WGPUTextureComponentSwizzleDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureComponentSwizzleDescriptor>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var swizzle: WGPUTextureComponentSwizzle
            get() = WGPUTextureComponentSwizzle.ByReference(NativeAddress(struct.swizzle.ptr))
            set(value) {
                val destBytes = struct.swizzle.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUTextureComponentSwizzle>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUTextureComponentSwizzleDescriptor.toCValue(): CValue<webgpu.native.WGPUTextureComponentSwizzleDescriptor> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    val dest_swizzle = this.swizzle.ptr.reinterpret<ByteVar>()
    val src_swizzle = this@toCValue.swizzle.handler.pointer.reinterpret<ByteVar>()
    val size_swizzle = sizeOf<webgpu.native.WGPUTextureComponentSwizzle>().toLong()
    for (i in 0L until size_swizzle) {
        dest_swizzle[i.toInt()] = src_swizzle[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureDescriptor) -> Unit): ArrayHolder<WGPUTextureDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureDescriptor>) : WGPUTextureDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var usage: ULong
            get() = handle.useContents { this.usage }
            set(value) { error("Setters not supported on ByValue") }
        override var dimension: WGPUTextureDimension
            get() = handle.useContents { this.dimension as WGPUTextureDimension }
            set(value) { error("Setters not supported on ByValue") }
        override var size: WGPUExtent3D
            get() = handle.useContents { WGPUExtent3D.ByReference(NativeAddress(this.size.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var mipLevelCount: UInt
            get() = handle.useContents { this.mipLevelCount }
            set(value) { error("Setters not supported on ByValue") }
        override var sampleCount: UInt
            get() = handle.useContents { this.sampleCount }
            set(value) { error("Setters not supported on ByValue") }
        override var viewFormatCount: ULong
            get() = handle.useContents { this.viewFormatCount }
            set(value) { error("Setters not supported on ByValue") }
        override var viewFormats: NativeAddress?
            get() = handle.useContents { this.viewFormats?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureDescriptor {
        private val struct: webgpu.native.WGPUTextureDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var usage: ULong
            get() = struct.usage
            set(value) { struct.usage = value }
        override var dimension: WGPUTextureDimension
            get() = struct.dimension as WGPUTextureDimension
            set(value) { struct.dimension = value }
        override var size: WGPUExtent3D
            get() = WGPUExtent3D.ByReference(NativeAddress(struct.size.ptr))
            set(value) {
                val destBytes = struct.size.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUExtent3D>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var mipLevelCount: UInt
            get() = struct.mipLevelCount
            set(value) { struct.mipLevelCount = value }
        override var sampleCount: UInt
            get() = struct.sampleCount
            set(value) { struct.sampleCount = value }
        override var viewFormatCount: ULong
            get() = struct.viewFormatCount
            set(value) { struct.viewFormatCount = value }
        override var viewFormats: NativeAddress?
            get() = struct.viewFormats?.let(::NativeAddress)
            set(value) { struct.viewFormats = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUTextureDescriptor.toCValue(): CValue<webgpu.native.WGPUTextureDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.usage = this@toCValue.usage
    this.dimension = this@toCValue.dimension
    val dest_size = this.size.ptr.reinterpret<ByteVar>()
    val src_size = this@toCValue.size.handler.pointer.reinterpret<ByteVar>()
    val size_size = sizeOf<webgpu.native.WGPUExtent3D>().toLong()
    for (i in 0L until size_size) {
        dest_size[i.toInt()] = src_size[i.toInt()]
    }
    this.format = this@toCValue.format
    this.mipLevelCount = this@toCValue.mipLevelCount
    this.sampleCount = this@toCValue.sampleCount
    this.viewFormatCount = this@toCValue.viewFormatCount
    this.viewFormats = this@toCValue.viewFormats?.pointer?.takeIf { this@toCValue.viewFormats?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexBufferLayout =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUVertexBufferLayout>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexBufferLayout) -> Unit): ArrayHolder<WGPUVertexBufferLayout> {
            val byteSize = sizeOf<webgpu.native.WGPUVertexBufferLayout>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUVertexBufferLayout>) : WGPUVertexBufferLayout {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var stepMode: WGPUVertexStepMode
            get() = handle.useContents { this.stepMode as WGPUVertexStepMode }
            set(value) { error("Setters not supported on ByValue") }
        override var arrayStride: ULong
            get() = handle.useContents { this.arrayStride }
            set(value) { error("Setters not supported on ByValue") }
        override var attributeCount: ULong
            get() = handle.useContents { this.attributeCount }
            set(value) { error("Setters not supported on ByValue") }
        override var attributes: WGPUVertexAttribute?
            get() = handle.useContents { this.attributes?.let(::NativeAddress)?.let { WGPUVertexAttribute(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUVertexBufferLayout {
        private val struct: webgpu.native.WGPUVertexBufferLayout
            get() = handler.pointer.reinterpret<webgpu.native.WGPUVertexBufferLayout>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var stepMode: WGPUVertexStepMode
            get() = struct.stepMode as WGPUVertexStepMode
            set(value) { struct.stepMode = value }
        override var arrayStride: ULong
            get() = struct.arrayStride
            set(value) { struct.arrayStride = value }
        override var attributeCount: ULong
            get() = struct.attributeCount
            set(value) { struct.attributeCount = value }
        override var attributes: WGPUVertexAttribute?
            get() = struct.attributes?.let(::NativeAddress)?.let { WGPUVertexAttribute(it) }
            set(value) { struct.attributes = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUVertexBufferLayout.toCValue(): CValue<webgpu.native.WGPUVertexBufferLayout> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.stepMode = this@toCValue.stepMode
    this.arrayStride = this@toCValue.arrayStride
    this.attributeCount = this@toCValue.attributeCount
    this.attributes = this@toCValue.attributes?.handler?.pointer?.takeIf { this@toCValue.attributes?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupDescriptor) -> Unit): ArrayHolder<WGPUBindGroupDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupDescriptor>) : WGPUBindGroupDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var layout: WGPUBindGroupLayout?
            get() = handle.useContents { this.layout?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var entryCount: ULong
            get() = handle.useContents { this.entryCount }
            set(value) { error("Setters not supported on ByValue") }
        override var entries: WGPUBindGroupEntry?
            get() = handle.useContents { this.entries?.let(::NativeAddress)?.let { WGPUBindGroupEntry(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupDescriptor {
        private val struct: webgpu.native.WGPUBindGroupDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var layout: WGPUBindGroupLayout?
            get() = struct.layout?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
            set(value) { struct.layout = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var entryCount: ULong
            get() = struct.entryCount
            set(value) { struct.entryCount = value }
        override var entries: WGPUBindGroupEntry?
            get() = struct.entries?.let(::NativeAddress)?.let { WGPUBindGroupEntry(it) }
            set(value) { struct.entries = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUBindGroupDescriptor.toCValue(): CValue<webgpu.native.WGPUBindGroupDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.layout = this@toCValue.layout?.handler?.pointer?.takeIf { this@toCValue.layout?.handler?.rawValue != 0L }?.reinterpret()
    this.entryCount = this@toCValue.entryCount
    this.entries = this@toCValue.entries?.handler?.pointer?.takeIf { this@toCValue.entries?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUBindGroupLayoutDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var entryCount: ULong
    actual var entries: WGPUBindGroupLayoutEntry?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupLayoutDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutDescriptor) -> Unit): ArrayHolder<WGPUBindGroupLayoutDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupLayoutDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupLayoutDescriptor>) : WGPUBindGroupLayoutDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var entryCount: ULong
            get() = handle.useContents { this.entryCount }
            set(value) { error("Setters not supported on ByValue") }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = handle.useContents { this.entries?.let(::NativeAddress)?.let { WGPUBindGroupLayoutEntry(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutDescriptor {
        private val struct: webgpu.native.WGPUBindGroupLayoutDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupLayoutDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var entryCount: ULong
            get() = struct.entryCount
            set(value) { struct.entryCount = value }
        override var entries: WGPUBindGroupLayoutEntry?
            get() = struct.entries?.let(::NativeAddress)?.let { WGPUBindGroupLayoutEntry(it) }
            set(value) { struct.entries = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUBindGroupLayoutDescriptor.toCValue(): CValue<webgpu.native.WGPUBindGroupLayoutDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.entryCount = this@toCValue.entryCount
    this.entries = this@toCValue.entries?.handler?.pointer?.takeIf { this@toCValue.entries?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUColorTargetState {
    actual var nextInChain: WGPUChainedStruct?
    actual var format: WGPUTextureFormat
    actual var blend: WGPUBlendState?
    actual var writeMask: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUColorTargetState = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUColorTargetState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUColorTargetState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColorTargetState) -> Unit): ArrayHolder<WGPUColorTargetState> {
            val byteSize = sizeOf<webgpu.native.WGPUColorTargetState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUColorTargetState>) : WGPUColorTargetState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var blend: WGPUBlendState?
            get() = handle.useContents { this.blend?.let(::NativeAddress)?.let { WGPUBlendState(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var writeMask: ULong
            get() = handle.useContents { this.writeMask }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUColorTargetState {
        private val struct: webgpu.native.WGPUColorTargetState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUColorTargetState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var blend: WGPUBlendState?
            get() = struct.blend?.let(::NativeAddress)?.let { WGPUBlendState(it) }
            set(value) { struct.blend = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var writeMask: ULong
            get() = struct.writeMask
            set(value) { struct.writeMask = value }
    }
}

fun WGPUColorTargetState.toCValue(): CValue<webgpu.native.WGPUColorTargetState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.format = this@toCValue.format
    this.blend = this@toCValue.blend?.handler?.pointer?.takeIf { this@toCValue.blend?.handler?.rawValue != 0L }?.reinterpret()
    this.writeMask = this@toCValue.writeMask
}

actual interface WGPUComputePipelineDescriptor {
    actual var nextInChain: WGPUChainedStruct?
    actual var label: WGPUStringView
    actual var layout: WGPUPipelineLayout?
    actual var compute: WGPUComputeState
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUComputePipelineDescriptor = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUComputePipelineDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUComputePipelineDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePipelineDescriptor) -> Unit): ArrayHolder<WGPUComputePipelineDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUComputePipelineDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUComputePipelineDescriptor>) : WGPUComputePipelineDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var layout: WGPUPipelineLayout?
            get() = handle.useContents { this.layout?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var compute: WGPUComputeState
            get() = handle.useContents { WGPUComputeState.ByReference(NativeAddress(this.compute.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUComputePipelineDescriptor {
        private val struct: webgpu.native.WGPUComputePipelineDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUComputePipelineDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var layout: WGPUPipelineLayout?
            get() = struct.layout?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
            set(value) { struct.layout = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var compute: WGPUComputeState
            get() = WGPUComputeState.ByReference(NativeAddress(struct.compute.ptr))
            set(value) {
                val destBytes = struct.compute.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUComputeState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUComputePipelineDescriptor.toCValue(): CValue<webgpu.native.WGPUComputePipelineDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.layout = this@toCValue.layout?.handler?.pointer?.takeIf { this@toCValue.layout?.handler?.rawValue != 0L }?.reinterpret()
    val dest_compute = this.compute.ptr.reinterpret<ByteVar>()
    val src_compute = this@toCValue.compute.handler.pointer.reinterpret<ByteVar>()
    val size_compute = sizeOf<webgpu.native.WGPUComputeState>().toLong()
    for (i in 0L until size_compute) {
        dest_compute[i.toInt()] = src_compute[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUDeviceDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceDescriptor) -> Unit): ArrayHolder<WGPUDeviceDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUDeviceDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUDeviceDescriptor>) : WGPUDeviceDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredFeatureCount: ULong
            get() = handle.useContents { this.requiredFeatureCount }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredFeatures: NativeAddress?
            get() = handle.useContents { this.requiredFeatures?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var requiredLimits: WGPULimits?
            get() = handle.useContents { this.requiredLimits?.let(::NativeAddress)?.let { WGPULimits(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var defaultQueue: WGPUQueueDescriptor
            get() = handle.useContents { WGPUQueueDescriptor.ByReference(NativeAddress(this.defaultQueue.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() = handle.useContents { WGPUDeviceLostCallbackInfo.ByReference(NativeAddress(this.deviceLostCallbackInfo.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() = handle.useContents { WGPUUncapturedErrorCallbackInfo.ByReference(NativeAddress(this.uncapturedErrorCallbackInfo.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUDeviceDescriptor {
        private val struct: webgpu.native.WGPUDeviceDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUDeviceDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var requiredFeatureCount: ULong
            get() = struct.requiredFeatureCount
            set(value) { struct.requiredFeatureCount = value }
        override var requiredFeatures: NativeAddress?
            get() = struct.requiredFeatures?.let(::NativeAddress)
            set(value) { struct.requiredFeatures = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var requiredLimits: WGPULimits?
            get() = struct.requiredLimits?.let(::NativeAddress)?.let { WGPULimits(it) }
            set(value) { struct.requiredLimits = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var defaultQueue: WGPUQueueDescriptor
            get() = WGPUQueueDescriptor.ByReference(NativeAddress(struct.defaultQueue.ptr))
            set(value) {
                val destBytes = struct.defaultQueue.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUQueueDescriptor>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
            get() = WGPUDeviceLostCallbackInfo.ByReference(NativeAddress(struct.deviceLostCallbackInfo.ptr))
            set(value) {
                val destBytes = struct.deviceLostCallbackInfo.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUDeviceLostCallbackInfo>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
            get() = WGPUUncapturedErrorCallbackInfo.ByReference(NativeAddress(struct.uncapturedErrorCallbackInfo.ptr))
            set(value) {
                val destBytes = struct.uncapturedErrorCallbackInfo.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUUncapturedErrorCallbackInfo>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUDeviceDescriptor.toCValue(): CValue<webgpu.native.WGPUDeviceDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.requiredFeatureCount = this@toCValue.requiredFeatureCount
    this.requiredFeatures = this@toCValue.requiredFeatures?.pointer?.takeIf { this@toCValue.requiredFeatures?.rawValue != 0L }?.reinterpret()
    this.requiredLimits = this@toCValue.requiredLimits?.handler?.pointer?.takeIf { this@toCValue.requiredLimits?.handler?.rawValue != 0L }?.reinterpret()
    val dest_defaultQueue = this.defaultQueue.ptr.reinterpret<ByteVar>()
    val src_defaultQueue = this@toCValue.defaultQueue.handler.pointer.reinterpret<ByteVar>()
    val size_defaultQueue = sizeOf<webgpu.native.WGPUQueueDescriptor>().toLong()
    for (i in 0L until size_defaultQueue) {
        dest_defaultQueue[i.toInt()] = src_defaultQueue[i.toInt()]
    }
    val dest_deviceLostCallbackInfo = this.deviceLostCallbackInfo.ptr.reinterpret<ByteVar>()
    val src_deviceLostCallbackInfo = this@toCValue.deviceLostCallbackInfo.handler.pointer.reinterpret<ByteVar>()
    val size_deviceLostCallbackInfo = sizeOf<webgpu.native.WGPUDeviceLostCallbackInfo>().toLong()
    for (i in 0L until size_deviceLostCallbackInfo) {
        dest_deviceLostCallbackInfo[i.toInt()] = src_deviceLostCallbackInfo[i.toInt()]
    }
    val dest_uncapturedErrorCallbackInfo = this.uncapturedErrorCallbackInfo.ptr.reinterpret<ByteVar>()
    val src_uncapturedErrorCallbackInfo = this@toCValue.uncapturedErrorCallbackInfo.handler.pointer.reinterpret<ByteVar>()
    val size_uncapturedErrorCallbackInfo = sizeOf<webgpu.native.WGPUUncapturedErrorCallbackInfo>().toLong()
    for (i in 0L until size_uncapturedErrorCallbackInfo) {
        dest_uncapturedErrorCallbackInfo[i.toInt()] = src_uncapturedErrorCallbackInfo[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPassDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderPassDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDescriptor) -> Unit): ArrayHolder<WGPURenderPassDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPURenderPassDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderPassDescriptor>) : WGPURenderPassDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var colorAttachmentCount: ULong
            get() = handle.useContents { this.colorAttachmentCount }
            set(value) { error("Setters not supported on ByValue") }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = handle.useContents { this.colorAttachments?.let(::NativeAddress)?.let { WGPURenderPassColorAttachment(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = handle.useContents { this.depthStencilAttachment?.let(::NativeAddress)?.let { WGPURenderPassDepthStencilAttachment(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = handle.useContents { this.occlusionQuerySet?.let(::NativeAddress)?.let { WGPUQuerySet(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = handle.useContents { this.timestampWrites?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderPassDescriptor {
        private val struct: webgpu.native.WGPURenderPassDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderPassDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var colorAttachmentCount: ULong
            get() = struct.colorAttachmentCount
            set(value) { struct.colorAttachmentCount = value }
        override var colorAttachments: WGPURenderPassColorAttachment?
            get() = struct.colorAttachments?.let(::NativeAddress)?.let { WGPURenderPassColorAttachment(it) }
            set(value) { struct.colorAttachments = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
            get() = struct.depthStencilAttachment?.let(::NativeAddress)?.let { WGPURenderPassDepthStencilAttachment(it) }
            set(value) { struct.depthStencilAttachment = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var occlusionQuerySet: WGPUQuerySet?
            get() = struct.occlusionQuerySet?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
            set(value) { struct.occlusionQuerySet = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var timestampWrites: WGPUPassTimestampWrites?
            get() = struct.timestampWrites?.let(::NativeAddress)?.let { WGPUPassTimestampWrites(it) }
            set(value) { struct.timestampWrites = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPURenderPassDescriptor.toCValue(): CValue<webgpu.native.WGPURenderPassDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.colorAttachmentCount = this@toCValue.colorAttachmentCount
    this.colorAttachments = this@toCValue.colorAttachments?.handler?.pointer?.takeIf { this@toCValue.colorAttachments?.handler?.rawValue != 0L }?.reinterpret()
    this.depthStencilAttachment = this@toCValue.depthStencilAttachment?.handler?.pointer?.takeIf { this@toCValue.depthStencilAttachment?.handler?.rawValue != 0L }?.reinterpret()
    this.occlusionQuerySet = this@toCValue.occlusionQuerySet?.handler?.pointer?.takeIf { this@toCValue.occlusionQuerySet?.handler?.rawValue != 0L }?.reinterpret()
    this.timestampWrites = this@toCValue.timestampWrites?.handler?.pointer?.takeIf { this@toCValue.timestampWrites?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUTextureViewDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUTextureViewDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureViewDescriptor) -> Unit): ArrayHolder<WGPUTextureViewDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPUTextureViewDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUTextureViewDescriptor>) : WGPUTextureViewDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var format: WGPUTextureFormat
            get() = handle.useContents { this.format as WGPUTextureFormat }
            set(value) { error("Setters not supported on ByValue") }
        override var dimension: WGPUTextureViewDimension
            get() = handle.useContents { this.dimension as WGPUTextureViewDimension }
            set(value) { error("Setters not supported on ByValue") }
        override var baseMipLevel: UInt
            get() = handle.useContents { this.baseMipLevel }
            set(value) { error("Setters not supported on ByValue") }
        override var mipLevelCount: UInt
            get() = handle.useContents { this.mipLevelCount }
            set(value) { error("Setters not supported on ByValue") }
        override var baseArrayLayer: UInt
            get() = handle.useContents { this.baseArrayLayer }
            set(value) { error("Setters not supported on ByValue") }
        override var arrayLayerCount: UInt
            get() = handle.useContents { this.arrayLayerCount }
            set(value) { error("Setters not supported on ByValue") }
        override var aspect: WGPUTextureAspect
            get() = handle.useContents { this.aspect as WGPUTextureAspect }
            set(value) { error("Setters not supported on ByValue") }
        override var usage: ULong
            get() = handle.useContents { this.usage }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUTextureViewDescriptor {
        private val struct: webgpu.native.WGPUTextureViewDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPUTextureViewDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var format: WGPUTextureFormat
            get() = struct.format as WGPUTextureFormat
            set(value) { struct.format = value }
        override var dimension: WGPUTextureViewDimension
            get() = struct.dimension as WGPUTextureViewDimension
            set(value) { struct.dimension = value }
        override var baseMipLevel: UInt
            get() = struct.baseMipLevel
            set(value) { struct.baseMipLevel = value }
        override var mipLevelCount: UInt
            get() = struct.mipLevelCount
            set(value) { struct.mipLevelCount = value }
        override var baseArrayLayer: UInt
            get() = struct.baseArrayLayer
            set(value) { struct.baseArrayLayer = value }
        override var arrayLayerCount: UInt
            get() = struct.arrayLayerCount
            set(value) { struct.arrayLayerCount = value }
        override var aspect: WGPUTextureAspect
            get() = struct.aspect as WGPUTextureAspect
            set(value) { struct.aspect = value }
        override var usage: ULong
            get() = struct.usage
            set(value) { struct.usage = value }
    }
}

fun WGPUTextureViewDescriptor.toCValue(): CValue<webgpu.native.WGPUTextureViewDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.format = this@toCValue.format
    this.dimension = this@toCValue.dimension
    this.baseMipLevel = this@toCValue.baseMipLevel
    this.mipLevelCount = this@toCValue.mipLevelCount
    this.baseArrayLayer = this@toCValue.baseArrayLayer
    this.arrayLayerCount = this@toCValue.arrayLayerCount
    this.aspect = this@toCValue.aspect
    this.usage = this@toCValue.usage
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
        actual fun allocate(allocator: MemoryAllocator): WGPUVertexState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUVertexState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexState) -> Unit): ArrayHolder<WGPUVertexState> {
            val byteSize = sizeOf<webgpu.native.WGPUVertexState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUVertexState>) : WGPUVertexState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var module: WGPUShaderModule?
            get() = handle.useContents { this.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var entryPoint: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.entryPoint.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var constantCount: ULong
            get() = handle.useContents { this.constantCount }
            set(value) { error("Setters not supported on ByValue") }
        override var constants: WGPUConstantEntry?
            get() = handle.useContents { this.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var bufferCount: ULong
            get() = handle.useContents { this.bufferCount }
            set(value) { error("Setters not supported on ByValue") }
        override var buffers: WGPUVertexBufferLayout?
            get() = handle.useContents { this.buffers?.let(::NativeAddress)?.let { WGPUVertexBufferLayout(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUVertexState {
        private val struct: webgpu.native.WGPUVertexState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUVertexState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var module: WGPUShaderModule?
            get() = struct.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) { struct.module = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.entryPoint.ptr))
            set(value) {
                val destBytes = struct.entryPoint.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var constantCount: ULong
            get() = struct.constantCount
            set(value) { struct.constantCount = value }
        override var constants: WGPUConstantEntry?
            get() = struct.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) { struct.constants = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var bufferCount: ULong
            get() = struct.bufferCount
            set(value) { struct.bufferCount = value }
        override var buffers: WGPUVertexBufferLayout?
            get() = struct.buffers?.let(::NativeAddress)?.let { WGPUVertexBufferLayout(it) }
            set(value) { struct.buffers = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUVertexState.toCValue(): CValue<webgpu.native.WGPUVertexState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.module = this@toCValue.module?.handler?.pointer?.takeIf { this@toCValue.module?.handler?.rawValue != 0L }?.reinterpret()
    val dest_entryPoint = this.entryPoint.ptr.reinterpret<ByteVar>()
    val src_entryPoint = this@toCValue.entryPoint.handler.pointer.reinterpret<ByteVar>()
    val size_entryPoint = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_entryPoint) {
        dest_entryPoint[i.toInt()] = src_entryPoint[i.toInt()]
    }
    this.constantCount = this@toCValue.constantCount
    this.constants = this@toCValue.constants?.handler?.pointer?.takeIf { this@toCValue.constants?.handler?.rawValue != 0L }?.reinterpret()
    this.bufferCount = this@toCValue.bufferCount
    this.buffers = this@toCValue.buffers?.handler?.pointer?.takeIf { this@toCValue.buffers?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUFragmentState =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUFragmentState>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFragmentState) -> Unit): ArrayHolder<WGPUFragmentState> {
            val byteSize = sizeOf<webgpu.native.WGPUFragmentState>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUFragmentState>) : WGPUFragmentState {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var module: WGPUShaderModule?
            get() = handle.useContents { this.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var entryPoint: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.entryPoint.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var constantCount: ULong
            get() = handle.useContents { this.constantCount }
            set(value) { error("Setters not supported on ByValue") }
        override var constants: WGPUConstantEntry?
            get() = handle.useContents { this.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var targetCount: ULong
            get() = handle.useContents { this.targetCount }
            set(value) { error("Setters not supported on ByValue") }
        override var targets: WGPUColorTargetState?
            get() = handle.useContents { this.targets?.let(::NativeAddress)?.let { WGPUColorTargetState(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUFragmentState {
        private val struct: webgpu.native.WGPUFragmentState
            get() = handler.pointer.reinterpret<webgpu.native.WGPUFragmentState>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var module: WGPUShaderModule?
            get() = struct.module?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
            set(value) { struct.module = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var entryPoint: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.entryPoint.ptr))
            set(value) {
                val destBytes = struct.entryPoint.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var constantCount: ULong
            get() = struct.constantCount
            set(value) { struct.constantCount = value }
        override var constants: WGPUConstantEntry?
            get() = struct.constants?.let(::NativeAddress)?.let { WGPUConstantEntry(it) }
            set(value) { struct.constants = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var targetCount: ULong
            get() = struct.targetCount
            set(value) { struct.targetCount = value }
        override var targets: WGPUColorTargetState?
            get() = struct.targets?.let(::NativeAddress)?.let { WGPUColorTargetState(it) }
            set(value) { struct.targets = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUFragmentState.toCValue(): CValue<webgpu.native.WGPUFragmentState> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.module = this@toCValue.module?.handler?.pointer?.takeIf { this@toCValue.module?.handler?.rawValue != 0L }?.reinterpret()
    val dest_entryPoint = this.entryPoint.ptr.reinterpret<ByteVar>()
    val src_entryPoint = this@toCValue.entryPoint.handler.pointer.reinterpret<ByteVar>()
    val size_entryPoint = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_entryPoint) {
        dest_entryPoint[i.toInt()] = src_entryPoint[i.toInt()]
    }
    this.constantCount = this@toCValue.constantCount
    this.constants = this@toCValue.constants?.handler?.pointer?.takeIf { this@toCValue.constants?.handler?.rawValue != 0L }?.reinterpret()
    this.targetCount = this@toCValue.targetCount
    this.targets = this@toCValue.targets?.handler?.pointer?.takeIf { this@toCValue.targets?.handler?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPURenderPipelineDescriptor =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURenderPipelineDescriptor>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPipelineDescriptor) -> Unit): ArrayHolder<WGPURenderPipelineDescriptor> {
            val byteSize = sizeOf<webgpu.native.WGPURenderPipelineDescriptor>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURenderPipelineDescriptor>) : WGPURenderPipelineDescriptor {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var layout: WGPUPipelineLayout?
            get() = handle.useContents { this.layout?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var vertex: WGPUVertexState
            get() = handle.useContents { WGPUVertexState.ByReference(NativeAddress(this.vertex.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var primitive: WGPUPrimitiveState
            get() = handle.useContents { WGPUPrimitiveState.ByReference(NativeAddress(this.primitive.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var depthStencil: WGPUDepthStencilState?
            get() = handle.useContents { this.depthStencil?.let(::NativeAddress)?.let { WGPUDepthStencilState(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var multisample: WGPUMultisampleState
            get() = handle.useContents { WGPUMultisampleState.ByReference(NativeAddress(this.multisample.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var fragment: WGPUFragmentState?
            get() = handle.useContents { this.fragment?.let(::NativeAddress)?.let { WGPUFragmentState(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURenderPipelineDescriptor {
        private val struct: webgpu.native.WGPURenderPipelineDescriptor
            get() = handler.pointer.reinterpret<webgpu.native.WGPURenderPipelineDescriptor>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var layout: WGPUPipelineLayout?
            get() = struct.layout?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
            set(value) { struct.layout = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var vertex: WGPUVertexState
            get() = WGPUVertexState.ByReference(NativeAddress(struct.vertex.ptr))
            set(value) {
                val destBytes = struct.vertex.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUVertexState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var primitive: WGPUPrimitiveState
            get() = WGPUPrimitiveState.ByReference(NativeAddress(struct.primitive.ptr))
            set(value) {
                val destBytes = struct.primitive.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUPrimitiveState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var depthStencil: WGPUDepthStencilState?
            get() = struct.depthStencil?.let(::NativeAddress)?.let { WGPUDepthStencilState(it) }
            set(value) { struct.depthStencil = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var multisample: WGPUMultisampleState
            get() = WGPUMultisampleState.ByReference(NativeAddress(struct.multisample.ptr))
            set(value) {
                val destBytes = struct.multisample.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUMultisampleState>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var fragment: WGPUFragmentState?
            get() = struct.fragment?.let(::NativeAddress)?.let { WGPUFragmentState(it) }
            set(value) { struct.fragment = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPURenderPipelineDescriptor.toCValue(): CValue<webgpu.native.WGPURenderPipelineDescriptor> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.layout = this@toCValue.layout?.handler?.pointer?.takeIf { this@toCValue.layout?.handler?.rawValue != 0L }?.reinterpret()
    val dest_vertex = this.vertex.ptr.reinterpret<ByteVar>()
    val src_vertex = this@toCValue.vertex.handler.pointer.reinterpret<ByteVar>()
    val size_vertex = sizeOf<webgpu.native.WGPUVertexState>().toLong()
    for (i in 0L until size_vertex) {
        dest_vertex[i.toInt()] = src_vertex[i.toInt()]
    }
    val dest_primitive = this.primitive.ptr.reinterpret<ByteVar>()
    val src_primitive = this@toCValue.primitive.handler.pointer.reinterpret<ByteVar>()
    val size_primitive = sizeOf<webgpu.native.WGPUPrimitiveState>().toLong()
    for (i in 0L until size_primitive) {
        dest_primitive[i.toInt()] = src_primitive[i.toInt()]
    }
    this.depthStencil = this@toCValue.depthStencil?.handler?.pointer?.takeIf { this@toCValue.depthStencil?.handler?.rawValue != 0L }?.reinterpret()
    val dest_multisample = this.multisample.ptr.reinterpret<ByteVar>()
    val src_multisample = this@toCValue.multisample.handler.pointer.reinterpret<ByteVar>()
    val size_multisample = sizeOf<webgpu.native.WGPUMultisampleState>().toLong()
    for (i in 0L until size_multisample) {
        dest_multisample[i.toInt()] = src_multisample[i.toInt()]
    }
    this.fragment = this@toCValue.fragment?.handler?.pointer?.takeIf { this@toCValue.fragment?.handler?.rawValue != 0L }?.reinterpret()
}

actual fun wgpuCreateInstance(descriptor: WGPUInstanceDescriptor?): WGPUInstance? {
    return webgpu.native.wgpuCreateInstance(descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUInstance(it) }
}

actual fun wgpuGetInstanceFeatures(features: WGPUSupportedInstanceFeatures?): Unit {
    webgpu.native.wgpuGetInstanceFeatures(features?.handler?.pointer?.takeIf { features.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuGetInstanceLimits(limits: WGPUInstanceLimits?): WGPUStatus {
    return webgpu.native.wgpuGetInstanceLimits(limits?.handler?.pointer?.takeIf { limits.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuHasInstanceFeature(feature: WGPUInstanceFeatureName): UInt {
    return webgpu.native.wgpuHasInstanceFeature(feature)
}

actual fun wgpuGetProcAddress(procName: WGPUStringView): NativeAddress? {
    return webgpu.native.wgpuGetProcAddress(procName.toCValue())?.let(::NativeAddress)
}

actual fun wgpuAdapterGetFeatures(adapter: WGPUAdapter?, features: WGPUSupportedFeatures?): Unit {
    webgpu.native.wgpuAdapterGetFeatures(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), features?.handler?.pointer?.takeIf { features.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuAdapterGetInfo(adapter: WGPUAdapter?, info: WGPUAdapterInfo?): WGPUStatus {
    return webgpu.native.wgpuAdapterGetInfo(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), info?.handler?.pointer?.takeIf { info.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuAdapterGetLimits(adapter: WGPUAdapter?, limits: WGPULimits?): WGPUStatus {
    return webgpu.native.wgpuAdapterGetLimits(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), limits?.handler?.pointer?.takeIf { limits.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuAdapterHasFeature(adapter: WGPUAdapter?, feature: WGPUFeatureName): UInt {
    return webgpu.native.wgpuAdapterHasFeature(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), feature)
}

actual fun wgpuAdapterRequestDevice(adapter: WGPUAdapter?, descriptor: WGPUDeviceDescriptor?, callbackInfo: WGPURequestDeviceCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuAdapterRequestDevice(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuAdapterAddRef(adapter: WGPUAdapter?): Unit {
    webgpu.native.wgpuAdapterAddRef(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuAdapterRelease(adapter: WGPUAdapter?): Unit {
    webgpu.native.wgpuAdapterRelease(adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuAdapterInfoFreeMembers(adapterInfo: WGPUAdapterInfo): Unit {
    webgpu.native.wgpuAdapterInfoFreeMembers(adapterInfo.toCValue())
    return
}

actual fun wgpuBindGroupSetLabel(bindGroup: WGPUBindGroup?, label: WGPUStringView): Unit {
    webgpu.native.wgpuBindGroupSetLabel(bindGroup?.handler?.pointer?.takeIf { bindGroup.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuBindGroupAddRef(bindGroup: WGPUBindGroup?): Unit {
    webgpu.native.wgpuBindGroupAddRef(bindGroup?.handler?.pointer?.takeIf { bindGroup.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBindGroupRelease(bindGroup: WGPUBindGroup?): Unit {
    webgpu.native.wgpuBindGroupRelease(bindGroup?.handler?.pointer?.takeIf { bindGroup.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: WGPUBindGroupLayout?, label: WGPUStringView): Unit {
    webgpu.native.wgpuBindGroupLayoutSetLabel(bindGroupLayout?.handler?.pointer?.takeIf { bindGroupLayout.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuBindGroupLayoutAddRef(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    webgpu.native.wgpuBindGroupLayoutAddRef(bindGroupLayout?.handler?.pointer?.takeIf { bindGroupLayout.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBindGroupLayoutRelease(bindGroupLayout: WGPUBindGroupLayout?): Unit {
    webgpu.native.wgpuBindGroupLayoutRelease(bindGroupLayout?.handler?.pointer?.takeIf { bindGroupLayout.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBufferDestroy(buffer: WGPUBuffer?): Unit {
    webgpu.native.wgpuBufferDestroy(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBufferGetConstMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return webgpu.native.wgpuBufferGetConstMappedRange(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, size)?.let(::NativeAddress)
}

actual fun wgpuBufferGetMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress? {
    return webgpu.native.wgpuBufferGetMappedRange(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, size)?.let(::NativeAddress)
}

actual fun wgpuBufferGetMapState(buffer: WGPUBuffer?): WGPUBufferMapState {
    return webgpu.native.wgpuBufferGetMapState(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuBufferGetSize(buffer: WGPUBuffer?): ULong {
    return webgpu.native.wgpuBufferGetSize(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuBufferGetUsage(buffer: WGPUBuffer?): ULong {
    return webgpu.native.wgpuBufferGetUsage(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuBufferMapAsync(buffer: WGPUBuffer?, mode: ULong, offset: ULong, size: ULong, callbackInfo: WGPUBufferMapCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuBufferMapAsync(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), mode, offset, size, callbackInfo.toCValue()))
}

actual fun wgpuBufferReadMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return webgpu.native.wgpuBufferReadMappedRange(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
}

actual fun wgpuBufferSetLabel(buffer: WGPUBuffer?, label: WGPUStringView): Unit {
    webgpu.native.wgpuBufferSetLabel(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuBufferUnmap(buffer: WGPUBuffer?): Unit {
    webgpu.native.wgpuBufferUnmap(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBufferWriteMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus {
    return webgpu.native.wgpuBufferWriteMappedRange(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
}

actual fun wgpuBufferAddRef(buffer: WGPUBuffer?): Unit {
    webgpu.native.wgpuBufferAddRef(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuBufferRelease(buffer: WGPUBuffer?): Unit {
    webgpu.native.wgpuBufferRelease(buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandBufferSetLabel(commandBuffer: WGPUCommandBuffer?, label: WGPUStringView): Unit {
    webgpu.native.wgpuCommandBufferSetLabel(commandBuffer?.handler?.pointer?.takeIf { commandBuffer.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuCommandBufferAddRef(commandBuffer: WGPUCommandBuffer?): Unit {
    webgpu.native.wgpuCommandBufferAddRef(commandBuffer?.handler?.pointer?.takeIf { commandBuffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandBufferRelease(commandBuffer: WGPUCommandBuffer?): Unit {
    webgpu.native.wgpuCommandBufferRelease(commandBuffer?.handler?.pointer?.takeIf { commandBuffer.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderBeginComputePass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUComputePassDescriptor?): WGPUComputePassEncoder? {
    return webgpu.native.wgpuCommandEncoderBeginComputePass(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUComputePassEncoder(it) }
}

actual fun wgpuCommandEncoderBeginRenderPass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPURenderPassDescriptor?): WGPURenderPassEncoder? {
    return webgpu.native.wgpuCommandEncoderBeginRenderPass(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPURenderPassEncoder(it) }
}

actual fun wgpuCommandEncoderClearBuffer(commandEncoder: WGPUCommandEncoder?, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    webgpu.native.wgpuCommandEncoderClearBuffer(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, size)
    return
}

actual fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUBuffer?, sourceOffset: ULong, destination: WGPUBuffer?, destinationOffset: ULong, size: ULong): Unit {
    webgpu.native.wgpuCommandEncoderCopyBufferToBuffer(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), source?.handler?.pointer?.takeIf { source.handler.rawValue != 0L }?.reinterpret(), sourceOffset, destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), destinationOffset, size)
    return
}

actual fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyBufferInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    webgpu.native.wgpuCommandEncoderCopyBufferToTexture(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), source?.handler?.pointer?.takeIf { source.handler.rawValue != 0L }?.reinterpret(), destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), copySize?.handler?.pointer?.takeIf { copySize.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyBufferInfo?, copySize: WGPUExtent3D?): Unit {
    webgpu.native.wgpuCommandEncoderCopyTextureToBuffer(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), source?.handler?.pointer?.takeIf { source.handler.rawValue != 0L }?.reinterpret(), destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), copySize?.handler?.pointer?.takeIf { copySize.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit {
    webgpu.native.wgpuCommandEncoderCopyTextureToTexture(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), source?.handler?.pointer?.takeIf { source.handler.rawValue != 0L }?.reinterpret(), destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), copySize?.handler?.pointer?.takeIf { copySize.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderFinish(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUCommandBufferDescriptor?): WGPUCommandBuffer? {
    return webgpu.native.wgpuCommandEncoderFinish(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUCommandBuffer(it) }
}

actual fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: WGPUCommandEncoder?, markerLabel: WGPUStringView): Unit {
    webgpu.native.wgpuCommandEncoderInsertDebugMarker(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), markerLabel.toCValue())
    return
}

actual fun wgpuCommandEncoderPopDebugGroup(commandEncoder: WGPUCommandEncoder?): Unit {
    webgpu.native.wgpuCommandEncoderPopDebugGroup(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderPushDebugGroup(commandEncoder: WGPUCommandEncoder?, groupLabel: WGPUStringView): Unit {
    webgpu.native.wgpuCommandEncoderPushDebugGroup(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), groupLabel.toCValue())
    return
}

actual fun wgpuCommandEncoderResolveQuerySet(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, firstQuery: UInt, queryCount: UInt, destination: WGPUBuffer?, destinationOffset: ULong): Unit {
    webgpu.native.wgpuCommandEncoderResolveQuerySet(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), firstQuery, queryCount, destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), destinationOffset)
    return
}

actual fun wgpuCommandEncoderSetLabel(commandEncoder: WGPUCommandEncoder?, label: WGPUStringView): Unit {
    webgpu.native.wgpuCommandEncoderSetLabel(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuCommandEncoderWriteTimestamp(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    webgpu.native.wgpuCommandEncoderWriteTimestamp(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuCommandEncoderAddRef(commandEncoder: WGPUCommandEncoder?): Unit {
    webgpu.native.wgpuCommandEncoderAddRef(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderRelease(commandEncoder: WGPUCommandEncoder?): Unit {
    webgpu.native.wgpuCommandEncoderRelease(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: WGPUComputePassEncoder?, workgroupCountX: UInt, workgroupCountY: UInt, workgroupCountZ: UInt): Unit {
    webgpu.native.wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), workgroupCountX, workgroupCountY, workgroupCountZ)
    return
}

actual fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: WGPUComputePassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    webgpu.native.wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), indirectBuffer?.handler?.pointer?.takeIf { indirectBuffer.handler.rawValue != 0L }?.reinterpret(), indirectOffset)
    return
}

actual fun wgpuComputePassEncoderEnd(computePassEncoder: WGPUComputePassEncoder?): Unit {
    webgpu.native.wgpuComputePassEncoderEnd(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: WGPUComputePassEncoder?, markerLabel: WGPUStringView): Unit {
    webgpu.native.wgpuComputePassEncoderInsertDebugMarker(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), markerLabel.toCValue())
    return
}

actual fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: WGPUComputePassEncoder?): Unit {
    webgpu.native.wgpuComputePassEncoderPopDebugGroup(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: WGPUComputePassEncoder?, groupLabel: WGPUStringView): Unit {
    webgpu.native.wgpuComputePassEncoderPushDebugGroup(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), groupLabel.toCValue())
    return
}

actual fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: WGPUComputePassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    webgpu.native.wgpuComputePassEncoderSetBindGroup(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), groupIndex, group?.handler?.pointer?.takeIf { group.handler.rawValue != 0L }?.reinterpret(), dynamicOffsetCount, dynamicOffsets?.pointer?.takeIf { dynamicOffsets.rawValue != 0L }?.reinterpret<UIntVar>())
    return
}

actual fun wgpuComputePassEncoderSetImmediates(computePassEncoder: WGPUComputePassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    webgpu.native.wgpuComputePassEncoderSetImmediates(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), offset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
    return
}

actual fun wgpuComputePassEncoderSetLabel(computePassEncoder: WGPUComputePassEncoder?, label: WGPUStringView): Unit {
    webgpu.native.wgpuComputePassEncoderSetLabel(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuComputePassEncoderSetPipeline(computePassEncoder: WGPUComputePassEncoder?, pipeline: WGPUComputePipeline?): Unit {
    webgpu.native.wgpuComputePassEncoderSetPipeline(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), pipeline?.handler?.pointer?.takeIf { pipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderAddRef(computePassEncoder: WGPUComputePassEncoder?): Unit {
    webgpu.native.wgpuComputePassEncoderAddRef(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderRelease(computePassEncoder: WGPUComputePassEncoder?): Unit {
    webgpu.native.wgpuComputePassEncoderRelease(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePipelineGetBindGroupLayout(computePipeline: WGPUComputePipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return webgpu.native.wgpuComputePipelineGetBindGroupLayout(computePipeline?.handler?.pointer?.takeIf { computePipeline.handler.rawValue != 0L }?.reinterpret(), groupIndex)?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuComputePipelineSetLabel(computePipeline: WGPUComputePipeline?, label: WGPUStringView): Unit {
    webgpu.native.wgpuComputePipelineSetLabel(computePipeline?.handler?.pointer?.takeIf { computePipeline.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuComputePipelineAddRef(computePipeline: WGPUComputePipeline?): Unit {
    webgpu.native.wgpuComputePipelineAddRef(computePipeline?.handler?.pointer?.takeIf { computePipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePipelineRelease(computePipeline: WGPUComputePipeline?): Unit {
    webgpu.native.wgpuComputePipelineRelease(computePipeline?.handler?.pointer?.takeIf { computePipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuDeviceCreateBindGroup(device: WGPUDevice?, descriptor: WGPUBindGroupDescriptor?): WGPUBindGroup? {
    return webgpu.native.wgpuDeviceCreateBindGroup(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUBindGroup(it) }
}

actual fun wgpuDeviceCreateBindGroupLayout(device: WGPUDevice?, descriptor: WGPUBindGroupLayoutDescriptor?): WGPUBindGroupLayout? {
    return webgpu.native.wgpuDeviceCreateBindGroupLayout(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuDeviceCreateBuffer(device: WGPUDevice?, descriptor: WGPUBufferDescriptor?): WGPUBuffer? {
    return webgpu.native.wgpuDeviceCreateBuffer(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUBuffer(it) }
}

actual fun wgpuDeviceCreateCommandEncoder(device: WGPUDevice?, descriptor: WGPUCommandEncoderDescriptor?): WGPUCommandEncoder? {
    return webgpu.native.wgpuDeviceCreateCommandEncoder(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUCommandEncoder(it) }
}

actual fun wgpuDeviceCreateComputePipeline(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?): WGPUComputePipeline? {
    return webgpu.native.wgpuDeviceCreateComputePipeline(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUComputePipeline(it) }
}

actual fun wgpuDeviceCreateComputePipelineAsync(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?, callbackInfo: WGPUCreateComputePipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuDeviceCreateComputePipelineAsync(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuDeviceCreatePipelineLayout(device: WGPUDevice?, descriptor: WGPUPipelineLayoutDescriptor?): WGPUPipelineLayout? {
    return webgpu.native.wgpuDeviceCreatePipelineLayout(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUPipelineLayout(it) }
}

actual fun wgpuDeviceCreateQuerySet(device: WGPUDevice?, descriptor: WGPUQuerySetDescriptor?): WGPUQuerySet? {
    return webgpu.native.wgpuDeviceCreateQuerySet(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUQuerySet(it) }
}

actual fun wgpuDeviceCreateRenderBundleEncoder(device: WGPUDevice?, descriptor: WGPURenderBundleEncoderDescriptor?): WGPURenderBundleEncoder? {
    return webgpu.native.wgpuDeviceCreateRenderBundleEncoder(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPURenderBundleEncoder(it) }
}

actual fun wgpuDeviceCreateRenderPipeline(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?): WGPURenderPipeline? {
    return webgpu.native.wgpuDeviceCreateRenderPipeline(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPURenderPipeline(it) }
}

actual fun wgpuDeviceCreateRenderPipelineAsync(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?, callbackInfo: WGPUCreateRenderPipelineAsyncCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuDeviceCreateRenderPipelineAsync(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuDeviceCreateSampler(device: WGPUDevice?, descriptor: WGPUSamplerDescriptor?): WGPUSampler? {
    return webgpu.native.wgpuDeviceCreateSampler(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUSampler(it) }
}

actual fun wgpuDeviceCreateShaderModule(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?): WGPUShaderModule? {
    return webgpu.native.wgpuDeviceCreateShaderModule(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

actual fun wgpuDeviceCreateTexture(device: WGPUDevice?, descriptor: WGPUTextureDescriptor?): WGPUTexture? {
    return webgpu.native.wgpuDeviceCreateTexture(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUTexture(it) }
}

actual fun wgpuDeviceDestroy(device: WGPUDevice?): Unit {
    webgpu.native.wgpuDeviceDestroy(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuDeviceGetAdapterInfo(device: WGPUDevice?, adapterInfo: WGPUAdapterInfo?): WGPUStatus {
    return webgpu.native.wgpuDeviceGetAdapterInfo(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), adapterInfo?.handler?.pointer?.takeIf { adapterInfo.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuDeviceGetFeatures(device: WGPUDevice?, features: WGPUSupportedFeatures?): Unit {
    webgpu.native.wgpuDeviceGetFeatures(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), features?.handler?.pointer?.takeIf { features.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuDeviceGetLimits(device: WGPUDevice?, limits: WGPULimits?): WGPUStatus {
    return webgpu.native.wgpuDeviceGetLimits(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), limits?.handler?.pointer?.takeIf { limits.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuDeviceGetLostFuture(device: WGPUDevice?): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuDeviceGetLostFuture(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret()))
}

actual fun wgpuDeviceGetQueue(device: WGPUDevice?): WGPUQueue? {
    return webgpu.native.wgpuDeviceGetQueue(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUQueue(it) }
}

actual fun wgpuDeviceHasFeature(device: WGPUDevice?, feature: WGPUFeatureName): UInt {
    return webgpu.native.wgpuDeviceHasFeature(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), feature)
}

actual fun wgpuDevicePopErrorScope(device: WGPUDevice?, callbackInfo: WGPUPopErrorScopeCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuDevicePopErrorScope(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuDevicePushErrorScope(device: WGPUDevice?, filter: WGPUErrorFilter): Unit {
    webgpu.native.wgpuDevicePushErrorScope(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), filter)
    return
}

actual fun wgpuDeviceSetLabel(device: WGPUDevice?, label: WGPUStringView): Unit {
    webgpu.native.wgpuDeviceSetLabel(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuDeviceAddRef(device: WGPUDevice?): Unit {
    webgpu.native.wgpuDeviceAddRef(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuDeviceRelease(device: WGPUDevice?): Unit {
    webgpu.native.wgpuDeviceRelease(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuExternalTextureSetLabel(externalTexture: WGPUExternalTexture?, label: WGPUStringView): Unit {
    webgpu.native.wgpuExternalTextureSetLabel(externalTexture?.handler?.pointer?.takeIf { externalTexture.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuExternalTextureAddRef(externalTexture: WGPUExternalTexture?): Unit {
    webgpu.native.wgpuExternalTextureAddRef(externalTexture?.handler?.pointer?.takeIf { externalTexture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuExternalTextureRelease(externalTexture: WGPUExternalTexture?): Unit {
    webgpu.native.wgpuExternalTextureRelease(externalTexture?.handler?.pointer?.takeIf { externalTexture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuInstanceCreateSurface(instance: WGPUInstance?, descriptor: WGPUSurfaceDescriptor?): WGPUSurface? {
    return webgpu.native.wgpuInstanceCreateSurface(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUSurface(it) }
}

actual fun wgpuInstanceGetWGSLLanguageFeatures(instance: WGPUInstance?, features: WGPUSupportedWGSLLanguageFeatures?): Unit {
    webgpu.native.wgpuInstanceGetWGSLLanguageFeatures(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), features?.handler?.pointer?.takeIf { features.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuInstanceHasWGSLLanguageFeature(instance: WGPUInstance?, feature: WGPUWGSLLanguageFeatureName): UInt {
    return webgpu.native.wgpuInstanceHasWGSLLanguageFeature(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), feature)
}

actual fun wgpuInstanceProcessEvents(instance: WGPUInstance?): Unit {
    webgpu.native.wgpuInstanceProcessEvents(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuInstanceRequestAdapter(instance: WGPUInstance?, options: WGPURequestAdapterOptions?, callbackInfo: WGPURequestAdapterCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuInstanceRequestAdapter(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), options?.handler?.pointer?.takeIf { options.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuInstanceWaitAny(instance: WGPUInstance?, futureCount: ULong, futures: WGPUFutureWaitInfo?, timeoutNS: ULong): WGPUWaitStatus {
    return webgpu.native.wgpuInstanceWaitAny(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), futureCount, futures?.handler?.pointer?.takeIf { futures.handler.rawValue != 0L }?.reinterpret(), timeoutNS)
}

actual fun wgpuInstanceAddRef(instance: WGPUInstance?): Unit {
    webgpu.native.wgpuInstanceAddRef(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuInstanceRelease(instance: WGPUInstance?): Unit {
    webgpu.native.wgpuInstanceRelease(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuPipelineLayoutSetLabel(pipelineLayout: WGPUPipelineLayout?, label: WGPUStringView): Unit {
    webgpu.native.wgpuPipelineLayoutSetLabel(pipelineLayout?.handler?.pointer?.takeIf { pipelineLayout.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuPipelineLayoutAddRef(pipelineLayout: WGPUPipelineLayout?): Unit {
    webgpu.native.wgpuPipelineLayoutAddRef(pipelineLayout?.handler?.pointer?.takeIf { pipelineLayout.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuPipelineLayoutRelease(pipelineLayout: WGPUPipelineLayout?): Unit {
    webgpu.native.wgpuPipelineLayoutRelease(pipelineLayout?.handler?.pointer?.takeIf { pipelineLayout.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQuerySetDestroy(querySet: WGPUQuerySet?): Unit {
    webgpu.native.wgpuQuerySetDestroy(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQuerySetGetCount(querySet: WGPUQuerySet?): UInt {
    return webgpu.native.wgpuQuerySetGetCount(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuQuerySetGetType(querySet: WGPUQuerySet?): WGPUQueryType {
    return webgpu.native.wgpuQuerySetGetType(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuQuerySetSetLabel(querySet: WGPUQuerySet?, label: WGPUStringView): Unit {
    webgpu.native.wgpuQuerySetSetLabel(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuQuerySetAddRef(querySet: WGPUQuerySet?): Unit {
    webgpu.native.wgpuQuerySetAddRef(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQuerySetRelease(querySet: WGPUQuerySet?): Unit {
    webgpu.native.wgpuQuerySetRelease(querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQueueOnSubmittedWorkDone(queue: WGPUQueue?, callbackInfo: WGPUQueueWorkDoneCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuQueueOnSubmittedWorkDone(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuQueueSetLabel(queue: WGPUQueue?, label: WGPUStringView): Unit {
    webgpu.native.wgpuQueueSetLabel(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuQueueSubmit(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): Unit {
    webgpu.native.wgpuQueueSubmit(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), commandCount, commands?.pointer?.takeIf { commands.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQueueWriteBuffer(queue: WGPUQueue?, buffer: WGPUBuffer?, bufferOffset: ULong, data: NativeAddress?, size: ULong): Unit {
    webgpu.native.wgpuQueueWriteBuffer(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), bufferOffset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
    return
}

actual fun wgpuQueueWriteTexture(queue: WGPUQueue?, destination: WGPUTexelCopyTextureInfo?, data: NativeAddress?, dataSize: ULong, dataLayout: WGPUTexelCopyBufferLayout?, writeSize: WGPUExtent3D?): Unit {
    webgpu.native.wgpuQueueWriteTexture(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), destination?.handler?.pointer?.takeIf { destination.handler.rawValue != 0L }?.reinterpret(), data?.pointer?.takeIf { data.rawValue != 0L }, dataSize, dataLayout?.handler?.pointer?.takeIf { dataLayout.handler.rawValue != 0L }?.reinterpret(), writeSize?.handler?.pointer?.takeIf { writeSize.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQueueAddRef(queue: WGPUQueue?): Unit {
    webgpu.native.wgpuQueueAddRef(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuQueueRelease(queue: WGPUQueue?): Unit {
    webgpu.native.wgpuQueueRelease(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleSetLabel(renderBundle: WGPURenderBundle?, label: WGPUStringView): Unit {
    webgpu.native.wgpuRenderBundleSetLabel(renderBundle?.handler?.pointer?.takeIf { renderBundle.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuRenderBundleAddRef(renderBundle: WGPURenderBundle?): Unit {
    webgpu.native.wgpuRenderBundleAddRef(renderBundle?.handler?.pointer?.takeIf { renderBundle.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleRelease(renderBundle: WGPURenderBundle?): Unit {
    webgpu.native.wgpuRenderBundleRelease(renderBundle?.handler?.pointer?.takeIf { renderBundle.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: WGPURenderBundleEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    webgpu.native.wgpuRenderBundleEncoderDraw(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), vertexCount, instanceCount, firstVertex, firstInstance)
    return
}

actual fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: WGPURenderBundleEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    webgpu.native.wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), indexCount, instanceCount, firstIndex, baseVertex, firstInstance)
    return
}

actual fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    webgpu.native.wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), indirectBuffer?.handler?.pointer?.takeIf { indirectBuffer.handler.rawValue != 0L }?.reinterpret(), indirectOffset)
    return
}

actual fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    webgpu.native.wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), indirectBuffer?.handler?.pointer?.takeIf { indirectBuffer.handler.rawValue != 0L }?.reinterpret(), indirectOffset)
    return
}

actual fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: WGPURenderBundleEncoder?, descriptor: WGPURenderBundleDescriptor?): WGPURenderBundle? {
    return webgpu.native.wgpuRenderBundleEncoderFinish(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPURenderBundle(it) }
}

actual fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: WGPURenderBundleEncoder?, markerLabel: WGPUStringView): Unit {
    webgpu.native.wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), markerLabel.toCValue())
    return
}

actual fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    webgpu.native.wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupLabel: WGPUStringView): Unit {
    webgpu.native.wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), groupLabel.toCValue())
    return
}

actual fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), groupIndex, group?.handler?.pointer?.takeIf { group.handler.rawValue != 0L }?.reinterpret(), dynamicOffsetCount, dynamicOffsets?.pointer?.takeIf { dynamicOffsets.rawValue != 0L }?.reinterpret<UIntVar>())
    return
}

actual fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: WGPURenderBundleEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), offset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
    return
}

actual fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), format, offset, size)
    return
}

actual fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: WGPURenderBundleEncoder?, label: WGPUStringView): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetLabel(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: WGPURenderBundleEncoder?, pipeline: WGPURenderPipeline?): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), pipeline?.handler?.pointer?.takeIf { pipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    webgpu.native.wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret(), slot, buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, size)
    return
}

actual fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    webgpu.native.wgpuRenderBundleEncoderAddRef(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: WGPURenderBundleEncoder?): Unit {
    webgpu.native.wgpuRenderBundleEncoderRelease(renderBundleEncoder?.handler?.pointer?.takeIf { renderBundleEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?, queryIndex: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuRenderPassEncoderDraw(renderPassEncoder: WGPURenderPassEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderDraw(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), vertexCount, instanceCount, firstVertex, firstInstance)
    return
}

actual fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: WGPURenderPassEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderDrawIndexed(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), indexCount, instanceCount, firstIndex, baseVertex, firstInstance)
    return
}

actual fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    webgpu.native.wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), indirectBuffer?.handler?.pointer?.takeIf { indirectBuffer.handler.rawValue != 0L }?.reinterpret(), indirectOffset)
    return
}

actual fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit {
    webgpu.native.wgpuRenderPassEncoderDrawIndirect(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), indirectBuffer?.handler?.pointer?.takeIf { indirectBuffer.handler.rawValue != 0L }?.reinterpret(), indirectOffset)
    return
}

actual fun wgpuRenderPassEncoderEnd(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderEnd(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: WGPURenderPassEncoder?, bundleCount: ULong, bundles: NativeAddress?): Unit {
    webgpu.native.wgpuRenderPassEncoderExecuteBundles(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), bundleCount, bundles?.pointer?.takeIf { bundles.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: WGPURenderPassEncoder?, markerLabel: WGPUStringView): Unit {
    webgpu.native.wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), markerLabel.toCValue())
    return
}

actual fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: WGPURenderPassEncoder?, groupLabel: WGPUStringView): Unit {
    webgpu.native.wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), groupLabel.toCValue())
    return
}

actual fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: WGPURenderPassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit {
    webgpu.native.wgpuRenderPassEncoderSetBindGroup(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), groupIndex, group?.handler?.pointer?.takeIf { group.handler.rawValue != 0L }?.reinterpret(), dynamicOffsetCount, dynamicOffsets?.pointer?.takeIf { dynamicOffsets.rawValue != 0L }?.reinterpret<UIntVar>())
    return
}

actual fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: WGPURenderPassEncoder?, color: WGPUColor?): Unit {
    webgpu.native.wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), color?.handler?.pointer?.takeIf { color.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: WGPURenderPassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit {
    webgpu.native.wgpuRenderPassEncoderSetImmediates(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), offset, data?.pointer?.takeIf { data.rawValue != 0L }, size)
    return
}

actual fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit {
    webgpu.native.wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), format, offset, size)
    return
}

actual fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: WGPURenderPassEncoder?, label: WGPUStringView): Unit {
    webgpu.native.wgpuRenderPassEncoderSetLabel(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: WGPURenderPassEncoder?, pipeline: WGPURenderPipeline?): Unit {
    webgpu.native.wgpuRenderPassEncoderSetPipeline(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), pipeline?.handler?.pointer?.takeIf { pipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: WGPURenderPassEncoder?, x: UInt, y: UInt, width: UInt, height: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderSetScissorRect(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), x, y, width, height)
    return
}

actual fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: WGPURenderPassEncoder?, reference: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderSetStencilReference(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), reference)
    return
}

actual fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: WGPURenderPassEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit {
    webgpu.native.wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), slot, buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, size)
    return
}

actual fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: WGPURenderPassEncoder?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit {
    webgpu.native.wgpuRenderPassEncoderSetViewport(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), x, y, width, height, minDepth, maxDepth)
    return
}

actual fun wgpuRenderPassEncoderAddRef(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderAddRef(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderRelease(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderRelease(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: WGPURenderPipeline?, groupIndex: UInt): WGPUBindGroupLayout? {
    return webgpu.native.wgpuRenderPipelineGetBindGroupLayout(renderPipeline?.handler?.pointer?.takeIf { renderPipeline.handler.rawValue != 0L }?.reinterpret(), groupIndex)?.let(::NativeAddress)?.let { WGPUBindGroupLayout(it) }
}

actual fun wgpuRenderPipelineSetLabel(renderPipeline: WGPURenderPipeline?, label: WGPUStringView): Unit {
    webgpu.native.wgpuRenderPipelineSetLabel(renderPipeline?.handler?.pointer?.takeIf { renderPipeline.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuRenderPipelineAddRef(renderPipeline: WGPURenderPipeline?): Unit {
    webgpu.native.wgpuRenderPipelineAddRef(renderPipeline?.handler?.pointer?.takeIf { renderPipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPipelineRelease(renderPipeline: WGPURenderPipeline?): Unit {
    webgpu.native.wgpuRenderPipelineRelease(renderPipeline?.handler?.pointer?.takeIf { renderPipeline.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSamplerSetLabel(sampler: WGPUSampler?, label: WGPUStringView): Unit {
    webgpu.native.wgpuSamplerSetLabel(sampler?.handler?.pointer?.takeIf { sampler.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuSamplerAddRef(sampler: WGPUSampler?): Unit {
    webgpu.native.wgpuSamplerAddRef(sampler?.handler?.pointer?.takeIf { sampler.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSamplerRelease(sampler: WGPUSampler?): Unit {
    webgpu.native.wgpuSamplerRelease(sampler?.handler?.pointer?.takeIf { sampler.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuShaderModuleGetCompilationInfo(shaderModule: WGPUShaderModule?, callbackInfo: WGPUCompilationInfoCallbackInfo): WGPUFuture {
    return WGPUFuture.ByValue(webgpu.native.wgpuShaderModuleGetCompilationInfo(shaderModule?.handler?.pointer?.takeIf { shaderModule.handler.rawValue != 0L }?.reinterpret(), callbackInfo.toCValue()))
}

actual fun wgpuShaderModuleSetLabel(shaderModule: WGPUShaderModule?, label: WGPUStringView): Unit {
    webgpu.native.wgpuShaderModuleSetLabel(shaderModule?.handler?.pointer?.takeIf { shaderModule.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuShaderModuleAddRef(shaderModule: WGPUShaderModule?): Unit {
    webgpu.native.wgpuShaderModuleAddRef(shaderModule?.handler?.pointer?.takeIf { shaderModule.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuShaderModuleRelease(shaderModule: WGPUShaderModule?): Unit {
    webgpu.native.wgpuShaderModuleRelease(shaderModule?.handler?.pointer?.takeIf { shaderModule.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: WGPUSupportedFeatures): Unit {
    webgpu.native.wgpuSupportedFeaturesFreeMembers(supportedFeatures.toCValue())
    return
}

actual fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: WGPUSupportedInstanceFeatures): Unit {
    webgpu.native.wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures.toCValue())
    return
}

actual fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: WGPUSupportedWGSLLanguageFeatures): Unit {
    webgpu.native.wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures.toCValue())
    return
}

actual fun wgpuSurfaceConfigure(surface: WGPUSurface?, config: WGPUSurfaceConfiguration?): Unit {
    webgpu.native.wgpuSurfaceConfigure(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret(), config?.handler?.pointer?.takeIf { config.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus {
    return webgpu.native.wgpuSurfaceGetCapabilities(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret(), adapter?.handler?.pointer?.takeIf { adapter.handler.rawValue != 0L }?.reinterpret(), capabilities?.handler?.pointer?.takeIf { capabilities.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit {
    webgpu.native.wgpuSurfaceGetCurrentTexture(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret(), surfaceTexture?.handler?.pointer?.takeIf { surfaceTexture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSurfacePresent(surface: WGPUSurface?): WGPUStatus {
    return webgpu.native.wgpuSurfacePresent(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuSurfaceSetLabel(surface: WGPUSurface?, label: WGPUStringView): Unit {
    webgpu.native.wgpuSurfaceSetLabel(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuSurfaceUnconfigure(surface: WGPUSurface?): Unit {
    webgpu.native.wgpuSurfaceUnconfigure(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSurfaceAddRef(surface: WGPUSurface?): Unit {
    webgpu.native.wgpuSurfaceAddRef(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSurfaceRelease(surface: WGPUSurface?): Unit {
    webgpu.native.wgpuSurfaceRelease(surface?.handler?.pointer?.takeIf { surface.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: WGPUSurfaceCapabilities): Unit {
    webgpu.native.wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities.toCValue())
    return
}

actual fun wgpuTextureCreateView(texture: WGPUTexture?, descriptor: WGPUTextureViewDescriptor?): WGPUTextureView? {
    return webgpu.native.wgpuTextureCreateView(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUTextureView(it) }
}

actual fun wgpuTextureDestroy(texture: WGPUTexture?): Unit {
    webgpu.native.wgpuTextureDestroy(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuTextureGetDepthOrArrayLayers(texture: WGPUTexture?): UInt {
    return webgpu.native.wgpuTextureGetDepthOrArrayLayers(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetDimension(texture: WGPUTexture?): WGPUTextureDimension {
    return webgpu.native.wgpuTextureGetDimension(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetFormat(texture: WGPUTexture?): WGPUTextureFormat {
    return webgpu.native.wgpuTextureGetFormat(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetHeight(texture: WGPUTexture?): UInt {
    return webgpu.native.wgpuTextureGetHeight(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetMipLevelCount(texture: WGPUTexture?): UInt {
    return webgpu.native.wgpuTextureGetMipLevelCount(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetSampleCount(texture: WGPUTexture?): UInt {
    return webgpu.native.wgpuTextureGetSampleCount(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetTextureBindingViewDimension(texture: WGPUTexture?): WGPUTextureViewDimension {
    return webgpu.native.wgpuTextureGetTextureBindingViewDimension(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetUsage(texture: WGPUTexture?): ULong {
    return webgpu.native.wgpuTextureGetUsage(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureGetWidth(texture: WGPUTexture?): UInt {
    return webgpu.native.wgpuTextureGetWidth(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuTextureSetLabel(texture: WGPUTexture?, label: WGPUStringView): Unit {
    webgpu.native.wgpuTextureSetLabel(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuTextureAddRef(texture: WGPUTexture?): Unit {
    webgpu.native.wgpuTextureAddRef(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuTextureRelease(texture: WGPUTexture?): Unit {
    webgpu.native.wgpuTextureRelease(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuTextureViewSetLabel(textureView: WGPUTextureView?, label: WGPUStringView): Unit {
    webgpu.native.wgpuTextureViewSetLabel(textureView?.handler?.pointer?.takeIf { textureView.handler.rawValue != 0L }?.reinterpret(), label.toCValue())
    return
}

actual fun wgpuTextureViewAddRef(textureView: WGPUTextureView?): Unit {
    webgpu.native.wgpuTextureViewAddRef(textureView?.handler?.pointer?.takeIf { textureView.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuTextureViewRelease(textureView: WGPUTextureView?): Unit {
    webgpu.native.wgpuTextureViewRelease(textureView?.handler?.pointer?.takeIf { textureView.handler.rawValue != 0L }?.reinterpret())
    return
}

actual interface WGPUXlibDisplayHandle {
    actual var display: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXlibDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXlibDisplayHandle =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUXlibDisplayHandle>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXlibDisplayHandle) -> Unit): ArrayHolder<WGPUXlibDisplayHandle> {
            val byteSize = sizeOf<webgpu.native.WGPUXlibDisplayHandle>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUXlibDisplayHandle>) : WGPUXlibDisplayHandle {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var display: NativeAddress?
            get() = handle.useContents { this.display?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var screen: Int
            get() = handle.useContents { this.screen }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUXlibDisplayHandle {
        private val struct: webgpu.native.WGPUXlibDisplayHandle
            get() = handler.pointer.reinterpret<webgpu.native.WGPUXlibDisplayHandle>().pointed
        
        override var display: NativeAddress?
            get() = struct.display?.let(::NativeAddress)
            set(value) { struct.display = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var screen: Int
            get() = struct.screen
            set(value) { struct.screen = value }
    }
}

fun WGPUXlibDisplayHandle.toCValue(): CValue<webgpu.native.WGPUXlibDisplayHandle> = cValue {
    this.display = this@toCValue.display?.pointer?.takeIf { this@toCValue.display?.rawValue != 0L }?.reinterpret()
    this.screen = this@toCValue.screen
}

actual interface WGPUXcbDisplayHandle {
    actual var connection: NativeAddress?
    actual var screen: Int
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUXcbDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUXcbDisplayHandle =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUXcbDisplayHandle>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXcbDisplayHandle) -> Unit): ArrayHolder<WGPUXcbDisplayHandle> {
            val byteSize = sizeOf<webgpu.native.WGPUXcbDisplayHandle>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUXcbDisplayHandle>) : WGPUXcbDisplayHandle {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var connection: NativeAddress?
            get() = handle.useContents { this.connection?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var screen: Int
            get() = handle.useContents { this.screen }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUXcbDisplayHandle {
        private val struct: webgpu.native.WGPUXcbDisplayHandle
            get() = handler.pointer.reinterpret<webgpu.native.WGPUXcbDisplayHandle>().pointed
        
        override var connection: NativeAddress?
            get() = struct.connection?.let(::NativeAddress)
            set(value) { struct.connection = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var screen: Int
            get() = struct.screen
            set(value) { struct.screen = value }
    }
}

fun WGPUXcbDisplayHandle.toCValue(): CValue<webgpu.native.WGPUXcbDisplayHandle> = cValue {
    this.connection = this@toCValue.connection?.pointer?.takeIf { this@toCValue.connection?.rawValue != 0L }?.reinterpret()
    this.screen = this@toCValue.screen
}

actual interface WGPUWaylandDisplayHandle {
    actual var display: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUWaylandDisplayHandle = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUWaylandDisplayHandle =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUWaylandDisplayHandle>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUWaylandDisplayHandle) -> Unit): ArrayHolder<WGPUWaylandDisplayHandle> {
            val byteSize = sizeOf<webgpu.native.WGPUWaylandDisplayHandle>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUWaylandDisplayHandle>) : WGPUWaylandDisplayHandle {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var display: NativeAddress?
            get() = handle.useContents { this.display?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUWaylandDisplayHandle {
        private val struct: webgpu.native.WGPUWaylandDisplayHandle
            get() = handler.pointer.reinterpret<webgpu.native.WGPUWaylandDisplayHandle>().pointed
        
        override var display: NativeAddress?
            get() = struct.display?.let(::NativeAddress)
            set(value) { struct.display = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUWaylandDisplayHandle.toCValue(): CValue<webgpu.native.WGPUWaylandDisplayHandle> = cValue {
    this.display = this@toCValue.display?.pointer?.takeIf { this@toCValue.display?.rawValue != 0L }?.reinterpret()
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
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeDisplayHandle =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUNativeDisplayHandle>().toLong()))
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeDisplayHandle) -> Unit): ArrayHolder<WGPUNativeDisplayHandle> {
            val byteSize = sizeOf<webgpu.native.WGPUNativeDisplayHandle>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
    value class ByValue(val handle: CValue<webgpu.native.WGPUNativeDisplayHandle>) : WGPUNativeDisplayHandle {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        override var type: WGPUNativeDisplayHandleType
            get() = handle.useContents { this }.type
            set(value) { error("Setters not supported on ByValue") }
        override val xlib: WGPUXlibDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xlib) WGPUXlibDisplayHandle.ByReference(NativeAddress(handle.useContents { this }.data.xlib.ptr)) else null
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            error("Setters not supported on ByValue")
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xcb) WGPUXcbDisplayHandle.ByReference(NativeAddress(handle.useContents { this }.data.xcb.ptr)) else null
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            error("Setters not supported on ByValue")
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Wayland) WGPUWaylandDisplayHandle.ByReference(NativeAddress(handle.useContents { this }.data.wayland.ptr)) else null
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            error("Setters not supported on ByValue")
        }
    }
    
    class ByReference(override val handler: NativeAddress) : WGPUNativeDisplayHandle {
        private val struct: webgpu.native.WGPUNativeDisplayHandle
            get() = handler.pointer.reinterpret<webgpu.native.WGPUNativeDisplayHandle>().pointed
        override var type: WGPUNativeDisplayHandleType
            get() = struct.type
            set(value) { struct.type = value }
        override val xlib: WGPUXlibDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xlib) WGPUXlibDisplayHandle.ByReference(NativeAddress(struct.data.xlib.ptr)) else null
        override fun setXlib(value: WGPUXlibDisplayHandle) {
            struct.type = WGPUNativeDisplayHandleType_Xlib
            val destBytes = struct.data.xlib.ptr.reinterpret<ByteVar>()
            val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
            for (i in 0 until sizeOf<webgpu.native.WGPUXlibDisplayHandle>()) destBytes[i] = srcBytes[i]
        }
        override val xcb: WGPUXcbDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Xcb) WGPUXcbDisplayHandle.ByReference(NativeAddress(struct.data.xcb.ptr)) else null
        override fun setXcb(value: WGPUXcbDisplayHandle) {
            struct.type = WGPUNativeDisplayHandleType_Xcb
            val destBytes = struct.data.xcb.ptr.reinterpret<ByteVar>()
            val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
            for (i in 0 until sizeOf<webgpu.native.WGPUXcbDisplayHandle>()) destBytes[i] = srcBytes[i]
        }
        override val wayland: WGPUWaylandDisplayHandle?
            get() = if (type == WGPUNativeDisplayHandleType_Wayland) WGPUWaylandDisplayHandle.ByReference(NativeAddress(struct.data.wayland.ptr)) else null
        override fun setWayland(value: WGPUWaylandDisplayHandle) {
            struct.type = WGPUNativeDisplayHandleType_Wayland
            val destBytes = struct.data.wayland.ptr.reinterpret<ByteVar>()
            val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
            for (i in 0 until sizeOf<webgpu.native.WGPUWaylandDisplayHandle>()) destBytes[i] = srcBytes[i]
        }
    }
}

fun WGPUNativeDisplayHandle.toCValue(): CValue<webgpu.native.WGPUNativeDisplayHandle> = cValue {
    this.type = this@toCValue.type
    this@toCValue.xlib?.let {
        val destBytes = this.data.xlib.ptr.reinterpret<ByteVar>()
        val srcBytes = it.handler.pointer.reinterpret<ByteVar>()
        for (i in 0 until sizeOf<webgpu.native.WGPUXlibDisplayHandle>()) destBytes[i] = srcBytes[i]
    }
    this@toCValue.xcb?.let {
        val destBytes = this.data.xcb.ptr.reinterpret<ByteVar>()
        val srcBytes = it.handler.pointer.reinterpret<ByteVar>()
        for (i in 0 until sizeOf<webgpu.native.WGPUXcbDisplayHandle>()) destBytes[i] = srcBytes[i]
    }
    this@toCValue.wayland?.let {
        val destBytes = this.data.wayland.ptr.reinterpret<ByteVar>()
        val srcBytes = it.handler.pointer.reinterpret<ByteVar>()
        for (i in 0 until sizeOf<webgpu.native.WGPUWaylandDisplayHandle>()) destBytes[i] = srcBytes[i]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUInstanceExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceExtras) -> Unit): ArrayHolder<WGPUInstanceExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUInstanceExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUInstanceExtras>) : WGPUInstanceExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var backends: ULong
            get() = handle.useContents { this.backends }
            set(value) { error("Setters not supported on ByValue") }
        override var flags: ULong
            get() = handle.useContents { this.flags }
            set(value) { error("Setters not supported on ByValue") }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = handle.useContents { this.dx12ShaderCompiler as WGPUDx12Compiler }
            set(value) { error("Setters not supported on ByValue") }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = handle.useContents { this.gles3MinorVersion as WGPUGles3MinorVersion }
            set(value) { error("Setters not supported on ByValue") }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = handle.useContents { this.glFenceBehaviour as WGPUGLFenceBehaviour }
            set(value) { error("Setters not supported on ByValue") }
        override var dxcPath: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.dxcPath.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = handle.useContents { this.dxcMaxShaderModel as WGPUDxcMaxShaderModel }
            set(value) { error("Setters not supported on ByValue") }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = handle.useContents { this.dx12PresentationSystem as WGPUDx12SwapchainKind }
            set(value) { error("Setters not supported on ByValue") }
        override var budgetForDeviceCreation: NativeAddress?
            get() = handle.useContents { this.budgetForDeviceCreation?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var budgetForDeviceLoss: NativeAddress?
            get() = handle.useContents { this.budgetForDeviceLoss?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var displayHandle: WGPUNativeDisplayHandle
            get() = handle.useContents { WGPUNativeDisplayHandle.ByReference(NativeAddress(this.displayHandle.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUInstanceExtras {
        private val struct: webgpu.native.WGPUInstanceExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUInstanceExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var backends: ULong
            get() = struct.backends
            set(value) { struct.backends = value }
        override var flags: ULong
            get() = struct.flags
            set(value) { struct.flags = value }
        override var dx12ShaderCompiler: WGPUDx12Compiler
            get() = struct.dx12ShaderCompiler as WGPUDx12Compiler
            set(value) { struct.dx12ShaderCompiler = value }
        override var gles3MinorVersion: WGPUGles3MinorVersion
            get() = struct.gles3MinorVersion as WGPUGles3MinorVersion
            set(value) { struct.gles3MinorVersion = value }
        override var glFenceBehaviour: WGPUGLFenceBehaviour
            get() = struct.glFenceBehaviour as WGPUGLFenceBehaviour
            set(value) { struct.glFenceBehaviour = value }
        override var dxcPath: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.dxcPath.ptr))
            set(value) {
                val destBytes = struct.dxcPath.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var dxcMaxShaderModel: WGPUDxcMaxShaderModel
            get() = struct.dxcMaxShaderModel as WGPUDxcMaxShaderModel
            set(value) { struct.dxcMaxShaderModel = value }
        override var dx12PresentationSystem: WGPUDx12SwapchainKind
            get() = struct.dx12PresentationSystem as WGPUDx12SwapchainKind
            set(value) { struct.dx12PresentationSystem = value }
        override var budgetForDeviceCreation: NativeAddress?
            get() = struct.budgetForDeviceCreation?.let(::NativeAddress)
            set(value) { struct.budgetForDeviceCreation = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var budgetForDeviceLoss: NativeAddress?
            get() = struct.budgetForDeviceLoss?.let(::NativeAddress)
            set(value) { struct.budgetForDeviceLoss = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var displayHandle: WGPUNativeDisplayHandle
            get() = WGPUNativeDisplayHandle.ByReference(NativeAddress(struct.displayHandle.ptr))
            set(value) {
                val destBytes = struct.displayHandle.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUNativeDisplayHandle>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUInstanceExtras.toCValue(): CValue<webgpu.native.WGPUInstanceExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.backends = this@toCValue.backends
    this.flags = this@toCValue.flags
    this.dx12ShaderCompiler = this@toCValue.dx12ShaderCompiler
    this.gles3MinorVersion = this@toCValue.gles3MinorVersion
    this.glFenceBehaviour = this@toCValue.glFenceBehaviour
    val dest_dxcPath = this.dxcPath.ptr.reinterpret<ByteVar>()
    val src_dxcPath = this@toCValue.dxcPath.handler.pointer.reinterpret<ByteVar>()
    val size_dxcPath = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_dxcPath) {
        dest_dxcPath[i.toInt()] = src_dxcPath[i.toInt()]
    }
    this.dxcMaxShaderModel = this@toCValue.dxcMaxShaderModel
    this.dx12PresentationSystem = this@toCValue.dx12PresentationSystem
    this.budgetForDeviceCreation = this@toCValue.budgetForDeviceCreation?.pointer?.takeIf { this@toCValue.budgetForDeviceCreation?.rawValue != 0L }?.reinterpret()
    this.budgetForDeviceLoss = this@toCValue.budgetForDeviceLoss?.pointer?.takeIf { this@toCValue.budgetForDeviceLoss?.rawValue != 0L }?.reinterpret()
    val dest_displayHandle = this.displayHandle.ptr.reinterpret<ByteVar>()
    val src_displayHandle = this@toCValue.displayHandle.handler.pointer.reinterpret<ByteVar>()
    val size_displayHandle = sizeOf<webgpu.native.WGPUNativeDisplayHandle>().toLong()
    for (i in 0L until size_displayHandle) {
        dest_displayHandle[i.toInt()] = src_displayHandle[i.toInt()]
    }
}

actual interface WGPUDeviceExtras {
    actual var chain: WGPUChainedStruct
    actual var tracePath: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUDeviceExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUDeviceExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUDeviceExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceExtras) -> Unit): ArrayHolder<WGPUDeviceExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUDeviceExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUDeviceExtras>) : WGPUDeviceExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var tracePath: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.tracePath.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUDeviceExtras {
        private val struct: webgpu.native.WGPUDeviceExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUDeviceExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var tracePath: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.tracePath.ptr))
            set(value) {
                val destBytes = struct.tracePath.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUDeviceExtras.toCValue(): CValue<webgpu.native.WGPUDeviceExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    val dest_tracePath = this.tracePath.ptr.reinterpret<ByteVar>()
    val src_tracePath = this@toCValue.tracePath.handler.pointer.reinterpret<ByteVar>()
    val size_tracePath = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_tracePath) {
        dest_tracePath[i.toInt()] = src_tracePath[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUNativeLimits =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUNativeLimits>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeLimits) -> Unit): ArrayHolder<WGPUNativeLimits> {
            val byteSize = sizeOf<webgpu.native.WGPUNativeLimits>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUNativeLimits>) : WGPUNativeLimits {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var maxNonSamplerBindings: UInt
            get() = handle.useContents { this.maxNonSamplerBindings }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = handle.useContents { this.maxBindingArrayElementsPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = handle.useContents { this.maxBindingArraySamplerElementsPerShaderStage }
            set(value) { error("Setters not supported on ByValue") }
        override var maxMultiviewViewCount: UInt
            get() = handle.useContents { this.maxMultiviewViewCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUNativeLimits {
        private val struct: webgpu.native.WGPUNativeLimits
            get() = handler.pointer.reinterpret<webgpu.native.WGPUNativeLimits>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var maxNonSamplerBindings: UInt
            get() = struct.maxNonSamplerBindings
            set(value) { struct.maxNonSamplerBindings = value }
        override var maxBindingArrayElementsPerShaderStage: UInt
            get() = struct.maxBindingArrayElementsPerShaderStage
            set(value) { struct.maxBindingArrayElementsPerShaderStage = value }
        override var maxBindingArraySamplerElementsPerShaderStage: UInt
            get() = struct.maxBindingArraySamplerElementsPerShaderStage
            set(value) { struct.maxBindingArraySamplerElementsPerShaderStage = value }
        override var maxMultiviewViewCount: UInt
            get() = struct.maxMultiviewViewCount
            set(value) { struct.maxMultiviewViewCount = value }
    }
}

fun WGPUNativeLimits.toCValue(): CValue<webgpu.native.WGPUNativeLimits> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.maxNonSamplerBindings = this@toCValue.maxNonSamplerBindings
    this.maxBindingArrayElementsPerShaderStage = this@toCValue.maxBindingArrayElementsPerShaderStage
    this.maxBindingArraySamplerElementsPerShaderStage = this@toCValue.maxBindingArraySamplerElementsPerShaderStage
    this.maxMultiviewViewCount = this@toCValue.maxMultiviewViewCount
}

actual interface WGPUShaderDefine {
    actual var name: WGPUStringView
    actual var value: WGPUStringView
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderDefine = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderDefine =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderDefine>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderDefine) -> Unit): ArrayHolder<WGPUShaderDefine> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderDefine>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderDefine>) : WGPUShaderDefine {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var name: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.name.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var value: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.value.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderDefine {
        private val struct: webgpu.native.WGPUShaderDefine
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderDefine>().pointed
        
        override var name: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.name.ptr))
            set(value) {
                val destBytes = struct.name.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var value: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.value.ptr))
            set(value) {
                val destBytes = struct.value.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUShaderDefine.toCValue(): CValue<webgpu.native.WGPUShaderDefine> = cValue {
    val dest_name = this.name.ptr.reinterpret<ByteVar>()
    val src_name = this@toCValue.name.handler.pointer.reinterpret<ByteVar>()
    val size_name = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_name) {
        dest_name[i.toInt()] = src_name[i.toInt()]
    }
    val dest_value = this.value.ptr.reinterpret<ByteVar>()
    val src_value = this@toCValue.value.handler.pointer.reinterpret<ByteVar>()
    val size_value = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_value) {
        dest_value[i.toInt()] = src_value[i.toInt()]
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
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderSourceGLSL =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderSourceGLSL>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceGLSL) -> Unit): ArrayHolder<WGPUShaderSourceGLSL> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderSourceGLSL>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderSourceGLSL>) : WGPUShaderSourceGLSL {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var stage: ULong
            get() = handle.useContents { this.stage }
            set(value) { error("Setters not supported on ByValue") }
        override var code: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.code.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var defineCount: UInt
            get() = handle.useContents { this.defineCount }
            set(value) { error("Setters not supported on ByValue") }
        override var defines: WGPUShaderDefine?
            get() = handle.useContents { this.defines?.let(::NativeAddress)?.let { WGPUShaderDefine(it) } }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderSourceGLSL {
        private val struct: webgpu.native.WGPUShaderSourceGLSL
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderSourceGLSL>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var stage: ULong
            get() = struct.stage
            set(value) { struct.stage = value }
        override var code: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.code.ptr))
            set(value) {
                val destBytes = struct.code.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var defineCount: UInt
            get() = struct.defineCount
            set(value) { struct.defineCount = value }
        override var defines: WGPUShaderDefine?
            get() = struct.defines?.let(::NativeAddress)?.let { WGPUShaderDefine(it) }
            set(value) { struct.defines = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUShaderSourceGLSL.toCValue(): CValue<webgpu.native.WGPUShaderSourceGLSL> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.stage = this@toCValue.stage
    val dest_code = this.code.ptr.reinterpret<ByteVar>()
    val src_code = this@toCValue.code.handler.pointer.reinterpret<ByteVar>()
    val size_code = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_code) {
        dest_code[i.toInt()] = src_code[i.toInt()]
    }
    this.defineCount = this@toCValue.defineCount
    this.defines = this@toCValue.defines?.handler?.pointer?.takeIf { this@toCValue.defines?.handler?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUShaderModuleDescriptorSpirV {
    actual var label: WGPUStringView
    actual var sourceSize: UInt
    actual var source: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptorSpirV = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptorSpirV =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUShaderModuleDescriptorSpirV>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptorSpirV) -> Unit): ArrayHolder<WGPUShaderModuleDescriptorSpirV> {
            val byteSize = sizeOf<webgpu.native.WGPUShaderModuleDescriptorSpirV>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUShaderModuleDescriptorSpirV>) : WGPUShaderModuleDescriptorSpirV {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var label: WGPUStringView
            get() = handle.useContents { WGPUStringView.ByReference(NativeAddress(this.label.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var sourceSize: UInt
            get() = handle.useContents { this.sourceSize }
            set(value) { error("Setters not supported on ByValue") }
        override var source: NativeAddress?
            get() = handle.useContents { this.source?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUShaderModuleDescriptorSpirV {
        private val struct: webgpu.native.WGPUShaderModuleDescriptorSpirV
            get() = handler.pointer.reinterpret<webgpu.native.WGPUShaderModuleDescriptorSpirV>().pointed
        
        override var label: WGPUStringView
            get() = WGPUStringView.ByReference(NativeAddress(struct.label.ptr))
            set(value) {
                val destBytes = struct.label.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUStringView>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var sourceSize: UInt
            get() = struct.sourceSize
            set(value) { struct.sourceSize = value }
        override var source: NativeAddress?
            get() = struct.source?.let(::NativeAddress)
            set(value) { struct.source = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUShaderModuleDescriptorSpirV.toCValue(): CValue<webgpu.native.WGPUShaderModuleDescriptorSpirV> = cValue {
    val dest_label = this.label.ptr.reinterpret<ByteVar>()
    val src_label = this@toCValue.label.handler.pointer.reinterpret<ByteVar>()
    val size_label = sizeOf<webgpu.native.WGPUStringView>().toLong()
    for (i in 0L until size_label) {
        dest_label[i.toInt()] = src_label[i.toInt()]
    }
    this.sourceSize = this@toCValue.sourceSize
    this.source = this@toCValue.source?.pointer?.takeIf { this@toCValue.source?.rawValue != 0L }?.reinterpret()
}

actual interface WGPURegistryReport {
    actual var numAllocated: ULong
    actual var numKeptFromUser: ULong
    actual var numReleasedFromUser: ULong
    actual var elementSize: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPURegistryReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPURegistryReport =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPURegistryReport>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURegistryReport) -> Unit): ArrayHolder<WGPURegistryReport> {
            val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPURegistryReport>) : WGPURegistryReport {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var numAllocated: ULong
            get() = handle.useContents { this.numAllocated }
            set(value) { error("Setters not supported on ByValue") }
        override var numKeptFromUser: ULong
            get() = handle.useContents { this.numKeptFromUser }
            set(value) { error("Setters not supported on ByValue") }
        override var numReleasedFromUser: ULong
            get() = handle.useContents { this.numReleasedFromUser }
            set(value) { error("Setters not supported on ByValue") }
        override var elementSize: ULong
            get() = handle.useContents { this.elementSize }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPURegistryReport {
        private val struct: webgpu.native.WGPURegistryReport
            get() = handler.pointer.reinterpret<webgpu.native.WGPURegistryReport>().pointed
        
        override var numAllocated: ULong
            get() = struct.numAllocated
            set(value) { struct.numAllocated = value }
        override var numKeptFromUser: ULong
            get() = struct.numKeptFromUser
            set(value) { struct.numKeptFromUser = value }
        override var numReleasedFromUser: ULong
            get() = struct.numReleasedFromUser
            set(value) { struct.numReleasedFromUser = value }
        override var elementSize: ULong
            get() = struct.elementSize
            set(value) { struct.elementSize = value }
    }
}

fun WGPURegistryReport.toCValue(): CValue<webgpu.native.WGPURegistryReport> = cValue {
    this.numAllocated = this@toCValue.numAllocated
    this.numKeptFromUser = this@toCValue.numKeptFromUser
    this.numReleasedFromUser = this@toCValue.numReleasedFromUser
    this.elementSize = this@toCValue.elementSize
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
        actual fun allocate(allocator: MemoryAllocator): WGPUHubReport =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUHubReport>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUHubReport) -> Unit): ArrayHolder<WGPUHubReport> {
            val byteSize = sizeOf<webgpu.native.WGPUHubReport>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUHubReport>) : WGPUHubReport {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var adapters: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.adapters.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var devices: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.devices.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var queues: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.queues.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var pipelineLayouts: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.pipelineLayouts.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var shaderModules: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.shaderModules.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var bindGroupLayouts: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.bindGroupLayouts.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var bindGroups: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.bindGroups.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var commandBuffers: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.commandBuffers.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var renderBundles: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.renderBundles.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var renderPipelines: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.renderPipelines.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var computePipelines: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.computePipelines.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var pipelineCaches: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.pipelineCaches.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var querySets: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.querySets.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var buffers: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.buffers.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var textures: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.textures.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var textureViews: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.textureViews.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var samplers: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.samplers.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUHubReport {
        private val struct: webgpu.native.WGPUHubReport
            get() = handler.pointer.reinterpret<webgpu.native.WGPUHubReport>().pointed
        
        override var adapters: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.adapters.ptr))
            set(value) {
                val destBytes = struct.adapters.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var devices: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.devices.ptr))
            set(value) {
                val destBytes = struct.devices.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var queues: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.queues.ptr))
            set(value) {
                val destBytes = struct.queues.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var pipelineLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.pipelineLayouts.ptr))
            set(value) {
                val destBytes = struct.pipelineLayouts.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var shaderModules: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.shaderModules.ptr))
            set(value) {
                val destBytes = struct.shaderModules.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var bindGroupLayouts: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.bindGroupLayouts.ptr))
            set(value) {
                val destBytes = struct.bindGroupLayouts.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var bindGroups: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.bindGroups.ptr))
            set(value) {
                val destBytes = struct.bindGroups.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var commandBuffers: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.commandBuffers.ptr))
            set(value) {
                val destBytes = struct.commandBuffers.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var renderBundles: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.renderBundles.ptr))
            set(value) {
                val destBytes = struct.renderBundles.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var renderPipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.renderPipelines.ptr))
            set(value) {
                val destBytes = struct.renderPipelines.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var computePipelines: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.computePipelines.ptr))
            set(value) {
                val destBytes = struct.computePipelines.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var pipelineCaches: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.pipelineCaches.ptr))
            set(value) {
                val destBytes = struct.pipelineCaches.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var querySets: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.querySets.ptr))
            set(value) {
                val destBytes = struct.querySets.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var buffers: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.buffers.ptr))
            set(value) {
                val destBytes = struct.buffers.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var textures: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.textures.ptr))
            set(value) {
                val destBytes = struct.textures.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var textureViews: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.textureViews.ptr))
            set(value) {
                val destBytes = struct.textureViews.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var samplers: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.samplers.ptr))
            set(value) {
                val destBytes = struct.samplers.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUHubReport.toCValue(): CValue<webgpu.native.WGPUHubReport> = cValue {
    val dest_adapters = this.adapters.ptr.reinterpret<ByteVar>()
    val src_adapters = this@toCValue.adapters.handler.pointer.reinterpret<ByteVar>()
    val size_adapters = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_adapters) {
        dest_adapters[i.toInt()] = src_adapters[i.toInt()]
    }
    val dest_devices = this.devices.ptr.reinterpret<ByteVar>()
    val src_devices = this@toCValue.devices.handler.pointer.reinterpret<ByteVar>()
    val size_devices = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_devices) {
        dest_devices[i.toInt()] = src_devices[i.toInt()]
    }
    val dest_queues = this.queues.ptr.reinterpret<ByteVar>()
    val src_queues = this@toCValue.queues.handler.pointer.reinterpret<ByteVar>()
    val size_queues = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_queues) {
        dest_queues[i.toInt()] = src_queues[i.toInt()]
    }
    val dest_pipelineLayouts = this.pipelineLayouts.ptr.reinterpret<ByteVar>()
    val src_pipelineLayouts = this@toCValue.pipelineLayouts.handler.pointer.reinterpret<ByteVar>()
    val size_pipelineLayouts = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_pipelineLayouts) {
        dest_pipelineLayouts[i.toInt()] = src_pipelineLayouts[i.toInt()]
    }
    val dest_shaderModules = this.shaderModules.ptr.reinterpret<ByteVar>()
    val src_shaderModules = this@toCValue.shaderModules.handler.pointer.reinterpret<ByteVar>()
    val size_shaderModules = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_shaderModules) {
        dest_shaderModules[i.toInt()] = src_shaderModules[i.toInt()]
    }
    val dest_bindGroupLayouts = this.bindGroupLayouts.ptr.reinterpret<ByteVar>()
    val src_bindGroupLayouts = this@toCValue.bindGroupLayouts.handler.pointer.reinterpret<ByteVar>()
    val size_bindGroupLayouts = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_bindGroupLayouts) {
        dest_bindGroupLayouts[i.toInt()] = src_bindGroupLayouts[i.toInt()]
    }
    val dest_bindGroups = this.bindGroups.ptr.reinterpret<ByteVar>()
    val src_bindGroups = this@toCValue.bindGroups.handler.pointer.reinterpret<ByteVar>()
    val size_bindGroups = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_bindGroups) {
        dest_bindGroups[i.toInt()] = src_bindGroups[i.toInt()]
    }
    val dest_commandBuffers = this.commandBuffers.ptr.reinterpret<ByteVar>()
    val src_commandBuffers = this@toCValue.commandBuffers.handler.pointer.reinterpret<ByteVar>()
    val size_commandBuffers = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_commandBuffers) {
        dest_commandBuffers[i.toInt()] = src_commandBuffers[i.toInt()]
    }
    val dest_renderBundles = this.renderBundles.ptr.reinterpret<ByteVar>()
    val src_renderBundles = this@toCValue.renderBundles.handler.pointer.reinterpret<ByteVar>()
    val size_renderBundles = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_renderBundles) {
        dest_renderBundles[i.toInt()] = src_renderBundles[i.toInt()]
    }
    val dest_renderPipelines = this.renderPipelines.ptr.reinterpret<ByteVar>()
    val src_renderPipelines = this@toCValue.renderPipelines.handler.pointer.reinterpret<ByteVar>()
    val size_renderPipelines = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_renderPipelines) {
        dest_renderPipelines[i.toInt()] = src_renderPipelines[i.toInt()]
    }
    val dest_computePipelines = this.computePipelines.ptr.reinterpret<ByteVar>()
    val src_computePipelines = this@toCValue.computePipelines.handler.pointer.reinterpret<ByteVar>()
    val size_computePipelines = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_computePipelines) {
        dest_computePipelines[i.toInt()] = src_computePipelines[i.toInt()]
    }
    val dest_pipelineCaches = this.pipelineCaches.ptr.reinterpret<ByteVar>()
    val src_pipelineCaches = this@toCValue.pipelineCaches.handler.pointer.reinterpret<ByteVar>()
    val size_pipelineCaches = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_pipelineCaches) {
        dest_pipelineCaches[i.toInt()] = src_pipelineCaches[i.toInt()]
    }
    val dest_querySets = this.querySets.ptr.reinterpret<ByteVar>()
    val src_querySets = this@toCValue.querySets.handler.pointer.reinterpret<ByteVar>()
    val size_querySets = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_querySets) {
        dest_querySets[i.toInt()] = src_querySets[i.toInt()]
    }
    val dest_buffers = this.buffers.ptr.reinterpret<ByteVar>()
    val src_buffers = this@toCValue.buffers.handler.pointer.reinterpret<ByteVar>()
    val size_buffers = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_buffers) {
        dest_buffers[i.toInt()] = src_buffers[i.toInt()]
    }
    val dest_textures = this.textures.ptr.reinterpret<ByteVar>()
    val src_textures = this@toCValue.textures.handler.pointer.reinterpret<ByteVar>()
    val size_textures = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_textures) {
        dest_textures[i.toInt()] = src_textures[i.toInt()]
    }
    val dest_textureViews = this.textureViews.ptr.reinterpret<ByteVar>()
    val src_textureViews = this@toCValue.textureViews.handler.pointer.reinterpret<ByteVar>()
    val size_textureViews = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_textureViews) {
        dest_textureViews[i.toInt()] = src_textureViews[i.toInt()]
    }
    val dest_samplers = this.samplers.ptr.reinterpret<ByteVar>()
    val src_samplers = this@toCValue.samplers.handler.pointer.reinterpret<ByteVar>()
    val size_samplers = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_samplers) {
        dest_samplers[i.toInt()] = src_samplers[i.toInt()]
    }
}

actual interface WGPUGlobalReport {
    actual var surfaces: WGPURegistryReport
    actual var hub: WGPUHubReport
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUGlobalReport = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUGlobalReport =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUGlobalReport>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUGlobalReport) -> Unit): ArrayHolder<WGPUGlobalReport> {
            val byteSize = sizeOf<webgpu.native.WGPUGlobalReport>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUGlobalReport>) : WGPUGlobalReport {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var surfaces: WGPURegistryReport
            get() = handle.useContents { WGPURegistryReport.ByReference(NativeAddress(this.surfaces.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var hub: WGPUHubReport
            get() = handle.useContents { WGPUHubReport.ByReference(NativeAddress(this.hub.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUGlobalReport {
        private val struct: webgpu.native.WGPUGlobalReport
            get() = handler.pointer.reinterpret<webgpu.native.WGPUGlobalReport>().pointed
        
        override var surfaces: WGPURegistryReport
            get() = WGPURegistryReport.ByReference(NativeAddress(struct.surfaces.ptr))
            set(value) {
                val destBytes = struct.surfaces.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var hub: WGPUHubReport
            get() = WGPUHubReport.ByReference(NativeAddress(struct.hub.ptr))
            set(value) {
                val destBytes = struct.hub.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUHubReport>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
    }
}

fun WGPUGlobalReport.toCValue(): CValue<webgpu.native.WGPUGlobalReport> = cValue {
    val dest_surfaces = this.surfaces.ptr.reinterpret<ByteVar>()
    val src_surfaces = this@toCValue.surfaces.handler.pointer.reinterpret<ByteVar>()
    val size_surfaces = sizeOf<webgpu.native.WGPURegistryReport>().toLong()
    for (i in 0L until size_surfaces) {
        dest_surfaces[i.toInt()] = src_surfaces[i.toInt()]
    }
    val dest_hub = this.hub.ptr.reinterpret<ByteVar>()
    val src_hub = this@toCValue.hub.handler.pointer.reinterpret<ByteVar>()
    val size_hub = sizeOf<webgpu.native.WGPUHubReport>().toLong()
    for (i in 0L until size_hub) {
        dest_hub[i.toInt()] = src_hub[i.toInt()]
    }
}

actual interface WGPUInstanceEnumerateAdapterOptions {
    actual var nextInChain: WGPUChainedStruct?
    actual var backends: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUInstanceEnumerateAdapterOptions = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUInstanceEnumerateAdapterOptions =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUInstanceEnumerateAdapterOptions>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceEnumerateAdapterOptions) -> Unit): ArrayHolder<WGPUInstanceEnumerateAdapterOptions> {
            val byteSize = sizeOf<webgpu.native.WGPUInstanceEnumerateAdapterOptions>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUInstanceEnumerateAdapterOptions>) : WGPUInstanceEnumerateAdapterOptions {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var nextInChain: WGPUChainedStruct?
            get() = handle.useContents { this.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) } }
            set(value) { error("Setters not supported on ByValue") }
        override var backends: ULong
            get() = handle.useContents { this.backends }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUInstanceEnumerateAdapterOptions {
        private val struct: webgpu.native.WGPUInstanceEnumerateAdapterOptions
            get() = handler.pointer.reinterpret<webgpu.native.WGPUInstanceEnumerateAdapterOptions>().pointed
        
        override var nextInChain: WGPUChainedStruct?
            get() = struct.nextInChain?.let(::NativeAddress)?.let { WGPUChainedStruct(it) }
            set(value) { struct.nextInChain = value?.handler?.pointer?.takeIf { value.handler.rawValue != 0L }?.reinterpret() }
        override var backends: ULong
            get() = struct.backends
            set(value) { struct.backends = value }
    }
}

fun WGPUInstanceEnumerateAdapterOptions.toCValue(): CValue<webgpu.native.WGPUInstanceEnumerateAdapterOptions> = cValue {
    this.nextInChain = this@toCValue.nextInChain?.handler?.pointer?.takeIf { this@toCValue.nextInChain?.handler?.rawValue != 0L }?.reinterpret()
    this.backends = this@toCValue.backends
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
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntryExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupEntryExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupEntryExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupEntryExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupEntryExtras>) : WGPUBindGroupEntryExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var buffers: NativeAddress?
            get() = handle.useContents { this.buffers?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var bufferCount: ULong
            get() = handle.useContents { this.bufferCount }
            set(value) { error("Setters not supported on ByValue") }
        override var samplers: NativeAddress?
            get() = handle.useContents { this.samplers?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var samplerCount: ULong
            get() = handle.useContents { this.samplerCount }
            set(value) { error("Setters not supported on ByValue") }
        override var textureViews: NativeAddress?
            get() = handle.useContents { this.textureViews?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var textureViewCount: ULong
            get() = handle.useContents { this.textureViewCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupEntryExtras {
        private val struct: webgpu.native.WGPUBindGroupEntryExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupEntryExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var buffers: NativeAddress?
            get() = struct.buffers?.let(::NativeAddress)
            set(value) { struct.buffers = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var bufferCount: ULong
            get() = struct.bufferCount
            set(value) { struct.bufferCount = value }
        override var samplers: NativeAddress?
            get() = struct.samplers?.let(::NativeAddress)
            set(value) { struct.samplers = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var samplerCount: ULong
            get() = struct.samplerCount
            set(value) { struct.samplerCount = value }
        override var textureViews: NativeAddress?
            get() = struct.textureViews?.let(::NativeAddress)
            set(value) { struct.textureViews = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var textureViewCount: ULong
            get() = struct.textureViewCount
            set(value) { struct.textureViewCount = value }
    }
}

fun WGPUBindGroupEntryExtras.toCValue(): CValue<webgpu.native.WGPUBindGroupEntryExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.buffers = this@toCValue.buffers?.pointer?.takeIf { this@toCValue.buffers?.rawValue != 0L }?.reinterpret()
    this.bufferCount = this@toCValue.bufferCount
    this.samplers = this@toCValue.samplers?.pointer?.takeIf { this@toCValue.samplers?.rawValue != 0L }?.reinterpret()
    this.samplerCount = this@toCValue.samplerCount
    this.textureViews = this@toCValue.textureViews?.pointer?.takeIf { this@toCValue.textureViews?.rawValue != 0L }?.reinterpret()
    this.textureViewCount = this@toCValue.textureViewCount
}

actual interface WGPUBindGroupLayoutEntryExtras {
    actual var chain: WGPUChainedStruct
    actual var count: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntryExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntryExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUBindGroupLayoutEntryExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntryExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUBindGroupLayoutEntryExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUBindGroupLayoutEntryExtras>) : WGPUBindGroupLayoutEntryExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var count: UInt
            get() = handle.useContents { this.count }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUBindGroupLayoutEntryExtras {
        private val struct: webgpu.native.WGPUBindGroupLayoutEntryExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUBindGroupLayoutEntryExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var count: UInt
            get() = struct.count
            set(value) { struct.count = value }
    }
}

fun WGPUBindGroupLayoutEntryExtras.toCValue(): CValue<webgpu.native.WGPUBindGroupLayoutEntryExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.count = this@toCValue.count
}

actual interface WGPUQuerySetDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var pipelineStatistics: NativeAddress?
    actual var pipelineStatisticCount: ULong
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptorExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUQuerySetDescriptorExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptorExtras) -> Unit): ArrayHolder<WGPUQuerySetDescriptorExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUQuerySetDescriptorExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUQuerySetDescriptorExtras>) : WGPUQuerySetDescriptorExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var pipelineStatistics: NativeAddress?
            get() = handle.useContents { this.pipelineStatistics?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        override var pipelineStatisticCount: ULong
            get() = handle.useContents { this.pipelineStatisticCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUQuerySetDescriptorExtras {
        private val struct: webgpu.native.WGPUQuerySetDescriptorExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUQuerySetDescriptorExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var pipelineStatistics: NativeAddress?
            get() = struct.pipelineStatistics?.let(::NativeAddress)
            set(value) { struct.pipelineStatistics = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
        override var pipelineStatisticCount: ULong
            get() = struct.pipelineStatisticCount
            set(value) { struct.pipelineStatisticCount = value }
    }
}

fun WGPUQuerySetDescriptorExtras.toCValue(): CValue<webgpu.native.WGPUQuerySetDescriptorExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.pipelineStatistics = this@toCValue.pipelineStatistics?.pointer?.takeIf { this@toCValue.pipelineStatistics?.rawValue != 0L }?.reinterpret()
    this.pipelineStatisticCount = this@toCValue.pipelineStatisticCount
}

actual interface WGPUSurfaceConfigurationExtras {
    actual var chain: WGPUChainedStruct
    actual var desiredMaximumFrameLatency: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceConfigurationExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfigurationExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceConfigurationExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfigurationExtras) -> Unit): ArrayHolder<WGPUSurfaceConfigurationExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceConfigurationExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceConfigurationExtras>) : WGPUSurfaceConfigurationExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var desiredMaximumFrameLatency: UInt
            get() = handle.useContents { this.desiredMaximumFrameLatency }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceConfigurationExtras {
        private val struct: webgpu.native.WGPUSurfaceConfigurationExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceConfigurationExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var desiredMaximumFrameLatency: UInt
            get() = struct.desiredMaximumFrameLatency
            set(value) { struct.desiredMaximumFrameLatency = value }
    }
}

fun WGPUSurfaceConfigurationExtras.toCValue(): CValue<webgpu.native.WGPUSurfaceConfigurationExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.desiredMaximumFrameLatency = this@toCValue.desiredMaximumFrameLatency
}

actual interface WGPUSurfaceSourceSwapChainPanel {
    actual var chain: WGPUChainedStruct
    actual var panelNative: NativeAddress?
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSurfaceSourceSwapChainPanel = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceSwapChainPanel =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSurfaceSourceSwapChainPanel>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceSwapChainPanel) -> Unit): ArrayHolder<WGPUSurfaceSourceSwapChainPanel> {
            val byteSize = sizeOf<webgpu.native.WGPUSurfaceSourceSwapChainPanel>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSurfaceSourceSwapChainPanel>) : WGPUSurfaceSourceSwapChainPanel {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var panelNative: NativeAddress?
            get() = handle.useContents { this.panelNative?.let(::NativeAddress) }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSurfaceSourceSwapChainPanel {
        private val struct: webgpu.native.WGPUSurfaceSourceSwapChainPanel
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSurfaceSourceSwapChainPanel>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var panelNative: NativeAddress?
            get() = struct.panelNative?.let(::NativeAddress)
            set(value) { struct.panelNative = value?.pointer?.takeIf { value.rawValue != 0L }?.reinterpret() }
    }
}

fun WGPUSurfaceSourceSwapChainPanel.toCValue(): CValue<webgpu.native.WGPUSurfaceSourceSwapChainPanel> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.panelNative = this@toCValue.panelNative?.pointer?.takeIf { this@toCValue.panelNative?.rawValue != 0L }?.reinterpret()
}

actual interface WGPUPrimitiveStateExtras {
    actual var chain: WGPUChainedStruct
    actual var polygonMode: WGPUPolygonMode
    actual var conservative: UInt
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUPrimitiveStateExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUPrimitiveStateExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUPrimitiveStateExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveStateExtras) -> Unit): ArrayHolder<WGPUPrimitiveStateExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUPrimitiveStateExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUPrimitiveStateExtras>) : WGPUPrimitiveStateExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var polygonMode: WGPUPolygonMode
            get() = handle.useContents { this.polygonMode as WGPUPolygonMode }
            set(value) { error("Setters not supported on ByValue") }
        override var conservative: UInt
            get() = handle.useContents { this.conservative }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUPrimitiveStateExtras {
        private val struct: webgpu.native.WGPUPrimitiveStateExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUPrimitiveStateExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var polygonMode: WGPUPolygonMode
            get() = struct.polygonMode as WGPUPolygonMode
            set(value) { struct.polygonMode = value }
        override var conservative: UInt
            get() = struct.conservative
            set(value) { struct.conservative = value }
    }
}

fun WGPUPrimitiveStateExtras.toCValue(): CValue<webgpu.native.WGPUPrimitiveStateExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.polygonMode = this@toCValue.polygonMode
    this.conservative = this@toCValue.conservative
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
        actual fun allocate(allocator: MemoryAllocator): WGPUImageSubresourceRange =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUImageSubresourceRange>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUImageSubresourceRange) -> Unit): ArrayHolder<WGPUImageSubresourceRange> {
            val byteSize = sizeOf<webgpu.native.WGPUImageSubresourceRange>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUImageSubresourceRange>) : WGPUImageSubresourceRange {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var aspect: WGPUTextureAspect
            get() = handle.useContents { this.aspect as WGPUTextureAspect }
            set(value) { error("Setters not supported on ByValue") }
        override var baseMipLevel: UInt
            get() = handle.useContents { this.baseMipLevel }
            set(value) { error("Setters not supported on ByValue") }
        override var mipLevelCount: UInt
            get() = handle.useContents { this.mipLevelCount }
            set(value) { error("Setters not supported on ByValue") }
        override var baseArrayLayer: UInt
            get() = handle.useContents { this.baseArrayLayer }
            set(value) { error("Setters not supported on ByValue") }
        override var arrayLayerCount: UInt
            get() = handle.useContents { this.arrayLayerCount }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUImageSubresourceRange {
        private val struct: webgpu.native.WGPUImageSubresourceRange
            get() = handler.pointer.reinterpret<webgpu.native.WGPUImageSubresourceRange>().pointed
        
        override var aspect: WGPUTextureAspect
            get() = struct.aspect as WGPUTextureAspect
            set(value) { struct.aspect = value }
        override var baseMipLevel: UInt
            get() = struct.baseMipLevel
            set(value) { struct.baseMipLevel = value }
        override var mipLevelCount: UInt
            get() = struct.mipLevelCount
            set(value) { struct.mipLevelCount = value }
        override var baseArrayLayer: UInt
            get() = struct.baseArrayLayer
            set(value) { struct.baseArrayLayer = value }
        override var arrayLayerCount: UInt
            get() = struct.arrayLayerCount
            set(value) { struct.arrayLayerCount = value }
    }
}

fun WGPUImageSubresourceRange.toCValue(): CValue<webgpu.native.WGPUImageSubresourceRange> = cValue {
    this.aspect = this@toCValue.aspect
    this.baseMipLevel = this@toCValue.baseMipLevel
    this.mipLevelCount = this@toCValue.mipLevelCount
    this.baseArrayLayer = this@toCValue.baseArrayLayer
    this.arrayLayerCount = this@toCValue.arrayLayerCount
}

actual interface WGPUSamplerDescriptorExtras {
    actual var chain: WGPUChainedStruct
    actual var samplerBorderColor: WGPUSamplerBorderColor
    actual val handler: NativeAddress
    actual companion object {
        actual operator fun invoke(address: NativeAddress): WGPUSamplerDescriptorExtras = ByReference(address)
        actual fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptorExtras =
            ByReference(allocator.allocate(sizeOf<webgpu.native.WGPUSamplerDescriptorExtras>().toLong()))
        
        actual fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptorExtras) -> Unit): ArrayHolder<WGPUSamplerDescriptorExtras> {
            val byteSize = sizeOf<webgpu.native.WGPUSamplerDescriptorExtras>().toLong()
            val segment = allocator.allocate(byteSize * size.toLong())
            for (i in 0 until size.toInt()) {
                val rawAddr = segment.rawValue + i.toLong() * byteSize
                provider(i.toUInt(), ByReference(NativeAddress(rawAddr)))
            }
            return ArrayHolder(segment)
        }
    }
    
        value class ByValue(val handle: CValue<webgpu.native.WGPUSamplerDescriptorExtras>) : WGPUSamplerDescriptorExtras {
        override val handler: NativeAddress
            get() = error("should not be call on CValue")
        
        override var chain: WGPUChainedStruct
            get() = handle.useContents { WGPUChainedStruct.ByReference(NativeAddress(this.chain.ptr)) }
            set(value) { error("Setters not supported on ByValue") }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = handle.useContents { this.samplerBorderColor as WGPUSamplerBorderColor }
            set(value) { error("Setters not supported on ByValue") }
        }
    
    class ByReference(override val handler: NativeAddress) : WGPUSamplerDescriptorExtras {
        private val struct: webgpu.native.WGPUSamplerDescriptorExtras
            get() = handler.pointer.reinterpret<webgpu.native.WGPUSamplerDescriptorExtras>().pointed
        
        override var chain: WGPUChainedStruct
            get() = WGPUChainedStruct.ByReference(NativeAddress(struct.chain.ptr))
            set(value) {
                val destBytes = struct.chain.ptr.reinterpret<ByteVar>()
                val srcBytes = value.handler.pointer.reinterpret<ByteVar>()
                val byteSize = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
                for (i in 0L until byteSize) {
                    destBytes[i.toInt()] = srcBytes[i.toInt()]
                }
            }
        override var samplerBorderColor: WGPUSamplerBorderColor
            get() = struct.samplerBorderColor as WGPUSamplerBorderColor
            set(value) { struct.samplerBorderColor = value }
    }
}

fun WGPUSamplerDescriptorExtras.toCValue(): CValue<webgpu.native.WGPUSamplerDescriptorExtras> = cValue {
    val dest_chain = this.chain.ptr.reinterpret<ByteVar>()
    val src_chain = this@toCValue.chain.handler.pointer.reinterpret<ByteVar>()
    val size_chain = sizeOf<webgpu.native.WGPUChainedStruct>().toLong()
    for (i in 0L until size_chain) {
        dest_chain[i.toInt()] = src_chain[i.toInt()]
    }
    this.samplerBorderColor = this@toCValue.samplerBorderColor
}

actual fun wgpuGenerateReport(instance: WGPUInstance?, report: WGPUGlobalReport?): Unit {
    webgpu.native.wgpuGenerateReport(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), report?.handler?.pointer?.takeIf { report.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuInstanceEnumerateAdapters(instance: WGPUInstance?, options: WGPUInstanceEnumerateAdapterOptions?, adapters: NativeAddress?): ULong {
    return webgpu.native.wgpuInstanceEnumerateAdapters(instance?.handler?.pointer?.takeIf { instance.handler.rawValue != 0L }?.reinterpret(), options?.handler?.pointer?.takeIf { options.handler.rawValue != 0L }?.reinterpret(), adapters?.pointer?.takeIf { adapters.rawValue != 0L }?.reinterpret())
}

actual fun wgpuQueueSubmitForIndex(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): ULong {
    return webgpu.native.wgpuQueueSubmitForIndex(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret(), commandCount, commands?.pointer?.takeIf { commands.rawValue != 0L }?.reinterpret())
}

actual fun wgpuQueueGetTimestampPeriod(queue: WGPUQueue?): Float {
    return webgpu.native.wgpuQueueGetTimestampPeriod(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuDevicePoll(device: WGPUDevice?, wait: UInt, submissionIndex: NativeAddress?): UInt {
    return webgpu.native.wgpuDevicePoll(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), wait, submissionIndex?.pointer?.takeIf { submissionIndex.rawValue != 0L }?.reinterpret<ULongVar>())
}

actual fun wgpuDeviceCreateShaderModuleSpirV(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptorSpirV?): WGPUShaderModule? {
    return webgpu.native.wgpuDeviceCreateShaderModuleSpirV(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

actual fun wgpuSetLogCallback(callback: NativeAddress?, userdata: NativeAddress?): Unit {
    webgpu.native.wgpuSetLogCallback(callback?.pointer?.takeIf { callback.rawValue != 0L }?.reinterpret(), userdata?.pointer?.takeIf { userdata.rawValue != 0L })
    return
}

actual fun wgpuSetLogLevel(level: WGPULogLevel): Unit {
    webgpu.native.wgpuSetLogLevel(level)
    return
}

actual fun wgpuGetVersion(): UInt {
    return webgpu.native.wgpuGetVersion()
}

actual fun wgpuDeviceGetNativeMetalDevice(device: WGPUDevice?): NativeAddress? {
    return webgpu.native.wgpuDeviceGetNativeMetalDevice(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)
}

actual fun wgpuQueueGetNativeMetalCommandQueue(queue: WGPUQueue?): NativeAddress? {
    return webgpu.native.wgpuQueueGetNativeMetalCommandQueue(queue?.handler?.pointer?.takeIf { queue.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)
}

actual fun wgpuTextureGetNativeMetalTexture(texture: WGPUTexture?): NativeAddress? {
    return webgpu.native.wgpuTextureGetNativeMetalTexture(texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret())?.let(::NativeAddress)
}

actual fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderMultiDrawIndirect(encoder?.handler?.pointer?.takeIf { encoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, count)
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder?.handler?.pointer?.takeIf { encoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, count)
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderMultiDrawIndirectCount(encoder?.handler?.pointer?.takeIf { encoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, count_buffer?.handler?.pointer?.takeIf { count_buffer.handler.rawValue != 0L }?.reinterpret(), count_buffer_offset, max_count)
    return
}

actual fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder?.handler?.pointer?.takeIf { encoder.handler.rawValue != 0L }?.reinterpret(), buffer?.handler?.pointer?.takeIf { buffer.handler.rawValue != 0L }?.reinterpret(), offset, count_buffer?.handler?.pointer?.takeIf { count_buffer.handler.rawValue != 0L }?.reinterpret(), count_buffer_offset, max_count)
    return
}

actual fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    webgpu.native.wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?): Unit {
    webgpu.native.wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit {
    webgpu.native.wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    webgpu.native.wgpuComputePassEncoderWriteTimestamp(computePassEncoder?.handler?.pointer?.takeIf { computePassEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit {
    webgpu.native.wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder?.handler?.pointer?.takeIf { renderPassEncoder.handler.rawValue != 0L }?.reinterpret(), querySet?.handler?.pointer?.takeIf { querySet.handler.rawValue != 0L }?.reinterpret(), queryIndex)
    return
}

actual fun wgpuDeviceStartGraphicsDebuggerCapture(device: WGPUDevice?): UInt {
    return webgpu.native.wgpuDeviceStartGraphicsDebuggerCapture(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())
}

actual fun wgpuDeviceStopGraphicsDebuggerCapture(device: WGPUDevice?): Unit {
    webgpu.native.wgpuDeviceStopGraphicsDebuggerCapture(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuCommandEncoderClearTexture(commandEncoder: WGPUCommandEncoder?, texture: WGPUTexture?, range: WGPUImageSubresourceRange?): Unit {
    webgpu.native.wgpuCommandEncoderClearTexture(commandEncoder?.handler?.pointer?.takeIf { commandEncoder.handler.rawValue != 0L }?.reinterpret(), texture?.handler?.pointer?.takeIf { texture.handler.rawValue != 0L }?.reinterpret(), range?.handler?.pointer?.takeIf { range.handler.rawValue != 0L }?.reinterpret())
    return
}

actual fun wgpuDeviceCreateShaderModuleTrusted(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?, runtimeChecks: ULong): WGPUShaderModule? {
    return webgpu.native.wgpuDeviceCreateShaderModuleTrusted(device?.handler?.pointer?.takeIf { device.handler.rawValue != 0L }?.reinterpret(), descriptor?.handler?.pointer?.takeIf { descriptor.handler.rawValue != 0L }?.reinterpret(), runtimeChecks)?.let(::NativeAddress)?.let { WGPUShaderModule(it) }
}

@OptIn(CallbackRuntimeApi::class)
private val WGPUProcTrampoline = staticCFunction<Unit> {
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

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUProc.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUProc,
): CallbackRegistration<WGPUProc> = CallbackRuntime.register(
    type = WGPUProcType,
    trampoline = NativeAddress(WGPUProcTrampoline),
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
    trampoline = NativeAddress(WGPUProcTrampoline),
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
    trampoline = NativeAddress(WGPUProcTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUBufferMapCallbackTrampoline = staticCFunction<UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUBufferMapCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUMapAsyncStatus,
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUBufferMapCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUBufferMapCallback,
): CallbackRegistration<WGPUBufferMapCallback> = CallbackRuntime.register(
    type = WGPUBufferMapCallbackType,
    trampoline = NativeAddress(WGPUBufferMapCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUBufferMapCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUCompilationInfoCallbackTrampoline = staticCFunction<UInt, COpaquePointer?, COpaquePointer?, COpaquePointer?, Unit> { status, compilationInfo, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUCompilationInfoCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUCompilationInfoRequestStatus,
                compilationInfo?.let(::NativeAddress),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCompilationInfoCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCompilationInfoCallback,
): CallbackRegistration<WGPUCompilationInfoCallback> = CallbackRuntime.register(
    type = WGPUCompilationInfoCallbackType,
    trampoline = NativeAddress(WGPUCompilationInfoCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUCompilationInfoCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUCreateComputePipelineAsyncCallbackTrampoline = staticCFunction<UInt, COpaquePointer?, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, pipeline, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUCreateComputePipelineAsyncCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUCreatePipelineAsyncStatus,
                pipeline?.let(::NativeAddress)?.let { WGPUComputePipeline(it) },
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCreateComputePipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateComputePipelineAsyncCallback,
): CallbackRegistration<WGPUCreateComputePipelineAsyncCallback> = CallbackRuntime.register(
    type = WGPUCreateComputePipelineAsyncCallbackType,
    trampoline = NativeAddress(WGPUCreateComputePipelineAsyncCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUCreateComputePipelineAsyncCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUCreateRenderPipelineAsyncCallbackTrampoline = staticCFunction<UInt, COpaquePointer?, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, pipeline, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUCreateRenderPipelineAsyncCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUCreatePipelineAsyncStatus,
                pipeline?.let(::NativeAddress)?.let { WGPURenderPipeline(it) },
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUCreateRenderPipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUCreateRenderPipelineAsyncCallback,
): CallbackRegistration<WGPUCreateRenderPipelineAsyncCallback> = CallbackRuntime.register(
    type = WGPUCreateRenderPipelineAsyncCallbackType,
    trampoline = NativeAddress(WGPUCreateRenderPipelineAsyncCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUCreateRenderPipelineAsyncCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUDeviceLostCallbackTrampoline = staticCFunction<COpaquePointer?, UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { device, reason, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUDeviceLostCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                device?.let(::NativeAddress),
                reason.toUInt() as WGPUDeviceLostReason,
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUDeviceLostCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUDeviceLostCallback,
): CallbackRegistration<WGPUDeviceLostCallback> = CallbackRuntime.register(
    type = WGPUDeviceLostCallbackType,
    trampoline = NativeAddress(WGPUDeviceLostCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUDeviceLostCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUPopErrorScopeCallbackTrampoline = staticCFunction<UInt, UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, type, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUPopErrorScopeCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUPopErrorScopeStatus,
                type.toUInt() as WGPUErrorType,
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUPopErrorScopeCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUPopErrorScopeCallback,
): CallbackRegistration<WGPUPopErrorScopeCallback> = CallbackRuntime.register(
    type = WGPUPopErrorScopeCallbackType,
    trampoline = NativeAddress(WGPUPopErrorScopeCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUPopErrorScopeCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUQueueWorkDoneCallbackTrampoline = staticCFunction<UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUQueueWorkDoneCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPUQueueWorkDoneStatus,
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUQueueWorkDoneCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUQueueWorkDoneCallback,
): CallbackRegistration<WGPUQueueWorkDoneCallback> = CallbackRuntime.register(
    type = WGPUQueueWorkDoneCallbackType,
    trampoline = NativeAddress(WGPUQueueWorkDoneCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUQueueWorkDoneCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPURequestAdapterCallbackTrampoline = staticCFunction<UInt, COpaquePointer?, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, adapter, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPURequestAdapterCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPURequestAdapterStatus,
                adapter?.let(::NativeAddress)?.let { WGPUAdapter(it) },
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPURequestAdapterCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestAdapterCallback,
): CallbackRegistration<WGPURequestAdapterCallback> = CallbackRuntime.register(
    type = WGPURequestAdapterCallbackType,
    trampoline = NativeAddress(WGPURequestAdapterCallbackTrampoline),
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
    trampoline = NativeAddress(WGPURequestAdapterCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPURequestDeviceCallbackTrampoline = staticCFunction<UInt, COpaquePointer?, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { status, device, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPURequestDeviceCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                status.toUInt() as WGPURequestDeviceStatus,
                device?.let(::NativeAddress)?.let { WGPUDevice(it) },
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPURequestDeviceCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPURequestDeviceCallback,
): CallbackRegistration<WGPURequestDeviceCallback> = CallbackRuntime.register(
    type = WGPURequestDeviceCallbackType,
    trampoline = NativeAddress(WGPURequestDeviceCallbackTrampoline),
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
    trampoline = NativeAddress(WGPURequestDeviceCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPUUncapturedErrorCallbackTrampoline = staticCFunction<COpaquePointer?, UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, COpaquePointer?, Unit> { device, type, message, userdata1, userdata2 ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPUUncapturedErrorCallbackType,
            userdata = userdata2?.let(::NativeAddress),
        ) { callback ->
            callback.invoke(
                device?.let(::NativeAddress),
                type.toUInt() as WGPUErrorType,
                WGPUStringView.ByValue(message),
                userdata1?.let(::NativeAddress),
            )
        }
    } catch (failure: Throwable) {
        CallbackRuntime.reportUnroutedFailure(failure)
    }
}

@OptIn(CallbackRuntimeApi::class)
actual fun WGPUUncapturedErrorCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPUUncapturedErrorCallback,
): CallbackRegistration<WGPUUncapturedErrorCallback> = CallbackRuntime.register(
    type = WGPUUncapturedErrorCallbackType,
    trampoline = NativeAddress(WGPUUncapturedErrorCallbackTrampoline),
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
    trampoline = NativeAddress(WGPUUncapturedErrorCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

@OptIn(CallbackRuntimeApi::class)
private val WGPULogCallbackTrampoline = staticCFunction<UInt, CValue<webgpu.native.WGPUStringView>, COpaquePointer?, Unit> { level, message, userdata ->
    try {
        CallbackRuntime.dispatchSafely(
            type = WGPULogCallbackType,
            userdata = userdata?.let(::NativeAddress),
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

@OptIn(CallbackRuntimeApi::class)
actual fun WGPULogCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler,
    callback: WGPULogCallback,
): CallbackRegistration<WGPULogCallback> = CallbackRuntime.register(
    type = WGPULogCallbackType,
    trampoline = NativeAddress(WGPULogCallbackTrampoline),
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
    trampoline = NativeAddress(WGPULogCallbackTrampoline),
    policy = policy,
    onError = onError,
    callback = callback,
)

internal actual fun wgpuSetLogCallbackCallbackBindingPreflight(): (NativeAddress?, NativeAddress?) -> Unit {
    return { callback, userdata ->
        webgpu.native.wgpuSetLogCallback(
            callback?.pointer?.takeIf { callback.rawValue != 0L }?.reinterpret(),
            userdata?.pointer?.takeIf { userdata.rawValue != 0L },
        )
    }
}

