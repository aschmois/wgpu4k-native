package io.ygdrasil.wgpu.android

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Union
import kotlin.jvm.JvmField

open class WGPUStringView(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var data: Pointer? = null
    @JvmField var length: Long = 0L
    override fun getFieldOrder() = listOf<String>("data", "length")
    class ByReference(pointer: Pointer? = null) : WGPUStringView(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUStringView(pointer), Structure.ByValue
}

open class WGPUChainedStruct(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var next: Pointer? = null
    @JvmField var sType: Int = 0
    override fun getFieldOrder() = listOf<String>("next", "sType")
    class ByReference(pointer: Pointer? = null) : WGPUChainedStruct(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUChainedStruct(pointer), Structure.ByValue
}

open class WGPUBufferMapCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUBufferMapCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBufferMapCallbackInfo(pointer), Structure.ByValue
}

open class WGPUCompilationInfoCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUCompilationInfoCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCompilationInfoCallbackInfo(pointer), Structure.ByValue
}

open class WGPUCreateComputePipelineAsyncCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUCreateComputePipelineAsyncCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCreateComputePipelineAsyncCallbackInfo(pointer), Structure.ByValue
}

open class WGPUCreateRenderPipelineAsyncCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUCreateRenderPipelineAsyncCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCreateRenderPipelineAsyncCallbackInfo(pointer), Structure.ByValue
}

open class WGPUDeviceLostCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUDeviceLostCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUDeviceLostCallbackInfo(pointer), Structure.ByValue
}

open class WGPUPopErrorScopeCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUPopErrorScopeCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUPopErrorScopeCallbackInfo(pointer), Structure.ByValue
}

open class WGPUQueueWorkDoneCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUQueueWorkDoneCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUQueueWorkDoneCallbackInfo(pointer), Structure.ByValue
}

open class WGPURequestAdapterCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPURequestAdapterCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURequestAdapterCallbackInfo(pointer), Structure.ByValue
}

open class WGPURequestDeviceCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var mode: Int = 0
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "mode", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPURequestDeviceCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURequestDeviceCallbackInfo(pointer), Structure.ByValue
}

open class WGPUUncapturedErrorCallbackInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var callback: Pointer? = null
    @JvmField var userdata1: Pointer? = null
    @JvmField var userdata2: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "callback", "userdata1", "userdata2")
    class ByReference(pointer: Pointer? = null) : WGPUUncapturedErrorCallbackInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUUncapturedErrorCallbackInfo(pointer), Structure.ByValue
}

open class WGPUAdapterInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var vendor: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var architecture: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var device: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var description: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var backendType: Int = 0
    @JvmField var adapterType: Int = 0
    @JvmField var vendorID: Int = 0
    @JvmField var deviceID: Int = 0
    @JvmField var subgroupMinSize: Int = 0
    @JvmField var subgroupMaxSize: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "vendor", "architecture", "device", "description", "backendType", "adapterType", "vendorID", "deviceID", "subgroupMinSize", "subgroupMaxSize")
    class ByReference(pointer: Pointer? = null) : WGPUAdapterInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUAdapterInfo(pointer), Structure.ByValue
}

open class WGPUBlendComponent(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var operation: Int = 0
    @JvmField var srcFactor: Int = 0
    @JvmField var dstFactor: Int = 0
    override fun getFieldOrder() = listOf<String>("operation", "srcFactor", "dstFactor")
    class ByReference(pointer: Pointer? = null) : WGPUBlendComponent(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBlendComponent(pointer), Structure.ByValue
}

open class WGPUBufferBindingLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var type: Int = 0
    @JvmField var hasDynamicOffset: Int = 0
    @JvmField var minBindingSize: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "type", "hasDynamicOffset", "minBindingSize")
    class ByReference(pointer: Pointer? = null) : WGPUBufferBindingLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBufferBindingLayout(pointer), Structure.ByValue
}

open class WGPUBufferDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var usage: Long = 0L
    @JvmField var size: Long = 0L
    @JvmField var mappedAtCreation: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "usage", "size", "mappedAtCreation")
    class ByReference(pointer: Pointer? = null) : WGPUBufferDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBufferDescriptor(pointer), Structure.ByValue
}

open class WGPUColor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var r: Double = 0.0
    @JvmField var g: Double = 0.0
    @JvmField var b: Double = 0.0
    @JvmField var a: Double = 0.0
    override fun getFieldOrder() = listOf<String>("r", "g", "b", "a")
    class ByReference(pointer: Pointer? = null) : WGPUColor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUColor(pointer), Structure.ByValue
}

open class WGPUCommandBufferDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPUCommandBufferDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCommandBufferDescriptor(pointer), Structure.ByValue
}

open class WGPUCommandEncoderDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPUCommandEncoderDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCommandEncoderDescriptor(pointer), Structure.ByValue
}

open class WGPUCompatibilityModeLimits(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var maxStorageBuffersInVertexStage: Int = 0
    @JvmField var maxStorageTexturesInVertexStage: Int = 0
    @JvmField var maxStorageBuffersInFragmentStage: Int = 0
    @JvmField var maxStorageTexturesInFragmentStage: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "maxStorageBuffersInVertexStage", "maxStorageTexturesInVertexStage", "maxStorageBuffersInFragmentStage", "maxStorageTexturesInFragmentStage")
    class ByReference(pointer: Pointer? = null) : WGPUCompatibilityModeLimits(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCompatibilityModeLimits(pointer), Structure.ByValue
}

open class WGPUCompilationMessage(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var message: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var type: Int = 0
    @JvmField var lineNum: Long = 0L
    @JvmField var linePos: Long = 0L
    @JvmField var offset: Long = 0L
    @JvmField var length: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "message", "type", "lineNum", "linePos", "offset", "length")
    class ByReference(pointer: Pointer? = null) : WGPUCompilationMessage(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCompilationMessage(pointer), Structure.ByValue
}

open class WGPUConstantEntry(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var key: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var value: Double = 0.0
    override fun getFieldOrder() = listOf<String>("nextInChain", "key", "value")
    class ByReference(pointer: Pointer? = null) : WGPUConstantEntry(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUConstantEntry(pointer), Structure.ByValue
}

open class WGPUExtent3D(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var width: Int = 0
    @JvmField var height: Int = 0
    @JvmField var depthOrArrayLayers: Int = 0
    override fun getFieldOrder() = listOf<String>("width", "height", "depthOrArrayLayers")
    class ByReference(pointer: Pointer? = null) : WGPUExtent3D(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUExtent3D(pointer), Structure.ByValue
}

open class WGPUExternalTextureBindingEntry(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var externalTexture: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "externalTexture")
    class ByReference(pointer: Pointer? = null) : WGPUExternalTextureBindingEntry(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUExternalTextureBindingEntry(pointer), Structure.ByValue
}

open class WGPUExternalTextureBindingLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    override fun getFieldOrder() = listOf<String>("chain")
    class ByReference(pointer: Pointer? = null) : WGPUExternalTextureBindingLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUExternalTextureBindingLayout(pointer), Structure.ByValue
}

open class WGPUFuture(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var id: Long = 0L
    override fun getFieldOrder() = listOf<String>("id")
    class ByReference(pointer: Pointer? = null) : WGPUFuture(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUFuture(pointer), Structure.ByValue
}

open class WGPUInstanceLimits(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var timedWaitAnyMaxCount: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "timedWaitAnyMaxCount")
    class ByReference(pointer: Pointer? = null) : WGPUInstanceLimits(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUInstanceLimits(pointer), Structure.ByValue
}

open class WGPUMultisampleState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var count: Int = 0
    @JvmField var mask: Int = 0
    @JvmField var alphaToCoverageEnabled: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "count", "mask", "alphaToCoverageEnabled")
    class ByReference(pointer: Pointer? = null) : WGPUMultisampleState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUMultisampleState(pointer), Structure.ByValue
}

open class WGPUOrigin3D(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var x: Int = 0
    @JvmField var y: Int = 0
    @JvmField var z: Int = 0
    override fun getFieldOrder() = listOf<String>("x", "y", "z")
    class ByReference(pointer: Pointer? = null) : WGPUOrigin3D(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUOrigin3D(pointer), Structure.ByValue
}

open class WGPUPassTimestampWrites(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var querySet: Pointer? = null
    @JvmField var beginningOfPassWriteIndex: Int = 0
    @JvmField var endOfPassWriteIndex: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "querySet", "beginningOfPassWriteIndex", "endOfPassWriteIndex")
    class ByReference(pointer: Pointer? = null) : WGPUPassTimestampWrites(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUPassTimestampWrites(pointer), Structure.ByValue
}

open class WGPUPipelineLayoutDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var bindGroupLayoutCount: Long = 0L
    @JvmField var bindGroupLayouts: Pointer? = null
    @JvmField var immediateSize: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "bindGroupLayoutCount", "bindGroupLayouts", "immediateSize")
    class ByReference(pointer: Pointer? = null) : WGPUPipelineLayoutDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUPipelineLayoutDescriptor(pointer), Structure.ByValue
}

open class WGPUPrimitiveState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var topology: Int = 0
    @JvmField var stripIndexFormat: Int = 0
    @JvmField var frontFace: Int = 0
    @JvmField var cullMode: Int = 0
    @JvmField var unclippedDepth: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "topology", "stripIndexFormat", "frontFace", "cullMode", "unclippedDepth")
    class ByReference(pointer: Pointer? = null) : WGPUPrimitiveState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUPrimitiveState(pointer), Structure.ByValue
}

open class WGPUQuerySetDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var type: Int = 0
    @JvmField var count: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "type", "count")
    class ByReference(pointer: Pointer? = null) : WGPUQuerySetDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUQuerySetDescriptor(pointer), Structure.ByValue
}

open class WGPUQueueDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPUQueueDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUQueueDescriptor(pointer), Structure.ByValue
}

open class WGPURenderBundleDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPURenderBundleDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderBundleDescriptor(pointer), Structure.ByValue
}

open class WGPURenderBundleEncoderDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var colorFormatCount: Long = 0L
    @JvmField var colorFormats: Pointer? = null
    @JvmField var depthStencilFormat: Int = 0
    @JvmField var sampleCount: Int = 0
    @JvmField var depthReadOnly: Int = 0
    @JvmField var stencilReadOnly: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "colorFormatCount", "colorFormats", "depthStencilFormat", "sampleCount", "depthReadOnly", "stencilReadOnly")
    class ByReference(pointer: Pointer? = null) : WGPURenderBundleEncoderDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderBundleEncoderDescriptor(pointer), Structure.ByValue
}

open class WGPURenderPassDepthStencilAttachment(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var view: Pointer? = null
    @JvmField var depthLoadOp: Int = 0
    @JvmField var depthStoreOp: Int = 0
    @JvmField var depthClearValue: Float = 0.0f
    @JvmField var depthReadOnly: Int = 0
    @JvmField var stencilLoadOp: Int = 0
    @JvmField var stencilStoreOp: Int = 0
    @JvmField var stencilClearValue: Int = 0
    @JvmField var stencilReadOnly: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "view", "depthLoadOp", "depthStoreOp", "depthClearValue", "depthReadOnly", "stencilLoadOp", "stencilStoreOp", "stencilClearValue", "stencilReadOnly")
    class ByReference(pointer: Pointer? = null) : WGPURenderPassDepthStencilAttachment(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderPassDepthStencilAttachment(pointer), Structure.ByValue
}

open class WGPURenderPassMaxDrawCount(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var maxDrawCount: Long = 0L
    override fun getFieldOrder() = listOf<String>("chain", "maxDrawCount")
    class ByReference(pointer: Pointer? = null) : WGPURenderPassMaxDrawCount(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderPassMaxDrawCount(pointer), Structure.ByValue
}

open class WGPURequestAdapterWebXROptions(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var xrCompatible: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "xrCompatible")
    class ByReference(pointer: Pointer? = null) : WGPURequestAdapterWebXROptions(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURequestAdapterWebXROptions(pointer), Structure.ByValue
}

open class WGPUSamplerBindingLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var type: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "type")
    class ByReference(pointer: Pointer? = null) : WGPUSamplerBindingLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSamplerBindingLayout(pointer), Structure.ByValue
}

open class WGPUSamplerDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var addressModeU: Int = 0
    @JvmField var addressModeV: Int = 0
    @JvmField var addressModeW: Int = 0
    @JvmField var magFilter: Int = 0
    @JvmField var minFilter: Int = 0
    @JvmField var mipmapFilter: Int = 0
    @JvmField var lodMinClamp: Float = 0.0f
    @JvmField var lodMaxClamp: Float = 0.0f
    @JvmField var compare: Int = 0
    @JvmField var maxAnisotropy: Short = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "addressModeU", "addressModeV", "addressModeW", "magFilter", "minFilter", "mipmapFilter", "lodMinClamp", "lodMaxClamp", "compare", "maxAnisotropy")
    class ByReference(pointer: Pointer? = null) : WGPUSamplerDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSamplerDescriptor(pointer), Structure.ByValue
}

open class WGPUShaderSourceSPIRV(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var codeSize: Int = 0
    @JvmField var code: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "codeSize", "code")
    class ByReference(pointer: Pointer? = null) : WGPUShaderSourceSPIRV(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderSourceSPIRV(pointer), Structure.ByValue
}

open class WGPUShaderSourceWGSL(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var code: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("chain", "code")
    class ByReference(pointer: Pointer? = null) : WGPUShaderSourceWGSL(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderSourceWGSL(pointer), Structure.ByValue
}

open class WGPUStencilFaceState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var compare: Int = 0
    @JvmField var failOp: Int = 0
    @JvmField var depthFailOp: Int = 0
    @JvmField var passOp: Int = 0
    override fun getFieldOrder() = listOf<String>("compare", "failOp", "depthFailOp", "passOp")
    class ByReference(pointer: Pointer? = null) : WGPUStencilFaceState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUStencilFaceState(pointer), Structure.ByValue
}

open class WGPUStorageTextureBindingLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var access: Int = 0
    @JvmField var format: Int = 0
    @JvmField var viewDimension: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "access", "format", "viewDimension")
    class ByReference(pointer: Pointer? = null) : WGPUStorageTextureBindingLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUStorageTextureBindingLayout(pointer), Structure.ByValue
}

open class WGPUSupportedFeatures(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var featureCount: Long = 0L
    @JvmField var features: Pointer? = null
    override fun getFieldOrder() = listOf<String>("featureCount", "features")
    class ByReference(pointer: Pointer? = null) : WGPUSupportedFeatures(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSupportedFeatures(pointer), Structure.ByValue
}

open class WGPUSupportedInstanceFeatures(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var featureCount: Long = 0L
    @JvmField var features: Pointer? = null
    override fun getFieldOrder() = listOf<String>("featureCount", "features")
    class ByReference(pointer: Pointer? = null) : WGPUSupportedInstanceFeatures(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSupportedInstanceFeatures(pointer), Structure.ByValue
}

open class WGPUSupportedWGSLLanguageFeatures(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var featureCount: Long = 0L
    @JvmField var features: Pointer? = null
    override fun getFieldOrder() = listOf<String>("featureCount", "features")
    class ByReference(pointer: Pointer? = null) : WGPUSupportedWGSLLanguageFeatures(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSupportedWGSLLanguageFeatures(pointer), Structure.ByValue
}

open class WGPUSurfaceCapabilities(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var usages: Long = 0L
    @JvmField var formatCount: Long = 0L
    @JvmField var formats: Pointer? = null
    @JvmField var presentModeCount: Long = 0L
    @JvmField var presentModes: Pointer? = null
    @JvmField var alphaModeCount: Long = 0L
    @JvmField var alphaModes: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "usages", "formatCount", "formats", "presentModeCount", "presentModes", "alphaModeCount", "alphaModes")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceCapabilities(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceCapabilities(pointer), Structure.ByValue
}

open class WGPUSurfaceColorManagement(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var colorSpace: Int = 0
    @JvmField var toneMappingMode: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "colorSpace", "toneMappingMode")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceColorManagement(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceColorManagement(pointer), Structure.ByValue
}

open class WGPUSurfaceConfiguration(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var device: Pointer? = null
    @JvmField var format: Int = 0
    @JvmField var usage: Long = 0L
    @JvmField var width: Int = 0
    @JvmField var height: Int = 0
    @JvmField var viewFormatCount: Long = 0L
    @JvmField var viewFormats: Pointer? = null
    @JvmField var alphaMode: Int = 0
    @JvmField var presentMode: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "device", "format", "usage", "width", "height", "viewFormatCount", "viewFormats", "alphaMode", "presentMode")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceConfiguration(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceConfiguration(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceAndroidNativeWindow(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var window: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "window")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceAndroidNativeWindow(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceAndroidNativeWindow(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceMetalLayer(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var layer: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "layer")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceMetalLayer(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceMetalLayer(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceWaylandSurface(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var display: Pointer? = null
    @JvmField var surface: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "display", "surface")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceWaylandSurface(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceWaylandSurface(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceWindowsHWND(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var hinstance: Pointer? = null
    @JvmField var hwnd: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "hinstance", "hwnd")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceWindowsHWND(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceWindowsHWND(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceXCBWindow(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var connection: Pointer? = null
    @JvmField var window: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "connection", "window")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceXCBWindow(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceXCBWindow(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceXlibWindow(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var display: Pointer? = null
    @JvmField var window: Long = 0L
    override fun getFieldOrder() = listOf<String>("chain", "display", "window")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceXlibWindow(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceXlibWindow(pointer), Structure.ByValue
}

open class WGPUSurfaceTexture(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var texture: Pointer? = null
    @JvmField var status: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "texture", "status")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceTexture(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceTexture(pointer), Structure.ByValue
}

open class WGPUTexelCopyBufferLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var offset: Long = 0L
    @JvmField var bytesPerRow: Int = 0
    @JvmField var rowsPerImage: Int = 0
    override fun getFieldOrder() = listOf<String>("offset", "bytesPerRow", "rowsPerImage")
    class ByReference(pointer: Pointer? = null) : WGPUTexelCopyBufferLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTexelCopyBufferLayout(pointer), Structure.ByValue
}

open class WGPUTextureBindingLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var sampleType: Int = 0
    @JvmField var viewDimension: Int = 0
    @JvmField var multisampled: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "sampleType", "viewDimension", "multisampled")
    class ByReference(pointer: Pointer? = null) : WGPUTextureBindingLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureBindingLayout(pointer), Structure.ByValue
}

open class WGPUTextureBindingViewDimension(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var textureBindingViewDimension: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "textureBindingViewDimension")
    class ByReference(pointer: Pointer? = null) : WGPUTextureBindingViewDimension(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureBindingViewDimension(pointer), Structure.ByValue
}

open class WGPUTextureComponentSwizzle(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var r: Int = 0
    @JvmField var g: Int = 0
    @JvmField var b: Int = 0
    @JvmField var a: Int = 0
    override fun getFieldOrder() = listOf<String>("r", "g", "b", "a")
    class ByReference(pointer: Pointer? = null) : WGPUTextureComponentSwizzle(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureComponentSwizzle(pointer), Structure.ByValue
}

open class WGPUVertexAttribute(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var format: Int = 0
    @JvmField var offset: Long = 0L
    @JvmField var shaderLocation: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "format", "offset", "shaderLocation")
    class ByReference(pointer: Pointer? = null) : WGPUVertexAttribute(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUVertexAttribute(pointer), Structure.ByValue
}

open class WGPUBindGroupEntry(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var binding: Int = 0
    @JvmField var buffer: Pointer? = null
    @JvmField var offset: Long = 0L
    @JvmField var size: Long = 0L
    @JvmField var sampler: Pointer? = null
    @JvmField var textureView: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "binding", "buffer", "offset", "size", "sampler", "textureView")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupEntry(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupEntry(pointer), Structure.ByValue
}

open class WGPUBindGroupLayoutEntry(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var binding: Int = 0
    @JvmField var visibility: Long = 0L
    @JvmField var bindingArraySize: Int = 0
    @JvmField var buffer: io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUBufferBindingLayout.ByValue()
    @JvmField var sampler: io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUSamplerBindingLayout.ByValue()
    @JvmField var texture: io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUTextureBindingLayout.ByValue()
    @JvmField var storageTexture: io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue = io.ygdrasil.wgpu.android.WGPUStorageTextureBindingLayout.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "binding", "visibility", "bindingArraySize", "buffer", "sampler", "texture", "storageTexture")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupLayoutEntry(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupLayoutEntry(pointer), Structure.ByValue
}

open class WGPUBlendState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var color: io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue()
    @JvmField var alpha: io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue = io.ygdrasil.wgpu.android.WGPUBlendComponent.ByValue()
    override fun getFieldOrder() = listOf<String>("color", "alpha")
    class ByReference(pointer: Pointer? = null) : WGPUBlendState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBlendState(pointer), Structure.ByValue
}

open class WGPUCompilationInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var messageCount: Long = 0L
    @JvmField var messages: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "messageCount", "messages")
    class ByReference(pointer: Pointer? = null) : WGPUCompilationInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUCompilationInfo(pointer), Structure.ByValue
}

open class WGPUComputePassDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var timestampWrites: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "timestampWrites")
    class ByReference(pointer: Pointer? = null) : WGPUComputePassDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUComputePassDescriptor(pointer), Structure.ByValue
}

open class WGPUComputeState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var module: Pointer? = null
    @JvmField var entryPoint: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var constantCount: Long = 0L
    @JvmField var constants: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "module", "entryPoint", "constantCount", "constants")
    class ByReference(pointer: Pointer? = null) : WGPUComputeState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUComputeState(pointer), Structure.ByValue
}

open class WGPUDepthStencilState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var format: Int = 0
    @JvmField var depthWriteEnabled: Int = 0
    @JvmField var depthCompare: Int = 0
    @JvmField var stencilFront: io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue()
    @JvmField var stencilBack: io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue = io.ygdrasil.wgpu.android.WGPUStencilFaceState.ByValue()
    @JvmField var stencilReadMask: Int = 0
    @JvmField var stencilWriteMask: Int = 0
    @JvmField var depthBias: Int = 0
    @JvmField var depthBiasSlopeScale: Float = 0.0f
    @JvmField var depthBiasClamp: Float = 0.0f
    override fun getFieldOrder() = listOf<String>("nextInChain", "format", "depthWriteEnabled", "depthCompare", "stencilFront", "stencilBack", "stencilReadMask", "stencilWriteMask", "depthBias", "depthBiasSlopeScale", "depthBiasClamp")
    class ByReference(pointer: Pointer? = null) : WGPUDepthStencilState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUDepthStencilState(pointer), Structure.ByValue
}

open class WGPUFutureWaitInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var future: io.ygdrasil.wgpu.android.WGPUFuture.ByValue = io.ygdrasil.wgpu.android.WGPUFuture.ByValue()
    @JvmField var completed: Int = 0
    override fun getFieldOrder() = listOf<String>("future", "completed")
    class ByReference(pointer: Pointer? = null) : WGPUFutureWaitInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUFutureWaitInfo(pointer), Structure.ByValue
}

open class WGPUInstanceDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var requiredFeatureCount: Long = 0L
    @JvmField var requiredFeatures: Pointer? = null
    @JvmField var requiredLimits: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "requiredFeatureCount", "requiredFeatures", "requiredLimits")
    class ByReference(pointer: Pointer? = null) : WGPUInstanceDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUInstanceDescriptor(pointer), Structure.ByValue
}

open class WGPULimits(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var maxTextureDimension1D: Int = 0
    @JvmField var maxTextureDimension2D: Int = 0
    @JvmField var maxTextureDimension3D: Int = 0
    @JvmField var maxTextureArrayLayers: Int = 0
    @JvmField var maxBindGroups: Int = 0
    @JvmField var maxBindGroupsPlusVertexBuffers: Int = 0
    @JvmField var maxBindingsPerBindGroup: Int = 0
    @JvmField var maxDynamicUniformBuffersPerPipelineLayout: Int = 0
    @JvmField var maxDynamicStorageBuffersPerPipelineLayout: Int = 0
    @JvmField var maxSampledTexturesPerShaderStage: Int = 0
    @JvmField var maxSamplersPerShaderStage: Int = 0
    @JvmField var maxStorageBuffersPerShaderStage: Int = 0
    @JvmField var maxStorageTexturesPerShaderStage: Int = 0
    @JvmField var maxUniformBuffersPerShaderStage: Int = 0
    @JvmField var maxUniformBufferBindingSize: Long = 0L
    @JvmField var maxStorageBufferBindingSize: Long = 0L
    @JvmField var minUniformBufferOffsetAlignment: Int = 0
    @JvmField var minStorageBufferOffsetAlignment: Int = 0
    @JvmField var maxVertexBuffers: Int = 0
    @JvmField var maxBufferSize: Long = 0L
    @JvmField var maxVertexAttributes: Int = 0
    @JvmField var maxVertexBufferArrayStride: Int = 0
    @JvmField var maxInterStageShaderVariables: Int = 0
    @JvmField var maxColorAttachments: Int = 0
    @JvmField var maxColorAttachmentBytesPerSample: Int = 0
    @JvmField var maxComputeWorkgroupStorageSize: Int = 0
    @JvmField var maxComputeInvocationsPerWorkgroup: Int = 0
    @JvmField var maxComputeWorkgroupSizeX: Int = 0
    @JvmField var maxComputeWorkgroupSizeY: Int = 0
    @JvmField var maxComputeWorkgroupSizeZ: Int = 0
    @JvmField var maxComputeWorkgroupsPerDimension: Int = 0
    @JvmField var maxImmediateSize: Int = 0
    override fun getFieldOrder() = listOf<String>("nextInChain", "maxTextureDimension1D", "maxTextureDimension2D", "maxTextureDimension3D", "maxTextureArrayLayers", "maxBindGroups", "maxBindGroupsPlusVertexBuffers", "maxBindingsPerBindGroup", "maxDynamicUniformBuffersPerPipelineLayout", "maxDynamicStorageBuffersPerPipelineLayout", "maxSampledTexturesPerShaderStage", "maxSamplersPerShaderStage", "maxStorageBuffersPerShaderStage", "maxStorageTexturesPerShaderStage", "maxUniformBuffersPerShaderStage", "maxUniformBufferBindingSize", "maxStorageBufferBindingSize", "minUniformBufferOffsetAlignment", "minStorageBufferOffsetAlignment", "maxVertexBuffers", "maxBufferSize", "maxVertexAttributes", "maxVertexBufferArrayStride", "maxInterStageShaderVariables", "maxColorAttachments", "maxColorAttachmentBytesPerSample", "maxComputeWorkgroupStorageSize", "maxComputeInvocationsPerWorkgroup", "maxComputeWorkgroupSizeX", "maxComputeWorkgroupSizeY", "maxComputeWorkgroupSizeZ", "maxComputeWorkgroupsPerDimension", "maxImmediateSize")
    class ByReference(pointer: Pointer? = null) : WGPULimits(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPULimits(pointer), Structure.ByValue
}

open class WGPURenderPassColorAttachment(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var view: Pointer? = null
    @JvmField var depthSlice: Int = 0
    @JvmField var resolveTarget: Pointer? = null
    @JvmField var loadOp: Int = 0
    @JvmField var storeOp: Int = 0
    @JvmField var clearValue: io.ygdrasil.wgpu.android.WGPUColor.ByValue = io.ygdrasil.wgpu.android.WGPUColor.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "view", "depthSlice", "resolveTarget", "loadOp", "storeOp", "clearValue")
    class ByReference(pointer: Pointer? = null) : WGPURenderPassColorAttachment(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderPassColorAttachment(pointer), Structure.ByValue
}

open class WGPURequestAdapterOptions(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var featureLevel: Int = 0
    @JvmField var powerPreference: Int = 0
    @JvmField var forceFallbackAdapter: Int = 0
    @JvmField var backendType: Int = 0
    @JvmField var compatibleSurface: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "featureLevel", "powerPreference", "forceFallbackAdapter", "backendType", "compatibleSurface")
    class ByReference(pointer: Pointer? = null) : WGPURequestAdapterOptions(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURequestAdapterOptions(pointer), Structure.ByValue
}

open class WGPUShaderModuleDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPUShaderModuleDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderModuleDescriptor(pointer), Structure.ByValue
}

open class WGPUSurfaceDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceDescriptor(pointer), Structure.ByValue
}

open class WGPUTexelCopyBufferInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var layout: io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue = io.ygdrasil.wgpu.android.WGPUTexelCopyBufferLayout.ByValue()
    @JvmField var buffer: Pointer? = null
    override fun getFieldOrder() = listOf<String>("layout", "buffer")
    class ByReference(pointer: Pointer? = null) : WGPUTexelCopyBufferInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTexelCopyBufferInfo(pointer), Structure.ByValue
}

open class WGPUTexelCopyTextureInfo(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var texture: Pointer? = null
    @JvmField var mipLevel: Int = 0
    @JvmField var origin: io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue = io.ygdrasil.wgpu.android.WGPUOrigin3D.ByValue()
    @JvmField var aspect: Int = 0
    override fun getFieldOrder() = listOf<String>("texture", "mipLevel", "origin", "aspect")
    class ByReference(pointer: Pointer? = null) : WGPUTexelCopyTextureInfo(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTexelCopyTextureInfo(pointer), Structure.ByValue
}

open class WGPUTextureComponentSwizzleDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var swizzle: io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue = io.ygdrasil.wgpu.android.WGPUTextureComponentSwizzle.ByValue()
    override fun getFieldOrder() = listOf<String>("chain", "swizzle")
    class ByReference(pointer: Pointer? = null) : WGPUTextureComponentSwizzleDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureComponentSwizzleDescriptor(pointer), Structure.ByValue
}

open class WGPUTextureDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var usage: Long = 0L
    @JvmField var dimension: Int = 0
    @JvmField var size: io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue = io.ygdrasil.wgpu.android.WGPUExtent3D.ByValue()
    @JvmField var format: Int = 0
    @JvmField var mipLevelCount: Int = 0
    @JvmField var sampleCount: Int = 0
    @JvmField var viewFormatCount: Long = 0L
    @JvmField var viewFormats: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "usage", "dimension", "size", "format", "mipLevelCount", "sampleCount", "viewFormatCount", "viewFormats")
    class ByReference(pointer: Pointer? = null) : WGPUTextureDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureDescriptor(pointer), Structure.ByValue
}

open class WGPUVertexBufferLayout(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var stepMode: Int = 0
    @JvmField var arrayStride: Long = 0L
    @JvmField var attributeCount: Long = 0L
    @JvmField var attributes: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "stepMode", "arrayStride", "attributeCount", "attributes")
    class ByReference(pointer: Pointer? = null) : WGPUVertexBufferLayout(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUVertexBufferLayout(pointer), Structure.ByValue
}

open class WGPUBindGroupDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var layout: Pointer? = null
    @JvmField var entryCount: Long = 0L
    @JvmField var entries: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "layout", "entryCount", "entries")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupDescriptor(pointer), Structure.ByValue
}

open class WGPUBindGroupLayoutDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var entryCount: Long = 0L
    @JvmField var entries: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "entryCount", "entries")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupLayoutDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupLayoutDescriptor(pointer), Structure.ByValue
}

open class WGPUColorTargetState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var format: Int = 0
    @JvmField var blend: Pointer? = null
    @JvmField var writeMask: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "format", "blend", "writeMask")
    class ByReference(pointer: Pointer? = null) : WGPUColorTargetState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUColorTargetState(pointer), Structure.ByValue
}

open class WGPUComputePipelineDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var layout: Pointer? = null
    @JvmField var compute: io.ygdrasil.wgpu.android.WGPUComputeState.ByValue = io.ygdrasil.wgpu.android.WGPUComputeState.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "layout", "compute")
    class ByReference(pointer: Pointer? = null) : WGPUComputePipelineDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUComputePipelineDescriptor(pointer), Structure.ByValue
}

open class WGPUDeviceDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var requiredFeatureCount: Long = 0L
    @JvmField var requiredFeatures: Pointer? = null
    @JvmField var requiredLimits: Pointer? = null
    @JvmField var defaultQueue: io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue = io.ygdrasil.wgpu.android.WGPUQueueDescriptor.ByValue()
    @JvmField var deviceLostCallbackInfo: io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUDeviceLostCallbackInfo.ByValue()
    @JvmField var uncapturedErrorCallbackInfo: io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue = io.ygdrasil.wgpu.android.WGPUUncapturedErrorCallbackInfo.ByValue()
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "requiredFeatureCount", "requiredFeatures", "requiredLimits", "defaultQueue", "deviceLostCallbackInfo", "uncapturedErrorCallbackInfo")
    class ByReference(pointer: Pointer? = null) : WGPUDeviceDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUDeviceDescriptor(pointer), Structure.ByValue
}

open class WGPURenderPassDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var colorAttachmentCount: Long = 0L
    @JvmField var colorAttachments: Pointer? = null
    @JvmField var depthStencilAttachment: Pointer? = null
    @JvmField var occlusionQuerySet: Pointer? = null
    @JvmField var timestampWrites: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "colorAttachmentCount", "colorAttachments", "depthStencilAttachment", "occlusionQuerySet", "timestampWrites")
    class ByReference(pointer: Pointer? = null) : WGPURenderPassDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderPassDescriptor(pointer), Structure.ByValue
}

open class WGPUTextureViewDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var format: Int = 0
    @JvmField var dimension: Int = 0
    @JvmField var baseMipLevel: Int = 0
    @JvmField var mipLevelCount: Int = 0
    @JvmField var baseArrayLayer: Int = 0
    @JvmField var arrayLayerCount: Int = 0
    @JvmField var aspect: Int = 0
    @JvmField var usage: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "format", "dimension", "baseMipLevel", "mipLevelCount", "baseArrayLayer", "arrayLayerCount", "aspect", "usage")
    class ByReference(pointer: Pointer? = null) : WGPUTextureViewDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUTextureViewDescriptor(pointer), Structure.ByValue
}

open class WGPUVertexState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var module: Pointer? = null
    @JvmField var entryPoint: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var constantCount: Long = 0L
    @JvmField var constants: Pointer? = null
    @JvmField var bufferCount: Long = 0L
    @JvmField var buffers: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "module", "entryPoint", "constantCount", "constants", "bufferCount", "buffers")
    class ByReference(pointer: Pointer? = null) : WGPUVertexState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUVertexState(pointer), Structure.ByValue
}

open class WGPUFragmentState(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var module: Pointer? = null
    @JvmField var entryPoint: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var constantCount: Long = 0L
    @JvmField var constants: Pointer? = null
    @JvmField var targetCount: Long = 0L
    @JvmField var targets: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "module", "entryPoint", "constantCount", "constants", "targetCount", "targets")
    class ByReference(pointer: Pointer? = null) : WGPUFragmentState(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUFragmentState(pointer), Structure.ByValue
}

open class WGPURenderPipelineDescriptor(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var layout: Pointer? = null
    @JvmField var vertex: io.ygdrasil.wgpu.android.WGPUVertexState.ByValue = io.ygdrasil.wgpu.android.WGPUVertexState.ByValue()
    @JvmField var primitive: io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue = io.ygdrasil.wgpu.android.WGPUPrimitiveState.ByValue()
    @JvmField var depthStencil: Pointer? = null
    @JvmField var multisample: io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue = io.ygdrasil.wgpu.android.WGPUMultisampleState.ByValue()
    @JvmField var fragment: Pointer? = null
    override fun getFieldOrder() = listOf<String>("nextInChain", "label", "layout", "vertex", "primitive", "depthStencil", "multisample", "fragment")
    class ByReference(pointer: Pointer? = null) : WGPURenderPipelineDescriptor(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURenderPipelineDescriptor(pointer), Structure.ByValue
}

open class WGPUXlibDisplayHandle(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var display: Pointer? = null
    @JvmField var screen: Int = 0
    override fun getFieldOrder() = listOf<String>("display", "screen")
    class ByReference(pointer: Pointer? = null) : WGPUXlibDisplayHandle(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUXlibDisplayHandle(pointer), Structure.ByValue
}

open class WGPUXcbDisplayHandle(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var connection: Pointer? = null
    @JvmField var screen: Int = 0
    override fun getFieldOrder() = listOf<String>("connection", "screen")
    class ByReference(pointer: Pointer? = null) : WGPUXcbDisplayHandle(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUXcbDisplayHandle(pointer), Structure.ByValue
}

open class WGPUWaylandDisplayHandle(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var display: Pointer? = null
    override fun getFieldOrder() = listOf<String>("display")
    class ByReference(pointer: Pointer? = null) : WGPUWaylandDisplayHandle(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUWaylandDisplayHandle(pointer), Structure.ByValue
}

open class WGPUNativeDisplayHandle(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var type: Int = 0
    @JvmField var data: Data = Data()
    override fun getFieldOrder() = listOf<String>("type", "data")
    class Data : com.sun.jna.Union(), Structure.ByValue {
        @JvmField var xlib: WGPUXlibDisplayHandle.ByValue = WGPUXlibDisplayHandle.ByValue()
        @JvmField var xcb: WGPUXcbDisplayHandle.ByValue = WGPUXcbDisplayHandle.ByValue()
        @JvmField var wayland: WGPUWaylandDisplayHandle.ByValue = WGPUWaylandDisplayHandle.ByValue()
    }
    class ByReference(pointer: Pointer? = null) : WGPUNativeDisplayHandle(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUNativeDisplayHandle(pointer), Structure.ByValue
}

open class WGPUInstanceExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var backends: Long = 0L
    @JvmField var flags: Long = 0L
    @JvmField var dx12ShaderCompiler: Int = 0
    @JvmField var gles3MinorVersion: Int = 0
    @JvmField var glFenceBehaviour: Int = 0
    @JvmField var dxcPath: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var dxcMaxShaderModel: Int = 0
    @JvmField var dx12PresentationSystem: Int = 0
    @JvmField var budgetForDeviceCreation: Pointer? = null
    @JvmField var budgetForDeviceLoss: Pointer? = null
    @JvmField var displayHandle: io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue = io.ygdrasil.wgpu.android.WGPUNativeDisplayHandle.ByValue()
    override fun getFieldOrder() = listOf<String>("chain", "backends", "flags", "dx12ShaderCompiler", "gles3MinorVersion", "glFenceBehaviour", "dxcPath", "dxcMaxShaderModel", "dx12PresentationSystem", "budgetForDeviceCreation", "budgetForDeviceLoss", "displayHandle")
    class ByReference(pointer: Pointer? = null) : WGPUInstanceExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUInstanceExtras(pointer), Structure.ByValue
}

open class WGPUDeviceExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var tracePath: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("chain", "tracePath")
    class ByReference(pointer: Pointer? = null) : WGPUDeviceExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUDeviceExtras(pointer), Structure.ByValue
}

open class WGPUNativeLimits(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var maxNonSamplerBindings: Int = 0
    @JvmField var maxBindingArrayElementsPerShaderStage: Int = 0
    @JvmField var maxBindingArraySamplerElementsPerShaderStage: Int = 0
    @JvmField var maxMultiviewViewCount: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "maxNonSamplerBindings", "maxBindingArrayElementsPerShaderStage", "maxBindingArraySamplerElementsPerShaderStage", "maxMultiviewViewCount")
    class ByReference(pointer: Pointer? = null) : WGPUNativeLimits(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUNativeLimits(pointer), Structure.ByValue
}

open class WGPUShaderDefine(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var name: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var value: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    override fun getFieldOrder() = listOf<String>("name", "value")
    class ByReference(pointer: Pointer? = null) : WGPUShaderDefine(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderDefine(pointer), Structure.ByValue
}

open class WGPUShaderSourceGLSL(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var stage: Long = 0L
    @JvmField var code: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var defineCount: Int = 0
    @JvmField var defines: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "stage", "code", "defineCount", "defines")
    class ByReference(pointer: Pointer? = null) : WGPUShaderSourceGLSL(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderSourceGLSL(pointer), Structure.ByValue
}

open class WGPUShaderModuleDescriptorSpirV(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue = io.ygdrasil.wgpu.android.WGPUStringView.ByValue()
    @JvmField var sourceSize: Int = 0
    @JvmField var source: Pointer? = null
    override fun getFieldOrder() = listOf<String>("label", "sourceSize", "source")
    class ByReference(pointer: Pointer? = null) : WGPUShaderModuleDescriptorSpirV(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUShaderModuleDescriptorSpirV(pointer), Structure.ByValue
}

open class WGPURegistryReport(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var numAllocated: Long = 0L
    @JvmField var numKeptFromUser: Long = 0L
    @JvmField var numReleasedFromUser: Long = 0L
    @JvmField var elementSize: Long = 0L
    override fun getFieldOrder() = listOf<String>("numAllocated", "numKeptFromUser", "numReleasedFromUser", "elementSize")
    class ByReference(pointer: Pointer? = null) : WGPURegistryReport(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPURegistryReport(pointer), Structure.ByValue
}

open class WGPUHubReport(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var adapters: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var devices: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var queues: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var pipelineLayouts: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var shaderModules: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var bindGroupLayouts: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var bindGroups: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var commandBuffers: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var renderBundles: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var renderPipelines: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var computePipelines: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var pipelineCaches: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var querySets: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var buffers: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var textures: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var textureViews: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var samplers: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    override fun getFieldOrder() = listOf<String>("adapters", "devices", "queues", "pipelineLayouts", "shaderModules", "bindGroupLayouts", "bindGroups", "commandBuffers", "renderBundles", "renderPipelines", "computePipelines", "pipelineCaches", "querySets", "buffers", "textures", "textureViews", "samplers")
    class ByReference(pointer: Pointer? = null) : WGPUHubReport(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUHubReport(pointer), Structure.ByValue
}

open class WGPUGlobalReport(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var surfaces: io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue = io.ygdrasil.wgpu.android.WGPURegistryReport.ByValue()
    @JvmField var hub: io.ygdrasil.wgpu.android.WGPUHubReport.ByValue = io.ygdrasil.wgpu.android.WGPUHubReport.ByValue()
    override fun getFieldOrder() = listOf<String>("surfaces", "hub")
    class ByReference(pointer: Pointer? = null) : WGPUGlobalReport(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUGlobalReport(pointer), Structure.ByValue
}

open class WGPUInstanceEnumerateAdapterOptions(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var nextInChain: Pointer? = null
    @JvmField var backends: Long = 0L
    override fun getFieldOrder() = listOf<String>("nextInChain", "backends")
    class ByReference(pointer: Pointer? = null) : WGPUInstanceEnumerateAdapterOptions(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUInstanceEnumerateAdapterOptions(pointer), Structure.ByValue
}

open class WGPUBindGroupEntryExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var buffers: Pointer? = null
    @JvmField var bufferCount: Long = 0L
    @JvmField var samplers: Pointer? = null
    @JvmField var samplerCount: Long = 0L
    @JvmField var textureViews: Pointer? = null
    @JvmField var textureViewCount: Long = 0L
    override fun getFieldOrder() = listOf<String>("chain", "buffers", "bufferCount", "samplers", "samplerCount", "textureViews", "textureViewCount")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupEntryExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupEntryExtras(pointer), Structure.ByValue
}

open class WGPUBindGroupLayoutEntryExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var count: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "count")
    class ByReference(pointer: Pointer? = null) : WGPUBindGroupLayoutEntryExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUBindGroupLayoutEntryExtras(pointer), Structure.ByValue
}

open class WGPUQuerySetDescriptorExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var pipelineStatistics: Pointer? = null
    @JvmField var pipelineStatisticCount: Long = 0L
    override fun getFieldOrder() = listOf<String>("chain", "pipelineStatistics", "pipelineStatisticCount")
    class ByReference(pointer: Pointer? = null) : WGPUQuerySetDescriptorExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUQuerySetDescriptorExtras(pointer), Structure.ByValue
}

open class WGPUSurfaceConfigurationExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var desiredMaximumFrameLatency: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "desiredMaximumFrameLatency")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceConfigurationExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceConfigurationExtras(pointer), Structure.ByValue
}

open class WGPUSurfaceSourceSwapChainPanel(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var panelNative: Pointer? = null
    override fun getFieldOrder() = listOf<String>("chain", "panelNative")
    class ByReference(pointer: Pointer? = null) : WGPUSurfaceSourceSwapChainPanel(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSurfaceSourceSwapChainPanel(pointer), Structure.ByValue
}

open class WGPUPrimitiveStateExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var polygonMode: Int = 0
    @JvmField var conservative: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "polygonMode", "conservative")
    class ByReference(pointer: Pointer? = null) : WGPUPrimitiveStateExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUPrimitiveStateExtras(pointer), Structure.ByValue
}

open class WGPUImageSubresourceRange(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var aspect: Int = 0
    @JvmField var baseMipLevel: Int = 0
    @JvmField var mipLevelCount: Int = 0
    @JvmField var baseArrayLayer: Int = 0
    @JvmField var arrayLayerCount: Int = 0
    override fun getFieldOrder() = listOf<String>("aspect", "baseMipLevel", "mipLevelCount", "baseArrayLayer", "arrayLayerCount")
    class ByReference(pointer: Pointer? = null) : WGPUImageSubresourceRange(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUImageSubresourceRange(pointer), Structure.ByValue
}

open class WGPUSamplerDescriptorExtras(pointer: Pointer? = null) : Structure(pointer) {
    @JvmField var chain: io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue = io.ygdrasil.wgpu.android.WGPUChainedStruct.ByValue()
    @JvmField var samplerBorderColor: Int = 0
    override fun getFieldOrder() = listOf<String>("chain", "samplerBorderColor")
    class ByReference(pointer: Pointer? = null) : WGPUSamplerDescriptorExtras(pointer), Structure.ByReference
    class ByValue(pointer: Pointer? = null) : WGPUSamplerDescriptorExtras(pointer), Structure.ByValue
}

internal interface wgpu_hLibrary : Library {
    fun wgpuCreateInstance(descriptor: Pointer?): Pointer?
    fun wgpuGetInstanceFeatures(features: Pointer?): Unit
    fun wgpuGetInstanceLimits(limits: Pointer?): Int
    fun wgpuHasInstanceFeature(feature: Int): Int
    fun wgpuGetProcAddress(procName: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Pointer?
    fun wgpuAdapterGetFeatures(adapter: Pointer?, features: Pointer?): Unit
    fun wgpuAdapterGetInfo(adapter: Pointer?, info: Pointer?): Int
    fun wgpuAdapterGetLimits(adapter: Pointer?, limits: Pointer?): Int
    fun wgpuAdapterHasFeature(adapter: Pointer?, feature: Int): Int
    fun wgpuAdapterRequestDevice(adapter: Pointer?, descriptor: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPURequestDeviceCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuAdapterAddRef(adapter: Pointer?): Unit
    fun wgpuAdapterRelease(adapter: Pointer?): Unit
    fun wgpuAdapterInfoFreeMembers(adapterInfo: io.ygdrasil.wgpu.android.WGPUAdapterInfo.ByValue): Unit
    fun wgpuBindGroupSetLabel(bindGroup: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuBindGroupAddRef(bindGroup: Pointer?): Unit
    fun wgpuBindGroupRelease(bindGroup: Pointer?): Unit
    fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuBindGroupLayoutAddRef(bindGroupLayout: Pointer?): Unit
    fun wgpuBindGroupLayoutRelease(bindGroupLayout: Pointer?): Unit
    fun wgpuBufferDestroy(buffer: Pointer?): Unit
    fun wgpuBufferGetConstMappedRange(buffer: Pointer?, offset: Long, size: Long): Pointer?
    fun wgpuBufferGetMappedRange(buffer: Pointer?, offset: Long, size: Long): Pointer?
    fun wgpuBufferGetMapState(buffer: Pointer?): Int
    fun wgpuBufferGetSize(buffer: Pointer?): Long
    fun wgpuBufferGetUsage(buffer: Pointer?): Long
    fun wgpuBufferMapAsync(buffer: Pointer?, mode: Long, offset: Long, size: Long, callbackInfo: io.ygdrasil.wgpu.android.WGPUBufferMapCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuBufferReadMappedRange(buffer: Pointer?, offset: Long, data: Pointer?, size: Long): Int
    fun wgpuBufferSetLabel(buffer: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuBufferUnmap(buffer: Pointer?): Unit
    fun wgpuBufferWriteMappedRange(buffer: Pointer?, offset: Long, data: Pointer?, size: Long): Int
    fun wgpuBufferAddRef(buffer: Pointer?): Unit
    fun wgpuBufferRelease(buffer: Pointer?): Unit
    fun wgpuCommandBufferSetLabel(commandBuffer: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuCommandBufferAddRef(commandBuffer: Pointer?): Unit
    fun wgpuCommandBufferRelease(commandBuffer: Pointer?): Unit
    fun wgpuCommandEncoderBeginComputePass(commandEncoder: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuCommandEncoderBeginRenderPass(commandEncoder: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuCommandEncoderClearBuffer(commandEncoder: Pointer?, buffer: Pointer?, offset: Long, size: Long): Unit
    fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: Pointer?, source: Pointer?, sourceOffset: Long, destination: Pointer?, destinationOffset: Long, size: Long): Unit
    fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: Pointer?, source: Pointer?, destination: Pointer?, copySize: Pointer?): Unit
    fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: Pointer?, source: Pointer?, destination: Pointer?, copySize: Pointer?): Unit
    fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: Pointer?, source: Pointer?, destination: Pointer?, copySize: Pointer?): Unit
    fun wgpuCommandEncoderFinish(commandEncoder: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: Pointer?, markerLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuCommandEncoderPopDebugGroup(commandEncoder: Pointer?): Unit
    fun wgpuCommandEncoderPushDebugGroup(commandEncoder: Pointer?, groupLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuCommandEncoderResolveQuerySet(commandEncoder: Pointer?, querySet: Pointer?, firstQuery: Int, queryCount: Int, destination: Pointer?, destinationOffset: Long): Unit
    fun wgpuCommandEncoderSetLabel(commandEncoder: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuCommandEncoderWriteTimestamp(commandEncoder: Pointer?, querySet: Pointer?, queryIndex: Int): Unit
    fun wgpuCommandEncoderAddRef(commandEncoder: Pointer?): Unit
    fun wgpuCommandEncoderRelease(commandEncoder: Pointer?): Unit
    fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: Pointer?, workgroupCountX: Int, workgroupCountY: Int, workgroupCountZ: Int): Unit
    fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: Pointer?, indirectBuffer: Pointer?, indirectOffset: Long): Unit
    fun wgpuComputePassEncoderEnd(computePassEncoder: Pointer?): Unit
    fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: Pointer?, markerLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: Pointer?): Unit
    fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: Pointer?, groupLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: Pointer?, groupIndex: Int, group: Pointer?, dynamicOffsetCount: Long, dynamicOffsets: Pointer?): Unit
    fun wgpuComputePassEncoderSetImmediates(computePassEncoder: Pointer?, offset: Int, data: Pointer?, size: Long): Unit
    fun wgpuComputePassEncoderSetLabel(computePassEncoder: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuComputePassEncoderSetPipeline(computePassEncoder: Pointer?, pipeline: Pointer?): Unit
    fun wgpuComputePassEncoderAddRef(computePassEncoder: Pointer?): Unit
    fun wgpuComputePassEncoderRelease(computePassEncoder: Pointer?): Unit
    fun wgpuComputePipelineGetBindGroupLayout(computePipeline: Pointer?, groupIndex: Int): Pointer?
    fun wgpuComputePipelineSetLabel(computePipeline: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuComputePipelineAddRef(computePipeline: Pointer?): Unit
    fun wgpuComputePipelineRelease(computePipeline: Pointer?): Unit
    fun wgpuDeviceCreateBindGroup(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateBindGroupLayout(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateBuffer(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateCommandEncoder(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateComputePipeline(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateComputePipelineAsync(device: Pointer?, descriptor: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPUCreateComputePipelineAsyncCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuDeviceCreatePipelineLayout(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateQuerySet(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateRenderBundleEncoder(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateRenderPipeline(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateRenderPipelineAsync(device: Pointer?, descriptor: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPUCreateRenderPipelineAsyncCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuDeviceCreateSampler(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateShaderModule(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceCreateTexture(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuDeviceDestroy(device: Pointer?): Unit
    fun wgpuDeviceGetAdapterInfo(device: Pointer?, adapterInfo: Pointer?): Int
    fun wgpuDeviceGetFeatures(device: Pointer?, features: Pointer?): Unit
    fun wgpuDeviceGetLimits(device: Pointer?, limits: Pointer?): Int
    fun wgpuDeviceGetLostFuture(device: Pointer?): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuDeviceGetQueue(device: Pointer?): Pointer?
    fun wgpuDeviceHasFeature(device: Pointer?, feature: Int): Int
    fun wgpuDevicePopErrorScope(device: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPUPopErrorScopeCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuDevicePushErrorScope(device: Pointer?, filter: Int): Unit
    fun wgpuDeviceSetLabel(device: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuDeviceAddRef(device: Pointer?): Unit
    fun wgpuDeviceRelease(device: Pointer?): Unit
    fun wgpuExternalTextureSetLabel(externalTexture: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuExternalTextureAddRef(externalTexture: Pointer?): Unit
    fun wgpuExternalTextureRelease(externalTexture: Pointer?): Unit
    fun wgpuInstanceCreateSurface(instance: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuInstanceGetWGSLLanguageFeatures(instance: Pointer?, features: Pointer?): Unit
    fun wgpuInstanceHasWGSLLanguageFeature(instance: Pointer?, feature: Int): Int
    fun wgpuInstanceProcessEvents(instance: Pointer?): Unit
    fun wgpuInstanceRequestAdapter(instance: Pointer?, options: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPURequestAdapterCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuInstanceWaitAny(instance: Pointer?, futureCount: Long, futures: Pointer?, timeoutNS: Long): Int
    fun wgpuInstanceAddRef(instance: Pointer?): Unit
    fun wgpuInstanceRelease(instance: Pointer?): Unit
    fun wgpuPipelineLayoutSetLabel(pipelineLayout: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuPipelineLayoutAddRef(pipelineLayout: Pointer?): Unit
    fun wgpuPipelineLayoutRelease(pipelineLayout: Pointer?): Unit
    fun wgpuQuerySetDestroy(querySet: Pointer?): Unit
    fun wgpuQuerySetGetCount(querySet: Pointer?): Int
    fun wgpuQuerySetGetType(querySet: Pointer?): Int
    fun wgpuQuerySetSetLabel(querySet: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuQuerySetAddRef(querySet: Pointer?): Unit
    fun wgpuQuerySetRelease(querySet: Pointer?): Unit
    fun wgpuQueueOnSubmittedWorkDone(queue: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPUQueueWorkDoneCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuQueueSetLabel(queue: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuQueueSubmit(queue: Pointer?, commandCount: Long, commands: Pointer?): Unit
    fun wgpuQueueWriteBuffer(queue: Pointer?, buffer: Pointer?, bufferOffset: Long, data: Pointer?, size: Long): Unit
    fun wgpuQueueWriteTexture(queue: Pointer?, destination: Pointer?, data: Pointer?, dataSize: Long, dataLayout: Pointer?, writeSize: Pointer?): Unit
    fun wgpuQueueAddRef(queue: Pointer?): Unit
    fun wgpuQueueRelease(queue: Pointer?): Unit
    fun wgpuRenderBundleSetLabel(renderBundle: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderBundleAddRef(renderBundle: Pointer?): Unit
    fun wgpuRenderBundleRelease(renderBundle: Pointer?): Unit
    fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: Pointer?, vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int): Unit
    fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: Pointer?, indexCount: Int, instanceCount: Int, firstIndex: Int, baseVertex: Int, firstInstance: Int): Unit
    fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: Pointer?, indirectBuffer: Pointer?, indirectOffset: Long): Unit
    fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: Pointer?, indirectBuffer: Pointer?, indirectOffset: Long): Unit
    fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: Pointer?, markerLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: Pointer?): Unit
    fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: Pointer?, groupLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: Pointer?, groupIndex: Int, group: Pointer?, dynamicOffsetCount: Long, dynamicOffsets: Pointer?): Unit
    fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: Pointer?, offset: Int, data: Pointer?, size: Long): Unit
    fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: Pointer?, buffer: Pointer?, format: Int, offset: Long, size: Long): Unit
    fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: Pointer?, pipeline: Pointer?): Unit
    fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: Pointer?, slot: Int, buffer: Pointer?, offset: Long, size: Long): Unit
    fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: Pointer?): Unit
    fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: Pointer?, queryIndex: Int): Unit
    fun wgpuRenderPassEncoderDraw(renderPassEncoder: Pointer?, vertexCount: Int, instanceCount: Int, firstVertex: Int, firstInstance: Int): Unit
    fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: Pointer?, indexCount: Int, instanceCount: Int, firstIndex: Int, baseVertex: Int, firstInstance: Int): Unit
    fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: Pointer?, indirectBuffer: Pointer?, indirectOffset: Long): Unit
    fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: Pointer?, indirectBuffer: Pointer?, indirectOffset: Long): Unit
    fun wgpuRenderPassEncoderEnd(renderPassEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: Pointer?, bundleCount: Long, bundles: Pointer?): Unit
    fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: Pointer?, markerLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: Pointer?, groupLabel: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: Pointer?, groupIndex: Int, group: Pointer?, dynamicOffsetCount: Long, dynamicOffsets: Pointer?): Unit
    fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: Pointer?, color: Pointer?): Unit
    fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: Pointer?, offset: Int, data: Pointer?, size: Long): Unit
    fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: Pointer?, buffer: Pointer?, format: Int, offset: Long, size: Long): Unit
    fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: Pointer?, pipeline: Pointer?): Unit
    fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: Pointer?, x: Int, y: Int, width: Int, height: Int): Unit
    fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: Pointer?, reference: Int): Unit
    fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: Pointer?, slot: Int, buffer: Pointer?, offset: Long, size: Long): Unit
    fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: Pointer?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit
    fun wgpuRenderPassEncoderAddRef(renderPassEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderRelease(renderPassEncoder: Pointer?): Unit
    fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: Pointer?, groupIndex: Int): Pointer?
    fun wgpuRenderPipelineSetLabel(renderPipeline: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuRenderPipelineAddRef(renderPipeline: Pointer?): Unit
    fun wgpuRenderPipelineRelease(renderPipeline: Pointer?): Unit
    fun wgpuSamplerSetLabel(sampler: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuSamplerAddRef(sampler: Pointer?): Unit
    fun wgpuSamplerRelease(sampler: Pointer?): Unit
    fun wgpuShaderModuleGetCompilationInfo(shaderModule: Pointer?, callbackInfo: io.ygdrasil.wgpu.android.WGPUCompilationInfoCallbackInfo.ByValue): io.ygdrasil.wgpu.android.WGPUFuture.ByValue
    fun wgpuShaderModuleSetLabel(shaderModule: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuShaderModuleAddRef(shaderModule: Pointer?): Unit
    fun wgpuShaderModuleRelease(shaderModule: Pointer?): Unit
    fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: io.ygdrasil.wgpu.android.WGPUSupportedFeatures.ByValue): Unit
    fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: io.ygdrasil.wgpu.android.WGPUSupportedInstanceFeatures.ByValue): Unit
    fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: io.ygdrasil.wgpu.android.WGPUSupportedWGSLLanguageFeatures.ByValue): Unit
    fun wgpuSurfaceConfigure(surface: Pointer?, config: Pointer?): Unit
    fun wgpuSurfaceGetCapabilities(surface: Pointer?, adapter: Pointer?, capabilities: Pointer?): Int
    fun wgpuSurfaceGetCurrentTexture(surface: Pointer?, surfaceTexture: Pointer?): Unit
    fun wgpuSurfacePresent(surface: Pointer?): Int
    fun wgpuSurfaceSetLabel(surface: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuSurfaceUnconfigure(surface: Pointer?): Unit
    fun wgpuSurfaceAddRef(surface: Pointer?): Unit
    fun wgpuSurfaceRelease(surface: Pointer?): Unit
    fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: io.ygdrasil.wgpu.android.WGPUSurfaceCapabilities.ByValue): Unit
    fun wgpuTextureCreateView(texture: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuTextureDestroy(texture: Pointer?): Unit
    fun wgpuTextureGetDepthOrArrayLayers(texture: Pointer?): Int
    fun wgpuTextureGetDimension(texture: Pointer?): Int
    fun wgpuTextureGetFormat(texture: Pointer?): Int
    fun wgpuTextureGetHeight(texture: Pointer?): Int
    fun wgpuTextureGetMipLevelCount(texture: Pointer?): Int
    fun wgpuTextureGetSampleCount(texture: Pointer?): Int
    fun wgpuTextureGetTextureBindingViewDimension(texture: Pointer?): Int
    fun wgpuTextureGetUsage(texture: Pointer?): Long
    fun wgpuTextureGetWidth(texture: Pointer?): Int
    fun wgpuTextureSetLabel(texture: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuTextureAddRef(texture: Pointer?): Unit
    fun wgpuTextureRelease(texture: Pointer?): Unit
    fun wgpuTextureViewSetLabel(textureView: Pointer?, label: io.ygdrasil.wgpu.android.WGPUStringView.ByValue): Unit
    fun wgpuTextureViewAddRef(textureView: Pointer?): Unit
    fun wgpuTextureViewRelease(textureView: Pointer?): Unit
    fun wgpuGenerateReport(instance: Pointer?, report: Pointer?): Unit
    fun wgpuInstanceEnumerateAdapters(instance: Pointer?, options: Pointer?, adapters: Pointer?): Long
    fun wgpuQueueSubmitForIndex(queue: Pointer?, commandCount: Long, commands: Pointer?): Long
    fun wgpuQueueGetTimestampPeriod(queue: Pointer?): Float
    fun wgpuDevicePoll(device: Pointer?, wait: Int, submissionIndex: Pointer?): Int
    fun wgpuDeviceCreateShaderModuleSpirV(device: Pointer?, descriptor: Pointer?): Pointer?
    fun wgpuSetLogCallback(callback: Pointer?, userdata: Pointer?): Unit
    fun wgpuSetLogLevel(level: Int): Unit
    fun wgpuGetVersion(): Int
    fun wgpuDeviceGetNativeMetalDevice(device: Pointer?): Pointer?
    fun wgpuQueueGetNativeMetalCommandQueue(queue: Pointer?): Pointer?
    fun wgpuTextureGetNativeMetalTexture(texture: Pointer?): Pointer?
    fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: Pointer?, buffer: Pointer?, offset: Long, count: Int): Unit
    fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: Pointer?, buffer: Pointer?, offset: Long, count: Int): Unit
    fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: Pointer?, buffer: Pointer?, offset: Long, count_buffer: Pointer?, count_buffer_offset: Long, max_count: Int): Unit
    fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: Pointer?, buffer: Pointer?, offset: Long, count_buffer: Pointer?, count_buffer_offset: Long, max_count: Int): Unit
    fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: Pointer?, querySet: Pointer?, queryIndex: Int): Unit
    fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: Pointer?): Unit
    fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: Pointer?, querySet: Pointer?, queryIndex: Int): Unit
    fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: Pointer?): Unit
    fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: Pointer?, querySet: Pointer?, queryIndex: Int): Unit
    fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: Pointer?, querySet: Pointer?, queryIndex: Int): Unit
    fun wgpuDeviceStartGraphicsDebuggerCapture(device: Pointer?): Int
    fun wgpuDeviceStopGraphicsDebuggerCapture(device: Pointer?): Unit
    fun wgpuCommandEncoderClearTexture(commandEncoder: Pointer?, texture: Pointer?, range: Pointer?): Unit
    fun wgpuDeviceCreateShaderModuleTrusted(device: Pointer?, descriptor: Pointer?, runtimeChecks: Long): Pointer?
}

internal val wgpu_hLibraryInstance: wgpu_hLibrary by lazy {
    Native.load("wgpu4k", wgpu_hLibrary::class.java)
}

