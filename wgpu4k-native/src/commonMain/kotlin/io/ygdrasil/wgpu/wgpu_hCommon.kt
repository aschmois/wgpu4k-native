package io.ygdrasil.wgpu

import io.ygdrasil.kffi.NativeAddress
import io.ygdrasil.kffi.Callback
import io.ygdrasil.kffi.CallbackExceptionHandler
import io.ygdrasil.kffi.CallbackPolicy
import io.ygdrasil.kffi.CallbackRegistration
import io.ygdrasil.kffi.CallbackRuntime
import io.ygdrasil.kffi.CallbackRuntimeApi
import io.ygdrasil.kffi.CallbackType
import io.ygdrasil.kffi.PreparedCallbackRegistration
import io.ygdrasil.kffi.UnsafeCallbackRearmApi
import io.ygdrasil.kffi.CString
import io.ygdrasil.kffi.ArrayHolder
import io.ygdrasil.kffi.MemoryAllocator
import kotlin.OptIn

/**
 * For reserved non-standard bitflag values, see @ref BitflagRegistry.
 */
typealias WGPUBufferUsage = ULong
const val WGPUBufferUsage_None : WGPUBufferUsage = 0uL
const val WGPUBufferUsage_MapRead : WGPUBufferUsage = 1uL
const val WGPUBufferUsage_MapWrite : WGPUBufferUsage = 2uL
const val WGPUBufferUsage_CopySrc : WGPUBufferUsage = 4uL
const val WGPUBufferUsage_CopyDst : WGPUBufferUsage = 8uL
const val WGPUBufferUsage_Index : WGPUBufferUsage = 16uL
const val WGPUBufferUsage_Vertex : WGPUBufferUsage = 32uL
const val WGPUBufferUsage_Uniform : WGPUBufferUsage = 64uL
const val WGPUBufferUsage_Storage : WGPUBufferUsage = 128uL
const val WGPUBufferUsage_Indirect : WGPUBufferUsage = 256uL
const val WGPUBufferUsage_QueryResolve : WGPUBufferUsage = 512uL

/**
 * For reserved non-standard bitflag values, see @ref BitflagRegistry.
 */
typealias WGPUColorWriteMask = ULong
const val WGPUColorWriteMask_None : WGPUColorWriteMask = 0uL
const val WGPUColorWriteMask_Red : WGPUColorWriteMask = 1uL
const val WGPUColorWriteMask_Green : WGPUColorWriteMask = 2uL
const val WGPUColorWriteMask_Blue : WGPUColorWriteMask = 4uL
const val WGPUColorWriteMask_Alpha : WGPUColorWriteMask = 8uL
const val WGPUColorWriteMask_All : WGPUColorWriteMask = 15uL

/**
 * For reserved non-standard bitflag values, see @ref BitflagRegistry.
 */
typealias WGPUMapMode = ULong
const val WGPUMapMode_None : WGPUMapMode = 0uL
const val WGPUMapMode_Read : WGPUMapMode = 1uL
const val WGPUMapMode_Write : WGPUMapMode = 2uL

/**
 * For reserved non-standard bitflag values, see @ref BitflagRegistry.
 */
typealias WGPUShaderStage = ULong
const val WGPUShaderStage_None : WGPUShaderStage = 0uL
const val WGPUShaderStage_Vertex : WGPUShaderStage = 1uL
const val WGPUShaderStage_Fragment : WGPUShaderStage = 2uL
const val WGPUShaderStage_Compute : WGPUShaderStage = 4uL

/**
 * For reserved non-standard bitflag values, see @ref BitflagRegistry.
 */
typealias WGPUTextureUsage = ULong
const val WGPUTextureUsage_None : WGPUTextureUsage = 0uL
const val WGPUTextureUsage_CopySrc : WGPUTextureUsage = 1uL
const val WGPUTextureUsage_CopyDst : WGPUTextureUsage = 2uL
const val WGPUTextureUsage_TextureBinding : WGPUTextureUsage = 4uL
const val WGPUTextureUsage_StorageBinding : WGPUTextureUsage = 8uL
const val WGPUTextureUsage_RenderAttachment : WGPUTextureUsage = 16uL
const val WGPUTextureUsage_TransientAttachment : WGPUTextureUsage = 32uL

/**
 * Bitflags selecting which graphics backends the @ref WGPUInstance should
 * enable.
 *
 * Pass in the @c backends field of @ref WGPUInstanceExtras.
 */
typealias WGPUInstanceBackend = ULong
const val WGPUInstanceBackend_All : WGPUInstanceBackend = 0uL
const val WGPUInstanceBackend_Vulkan : WGPUInstanceBackend = 1uL
const val WGPUInstanceBackend_GL : WGPUInstanceBackend = 2uL
const val WGPUInstanceBackend_Metal : WGPUInstanceBackend = 4uL
const val WGPUInstanceBackend_DX12 : WGPUInstanceBackend = 8uL
const val WGPUInstanceBackend_BrowserWebGPU : WGPUInstanceBackend = 32uL
const val WGPUInstanceBackend_Primary : WGPUInstanceBackend = 45uL
const val WGPUInstanceBackend_Secondary : WGPUInstanceBackend = 2uL
const val WGPUInstanceBackend_Force32 : WGPUInstanceBackend = 2147483647uL

/**
 * Bitflags controlling instance debugging and validation behavior.
 *
 * These are not part of the WebGPU standard.
 *
 * Pass in the @c flags field of @ref WGPUInstanceExtras.
 */
typealias WGPUInstanceFlag = ULong
const val WGPUInstanceFlag_Empty : WGPUInstanceFlag = 0uL
const val WGPUInstanceFlag_Debug : WGPUInstanceFlag = 1uL
const val WGPUInstanceFlag_Validation : WGPUInstanceFlag = 2uL
const val WGPUInstanceFlag_DiscardHalLabels : WGPUInstanceFlag = 4uL
const val WGPUInstanceFlag_AllowUnderlyingNoncompliantAdapter : WGPUInstanceFlag = 8uL
const val WGPUInstanceFlag_GPUBasedValidation : WGPUInstanceFlag = 16uL
const val WGPUInstanceFlag_ValidationIndirectCall : WGPUInstanceFlag = 32uL
const val WGPUInstanceFlag_AutomaticTimestampNormalization : WGPUInstanceFlag = 64uL
const val WGPUInstanceFlag_Default : WGPUInstanceFlag = 16777216uL
const val WGPUInstanceFlag_Debugging : WGPUInstanceFlag = 33554432uL
const val WGPUInstanceFlag_AdvancedDebugging : WGPUInstanceFlag = 67108864uL
const val WGPUInstanceFlag_WithEnv : WGPUInstanceFlag = 134217728uL
const val WGPUInstanceFlag_Force32 : WGPUInstanceFlag = 2147483647uL

/**
 * Describes how shader bound checks should be performed.
 */
typealias WGPUShaderRuntimeChecks = ULong
const val WGPUShaderRuntimeChecks_None : WGPUShaderRuntimeChecks = 0uL
const val WGPUShaderRuntimeChecks_BoundsChecks : WGPUShaderRuntimeChecks = 1uL
const val WGPUShaderRuntimeChecks_ForceLoopBounding : WGPUShaderRuntimeChecks = 2uL
const val WGPUShaderRuntimeChecks_RayQueryInitializationTracking : WGPUShaderRuntimeChecks = 4uL
const val WGPUShaderRuntimeChecks_TaskShaderDispatchTracking : WGPUShaderRuntimeChecks = 8uL
const val WGPUShaderRuntimeChecks_MeshShaderPrimitiveIndicesClamp : WGPUShaderRuntimeChecks = 16uL

/**
 * Nullable value defining a pointer+length view into a UTF-8 encoded string.
 *
 * Values passed into the API may use the special length value @ref WGPU_STRLEN
 * to indicate a null-terminated string.
 * Non-null values passed out of the API (for example as callback arguments)
 * always provide an explicit length and **may or may not be null-terminated**.
 *
 * Some inputs to the API accept null values. Those which do not accept null
 * values "default" to the empty string when null values are passed.
 *
 * Values are encoded as follows:
 * - `{NULL, WGPU_STRLEN}`: the null value.
 * - `{non_null_pointer, WGPU_STRLEN}`: a null-terminated string view.
 * - `{any, 0}`: the empty string.
 * - `{NULL, non_zero_length}`: not allowed (null dereference).
 * - `{non_null_pointer, non_zero_length}`: an explictly-sized string view with
 * size `non_zero_length` (in bytes).
 *
 * For info on how this is used in various places, see \ref Strings.
 */
expect interface WGPUStringView {
    var data: CString?
    var length: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUStringView
        fun allocate(allocator: MemoryAllocator): WGPUStringView
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStringView) -> Unit): ArrayHolder<WGPUStringView>
    }
}

/**
 * \defgroup Objects Objects
 * \brief Opaque, non-dispatchable handles to WebGPU objects.
 *
 * @{
 */
expect value class WGPUAdapter(val handler: NativeAddress)

expect value class WGPUBindGroup(val handler: NativeAddress)

expect value class WGPUBindGroupLayout(val handler: NativeAddress)

expect value class WGPUBuffer(val handler: NativeAddress)

expect value class WGPUCommandBuffer(val handler: NativeAddress)

expect value class WGPUCommandEncoder(val handler: NativeAddress)

expect value class WGPUComputePassEncoder(val handler: NativeAddress)

expect value class WGPUComputePipeline(val handler: NativeAddress)

/**
 * TODO
 *
 * Releasing the last ref to a `WGPUDevice` also calls @ref wgpuDeviceDestroy.
 * For more info, see @ref DeviceRelease.
 */
expect value class WGPUDevice(val handler: NativeAddress)

/**
 * A sampleable 2D texture that may perform 0-copy YUV sampling internally. Creation of @ref WGPUExternalTexture is extremely implementation-dependent and not defined in this header.
 */
expect value class WGPUExternalTexture(val handler: NativeAddress)

expect value class WGPUInstance(val handler: NativeAddress)

expect value class WGPUPipelineLayout(val handler: NativeAddress)

expect value class WGPUQuerySet(val handler: NativeAddress)

expect value class WGPUQueue(val handler: NativeAddress)

expect value class WGPURenderBundle(val handler: NativeAddress)

expect value class WGPURenderBundleEncoder(val handler: NativeAddress)

expect value class WGPURenderPassEncoder(val handler: NativeAddress)

expect value class WGPURenderPipeline(val handler: NativeAddress)

expect value class WGPUSampler(val handler: NativeAddress)

expect value class WGPUShaderModule(val handler: NativeAddress)

/**
 * An object used to continuously present image data to the user, see @ref Surfaces for more details.
 */
expect value class WGPUSurface(val handler: NativeAddress)

expect value class WGPUTexture(val handler: NativeAddress)

expect value class WGPUTextureView(val handler: NativeAddress)

/**
 * \defgroup Enumerations Enumerations
 * \brief Enums.
 *
 * @{
 */
typealias WGPUAdapterType = UInt
const val WGPUAdapterType_DiscreteGPU : WGPUAdapterType = 1u
const val WGPUAdapterType_IntegratedGPU : WGPUAdapterType = 2u
const val WGPUAdapterType_CPU : WGPUAdapterType = 3u
const val WGPUAdapterType_Unknown : WGPUAdapterType = 4u
const val WGPUAdapterType_Force32 : WGPUAdapterType = 2147483647u

typealias WGPUAddressMode = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUAddressMode_Undefined : WGPUAddressMode = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUAddressMode_ClampToEdge : WGPUAddressMode = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUAddressMode_Repeat : WGPUAddressMode = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUAddressMode_MirrorRepeat : WGPUAddressMode = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUAddressMode_Force32 : WGPUAddressMode = 2147483647u

typealias WGPUBackendType = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_Undefined : WGPUBackendType = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_Null : WGPUBackendType = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_WebGPU : WGPUBackendType = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_D3D11 : WGPUBackendType = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_D3D12 : WGPUBackendType = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_Metal : WGPUBackendType = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_Vulkan : WGPUBackendType = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_OpenGL : WGPUBackendType = 7u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_OpenGLES : WGPUBackendType = 8u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBackendType_Force32 : WGPUBackendType = 2147483647u

typealias WGPUBlendFactor = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Undefined : WGPUBlendFactor = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Zero : WGPUBlendFactor = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_One : WGPUBlendFactor = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Src : WGPUBlendFactor = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusSrc : WGPUBlendFactor = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_SrcAlpha : WGPUBlendFactor = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusSrcAlpha : WGPUBlendFactor = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Dst : WGPUBlendFactor = 7u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusDst : WGPUBlendFactor = 8u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_DstAlpha : WGPUBlendFactor = 9u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusDstAlpha : WGPUBlendFactor = 10u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_SrcAlphaSaturated : WGPUBlendFactor = 11u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Constant : WGPUBlendFactor = 12u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusConstant : WGPUBlendFactor = 13u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Src1 : WGPUBlendFactor = 14u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusSrc1 : WGPUBlendFactor = 15u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Src1Alpha : WGPUBlendFactor = 16u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_OneMinusSrc1Alpha : WGPUBlendFactor = 17u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendFactor_Force32 : WGPUBlendFactor = 2147483647u

typealias WGPUBlendOperation = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Undefined : WGPUBlendOperation = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Add : WGPUBlendOperation = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Subtract : WGPUBlendOperation = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_ReverseSubtract : WGPUBlendOperation = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Min : WGPUBlendOperation = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Max : WGPUBlendOperation = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBlendOperation_Force32 : WGPUBlendOperation = 2147483647u

typealias WGPUBufferBindingType = UInt
/**
 * `0`. Indicates that this @ref WGPUBufferBindingLayout member of
 * its parent @ref WGPUBindGroupLayoutEntry is not used.
 * (See also @ref SentinelValues.)
 */
const val WGPUBufferBindingType_BindingNotUsed : WGPUBufferBindingType = 0u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBufferBindingType_Undefined : WGPUBufferBindingType = 1u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBufferBindingType_Uniform : WGPUBufferBindingType = 2u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBufferBindingType_Storage : WGPUBufferBindingType = 3u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBufferBindingType_ReadOnlyStorage : WGPUBufferBindingType = 4u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUBufferBindingType_Force32 : WGPUBufferBindingType = 2147483647u

typealias WGPUBufferMapState = UInt
const val WGPUBufferMapState_Unmapped : WGPUBufferMapState = 1u
const val WGPUBufferMapState_Pending : WGPUBufferMapState = 2u
const val WGPUBufferMapState_Mapped : WGPUBufferMapState = 3u
const val WGPUBufferMapState_Force32 : WGPUBufferMapState = 2147483647u

/**
 * The callback mode controls how a callback for an asynchronous operation may be fired. See @ref Asynchronous-Operations for how these are used.
 */
typealias WGPUCallbackMode = UInt
/**
 * Callbacks created with `WGPUCallbackMode_WaitAnyOnly`:
 * - fire when the asynchronous operation's future is passed to a call to @ref wgpuInstanceWaitAny
 * AND the operation has already completed or it completes inside the call to @ref wgpuInstanceWaitAny.
 */
const val WGPUCallbackMode_WaitAnyOnly : WGPUCallbackMode = 1u
/**
 * Callbacks created with `WGPUCallbackMode_AllowProcessEvents`:
 * - fire for the same reasons as callbacks created with `WGPUCallbackMode_WaitAnyOnly`
 * - fire inside a call to @ref wgpuInstanceProcessEvents if the asynchronous operation is complete.
 */
const val WGPUCallbackMode_AllowProcessEvents : WGPUCallbackMode = 2u
/**
 * Callbacks created with `WGPUCallbackMode_AllowSpontaneous`:
 * - fire for the same reasons as callbacks created with `WGPUCallbackMode_AllowProcessEvents`
 * - **may** fire spontaneously on an arbitrary or application thread, when the WebGPU implementations discovers that the asynchronous operation is complete.
 *
 * Implementations _should_ fire spontaneous callbacks as soon as possible.
 *
 * @note Because spontaneous callbacks may fire at an arbitrary time on an arbitrary thread, applications should take extra care when acquiring locks or mutating state inside the callback. It undefined behavior to re-entrantly call into the webgpu.h API if the callback fires while inside the callstack of another webgpu.h function that is not `wgpuInstanceWaitAny` or `wgpuInstanceProcessEvents`.
 */
const val WGPUCallbackMode_AllowSpontaneous : WGPUCallbackMode = 3u
/**
 * Callbacks created with `WGPUCallbackMode_AllowSpontaneous`:
 * - fire for the same reasons as callbacks created with `WGPUCallbackMode_AllowProcessEvents`
 * - **may** fire spontaneously on an arbitrary or application thread, when the WebGPU implementations discovers that the asynchronous operation is complete.
 *
 * Implementations _should_ fire spontaneous callbacks as soon as possible.
 *
 * @note Because spontaneous callbacks may fire at an arbitrary time on an arbitrary thread, applications should take extra care when acquiring locks or mutating state inside the callback. It undefined behavior to re-entrantly call into the webgpu.h API if the callback fires while inside the callstack of another webgpu.h function that is not `wgpuInstanceWaitAny` or `wgpuInstanceProcessEvents`.
 */
const val WGPUCallbackMode_Force32 : WGPUCallbackMode = 2147483647u

typealias WGPUCompareFunction = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Undefined : WGPUCompareFunction = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Never : WGPUCompareFunction = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Less : WGPUCompareFunction = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Equal : WGPUCompareFunction = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_LessEqual : WGPUCompareFunction = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Greater : WGPUCompareFunction = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_NotEqual : WGPUCompareFunction = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_GreaterEqual : WGPUCompareFunction = 7u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Always : WGPUCompareFunction = 8u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCompareFunction_Force32 : WGPUCompareFunction = 2147483647u

typealias WGPUCompilationInfoRequestStatus = UInt
const val WGPUCompilationInfoRequestStatus_Success : WGPUCompilationInfoRequestStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCompilationInfoRequestStatus_CallbackCancelled : WGPUCompilationInfoRequestStatus = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCompilationInfoRequestStatus_Force32 : WGPUCompilationInfoRequestStatus = 2147483647u

typealias WGPUCompilationMessageType = UInt
const val WGPUCompilationMessageType_Error : WGPUCompilationMessageType = 1u
const val WGPUCompilationMessageType_Warning : WGPUCompilationMessageType = 2u
const val WGPUCompilationMessageType_Info : WGPUCompilationMessageType = 3u
const val WGPUCompilationMessageType_Force32 : WGPUCompilationMessageType = 2147483647u

typealias WGPUComponentSwizzle = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUComponentSwizzle_Undefined : WGPUComponentSwizzle = 0u
/**
 * Force its value to 0.
 */
const val WGPUComponentSwizzle_Zero : WGPUComponentSwizzle = 1u
/**
 * Force its value to 1.
 */
const val WGPUComponentSwizzle_One : WGPUComponentSwizzle = 2u
/**
 * Take its value from the red channel of the texture.
 */
const val WGPUComponentSwizzle_R : WGPUComponentSwizzle = 3u
/**
 * Take its value from the green channel of the texture.
 */
const val WGPUComponentSwizzle_G : WGPUComponentSwizzle = 4u
/**
 * Take its value from the blue channel of the texture.
 */
const val WGPUComponentSwizzle_B : WGPUComponentSwizzle = 5u
/**
 * Take its value from the alpha channel of the texture.
 */
const val WGPUComponentSwizzle_A : WGPUComponentSwizzle = 6u
/**
 * Take its value from the alpha channel of the texture.
 */
const val WGPUComponentSwizzle_Force32 : WGPUComponentSwizzle = 2147483647u

/**
 * Describes how frames are composited with other contents on the screen when @ref wgpuSurfacePresent is called.
 */
typealias WGPUCompositeAlphaMode = UInt
/**
 * `0`. Lets the WebGPU implementation choose the best mode (supported, and with the best performance) between @ref WGPUCompositeAlphaMode_Opaque or @ref WGPUCompositeAlphaMode_Inherit.
 */
const val WGPUCompositeAlphaMode_Auto : WGPUCompositeAlphaMode = 0u
/**
 * The alpha component of the image is ignored and teated as if it is always 1.0.
 */
const val WGPUCompositeAlphaMode_Opaque : WGPUCompositeAlphaMode = 1u
/**
 * The alpha component is respected and non-alpha components are assumed to be already multiplied with the alpha component. For example, (0.5, 0, 0, 0.5) is semi-transparent bright red.
 */
const val WGPUCompositeAlphaMode_Premultiplied : WGPUCompositeAlphaMode = 2u
/**
 * The alpha component is respected and non-alpha components are assumed to NOT be already multiplied with the alpha component. For example, (1.0, 0, 0, 0.5) is semi-transparent bright red.
 */
const val WGPUCompositeAlphaMode_Unpremultiplied : WGPUCompositeAlphaMode = 3u
/**
 * The handling of the alpha component is unknown to WebGPU and should be handled by the application using system-specific APIs. This mode may be unavailable (for example on Wasm).
 */
const val WGPUCompositeAlphaMode_Inherit : WGPUCompositeAlphaMode = 4u
/**
 * The handling of the alpha component is unknown to WebGPU and should be handled by the application using system-specific APIs. This mode may be unavailable (for example on Wasm).
 */
const val WGPUCompositeAlphaMode_Force32 : WGPUCompositeAlphaMode = 2147483647u

typealias WGPUCreatePipelineAsyncStatus = UInt
const val WGPUCreatePipelineAsyncStatus_Success : WGPUCreatePipelineAsyncStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCreatePipelineAsyncStatus_CallbackCancelled : WGPUCreatePipelineAsyncStatus = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCreatePipelineAsyncStatus_ValidationError : WGPUCreatePipelineAsyncStatus = 3u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCreatePipelineAsyncStatus_InternalError : WGPUCreatePipelineAsyncStatus = 4u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUCreatePipelineAsyncStatus_Force32 : WGPUCreatePipelineAsyncStatus = 2147483647u

typealias WGPUCullMode = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCullMode_Undefined : WGPUCullMode = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCullMode_None : WGPUCullMode = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCullMode_Front : WGPUCullMode = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCullMode_Back : WGPUCullMode = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUCullMode_Force32 : WGPUCullMode = 2147483647u

typealias WGPUDeviceLostReason = UInt
const val WGPUDeviceLostReason_Unknown : WGPUDeviceLostReason = 1u
const val WGPUDeviceLostReason_Destroyed : WGPUDeviceLostReason = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUDeviceLostReason_CallbackCancelled : WGPUDeviceLostReason = 3u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUDeviceLostReason_FailedCreation : WGPUDeviceLostReason = 4u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUDeviceLostReason_Force32 : WGPUDeviceLostReason = 2147483647u

typealias WGPUErrorFilter = UInt
const val WGPUErrorFilter_Validation : WGPUErrorFilter = 1u
const val WGPUErrorFilter_OutOfMemory : WGPUErrorFilter = 2u
const val WGPUErrorFilter_Internal : WGPUErrorFilter = 3u
const val WGPUErrorFilter_Force32 : WGPUErrorFilter = 2147483647u

typealias WGPUErrorType = UInt
const val WGPUErrorType_NoError : WGPUErrorType = 1u
const val WGPUErrorType_Validation : WGPUErrorType = 2u
const val WGPUErrorType_OutOfMemory : WGPUErrorType = 3u
const val WGPUErrorType_Internal : WGPUErrorType = 4u
const val WGPUErrorType_Unknown : WGPUErrorType = 5u
const val WGPUErrorType_Force32 : WGPUErrorType = 2147483647u

/**
 * See @ref WGPURequestAdapterOptions::featureLevel.
 */
typealias WGPUFeatureLevel = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFeatureLevel_Undefined : WGPUFeatureLevel = 0u
/**
 * "Compatibility" profile which can be supported on OpenGL ES 3.1 and D3D11.
 */
const val WGPUFeatureLevel_Compatibility : WGPUFeatureLevel = 1u
/**
 * "Core" profile which can be supported on Vulkan/Metal/D3D12 (at least).
 */
const val WGPUFeatureLevel_Core : WGPUFeatureLevel = 2u
/**
 * "Core" profile which can be supported on Vulkan/Metal/D3D12 (at least).
 */
const val WGPUFeatureLevel_Force32 : WGPUFeatureLevel = 2147483647u

typealias WGPUFeatureName = UInt
const val WGPUFeatureName_CoreFeaturesAndLimits : WGPUFeatureName = 1u
const val WGPUFeatureName_DepthClipControl : WGPUFeatureName = 2u
const val WGPUFeatureName_Depth32FloatStencil8 : WGPUFeatureName = 3u
const val WGPUFeatureName_TextureCompressionBC : WGPUFeatureName = 4u
const val WGPUFeatureName_TextureCompressionBCSliced3D : WGPUFeatureName = 5u
const val WGPUFeatureName_TextureCompressionETC2 : WGPUFeatureName = 6u
const val WGPUFeatureName_TextureCompressionASTC : WGPUFeatureName = 7u
const val WGPUFeatureName_TextureCompressionASTCSliced3D : WGPUFeatureName = 8u
const val WGPUFeatureName_TimestampQuery : WGPUFeatureName = 9u
const val WGPUFeatureName_IndirectFirstInstance : WGPUFeatureName = 10u
const val WGPUFeatureName_ShaderF16 : WGPUFeatureName = 11u
const val WGPUFeatureName_RG11B10UfloatRenderable : WGPUFeatureName = 12u
const val WGPUFeatureName_BGRA8UnormStorage : WGPUFeatureName = 13u
const val WGPUFeatureName_Float32Filterable : WGPUFeatureName = 14u
const val WGPUFeatureName_Float32Blendable : WGPUFeatureName = 15u
const val WGPUFeatureName_ClipDistances : WGPUFeatureName = 16u
const val WGPUFeatureName_DualSourceBlending : WGPUFeatureName = 17u
const val WGPUFeatureName_Subgroups : WGPUFeatureName = 18u
const val WGPUFeatureName_TextureFormatsTier1 : WGPUFeatureName = 19u
const val WGPUFeatureName_TextureFormatsTier2 : WGPUFeatureName = 20u
const val WGPUFeatureName_PrimitiveIndex : WGPUFeatureName = 21u
const val WGPUFeatureName_TextureComponentSwizzle : WGPUFeatureName = 22u
const val WGPUFeatureName_Force32 : WGPUFeatureName = 2147483647u

typealias WGPUFilterMode = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFilterMode_Undefined : WGPUFilterMode = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFilterMode_Nearest : WGPUFilterMode = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFilterMode_Linear : WGPUFilterMode = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFilterMode_Force32 : WGPUFilterMode = 2147483647u

typealias WGPUFrontFace = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFrontFace_Undefined : WGPUFrontFace = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFrontFace_CCW : WGPUFrontFace = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFrontFace_CW : WGPUFrontFace = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUFrontFace_Force32 : WGPUFrontFace = 2147483647u

typealias WGPUIndexFormat = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUIndexFormat_Undefined : WGPUIndexFormat = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUIndexFormat_Uint16 : WGPUIndexFormat = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUIndexFormat_Uint32 : WGPUIndexFormat = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUIndexFormat_Force32 : WGPUIndexFormat = 2147483647u

typealias WGPUInstanceFeatureName = UInt
/**
 * Enable use of ::wgpuInstanceWaitAny with `timeoutNS > 0`.
 */
const val WGPUInstanceFeatureName_TimedWaitAny : WGPUInstanceFeatureName = 1u
/**
 * Enable passing SPIR-V shaders to @ref wgpuDeviceCreateShaderModule,
 * via @ref WGPUShaderSourceSPIRV.
 */
const val WGPUInstanceFeatureName_ShaderSourceSPIRV : WGPUInstanceFeatureName = 2u
/**
 * Normally, a @ref WGPUAdapter can only create a single device. If this is
 * available and enabled, then adapters won't immediately expire when they
 * create a device, so can be reused to make multiple devices. They may
 * still expire for other reasons.
 */
const val WGPUInstanceFeatureName_MultipleDevicesPerAdapter : WGPUInstanceFeatureName = 3u
/**
 * Normally, a @ref WGPUAdapter can only create a single device. If this is
 * available and enabled, then adapters won't immediately expire when they
 * create a device, so can be reused to make multiple devices. They may
 * still expire for other reasons.
 */
const val WGPUInstanceFeatureName_Force32 : WGPUInstanceFeatureName = 2147483647u

typealias WGPULoadOp = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPULoadOp_Undefined : WGPULoadOp = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPULoadOp_Load : WGPULoadOp = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPULoadOp_Clear : WGPULoadOp = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPULoadOp_Force32 : WGPULoadOp = 2147483647u

typealias WGPUMapAsyncStatus = UInt
const val WGPUMapAsyncStatus_Success : WGPUMapAsyncStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUMapAsyncStatus_CallbackCancelled : WGPUMapAsyncStatus = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUMapAsyncStatus_Error : WGPUMapAsyncStatus = 3u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUMapAsyncStatus_Aborted : WGPUMapAsyncStatus = 4u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUMapAsyncStatus_Force32 : WGPUMapAsyncStatus = 2147483647u

typealias WGPUMipmapFilterMode = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUMipmapFilterMode_Undefined : WGPUMipmapFilterMode = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUMipmapFilterMode_Nearest : WGPUMipmapFilterMode = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUMipmapFilterMode_Linear : WGPUMipmapFilterMode = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUMipmapFilterMode_Force32 : WGPUMipmapFilterMode = 2147483647u

typealias WGPUOptionalBool = UInt
/**
 * `0`.
 */
const val WGPUOptionalBool_False : WGPUOptionalBool = 0u
/**
 * `0`.
 */
const val WGPUOptionalBool_True : WGPUOptionalBool = 1u
/**
 * `0`.
 */
const val WGPUOptionalBool_Undefined : WGPUOptionalBool = 2u
/**
 * `0`.
 */
const val WGPUOptionalBool_Force32 : WGPUOptionalBool = 2147483647u

typealias WGPUPopErrorScopeStatus = UInt
/**
 * The error scope stack was successfully popped and a result was reported.
 */
const val WGPUPopErrorScopeStatus_Success : WGPUPopErrorScopeStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUPopErrorScopeStatus_CallbackCancelled : WGPUPopErrorScopeStatus = 2u
/**
 * The error scope stack could not be popped, because it was empty.
 */
const val WGPUPopErrorScopeStatus_Error : WGPUPopErrorScopeStatus = 3u
/**
 * The error scope stack could not be popped, because it was empty.
 */
const val WGPUPopErrorScopeStatus_Force32 : WGPUPopErrorScopeStatus = 2147483647u

typealias WGPUPowerPreference = UInt
/**
 * `0`. No preference. (See also @ref SentinelValues.)
 */
const val WGPUPowerPreference_Undefined : WGPUPowerPreference = 0u
/**
 * `0`. No preference. (See also @ref SentinelValues.)
 */
const val WGPUPowerPreference_LowPower : WGPUPowerPreference = 1u
/**
 * `0`. No preference. (See also @ref SentinelValues.)
 */
const val WGPUPowerPreference_HighPerformance : WGPUPowerPreference = 2u
/**
 * `0`. No preference. (See also @ref SentinelValues.)
 */
const val WGPUPowerPreference_Force32 : WGPUPowerPreference = 2147483647u

typealias WGPUPredefinedColorSpace = UInt
const val WGPUPredefinedColorSpace_SRGB : WGPUPredefinedColorSpace = 1u
const val WGPUPredefinedColorSpace_DisplayP3 : WGPUPredefinedColorSpace = 2u
const val WGPUPredefinedColorSpace_Force32 : WGPUPredefinedColorSpace = 2147483647u

/**
 * Describes when and in which order frames are presented on the screen when @ref wgpuSurfacePresent is called.
 */
typealias WGPUPresentMode = UInt
/**
 * `0`. Present mode is not specified. Use the default.
 */
const val WGPUPresentMode_Undefined : WGPUPresentMode = 0u
/**
 * The presentation of the image to the user waits for the next vertical blanking period to update in a first-in, first-out manner.
 * Tearing cannot be observed and frame-loop will be limited to the display's refresh rate.
 * This is the only mode that's always available.
 */
const val WGPUPresentMode_Fifo : WGPUPresentMode = 1u
/**
 * The presentation of the image to the user tries to wait for the next vertical blanking period but may decide to not wait if a frame is presented late.
 * Tearing can sometimes be observed but late-frame don't produce a full-frame stutter in the presentation.
 * This is still a first-in, first-out mechanism so a frame-loop will be limited to the display's refresh rate.
 */
const val WGPUPresentMode_FifoRelaxed : WGPUPresentMode = 2u
/**
 * The presentation of the image to the user is updated immediately without waiting for a vertical blank.
 * Tearing can be observed but latency is minimized.
 */
const val WGPUPresentMode_Immediate : WGPUPresentMode = 3u
/**
 * The presentation of the image to the user waits for the next vertical blanking period to update to the latest provided image.
 * Tearing cannot be observed and a frame-loop is not limited to the display's refresh rate.
 */
const val WGPUPresentMode_Mailbox : WGPUPresentMode = 4u
/**
 * The presentation of the image to the user waits for the next vertical blanking period to update to the latest provided image.
 * Tearing cannot be observed and a frame-loop is not limited to the display's refresh rate.
 */
const val WGPUPresentMode_Force32 : WGPUPresentMode = 2147483647u

typealias WGPUPrimitiveTopology = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_Undefined : WGPUPrimitiveTopology = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_PointList : WGPUPrimitiveTopology = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_LineList : WGPUPrimitiveTopology = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_LineStrip : WGPUPrimitiveTopology = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_TriangleList : WGPUPrimitiveTopology = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_TriangleStrip : WGPUPrimitiveTopology = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUPrimitiveTopology_Force32 : WGPUPrimitiveTopology = 2147483647u

typealias WGPUQueryType = UInt
const val WGPUQueryType_Occlusion : WGPUQueryType = 1u
const val WGPUQueryType_Timestamp : WGPUQueryType = 2u
const val WGPUQueryType_Force32 : WGPUQueryType = 2147483647u

typealias WGPUQueueWorkDoneStatus = UInt
const val WGPUQueueWorkDoneStatus_Success : WGPUQueueWorkDoneStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPUQueueWorkDoneStatus_CallbackCancelled : WGPUQueueWorkDoneStatus = 2u
/**
 * There was some deterministic error. (Note this is currently never used,
 * but it will be relevant when it's possible to create a queue object.)
 */
const val WGPUQueueWorkDoneStatus_Error : WGPUQueueWorkDoneStatus = 3u
/**
 * There was some deterministic error. (Note this is currently never used,
 * but it will be relevant when it's possible to create a queue object.)
 */
const val WGPUQueueWorkDoneStatus_Force32 : WGPUQueueWorkDoneStatus = 2147483647u

typealias WGPURequestAdapterStatus = UInt
const val WGPURequestAdapterStatus_Success : WGPURequestAdapterStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestAdapterStatus_CallbackCancelled : WGPURequestAdapterStatus = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestAdapterStatus_Unavailable : WGPURequestAdapterStatus = 3u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestAdapterStatus_Error : WGPURequestAdapterStatus = 4u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestAdapterStatus_Force32 : WGPURequestAdapterStatus = 2147483647u

typealias WGPURequestDeviceStatus = UInt
const val WGPURequestDeviceStatus_Success : WGPURequestDeviceStatus = 1u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestDeviceStatus_CallbackCancelled : WGPURequestDeviceStatus = 2u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestDeviceStatus_Error : WGPURequestDeviceStatus = 3u
/**
 * See @ref CallbackStatuses.
 */
const val WGPURequestDeviceStatus_Force32 : WGPURequestDeviceStatus = 2147483647u

typealias WGPUSamplerBindingType = UInt
/**
 * `0`. Indicates that this @ref WGPUSamplerBindingLayout member of
 * its parent @ref WGPUBindGroupLayoutEntry is not used.
 * (See also @ref SentinelValues.)
 */
const val WGPUSamplerBindingType_BindingNotUsed : WGPUSamplerBindingType = 0u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUSamplerBindingType_Undefined : WGPUSamplerBindingType = 1u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUSamplerBindingType_Filtering : WGPUSamplerBindingType = 2u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUSamplerBindingType_NonFiltering : WGPUSamplerBindingType = 3u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUSamplerBindingType_Comparison : WGPUSamplerBindingType = 4u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUSamplerBindingType_Force32 : WGPUSamplerBindingType = 2147483647u

/**
 * Status code returned (synchronously) from many operations. Generally
 * indicates an invalid input like an unknown enum value or @ref OutStructChainError.
 * Read the function's documentation for specific error conditions.
 */
typealias WGPUStatus = UInt
const val WGPUStatus_Success : WGPUStatus = 1u
const val WGPUStatus_Error : WGPUStatus = 2u
const val WGPUStatus_Force32 : WGPUStatus = 2147483647u

typealias WGPUStencilOperation = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Undefined : WGPUStencilOperation = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Keep : WGPUStencilOperation = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Zero : WGPUStencilOperation = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Replace : WGPUStencilOperation = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Invert : WGPUStencilOperation = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_IncrementClamp : WGPUStencilOperation = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_DecrementClamp : WGPUStencilOperation = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_IncrementWrap : WGPUStencilOperation = 7u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_DecrementWrap : WGPUStencilOperation = 8u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStencilOperation_Force32 : WGPUStencilOperation = 2147483647u

typealias WGPUStorageTextureAccess = UInt
/**
 * `0`. Indicates that this @ref WGPUStorageTextureBindingLayout member of
 * its parent @ref WGPUBindGroupLayoutEntry is not used.
 * (See also @ref SentinelValues.)
 */
const val WGPUStorageTextureAccess_BindingNotUsed : WGPUStorageTextureAccess = 0u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStorageTextureAccess_Undefined : WGPUStorageTextureAccess = 1u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStorageTextureAccess_WriteOnly : WGPUStorageTextureAccess = 2u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStorageTextureAccess_ReadOnly : WGPUStorageTextureAccess = 3u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStorageTextureAccess_ReadWrite : WGPUStorageTextureAccess = 4u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStorageTextureAccess_Force32 : WGPUStorageTextureAccess = 2147483647u

typealias WGPUStoreOp = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStoreOp_Undefined : WGPUStoreOp = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStoreOp_Store : WGPUStoreOp = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStoreOp_Discard : WGPUStoreOp = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUStoreOp_Force32 : WGPUStoreOp = 2147483647u

typealias WGPUSType = UInt
const val WGPUSType_ShaderSourceSPIRV : WGPUSType = 1u
const val WGPUSType_ShaderSourceWGSL : WGPUSType = 2u
const val WGPUSType_RenderPassMaxDrawCount : WGPUSType = 3u
const val WGPUSType_SurfaceSourceMetalLayer : WGPUSType = 4u
const val WGPUSType_SurfaceSourceWindowsHWND : WGPUSType = 5u
const val WGPUSType_SurfaceSourceXlibWindow : WGPUSType = 6u
const val WGPUSType_SurfaceSourceWaylandSurface : WGPUSType = 7u
const val WGPUSType_SurfaceSourceAndroidNativeWindow : WGPUSType = 8u
const val WGPUSType_SurfaceSourceXCBWindow : WGPUSType = 9u
const val WGPUSType_SurfaceColorManagement : WGPUSType = 10u
const val WGPUSType_RequestAdapterWebXROptions : WGPUSType = 11u
const val WGPUSType_TextureComponentSwizzleDescriptor : WGPUSType = 12u
const val WGPUSType_ExternalTextureBindingLayout : WGPUSType = 13u
const val WGPUSType_ExternalTextureBindingEntry : WGPUSType = 14u
const val WGPUSType_CompatibilityModeLimits : WGPUSType = 15u
const val WGPUSType_TextureBindingViewDimension : WGPUSType = 16u
const val WGPUSType_Force32 : WGPUSType = 2147483647u

/**
 * The status enum for @ref wgpuSurfaceGetCurrentTexture.
 */
typealias WGPUSurfaceGetCurrentTextureStatus = UInt
/**
 * Yay! Everything is good and we can render this frame.
 */
const val WGPUSurfaceGetCurrentTextureStatus_SuccessOptimal : WGPUSurfaceGetCurrentTextureStatus = 1u
/**
 * Still OK - the surface can present the frame, but in a suboptimal way. The surface may need reconfiguration.
 */
const val WGPUSurfaceGetCurrentTextureStatus_SuccessSuboptimal : WGPUSurfaceGetCurrentTextureStatus = 2u
/**
 * Some operation timed out while trying to acquire the frame.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Timeout : WGPUSurfaceGetCurrentTextureStatus = 3u
/**
 * The surface is too different to be used, compared to when it was originally created.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Outdated : WGPUSurfaceGetCurrentTextureStatus = 4u
/**
 * The connection to whatever owns the surface was lost, or generally needs to be fully reinitialized.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Lost : WGPUSurfaceGetCurrentTextureStatus = 5u
/**
 * There was some deterministic error (for example, the surface is not configured, or there was an @ref OutStructChainError). Should produce @ref ImplementationDefinedLogging containing details.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Error : WGPUSurfaceGetCurrentTextureStatus = 6u
/**
 * There was some deterministic error (for example, the surface is not configured, or there was an @ref OutStructChainError). Should produce @ref ImplementationDefinedLogging containing details.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Force32 : WGPUSurfaceGetCurrentTextureStatus = 2147483647u

typealias WGPUTextureAspect = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureAspect_Undefined : WGPUTextureAspect = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureAspect_All : WGPUTextureAspect = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureAspect_StencilOnly : WGPUTextureAspect = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureAspect_DepthOnly : WGPUTextureAspect = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureAspect_Force32 : WGPUTextureAspect = 2147483647u

typealias WGPUTextureDimension = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureDimension_Undefined : WGPUTextureDimension = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureDimension_1D : WGPUTextureDimension = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureDimension_2D : WGPUTextureDimension = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureDimension_3D : WGPUTextureDimension = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureDimension_Force32 : WGPUTextureDimension = 2147483647u

typealias WGPUTextureFormat = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Undefined : WGPUTextureFormat = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R8Unorm : WGPUTextureFormat = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R8Snorm : WGPUTextureFormat = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R8Uint : WGPUTextureFormat = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R8Sint : WGPUTextureFormat = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R16Unorm : WGPUTextureFormat = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R16Snorm : WGPUTextureFormat = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R16Uint : WGPUTextureFormat = 7u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R16Sint : WGPUTextureFormat = 8u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R16Float : WGPUTextureFormat = 9u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG8Unorm : WGPUTextureFormat = 10u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG8Snorm : WGPUTextureFormat = 11u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG8Uint : WGPUTextureFormat = 12u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG8Sint : WGPUTextureFormat = 13u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R32Float : WGPUTextureFormat = 14u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R32Uint : WGPUTextureFormat = 15u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_R32Sint : WGPUTextureFormat = 16u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG16Unorm : WGPUTextureFormat = 17u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG16Snorm : WGPUTextureFormat = 18u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG16Uint : WGPUTextureFormat = 19u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG16Sint : WGPUTextureFormat = 20u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG16Float : WGPUTextureFormat = 21u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA8Unorm : WGPUTextureFormat = 22u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA8UnormSrgb : WGPUTextureFormat = 23u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA8Snorm : WGPUTextureFormat = 24u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA8Uint : WGPUTextureFormat = 25u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA8Sint : WGPUTextureFormat = 26u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BGRA8Unorm : WGPUTextureFormat = 27u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BGRA8UnormSrgb : WGPUTextureFormat = 28u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGB10A2Uint : WGPUTextureFormat = 29u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGB10A2Unorm : WGPUTextureFormat = 30u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG11B10Ufloat : WGPUTextureFormat = 31u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGB9E5Ufloat : WGPUTextureFormat = 32u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG32Float : WGPUTextureFormat = 33u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG32Uint : WGPUTextureFormat = 34u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RG32Sint : WGPUTextureFormat = 35u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA16Unorm : WGPUTextureFormat = 36u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA16Snorm : WGPUTextureFormat = 37u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA16Uint : WGPUTextureFormat = 38u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA16Sint : WGPUTextureFormat = 39u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA16Float : WGPUTextureFormat = 40u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA32Float : WGPUTextureFormat = 41u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA32Uint : WGPUTextureFormat = 42u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_RGBA32Sint : WGPUTextureFormat = 43u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Stencil8 : WGPUTextureFormat = 44u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Depth16Unorm : WGPUTextureFormat = 45u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Depth24Plus : WGPUTextureFormat = 46u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Depth24PlusStencil8 : WGPUTextureFormat = 47u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Depth32Float : WGPUTextureFormat = 48u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Depth32FloatStencil8 : WGPUTextureFormat = 49u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC1RGBAUnorm : WGPUTextureFormat = 50u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC1RGBAUnormSrgb : WGPUTextureFormat = 51u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC2RGBAUnorm : WGPUTextureFormat = 52u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC2RGBAUnormSrgb : WGPUTextureFormat = 53u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC3RGBAUnorm : WGPUTextureFormat = 54u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC3RGBAUnormSrgb : WGPUTextureFormat = 55u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC4RUnorm : WGPUTextureFormat = 56u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC4RSnorm : WGPUTextureFormat = 57u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC5RGUnorm : WGPUTextureFormat = 58u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC5RGSnorm : WGPUTextureFormat = 59u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC6HRGBUfloat : WGPUTextureFormat = 60u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC6HRGBFloat : WGPUTextureFormat = 61u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC7RGBAUnorm : WGPUTextureFormat = 62u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_BC7RGBAUnormSrgb : WGPUTextureFormat = 63u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGB8Unorm : WGPUTextureFormat = 64u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGB8UnormSrgb : WGPUTextureFormat = 65u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGB8A1Unorm : WGPUTextureFormat = 66u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGB8A1UnormSrgb : WGPUTextureFormat = 67u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGBA8Unorm : WGPUTextureFormat = 68u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ETC2RGBA8UnormSrgb : WGPUTextureFormat = 69u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_EACR11Unorm : WGPUTextureFormat = 70u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_EACR11Snorm : WGPUTextureFormat = 71u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_EACRG11Unorm : WGPUTextureFormat = 72u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_EACRG11Snorm : WGPUTextureFormat = 73u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC4x4Unorm : WGPUTextureFormat = 74u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC4x4UnormSrgb : WGPUTextureFormat = 75u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC5x4Unorm : WGPUTextureFormat = 76u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC5x4UnormSrgb : WGPUTextureFormat = 77u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC5x5Unorm : WGPUTextureFormat = 78u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC5x5UnormSrgb : WGPUTextureFormat = 79u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC6x5Unorm : WGPUTextureFormat = 80u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC6x5UnormSrgb : WGPUTextureFormat = 81u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC6x6Unorm : WGPUTextureFormat = 82u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC6x6UnormSrgb : WGPUTextureFormat = 83u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x5Unorm : WGPUTextureFormat = 84u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x5UnormSrgb : WGPUTextureFormat = 85u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x6Unorm : WGPUTextureFormat = 86u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x6UnormSrgb : WGPUTextureFormat = 87u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x8Unorm : WGPUTextureFormat = 88u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC8x8UnormSrgb : WGPUTextureFormat = 89u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x5Unorm : WGPUTextureFormat = 90u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x5UnormSrgb : WGPUTextureFormat = 91u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x6Unorm : WGPUTextureFormat = 92u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x6UnormSrgb : WGPUTextureFormat = 93u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x8Unorm : WGPUTextureFormat = 94u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x8UnormSrgb : WGPUTextureFormat = 95u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x10Unorm : WGPUTextureFormat = 96u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC10x10UnormSrgb : WGPUTextureFormat = 97u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC12x10Unorm : WGPUTextureFormat = 98u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC12x10UnormSrgb : WGPUTextureFormat = 99u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC12x12Unorm : WGPUTextureFormat = 100u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_ASTC12x12UnormSrgb : WGPUTextureFormat = 101u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureFormat_Force32 : WGPUTextureFormat = 2147483647u

typealias WGPUTextureSampleType = UInt
/**
 * `0`. Indicates that this @ref WGPUTextureBindingLayout member of
 * its parent @ref WGPUBindGroupLayoutEntry is not used.
 * (See also @ref SentinelValues.)
 */
const val WGPUTextureSampleType_BindingNotUsed : WGPUTextureSampleType = 0u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Undefined : WGPUTextureSampleType = 1u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Float : WGPUTextureSampleType = 2u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_UnfilterableFloat : WGPUTextureSampleType = 3u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Depth : WGPUTextureSampleType = 4u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Sint : WGPUTextureSampleType = 5u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Uint : WGPUTextureSampleType = 6u
/**
 * `1`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureSampleType_Force32 : WGPUTextureSampleType = 2147483647u

typealias WGPUTextureViewDimension = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_Undefined : WGPUTextureViewDimension = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_1D : WGPUTextureViewDimension = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_2D : WGPUTextureViewDimension = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_2DArray : WGPUTextureViewDimension = 3u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_Cube : WGPUTextureViewDimension = 4u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_CubeArray : WGPUTextureViewDimension = 5u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_3D : WGPUTextureViewDimension = 6u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUTextureViewDimension_Force32 : WGPUTextureViewDimension = 2147483647u

typealias WGPUToneMappingMode = UInt
const val WGPUToneMappingMode_Standard : WGPUToneMappingMode = 1u
const val WGPUToneMappingMode_Extended : WGPUToneMappingMode = 2u
const val WGPUToneMappingMode_Force32 : WGPUToneMappingMode = 2147483647u

typealias WGPUVertexFormat = UInt
const val WGPUVertexFormat_Uint8 : WGPUVertexFormat = 1u
const val WGPUVertexFormat_Uint8x2 : WGPUVertexFormat = 2u
const val WGPUVertexFormat_Uint8x4 : WGPUVertexFormat = 3u
const val WGPUVertexFormat_Sint8 : WGPUVertexFormat = 4u
const val WGPUVertexFormat_Sint8x2 : WGPUVertexFormat = 5u
const val WGPUVertexFormat_Sint8x4 : WGPUVertexFormat = 6u
const val WGPUVertexFormat_Unorm8 : WGPUVertexFormat = 7u
const val WGPUVertexFormat_Unorm8x2 : WGPUVertexFormat = 8u
const val WGPUVertexFormat_Unorm8x4 : WGPUVertexFormat = 9u
const val WGPUVertexFormat_Snorm8 : WGPUVertexFormat = 10u
const val WGPUVertexFormat_Snorm8x2 : WGPUVertexFormat = 11u
const val WGPUVertexFormat_Snorm8x4 : WGPUVertexFormat = 12u
const val WGPUVertexFormat_Uint16 : WGPUVertexFormat = 13u
const val WGPUVertexFormat_Uint16x2 : WGPUVertexFormat = 14u
const val WGPUVertexFormat_Uint16x4 : WGPUVertexFormat = 15u
const val WGPUVertexFormat_Sint16 : WGPUVertexFormat = 16u
const val WGPUVertexFormat_Sint16x2 : WGPUVertexFormat = 17u
const val WGPUVertexFormat_Sint16x4 : WGPUVertexFormat = 18u
const val WGPUVertexFormat_Unorm16 : WGPUVertexFormat = 19u
const val WGPUVertexFormat_Unorm16x2 : WGPUVertexFormat = 20u
const val WGPUVertexFormat_Unorm16x4 : WGPUVertexFormat = 21u
const val WGPUVertexFormat_Snorm16 : WGPUVertexFormat = 22u
const val WGPUVertexFormat_Snorm16x2 : WGPUVertexFormat = 23u
const val WGPUVertexFormat_Snorm16x4 : WGPUVertexFormat = 24u
const val WGPUVertexFormat_Float16 : WGPUVertexFormat = 25u
const val WGPUVertexFormat_Float16x2 : WGPUVertexFormat = 26u
const val WGPUVertexFormat_Float16x4 : WGPUVertexFormat = 27u
const val WGPUVertexFormat_Float32 : WGPUVertexFormat = 28u
const val WGPUVertexFormat_Float32x2 : WGPUVertexFormat = 29u
const val WGPUVertexFormat_Float32x3 : WGPUVertexFormat = 30u
const val WGPUVertexFormat_Float32x4 : WGPUVertexFormat = 31u
const val WGPUVertexFormat_Uint32 : WGPUVertexFormat = 32u
const val WGPUVertexFormat_Uint32x2 : WGPUVertexFormat = 33u
const val WGPUVertexFormat_Uint32x3 : WGPUVertexFormat = 34u
const val WGPUVertexFormat_Uint32x4 : WGPUVertexFormat = 35u
const val WGPUVertexFormat_Sint32 : WGPUVertexFormat = 36u
const val WGPUVertexFormat_Sint32x2 : WGPUVertexFormat = 37u
const val WGPUVertexFormat_Sint32x3 : WGPUVertexFormat = 38u
const val WGPUVertexFormat_Sint32x4 : WGPUVertexFormat = 39u
const val WGPUVertexFormat_Unorm10_10_10_2 : WGPUVertexFormat = 40u
const val WGPUVertexFormat_Unorm8x4BGRA : WGPUVertexFormat = 41u
const val WGPUVertexFormat_Force32 : WGPUVertexFormat = 2147483647u

typealias WGPUVertexStepMode = UInt
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUVertexStepMode_Undefined : WGPUVertexStepMode = 0u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUVertexStepMode_Vertex : WGPUVertexStepMode = 1u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUVertexStepMode_Instance : WGPUVertexStepMode = 2u
/**
 * `0`. Indicates no value is passed for this argument. See @ref SentinelValues.
 */
const val WGPUVertexStepMode_Force32 : WGPUVertexStepMode = 2147483647u

/**
 * Status returned from a call to ::wgpuInstanceWaitAny.
 */
typealias WGPUWaitStatus = UInt
/**
 * At least one WGPUFuture completed successfully.
 */
const val WGPUWaitStatus_Success : WGPUWaitStatus = 1u
/**
 * The wait operation succeeded, but no WGPUFutures completed within the timeout.
 */
const val WGPUWaitStatus_TimedOut : WGPUWaitStatus = 2u
/**
 * The call was invalid for some reason (see @ref Wait-Any).
 * Should produce @ref ImplementationDefinedLogging containing details.
 */
const val WGPUWaitStatus_Error : WGPUWaitStatus = 3u
/**
 * The call was invalid for some reason (see @ref Wait-Any).
 * Should produce @ref ImplementationDefinedLogging containing details.
 */
const val WGPUWaitStatus_Force32 : WGPUWaitStatus = 2147483647u

typealias WGPUWGSLLanguageFeatureName = UInt
const val WGPUWGSLLanguageFeatureName_ReadonlyAndReadwriteStorageTextures : WGPUWGSLLanguageFeatureName = 1u
const val WGPUWGSLLanguageFeatureName_Packed4x8IntegerDotProduct : WGPUWGSLLanguageFeatureName = 2u
const val WGPUWGSLLanguageFeatureName_UnrestrictedPointerParameters : WGPUWGSLLanguageFeatureName = 3u
const val WGPUWGSLLanguageFeatureName_PointerCompositeAccess : WGPUWGSLLanguageFeatureName = 4u
const val WGPUWGSLLanguageFeatureName_UniformBufferStandardLayout : WGPUWGSLLanguageFeatureName = 5u
const val WGPUWGSLLanguageFeatureName_SubgroupId : WGPUWGSLLanguageFeatureName = 6u
const val WGPUWGSLLanguageFeatureName_TextureAndSamplerLet : WGPUWGSLLanguageFeatureName = 7u
const val WGPUWGSLLanguageFeatureName_SubgroupUniformity : WGPUWGSLLanguageFeatureName = 8u
const val WGPUWGSLLanguageFeatureName_TextureFormatsTier1 : WGPUWGSLLanguageFeatureName = 9u
const val WGPUWGSLLanguageFeatureName_LinearIndexing : WGPUWGSLLanguageFeatureName = 10u
const val WGPUWGSLLanguageFeatureName_Force32 : WGPUWGSLLanguageFeatureName = 2147483647u

/**
 * @} * /
 * / **
 * \defgroup ChainedStructures Chained Structures
 * \brief Structures used to extend descriptors.
 *
 * @{
 */
expect interface WGPUChainedStruct {
    var next: WGPUChainedStruct?
    var sType: WGPUSType
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUChainedStruct
        fun allocate(allocator: MemoryAllocator): WGPUChainedStruct
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUChainedStruct) -> Unit): ArrayHolder<WGPUChainedStruct>
    }
}

/**
 * \defgroup CallbackInfoStructs Callback Info Structs
 * \brief Callback info structures that are used in asynchronous functions.
 *
 * @{
 */
expect interface WGPUBufferMapCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBufferMapCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUBufferMapCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferMapCallbackInfo) -> Unit): ArrayHolder<WGPUBufferMapCallbackInfo>
    }
}

expect interface WGPUCompilationInfoCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCompilationInfoCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUCompilationInfoCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfoCallbackInfo) -> Unit): ArrayHolder<WGPUCompilationInfoCallbackInfo>
    }
}

expect interface WGPUCreateComputePipelineAsyncCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCreateComputePipelineAsyncCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUCreateComputePipelineAsyncCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateComputePipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateComputePipelineAsyncCallbackInfo>
    }
}

expect interface WGPUCreateRenderPipelineAsyncCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCreateRenderPipelineAsyncCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUCreateRenderPipelineAsyncCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCreateRenderPipelineAsyncCallbackInfo) -> Unit): ArrayHolder<WGPUCreateRenderPipelineAsyncCallbackInfo>
    }
}

expect interface WGPUDeviceLostCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUDeviceLostCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUDeviceLostCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceLostCallbackInfo) -> Unit): ArrayHolder<WGPUDeviceLostCallbackInfo>
    }
}

expect interface WGPUPopErrorScopeCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUPopErrorScopeCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUPopErrorScopeCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPopErrorScopeCallbackInfo) -> Unit): ArrayHolder<WGPUPopErrorScopeCallbackInfo>
    }
}

expect interface WGPUQueueWorkDoneCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUQueueWorkDoneCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUQueueWorkDoneCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueWorkDoneCallbackInfo) -> Unit): ArrayHolder<WGPUQueueWorkDoneCallbackInfo>
    }
}

expect interface WGPURequestAdapterCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURequestAdapterCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPURequestAdapterCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterCallbackInfo) -> Unit): ArrayHolder<WGPURequestAdapterCallbackInfo>
    }
}

expect interface WGPURequestDeviceCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Controls when the callback may be called.
     *
     * Has no default. The `INIT` macro sets this to (@ref WGPUCallbackMode)0.
     */
    var mode: WGPUCallbackMode
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURequestDeviceCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPURequestDeviceCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestDeviceCallbackInfo) -> Unit): ArrayHolder<WGPURequestDeviceCallbackInfo>
    }
}

expect interface WGPUUncapturedErrorCallbackInfo {
    var nextInChain: WGPUChainedStruct?
    var callback: NativeAddress?
    var userdata1: NativeAddress?
    var userdata2: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUUncapturedErrorCallbackInfo
        fun allocate(allocator: MemoryAllocator): WGPUUncapturedErrorCallbackInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUUncapturedErrorCallbackInfo) -> Unit): ArrayHolder<WGPUUncapturedErrorCallbackInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_ADAPTER_INFO_INIT as initializer.
 */
expect interface WGPUAdapterInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is an \ref OutputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var vendor: WGPUStringView
    /**
     * This is an \ref OutputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var architecture: WGPUStringView
    /**
     * This is an \ref OutputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var device: WGPUStringView
    /**
     * This is an \ref OutputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var description: WGPUStringView
    /**
     * The `INIT` macro sets this to @ref WGPUBackendType_Undefined.
     */
    var backendType: WGPUBackendType
    /**
     * The `INIT` macro sets this to (@ref WGPUAdapterType)0.
     */
    var adapterType: WGPUAdapterType
    /**
     * The `INIT` macro sets this to `0`.
     */
    var vendorID: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var deviceID: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var subgroupMinSize: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var subgroupMaxSize: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUAdapterInfo
        fun allocate(allocator: MemoryAllocator): WGPUAdapterInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUAdapterInfo) -> Unit): ArrayHolder<WGPUAdapterInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_BLEND_COMPONENT_INIT as initializer.
 */
expect interface WGPUBlendComponent {
    /**
     * If set to @ref WGPUBlendOperation_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUBlendOperation_Add.
     *
     * The `INIT` macro sets this to @ref WGPUBlendOperation_Undefined.
     */
    var operation: WGPUBlendOperation
    /**
     * If set to @ref WGPUBlendFactor_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUBlendFactor_One.
     *
     * The `INIT` macro sets this to @ref WGPUBlendFactor_Undefined.
     */
    var srcFactor: WGPUBlendFactor
    /**
     * If set to @ref WGPUBlendFactor_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUBlendFactor_Zero.
     *
     * The `INIT` macro sets this to @ref WGPUBlendFactor_Undefined.
     */
    var dstFactor: WGPUBlendFactor
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBlendComponent
        fun allocate(allocator: MemoryAllocator): WGPUBlendComponent
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendComponent) -> Unit): ArrayHolder<WGPUBlendComponent>
    }
}

/**
 * Default values can be set using @ref WGPU_BUFFER_BINDING_LAYOUT_INIT as initializer.
 */
expect interface WGPUBufferBindingLayout {
    var nextInChain: WGPUChainedStruct?
    /**
     * If set to @ref WGPUBufferBindingType_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUBufferBindingType_Uniform.
     *
     * The `INIT` macro sets this to @ref WGPUBufferBindingType_Undefined.
     */
    var type: WGPUBufferBindingType
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var hasDynamicOffset: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var minBindingSize: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBufferBindingLayout
        fun allocate(allocator: MemoryAllocator): WGPUBufferBindingLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferBindingLayout) -> Unit): ArrayHolder<WGPUBufferBindingLayout>
    }
}

/**
 * Default values can be set using @ref WGPU_BUFFER_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUBufferDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to @ref WGPUBufferUsage_None.
     */
    var usage: ULong
    /**
     * The `INIT` macro sets this to `0`.
     */
    var size: ULong
    /**
     * When true, the buffer is mapped in write mode at creation. It should thus be unmapped once its initial data has been written.
     *
     * @note Mapping at creation does **not** require the usage @ref WGPUBufferUsage_MapWrite.
     *
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var mappedAtCreation: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBufferDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUBufferDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBufferDescriptor) -> Unit): ArrayHolder<WGPUBufferDescriptor>
    }
}

/**
 * An RGBA color. Represents a `f32`, `i32`, or `u32` color using @ref DoubleAsSupertype.
 *
 * If any channel is non-finite, produces a @ref NonFiniteFloatValueError.
 *
 * Default values can be set using @ref WGPU_COLOR_INIT as initializer.
 */
expect interface WGPUColor {
    /**
     * The `INIT` macro sets this to `0.`.
     */
    var r: Double
    /**
     * The `INIT` macro sets this to `0.`.
     */
    var g: Double
    /**
     * The `INIT` macro sets this to `0.`.
     */
    var b: Double
    /**
     * The `INIT` macro sets this to `0.`.
     */
    var a: Double
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUColor
        fun allocate(allocator: MemoryAllocator): WGPUColor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColor) -> Unit): ArrayHolder<WGPUColor>
    }
}

/**
 * Default values can be set using @ref WGPU_COMMAND_BUFFER_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUCommandBufferDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCommandBufferDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUCommandBufferDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandBufferDescriptor) -> Unit): ArrayHolder<WGPUCommandBufferDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_COMMAND_ENCODER_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUCommandEncoderDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCommandEncoderDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUCommandEncoderDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCommandEncoderDescriptor) -> Unit): ArrayHolder<WGPUCommandEncoderDescriptor>
    }
}

/**
 * Note: While Compatibility Mode is optional to implement, this extension struct
 * is required to be supported (for both queries and requests) and behave as
 * defined in the WebGPU spec.
 *
 * Default values can be set using @ref WGPU_COMPATIBILITY_MODE_LIMITS_INIT as initializer.
 */
expect interface WGPUCompatibilityModeLimits {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageBuffersInVertexStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageTexturesInVertexStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageBuffersInFragmentStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageTexturesInFragmentStage: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCompatibilityModeLimits
        fun allocate(allocator: MemoryAllocator): WGPUCompatibilityModeLimits
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompatibilityModeLimits) -> Unit): ArrayHolder<WGPUCompatibilityModeLimits>
    }
}

/**
 * This is an @ref ImplementationAllocatedStructChain root.
 * Arbitrary chains must be handled gracefully by the application!
 *
 * Default values can be set using @ref WGPU_COMPILATION_MESSAGE_INIT as initializer.
 */
expect interface WGPUCompilationMessage {
    var nextInChain: WGPUChainedStruct?
    /**
     * A @ref LocalizableHumanReadableMessageString.
     *
     * This is an \ref OutputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var message: WGPUStringView
    /**
     * Severity level of the message.
     *
     * The `INIT` macro sets this to (@ref WGPUCompilationMessageType)0.
     */
    var type: WGPUCompilationMessageType
    /**
     * Line number where the message is attached, starting at 1.
     *
     * The `INIT` macro sets this to `0`.
     */
    var lineNum: ULong
    /**
     * Offset in UTF-8 code units (bytes) from the beginning of the line, starting at 1.
     *
     * The `INIT` macro sets this to `0`.
     */
    var linePos: ULong
    /**
     * Offset in UTF-8 code units (bytes) from the beginning of the shader code, starting at 0.
     *
     * The `INIT` macro sets this to `0`.
     */
    var offset: ULong
    /**
     * Length in UTF-8 code units (bytes) of the span the message corresponds to.
     *
     * The `INIT` macro sets this to `0`.
     */
    var length: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCompilationMessage
        fun allocate(allocator: MemoryAllocator): WGPUCompilationMessage
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationMessage) -> Unit): ArrayHolder<WGPUCompilationMessage>
    }
}

/**
 * Default values can be set using @ref WGPU_CONSTANT_ENTRY_INIT as initializer.
 */
expect interface WGPUConstantEntry {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var key: WGPUStringView
    /**
     * Represents a WGSL numeric or boolean value using @ref DoubleAsSupertype.
     *
     * If non-finite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to `0.`.
     */
    var value: Double
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUConstantEntry
        fun allocate(allocator: MemoryAllocator): WGPUConstantEntry
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUConstantEntry) -> Unit): ArrayHolder<WGPUConstantEntry>
    }
}

/**
 * Default values can be set using @ref WGPU_EXTENT_3D_INIT as initializer.
 */
expect interface WGPUExtent3D {
    /**
     * The `INIT` macro sets this to `0`.
     */
    var width: UInt
    /**
     * The `INIT` macro sets this to `1`.
     */
    var height: UInt
    /**
     * The `INIT` macro sets this to `1`.
     */
    var depthOrArrayLayers: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUExtent3D
        fun allocate(allocator: MemoryAllocator): WGPUExtent3D
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExtent3D) -> Unit): ArrayHolder<WGPUExtent3D>
    }
}

/**
 * Chained in an @ref WGPUBindGroupEntry to set it to an @ref WGPUExternalTexture. This must have a corresponding @ref WGPUExternalTextureBindingLayout in the @ref WGPUBindGroupLayout.
 *
 * Default values can be set using @ref WGPU_EXTERNAL_TEXTURE_BINDING_ENTRY_INIT as initializer.
 */
expect interface WGPUExternalTextureBindingEntry {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var externalTexture: WGPUExternalTexture?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingEntry
        fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingEntry
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingEntry) -> Unit): ArrayHolder<WGPUExternalTextureBindingEntry>
    }
}

/**
 * Chained in @ref WGPUBindGroupLayoutEntry to specify that the corresponding entries in an @ref WGPUBindGroup will contain an @ref WGPUExternalTexture.
 *
 * Default values can be set using @ref WGPU_EXTERNAL_TEXTURE_BINDING_LAYOUT_INIT as initializer.
 */
expect interface WGPUExternalTextureBindingLayout {
    var chain: WGPUChainedStruct
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUExternalTextureBindingLayout
        fun allocate(allocator: MemoryAllocator): WGPUExternalTextureBindingLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUExternalTextureBindingLayout) -> Unit): ArrayHolder<WGPUExternalTextureBindingLayout>
    }
}

/**
 * Opaque handle to an asynchronous operation. See @ref Asynchronous-Operations for more information.
 *
 * Default values can be set using @ref WGPU_FUTURE_INIT as initializer.
 */
expect interface WGPUFuture {
    /**
     * Opaque id of the @ref WGPUFuture
     *
     * The `INIT` macro sets this to `0`.
     */
    var id: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUFuture
        fun allocate(allocator: MemoryAllocator): WGPUFuture
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFuture) -> Unit): ArrayHolder<WGPUFuture>
    }
}

/**
 * Default values can be set using @ref WGPU_INSTANCE_LIMITS_INIT as initializer.
 */
expect interface WGPUInstanceLimits {
    var nextInChain: WGPUChainedStruct?
    /**
     * The maximum number @ref WGPUFutureWaitInfo supported in a call to ::wgpuInstanceWaitAny with `timeoutNS > 0`.
     *
     * The `INIT` macro sets this to `0`.
     */
    var timedWaitAnyMaxCount: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUInstanceLimits
        fun allocate(allocator: MemoryAllocator): WGPUInstanceLimits
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceLimits) -> Unit): ArrayHolder<WGPUInstanceLimits>
    }
}

/**
 * Default values can be set using @ref WGPU_MULTISAMPLE_STATE_INIT as initializer.
 */
expect interface WGPUMultisampleState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `1`.
     */
    var count: UInt
    /**
     * The `INIT` macro sets this to `0xFFFFFFFF`.
     */
    var mask: UInt
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var alphaToCoverageEnabled: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUMultisampleState
        fun allocate(allocator: MemoryAllocator): WGPUMultisampleState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUMultisampleState) -> Unit): ArrayHolder<WGPUMultisampleState>
    }
}

/**
 * Default values can be set using @ref WGPU_ORIGIN_3D_INIT as initializer.
 */
expect interface WGPUOrigin3D {
    /**
     * The `INIT` macro sets this to `0`.
     */
    var x: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var y: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var z: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUOrigin3D
        fun allocate(allocator: MemoryAllocator): WGPUOrigin3D
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUOrigin3D) -> Unit): ArrayHolder<WGPUOrigin3D>
    }
}

/**
 * Default values can be set using @ref WGPU_PASS_TIMESTAMP_WRITES_INIT as initializer.
 */
expect interface WGPUPassTimestampWrites {
    var nextInChain: WGPUChainedStruct?
    /**
     * Query set to write timestamps to.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var querySet: WGPUQuerySet?
    /**
     * The `INIT` macro sets this to @ref WGPU_QUERY_SET_INDEX_UNDEFINED.
     */
    var beginningOfPassWriteIndex: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_QUERY_SET_INDEX_UNDEFINED.
     */
    var endOfPassWriteIndex: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUPassTimestampWrites
        fun allocate(allocator: MemoryAllocator): WGPUPassTimestampWrites
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPassTimestampWrites) -> Unit): ArrayHolder<WGPUPassTimestampWrites>
    }
}

/**
 * Default values can be set using @ref WGPU_PIPELINE_LAYOUT_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUPipelineLayoutDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * Array count for `bindGroupLayouts`. The `INIT` macro sets this to 0.
     */
    var bindGroupLayoutCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var bindGroupLayouts: NativeAddress?
    /**
     * The `INIT` macro sets this to `0`.
     */
    var immediateSize: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUPipelineLayoutDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUPipelineLayoutDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPipelineLayoutDescriptor) -> Unit): ArrayHolder<WGPUPipelineLayoutDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_PRIMITIVE_STATE_INIT as initializer.
 */
expect interface WGPUPrimitiveState {
    var nextInChain: WGPUChainedStruct?
    /**
     * If set to @ref WGPUPrimitiveTopology_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUPrimitiveTopology_TriangleList.
     *
     * The `INIT` macro sets this to @ref WGPUPrimitiveTopology_Undefined.
     */
    var topology: WGPUPrimitiveTopology
    /**
     * The `INIT` macro sets this to @ref WGPUIndexFormat_Undefined.
     */
    var stripIndexFormat: WGPUIndexFormat
    /**
     * If set to @ref WGPUFrontFace_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUFrontFace_CCW.
     *
     * The `INIT` macro sets this to @ref WGPUFrontFace_Undefined.
     */
    var frontFace: WGPUFrontFace
    /**
     * If set to @ref WGPUCullMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUCullMode_None.
     *
     * The `INIT` macro sets this to @ref WGPUCullMode_Undefined.
     */
    var cullMode: WGPUCullMode
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var unclippedDepth: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUPrimitiveState
        fun allocate(allocator: MemoryAllocator): WGPUPrimitiveState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveState) -> Unit): ArrayHolder<WGPUPrimitiveState>
    }
}

/**
 * Default values can be set using @ref WGPU_QUERY_SET_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUQuerySetDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to (@ref WGPUQueryType)0.
     */
    var type: WGPUQueryType
    /**
     * The `INIT` macro sets this to `0`.
     */
    var count: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptor) -> Unit): ArrayHolder<WGPUQuerySetDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_QUEUE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUQueueDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUQueueDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUQueueDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQueueDescriptor) -> Unit): ArrayHolder<WGPUQueueDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_BUNDLE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPURenderBundleDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderBundleDescriptor
        fun allocate(allocator: MemoryAllocator): WGPURenderBundleDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleDescriptor) -> Unit): ArrayHolder<WGPURenderBundleDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_BUNDLE_ENCODER_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPURenderBundleEncoderDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * Array count for `colorFormats`. The `INIT` macro sets this to 0.
     */
    var colorFormatCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var colorFormats: NativeAddress?
    /**
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var depthStencilFormat: WGPUTextureFormat
    /**
     * The `INIT` macro sets this to `1`.
     */
    var sampleCount: UInt
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var depthReadOnly: UInt
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var stencilReadOnly: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderBundleEncoderDescriptor
        fun allocate(allocator: MemoryAllocator): WGPURenderBundleEncoderDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderBundleEncoderDescriptor) -> Unit): ArrayHolder<WGPURenderBundleEncoderDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_PASS_DEPTH_STENCIL_ATTACHMENT_INIT as initializer.
 */
expect interface WGPURenderPassDepthStencilAttachment {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var view: WGPUTextureView?
    /**
     * The `INIT` macro sets this to @ref WGPULoadOp_Undefined.
     */
    var depthLoadOp: WGPULoadOp
    /**
     * The `INIT` macro sets this to @ref WGPUStoreOp_Undefined.
     */
    var depthStoreOp: WGPUStoreOp
    /**
     * This is a @ref NullableFloatingPointType.
     *
     * If `NaN`, indicates an `undefined` value (as defined by the JS spec).
     * Use @ref WGPU_DEPTH_CLEAR_VALUE_UNDEFINED to indicate this semantically.
     *
     * If infinite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to @ref WGPU_DEPTH_CLEAR_VALUE_UNDEFINED.
     */
    var depthClearValue: Float
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var depthReadOnly: UInt
    /**
     * The `INIT` macro sets this to @ref WGPULoadOp_Undefined.
     */
    var stencilLoadOp: WGPULoadOp
    /**
     * The `INIT` macro sets this to @ref WGPUStoreOp_Undefined.
     */
    var stencilStoreOp: WGPUStoreOp
    /**
     * The `INIT` macro sets this to `0`.
     */
    var stencilClearValue: UInt
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var stencilReadOnly: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderPassDepthStencilAttachment
        fun allocate(allocator: MemoryAllocator): WGPURenderPassDepthStencilAttachment
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDepthStencilAttachment) -> Unit): ArrayHolder<WGPURenderPassDepthStencilAttachment>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_PASS_MAX_DRAW_COUNT_INIT as initializer.
 */
expect interface WGPURenderPassMaxDrawCount {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to `50000000`.
     */
    var maxDrawCount: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderPassMaxDrawCount
        fun allocate(allocator: MemoryAllocator): WGPURenderPassMaxDrawCount
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassMaxDrawCount) -> Unit): ArrayHolder<WGPURenderPassMaxDrawCount>
    }
}

/**
 * Extension providing requestAdapter options for implementations with WebXR interop (i.e. Wasm).
 *
 * Default values can be set using @ref WGPU_REQUEST_ADAPTER_WEBXR_OPTIONS_INIT as initializer.
 */
expect interface WGPURequestAdapterWebXROptions {
    var chain: WGPUChainedStruct
    /**
     * Sets the `xrCompatible` option in the JS API.
     *
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var xrCompatible: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURequestAdapterWebXROptions
        fun allocate(allocator: MemoryAllocator): WGPURequestAdapterWebXROptions
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterWebXROptions) -> Unit): ArrayHolder<WGPURequestAdapterWebXROptions>
    }
}

/**
 * Default values can be set using @ref WGPU_SAMPLER_BINDING_LAYOUT_INIT as initializer.
 */
expect interface WGPUSamplerBindingLayout {
    var nextInChain: WGPUChainedStruct?
    /**
     * If set to @ref WGPUSamplerBindingType_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUSamplerBindingType_Filtering.
     *
     * The `INIT` macro sets this to @ref WGPUSamplerBindingType_Undefined.
     */
    var type: WGPUSamplerBindingType
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSamplerBindingLayout
        fun allocate(allocator: MemoryAllocator): WGPUSamplerBindingLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerBindingLayout) -> Unit): ArrayHolder<WGPUSamplerBindingLayout>
    }
}

/**
 * Default values can be set using @ref WGPU_SAMPLER_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUSamplerDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * If set to @ref WGPUAddressMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUAddressMode_ClampToEdge.
     *
     * The `INIT` macro sets this to @ref WGPUAddressMode_Undefined.
     */
    var addressModeU: WGPUAddressMode
    /**
     * If set to @ref WGPUAddressMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUAddressMode_ClampToEdge.
     *
     * The `INIT` macro sets this to @ref WGPUAddressMode_Undefined.
     */
    var addressModeV: WGPUAddressMode
    /**
     * If set to @ref WGPUAddressMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUAddressMode_ClampToEdge.
     *
     * The `INIT` macro sets this to @ref WGPUAddressMode_Undefined.
     */
    var addressModeW: WGPUAddressMode
    /**
     * If set to @ref WGPUFilterMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUFilterMode_Nearest.
     *
     * The `INIT` macro sets this to @ref WGPUFilterMode_Undefined.
     */
    var magFilter: WGPUFilterMode
    /**
     * If set to @ref WGPUFilterMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUFilterMode_Nearest.
     *
     * The `INIT` macro sets this to @ref WGPUFilterMode_Undefined.
     */
    var minFilter: WGPUFilterMode
    /**
     * If set to @ref WGPUFilterMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUMipmapFilterMode_Nearest.
     *
     * The `INIT` macro sets this to @ref WGPUMipmapFilterMode_Undefined.
     */
    var mipmapFilter: WGPUMipmapFilterMode
    /**
     * TODO
     *
     * If non-finite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to `0.f`.
     */
    var lodMinClamp: Float
    /**
     * TODO
     *
     * If non-finite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to `32.f`.
     */
    var lodMaxClamp: Float
    /**
     * The `INIT` macro sets this to @ref WGPUCompareFunction_Undefined.
     */
    var compare: WGPUCompareFunction
    /**
     * The `INIT` macro sets this to `1`.
     */
    var maxAnisotropy: UShort
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSamplerDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptor) -> Unit): ArrayHolder<WGPUSamplerDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_SHADER_SOURCE_SPIRV_INIT as initializer.
 */
expect interface WGPUShaderSourceSPIRV {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to `0`.
     */
    var codeSize: UInt
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var code: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderSourceSPIRV
        fun allocate(allocator: MemoryAllocator): WGPUShaderSourceSPIRV
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceSPIRV) -> Unit): ArrayHolder<WGPUShaderSourceSPIRV>
    }
}

/**
 * Default values can be set using @ref WGPU_SHADER_SOURCE_WGSL_INIT as initializer.
 */
expect interface WGPUShaderSourceWGSL {
    var chain: WGPUChainedStruct
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var code: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderSourceWGSL
        fun allocate(allocator: MemoryAllocator): WGPUShaderSourceWGSL
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceWGSL) -> Unit): ArrayHolder<WGPUShaderSourceWGSL>
    }
}

/**
 * Default values can be set using @ref WGPU_STENCIL_FACE_STATE_INIT as initializer.
 */
expect interface WGPUStencilFaceState {
    /**
     * If set to @ref WGPUCompareFunction_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUCompareFunction_Always.
     *
     * The `INIT` macro sets this to @ref WGPUCompareFunction_Undefined.
     */
    var compare: WGPUCompareFunction
    /**
     * If set to @ref WGPUStencilOperation_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUStencilOperation_Keep.
     *
     * The `INIT` macro sets this to @ref WGPUStencilOperation_Undefined.
     */
    var failOp: WGPUStencilOperation
    /**
     * If set to @ref WGPUStencilOperation_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUStencilOperation_Keep.
     *
     * The `INIT` macro sets this to @ref WGPUStencilOperation_Undefined.
     */
    var depthFailOp: WGPUStencilOperation
    /**
     * If set to @ref WGPUStencilOperation_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUStencilOperation_Keep.
     *
     * The `INIT` macro sets this to @ref WGPUStencilOperation_Undefined.
     */
    var passOp: WGPUStencilOperation
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUStencilFaceState
        fun allocate(allocator: MemoryAllocator): WGPUStencilFaceState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStencilFaceState) -> Unit): ArrayHolder<WGPUStencilFaceState>
    }
}

/**
 * Default values can be set using @ref WGPU_STORAGE_TEXTURE_BINDING_LAYOUT_INIT as initializer.
 */
expect interface WGPUStorageTextureBindingLayout {
    var nextInChain: WGPUChainedStruct?
    /**
     * If set to @ref WGPUStorageTextureAccess_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUStorageTextureAccess_WriteOnly.
     *
     * The `INIT` macro sets this to @ref WGPUStorageTextureAccess_Undefined.
     */
    var access: WGPUStorageTextureAccess
    /**
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * If set to @ref WGPUTextureViewDimension_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureViewDimension_2D.
     *
     * The `INIT` macro sets this to @ref WGPUTextureViewDimension_Undefined.
     */
    var viewDimension: WGPUTextureViewDimension
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUStorageTextureBindingLayout
        fun allocate(allocator: MemoryAllocator): WGPUStorageTextureBindingLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUStorageTextureBindingLayout) -> Unit): ArrayHolder<WGPUStorageTextureBindingLayout>
    }
}

/**
 * Default values can be set using @ref WGPU_SUPPORTED_FEATURES_INIT as initializer.
 */
expect interface WGPUSupportedFeatures {
    /**
     * Array count for `features`. The `INIT` macro sets this to 0.
     */
    var featureCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var features: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSupportedFeatures
        fun allocate(allocator: MemoryAllocator): WGPUSupportedFeatures
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedFeatures) -> Unit): ArrayHolder<WGPUSupportedFeatures>
    }
}

/**
 * Default values can be set using @ref WGPU_SUPPORTED_INSTANCE_FEATURES_INIT as initializer.
 */
expect interface WGPUSupportedInstanceFeatures {
    /**
     * Array count for `features`. The `INIT` macro sets this to 0.
     */
    var featureCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var features: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSupportedInstanceFeatures
        fun allocate(allocator: MemoryAllocator): WGPUSupportedInstanceFeatures
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedInstanceFeatures) -> Unit): ArrayHolder<WGPUSupportedInstanceFeatures>
    }
}

/**
 * Default values can be set using @ref WGPU_SUPPORTED_WGSL_LANGUAGE_FEATURES_INIT as initializer.
 */
expect interface WGPUSupportedWGSLLanguageFeatures {
    /**
     * Array count for `features`. The `INIT` macro sets this to 0.
     */
    var featureCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var features: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSupportedWGSLLanguageFeatures
        fun allocate(allocator: MemoryAllocator): WGPUSupportedWGSLLanguageFeatures
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSupportedWGSLLanguageFeatures) -> Unit): ArrayHolder<WGPUSupportedWGSLLanguageFeatures>
    }
}

/**
 * Filled by @ref wgpuSurfaceGetCapabilities with what's supported for @ref wgpuSurfaceConfigure for a pair of @ref WGPUSurface and @ref WGPUAdapter.
 *
 * Default values can be set using @ref WGPU_SURFACE_CAPABILITIES_INIT as initializer.
 */
expect interface WGPUSurfaceCapabilities {
    var nextInChain: WGPUChainedStruct?
    /**
     * The bit set of supported @ref WGPUTextureUsage bits.
     * Guaranteed to contain @ref WGPUTextureUsage_RenderAttachment.
     *
     * The `INIT` macro sets this to @ref WGPUTextureUsage_None.
     */
    var usages: ULong
    /**
     * Array count for `formats`. The `INIT` macro sets this to 0.
     */
    var formatCount: ULong
    /**
     * A list of supported @ref WGPUTextureFormat values, in order of preference.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var formats: NativeAddress?
    /**
     * Array count for `presentModes`. The `INIT` macro sets this to 0.
     */
    var presentModeCount: ULong
    /**
     * A list of supported @ref WGPUPresentMode values.
     * Guaranteed to contain @ref WGPUPresentMode_Fifo.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var presentModes: NativeAddress?
    /**
     * Array count for `alphaModes`. The `INIT` macro sets this to 0.
     */
    var alphaModeCount: ULong
    /**
     * A list of supported @ref WGPUCompositeAlphaMode values.
     * @ref WGPUCompositeAlphaMode_Auto will be an alias for the first element and will never be present in this array.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var alphaModes: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceCapabilities
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceCapabilities
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceCapabilities) -> Unit): ArrayHolder<WGPUSurfaceCapabilities>
    }
}

/**
 * Extension of @ref WGPUSurfaceConfiguration for color spaces and HDR.
 *
 * Default values can be set using @ref WGPU_SURFACE_COLOR_MANAGEMENT_INIT as initializer.
 */
expect interface WGPUSurfaceColorManagement {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to (@ref WGPUPredefinedColorSpace)0.
     */
    var colorSpace: WGPUPredefinedColorSpace
    /**
     * The `INIT` macro sets this to (@ref WGPUToneMappingMode)0.
     */
    var toneMappingMode: WGPUToneMappingMode
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceColorManagement
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceColorManagement
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceColorManagement) -> Unit): ArrayHolder<WGPUSurfaceColorManagement>
    }
}

/**
 * Options to @ref wgpuSurfaceConfigure for defining how a @ref WGPUSurface will be rendered to and presented to the user.
 * See @ref Surface-Configuration for more details.
 *
 * Default values can be set using @ref WGPU_SURFACE_CONFIGURATION_INIT as initializer.
 */
expect interface WGPUSurfaceConfiguration {
    var nextInChain: WGPUChainedStruct?
    /**
     * The @ref WGPUDevice to use to render to surface's textures.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var device: WGPUDevice?
    /**
     * The @ref WGPUTextureFormat of the surface's textures.
     *
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * The @ref WGPUTextureUsage of the surface's textures.
     *
     * The `INIT` macro sets this to @ref WGPUTextureUsage_RenderAttachment.
     */
    var usage: ULong
    /**
     * The width of the surface's textures.
     *
     * The `INIT` macro sets this to `0`.
     */
    var width: UInt
    /**
     * The height of the surface's textures.
     *
     * The `INIT` macro sets this to `0`.
     */
    var height: UInt
    /**
     * Array count for `viewFormats`. The `INIT` macro sets this to 0.
     */
    var viewFormatCount: ULong
    /**
     * The additional @ref WGPUTextureFormat for @ref WGPUTextureView format reinterpretation of the surface's textures.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var viewFormats: NativeAddress?
    /**
     * How the surface's frames will be composited on the screen.
     *
     * If set to @ref WGPUCompositeAlphaMode_Auto,
     * [defaults] to @ref WGPUCompositeAlphaMode_Inherit in native (allowing the mode
     * to be configured externally), and to @ref WGPUCompositeAlphaMode_Opaque in Wasm.
     *
     * The `INIT` macro sets this to @ref WGPUCompositeAlphaMode_Auto.
     */
    var alphaMode: WGPUCompositeAlphaMode
    /**
     * When and in which order the surface's frames will be shown on the screen.
     *
     * If set to @ref WGPUPresentMode_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUPresentMode_Fifo.
     *
     * The `INIT` macro sets this to @ref WGPUPresentMode_Undefined.
     */
    var presentMode: WGPUPresentMode
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceConfiguration
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfiguration
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfiguration) -> Unit): ArrayHolder<WGPUSurfaceConfiguration>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping an Android [`ANativeWindow`](https://developer.android.com/ndk/reference/group/a-native-window).
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_ANDROID_NATIVE_WINDOW_INIT as initializer.
 */
expect interface WGPUSurfaceSourceAndroidNativeWindow {
    var chain: WGPUChainedStruct
    /**
     * The pointer to the [`ANativeWindow`](https://developer.android.com/ndk/reference/group/a-native-window) that will be wrapped by the @ref WGPUSurface.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var window: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceAndroidNativeWindow
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceAndroidNativeWindow
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceAndroidNativeWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceAndroidNativeWindow>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping a [`CAMetalLayer`](https://developer.apple.com/documentation/quartzcore/cametallayer?language=objc).
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_METAL_LAYER_INIT as initializer.
 */
expect interface WGPUSurfaceSourceMetalLayer {
    var chain: WGPUChainedStruct
    /**
     * The pointer to the [`CAMetalLayer`](https://developer.apple.com/documentation/quartzcore/cametallayer?language=objc) that will be wrapped by the @ref WGPUSurface.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var layer: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceMetalLayer
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceMetalLayer
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceMetalLayer) -> Unit): ArrayHolder<WGPUSurfaceSourceMetalLayer>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping a [Wayland](https://wayland.freedesktop.org/) [`wl_surface`](https://wayland.freedesktop.org/docs/html/apa.html#protocol-spec-wl_surface).
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_WAYLAND_SURFACE_INIT as initializer.
 */
expect interface WGPUSurfaceSourceWaylandSurface {
    var chain: WGPUChainedStruct
    /**
     * A [`wl_display`](https://wayland.freedesktop.org/docs/html/apa.html#protocol-spec-wl_display) for this Wayland instance.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var display: NativeAddress?
    /**
     * A [`wl_surface`](https://wayland.freedesktop.org/docs/html/apa.html#protocol-spec-wl_surface) that will be wrapped by the @ref WGPUSurface
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var surface: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWaylandSurface
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWaylandSurface
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWaylandSurface) -> Unit): ArrayHolder<WGPUSurfaceSourceWaylandSurface>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping a Windows [`HWND`](https://learn.microsoft.com/en-us/windows/apps/develop/ui-input/retrieve-hwnd).
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_WINDOWS_HWND_INIT as initializer.
 */
expect interface WGPUSurfaceSourceWindowsHWND {
    var chain: WGPUChainedStruct
    /**
     * The [`HINSTANCE`](https://learn.microsoft.com/en-us/windows/win32/learnwin32/winmain--the-application-entry-point) for this application.
     * Most commonly `GetModuleHandle(nullptr)`.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var hinstance: NativeAddress?
    /**
     * The [`HWND`](https://learn.microsoft.com/en-us/windows/apps/develop/ui-input/retrieve-hwnd) that will be wrapped by the @ref WGPUSurface.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var hwnd: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceWindowsHWND
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceWindowsHWND
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceWindowsHWND) -> Unit): ArrayHolder<WGPUSurfaceSourceWindowsHWND>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping an [XCB](https://xcb.freedesktop.org/) `xcb_window_t`.
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_XCB_WINDOW_INIT as initializer.
 */
expect interface WGPUSurfaceSourceXCBWindow {
    var chain: WGPUChainedStruct
    /**
     * The `xcb_connection_t` for the connection to the X server.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var connection: NativeAddress?
    /**
     * The `xcb_window_t` for the window that will be wrapped by the @ref WGPUSurface.
     *
     * The `INIT` macro sets this to `0`.
     */
    var window: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXCBWindow
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXCBWindow
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXCBWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXCBWindow>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make an @ref WGPUSurface wrapping an [Xlib](https://www.x.org/releases/current/doc/libX11/libX11/libX11.html) `Window`.
 *
 * Default values can be set using @ref WGPU_SURFACE_SOURCE_XLIB_WINDOW_INIT as initializer.
 */
expect interface WGPUSurfaceSourceXlibWindow {
    var chain: WGPUChainedStruct
    /**
     * A pointer to the [`Display`](https://www.x.org/releases/current/doc/libX11/libX11/libX11.html#Opening_the_Display) connected to the X server.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var display: NativeAddress?
    /**
     * The [`Window`](https://www.x.org/releases/current/doc/libX11/libX11/libX11.html#Creating_Windows) that will be wrapped by the @ref WGPUSurface.
     *
     * The `INIT` macro sets this to `0`.
     */
    var window: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceXlibWindow
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceXlibWindow
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceXlibWindow) -> Unit): ArrayHolder<WGPUSurfaceSourceXlibWindow>
    }
}

/**
 * Queried each frame from a @ref WGPUSurface to get a @ref WGPUTexture to render to along with some metadata.
 * See @ref Surface-Presenting for more details.
 *
 * Default values can be set using @ref WGPU_SURFACE_TEXTURE_INIT as initializer.
 */
expect interface WGPUSurfaceTexture {
    var nextInChain: WGPUChainedStruct?
    /**
     * The @ref WGPUTexture representing the frame that will be shown on the surface.
     * It is @ref ReturnedWithOwnership from @ref wgpuSurfaceGetCurrentTexture.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var texture: WGPUTexture?
    /**
     * Whether the call to @ref wgpuSurfaceGetCurrentTexture succeeded and a hint as to why it might not have.
     *
     * The `INIT` macro sets this to (@ref WGPUSurfaceGetCurrentTextureStatus)0.
     */
    var status: WGPUSurfaceGetCurrentTextureStatus
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceTexture
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceTexture
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceTexture) -> Unit): ArrayHolder<WGPUSurfaceTexture>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXEL_COPY_BUFFER_LAYOUT_INIT as initializer.
 */
expect interface WGPUTexelCopyBufferLayout {
    /**
     * The `INIT` macro sets this to `0`.
     */
    var offset: ULong
    /**
     * The `INIT` macro sets this to @ref WGPU_COPY_STRIDE_UNDEFINED.
     */
    var bytesPerRow: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_COPY_STRIDE_UNDEFINED.
     */
    var rowsPerImage: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferLayout
        fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferLayout) -> Unit): ArrayHolder<WGPUTexelCopyBufferLayout>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXTURE_BINDING_LAYOUT_INIT as initializer.
 */
expect interface WGPUTextureBindingLayout {
    var nextInChain: WGPUChainedStruct?
    /**
     * If set to @ref WGPUTextureSampleType_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureSampleType_Float.
     *
     * The `INIT` macro sets this to @ref WGPUTextureSampleType_Undefined.
     */
    var sampleType: WGPUTextureSampleType
    /**
     * If set to @ref WGPUTextureViewDimension_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureViewDimension_2D.
     *
     * The `INIT` macro sets this to @ref WGPUTextureViewDimension_Undefined.
     */
    var viewDimension: WGPUTextureViewDimension
    /**
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var multisampled: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureBindingLayout
        fun allocate(allocator: MemoryAllocator): WGPUTextureBindingLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingLayout) -> Unit): ArrayHolder<WGPUTextureBindingLayout>
    }
}

/**
 * Note: While Compatibility Mode is optional to implement, this extension struct
 * is required to be accepted (but per the WebGPU spec, its contents are ignored
 * on devices that have the @ref WGPUFeatureName_CoreFeaturesAndLimits feature).
 *
 * Default values can be set using @ref WGPU_TEXTURE_BINDING_VIEW_DIMENSION_INIT as initializer.
 */
expect interface WGPUTextureBindingViewDimension {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to @ref WGPUTextureViewDimension_Undefined.
     */
    var textureBindingViewDimension: WGPUTextureViewDimension
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureBindingViewDimension
        fun allocate(allocator: MemoryAllocator): WGPUTextureBindingViewDimension
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureBindingViewDimension) -> Unit): ArrayHolder<WGPUTextureBindingViewDimension>
    }
}

/**
 * When accessed by a shader, the red/green/blue/alpha channels are replaced
 * by the value corresponding to the component specified in r, g, b, and a,
 * respectively unlike the JS API which uses a string of length four, with
 * each character mapping to the texture view's red/green/blue/alpha channels.
 *
 * Default values can be set using @ref WGPU_TEXTURE_COMPONENT_SWIZZLE_INIT as initializer.
 */
expect interface WGPUTextureComponentSwizzle {
    /**
     * The value that replaces the red channel in the shader.
     *
     * If set to @ref WGPUComponentSwizzle_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUComponentSwizzle_R.
     *
     * The `INIT` macro sets this to @ref WGPUComponentSwizzle_Undefined.
     */
    var r: WGPUComponentSwizzle
    /**
     * The value that replaces the green channel in the shader.
     *
     * If set to @ref WGPUComponentSwizzle_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUComponentSwizzle_G.
     *
     * The `INIT` macro sets this to @ref WGPUComponentSwizzle_Undefined.
     */
    var g: WGPUComponentSwizzle
    /**
     * The value that replaces the blue channel in the shader.
     *
     * If set to @ref WGPUComponentSwizzle_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUComponentSwizzle_B.
     *
     * The `INIT` macro sets this to @ref WGPUComponentSwizzle_Undefined.
     */
    var b: WGPUComponentSwizzle
    /**
     * The value that replaces the alpha channel in the shader.
     *
     * If set to @ref WGPUComponentSwizzle_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUComponentSwizzle_A.
     *
     * The `INIT` macro sets this to @ref WGPUComponentSwizzle_Undefined.
     */
    var a: WGPUComponentSwizzle
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzle
        fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzle
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzle) -> Unit): ArrayHolder<WGPUTextureComponentSwizzle>
    }
}

/**
 * Default values can be set using @ref WGPU_VERTEX_ATTRIBUTE_INIT as initializer.
 */
expect interface WGPUVertexAttribute {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to (@ref WGPUVertexFormat)0.
     */
    var format: WGPUVertexFormat
    /**
     * The `INIT` macro sets this to `0`.
     */
    var offset: ULong
    /**
     * The `INIT` macro sets this to `0`.
     */
    var shaderLocation: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUVertexAttribute
        fun allocate(allocator: MemoryAllocator): WGPUVertexAttribute
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexAttribute) -> Unit): ArrayHolder<WGPUVertexAttribute>
    }
}

/**
 * Default values can be set using @ref WGPU_BIND_GROUP_ENTRY_INIT as initializer.
 */
expect interface WGPUBindGroupEntry {
    var nextInChain: WGPUChainedStruct?
    /**
     * Binding index in the bind group.
     *
     * The `INIT` macro sets this to `0`.
     */
    var binding: UInt
    /**
     * Set this if the binding is a buffer object.
     * Otherwise must be null.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var buffer: WGPUBuffer?
    /**
     * If the binding is a buffer, this is the byte offset of the binding range.
     * Otherwise ignored.
     *
     * The `INIT` macro sets this to `0`.
     */
    var offset: ULong
    /**
     * If the binding is a buffer, this is the byte size of the binding range
     * (@ref WGPU_WHOLE_SIZE means the binding ends at the end of the buffer).
     * Otherwise ignored.
     *
     * The `INIT` macro sets this to @ref WGPU_WHOLE_SIZE.
     */
    var size: ULong
    /**
     * Set this if the binding is a sampler object.
     * Otherwise must be null.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var sampler: WGPUSampler?
    /**
     * Set this if the binding is a texture view object.
     * Otherwise must be null.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var textureView: WGPUTextureView?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupEntry
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntry
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntry) -> Unit): ArrayHolder<WGPUBindGroupEntry>
    }
}

/**
 * Default values can be set using @ref WGPU_BIND_GROUP_LAYOUT_ENTRY_INIT as initializer.
 */
expect interface WGPUBindGroupLayoutEntry {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `0`.
     */
    var binding: UInt
    /**
     * The `INIT` macro sets this to @ref WGPUShaderStage_None.
     */
    var visibility: ULong
    /**
     * If non-zero, this entry defines a binding array with this size.
     *
     * The `INIT` macro sets this to `0`.
     */
    var bindingArraySize: UInt
    /**
     * The `INIT` macro sets this to zero (which sets the entry to `BindingNotUsed`).
     */
    var buffer: WGPUBufferBindingLayout
    /**
     * The `INIT` macro sets this to zero (which sets the entry to `BindingNotUsed`).
     */
    var sampler: WGPUSamplerBindingLayout
    /**
     * The `INIT` macro sets this to zero (which sets the entry to `BindingNotUsed`).
     */
    var texture: WGPUTextureBindingLayout
    /**
     * The `INIT` macro sets this to zero (which sets the entry to `BindingNotUsed`).
     */
    var storageTexture: WGPUStorageTextureBindingLayout
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntry
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntry
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntry) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntry>
    }
}

/**
 * Default values can be set using @ref WGPU_BLEND_STATE_INIT as initializer.
 */
expect interface WGPUBlendState {
    /**
     * The `INIT` macro sets this to @ref WGPU_BLEND_COMPONENT_INIT.
     */
    var color: WGPUBlendComponent
    /**
     * The `INIT` macro sets this to @ref WGPU_BLEND_COMPONENT_INIT.
     */
    var alpha: WGPUBlendComponent
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBlendState
        fun allocate(allocator: MemoryAllocator): WGPUBlendState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBlendState) -> Unit): ArrayHolder<WGPUBlendState>
    }
}

/**
 * This is an @ref ImplementationAllocatedStructChain root.
 * Arbitrary chains must be handled gracefully by the application!
 *
 * Default values can be set using @ref WGPU_COMPILATION_INFO_INIT as initializer.
 */
expect interface WGPUCompilationInfo {
    var nextInChain: WGPUChainedStruct?
    /**
     * Array count for `messages`. The `INIT` macro sets this to 0.
     */
    var messageCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var messages: WGPUCompilationMessage?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUCompilationInfo
        fun allocate(allocator: MemoryAllocator): WGPUCompilationInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUCompilationInfo) -> Unit): ArrayHolder<WGPUCompilationInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_COMPUTE_PASS_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUComputePassDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var timestampWrites: WGPUPassTimestampWrites?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUComputePassDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUComputePassDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePassDescriptor) -> Unit): ArrayHolder<WGPUComputePassDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_COMPUTE_STATE_INIT as initializer.
 */
expect interface WGPUComputeState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var module: WGPUShaderModule?
    /**
     * This is a \ref NullableInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var entryPoint: WGPUStringView
    /**
     * Array count for `constants`. The `INIT` macro sets this to 0.
     */
    var constantCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var constants: WGPUConstantEntry?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUComputeState
        fun allocate(allocator: MemoryAllocator): WGPUComputeState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputeState) -> Unit): ArrayHolder<WGPUComputeState>
    }
}

/**
 * Default values can be set using @ref WGPU_DEPTH_STENCIL_STATE_INIT as initializer.
 */
expect interface WGPUDepthStencilState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * The `INIT` macro sets this to @ref WGPUOptionalBool_Undefined.
     */
    var depthWriteEnabled: WGPUOptionalBool
    /**
     * The `INIT` macro sets this to @ref WGPUCompareFunction_Undefined.
     */
    var depthCompare: WGPUCompareFunction
    /**
     * The `INIT` macro sets this to @ref WGPU_STENCIL_FACE_STATE_INIT.
     */
    var stencilFront: WGPUStencilFaceState
    /**
     * The `INIT` macro sets this to @ref WGPU_STENCIL_FACE_STATE_INIT.
     */
    var stencilBack: WGPUStencilFaceState
    /**
     * The `INIT` macro sets this to `0xFFFFFFFF`.
     */
    var stencilReadMask: UInt
    /**
     * The `INIT` macro sets this to `0xFFFFFFFF`.
     */
    var stencilWriteMask: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var depthBias: Int
    /**
     * TODO
     *
     * If non-finite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to `0.f`.
     */
    var depthBiasSlopeScale: Float
    /**
     * TODO
     *
     * If non-finite, produces a @ref NonFiniteFloatValueError.
     *
     * The `INIT` macro sets this to `0.f`.
     */
    var depthBiasClamp: Float
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUDepthStencilState
        fun allocate(allocator: MemoryAllocator): WGPUDepthStencilState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDepthStencilState) -> Unit): ArrayHolder<WGPUDepthStencilState>
    }
}

/**
 * Struct holding a future to wait on, and a `completed` boolean flag.
 *
 * Default values can be set using @ref WGPU_FUTURE_WAIT_INFO_INIT as initializer.
 */
expect interface WGPUFutureWaitInfo {
    /**
     * The future to wait on.
     *
     * The `INIT` macro sets this to @ref WGPU_FUTURE_INIT.
     */
    var future: WGPUFuture
    /**
     * Whether or not the future completed.
     *
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var completed: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUFutureWaitInfo
        fun allocate(allocator: MemoryAllocator): WGPUFutureWaitInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFutureWaitInfo) -> Unit): ArrayHolder<WGPUFutureWaitInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_INSTANCE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUInstanceDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * Array count for `requiredFeatures`. The `INIT` macro sets this to 0.
     */
    var requiredFeatureCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var requiredFeatures: NativeAddress?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var requiredLimits: WGPUInstanceLimits?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUInstanceDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUInstanceDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceDescriptor) -> Unit): ArrayHolder<WGPUInstanceDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_LIMITS_INIT as initializer.
 */
expect interface WGPULimits {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxTextureDimension1D: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxTextureDimension2D: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxTextureDimension3D: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxTextureArrayLayers: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxBindGroups: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxBindGroupsPlusVertexBuffers: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxBindingsPerBindGroup: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxDynamicUniformBuffersPerPipelineLayout: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxDynamicStorageBuffersPerPipelineLayout: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxSampledTexturesPerShaderStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxSamplersPerShaderStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageBuffersPerShaderStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxStorageTexturesPerShaderStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxUniformBuffersPerShaderStage: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U64_UNDEFINED.
     */
    var maxUniformBufferBindingSize: ULong
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U64_UNDEFINED.
     */
    var maxStorageBufferBindingSize: ULong
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var minUniformBufferOffsetAlignment: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var minStorageBufferOffsetAlignment: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxVertexBuffers: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U64_UNDEFINED.
     */
    var maxBufferSize: ULong
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxVertexAttributes: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxVertexBufferArrayStride: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxInterStageShaderVariables: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxColorAttachments: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxColorAttachmentBytesPerSample: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeWorkgroupStorageSize: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeInvocationsPerWorkgroup: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeWorkgroupSizeX: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeWorkgroupSizeY: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeWorkgroupSizeZ: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxComputeWorkgroupsPerDimension: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_LIMIT_U32_UNDEFINED.
     */
    var maxImmediateSize: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPULimits
        fun allocate(allocator: MemoryAllocator): WGPULimits
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPULimits) -> Unit): ArrayHolder<WGPULimits>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_PASS_COLOR_ATTACHMENT_INIT as initializer.
 */
expect interface WGPURenderPassColorAttachment {
    var nextInChain: WGPUChainedStruct?
    /**
     * If `NULL`, indicates a hole in the parent
     * @ref WGPURenderPassDescriptor::colorAttachments array.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var view: WGPUTextureView?
    /**
     * The `INIT` macro sets this to @ref WGPU_DEPTH_SLICE_UNDEFINED.
     */
    var depthSlice: UInt
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var resolveTarget: WGPUTextureView?
    /**
     * The `INIT` macro sets this to @ref WGPULoadOp_Undefined.
     */
    var loadOp: WGPULoadOp
    /**
     * The `INIT` macro sets this to @ref WGPUStoreOp_Undefined.
     */
    var storeOp: WGPUStoreOp
    /**
     * The `INIT` macro sets this to @ref WGPU_COLOR_INIT.
     */
    var clearValue: WGPUColor
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderPassColorAttachment
        fun allocate(allocator: MemoryAllocator): WGPURenderPassColorAttachment
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassColorAttachment) -> Unit): ArrayHolder<WGPURenderPassColorAttachment>
    }
}

/**
 * Default values can be set using @ref WGPU_REQUEST_ADAPTER_OPTIONS_INIT as initializer.
 */
expect interface WGPURequestAdapterOptions {
    var nextInChain: WGPUChainedStruct?
    /**
     * "Feature level" for the adapter request. If an adapter is returned, it must support the features and limits in the requested feature level.
     *
     * If set to @ref WGPUFeatureLevel_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUFeatureLevel_Core.
     * Additionally, implementations may ignore @ref WGPUFeatureLevel_Compatibility
     * and provide @ref WGPUFeatureLevel_Core instead.
     *
     * The `INIT` macro sets this to @ref WGPUFeatureLevel_Undefined.
     */
    var featureLevel: WGPUFeatureLevel
    /**
     * The `INIT` macro sets this to @ref WGPUPowerPreference_Undefined.
     */
    var powerPreference: WGPUPowerPreference
    /**
     * If true, requires the adapter to be a "fallback" adapter as defined by the JS spec.
     * If this is not possible, the request returns null.
     *
     * The `INIT` macro sets this to `WGPU_FALSE`.
     */
    var forceFallbackAdapter: UInt
    /**
     * If set, requires the adapter to have a particular backend type.
     * If this is not possible, the request returns null.
     *
     * The `INIT` macro sets this to @ref WGPUBackendType_Undefined.
     */
    var backendType: WGPUBackendType
    /**
     * If set, requires the adapter to be able to output to a particular surface.
     * If this is not possible, the request returns null.
     *
     * The `INIT` macro sets this to `NULL`.
     */
    var compatibleSurface: WGPUSurface?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURequestAdapterOptions
        fun allocate(allocator: MemoryAllocator): WGPURequestAdapterOptions
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURequestAdapterOptions) -> Unit): ArrayHolder<WGPURequestAdapterOptions>
    }
}

/**
 * Default values can be set using @ref WGPU_SHADER_MODULE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUShaderModuleDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptor) -> Unit): ArrayHolder<WGPUShaderModuleDescriptor>
    }
}

/**
 * The root descriptor for the creation of an @ref WGPUSurface with @ref wgpuInstanceCreateSurface.
 * It isn't sufficient by itself and must have one of the `WGPUSurfaceSource*` in its chain.
 * See @ref Surface-Creation for more details.
 *
 * Default values can be set using @ref WGPU_SURFACE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUSurfaceDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * Label used to refer to the object.
     *
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceDescriptor) -> Unit): ArrayHolder<WGPUSurfaceDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXEL_COPY_BUFFER_INFO_INIT as initializer.
 */
expect interface WGPUTexelCopyBufferInfo {
    /**
     * The `INIT` macro sets this to @ref WGPU_TEXEL_COPY_BUFFER_LAYOUT_INIT.
     */
    var layout: WGPUTexelCopyBufferLayout
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var buffer: WGPUBuffer?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTexelCopyBufferInfo
        fun allocate(allocator: MemoryAllocator): WGPUTexelCopyBufferInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyBufferInfo) -> Unit): ArrayHolder<WGPUTexelCopyBufferInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXEL_COPY_TEXTURE_INFO_INIT as initializer.
 */
expect interface WGPUTexelCopyTextureInfo {
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var texture: WGPUTexture?
    /**
     * The `INIT` macro sets this to `0`.
     */
    var mipLevel: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_ORIGIN_3D_INIT.
     */
    var origin: WGPUOrigin3D
    /**
     * If set to @ref WGPUTextureAspect_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureAspect_All.
     *
     * The `INIT` macro sets this to @ref WGPUTextureAspect_Undefined.
     */
    var aspect: WGPUTextureAspect
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTexelCopyTextureInfo
        fun allocate(allocator: MemoryAllocator): WGPUTexelCopyTextureInfo
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTexelCopyTextureInfo) -> Unit): ArrayHolder<WGPUTexelCopyTextureInfo>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXTURE_COMPONENT_SWIZZLE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUTextureComponentSwizzleDescriptor {
    var chain: WGPUChainedStruct
    /**
     * The `INIT` macro sets this to @ref WGPU_TEXTURE_COMPONENT_SWIZZLE_INIT.
     */
    var swizzle: WGPUTextureComponentSwizzle
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureComponentSwizzleDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUTextureComponentSwizzleDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureComponentSwizzleDescriptor) -> Unit): ArrayHolder<WGPUTextureComponentSwizzleDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXTURE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUTextureDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to @ref WGPUTextureUsage_None.
     */
    var usage: ULong
    /**
     * If set to @ref WGPUTextureDimension_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureDimension_2D.
     *
     * The `INIT` macro sets this to @ref WGPUTextureDimension_Undefined.
     */
    var dimension: WGPUTextureDimension
    /**
     * The `INIT` macro sets this to @ref WGPU_EXTENT_3D_INIT.
     */
    var size: WGPUExtent3D
    /**
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * The `INIT` macro sets this to `1`.
     */
    var mipLevelCount: UInt
    /**
     * The `INIT` macro sets this to `1`.
     */
    var sampleCount: UInt
    /**
     * Array count for `viewFormats`. The `INIT` macro sets this to 0.
     */
    var viewFormatCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var viewFormats: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUTextureDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureDescriptor) -> Unit): ArrayHolder<WGPUTextureDescriptor>
    }
}

/**
 * If `attributes` is empty *and* `stepMode` is @ref WGPUVertexStepMode_Undefined,
 * indicates a "hole" in the parent @ref WGPUVertexState `buffers` array,
 * with behavior equivalent to `null` in the JS API.
 *
 * If `attributes` is empty but `stepMode` is *not* @ref WGPUVertexStepMode_Undefined,
 * indicates a vertex buffer with no attributes, with behavior equivalent to
 * `{ attributes: [] }` in the JS API. (TODO: If the JS API changes not to
 * distinguish these cases, then this distinction doesn't matter and we can
 * remove this documentation.)
 *
 * If `stepMode` is @ref WGPUVertexStepMode_Undefined but `attributes` is *not* empty,
 * `stepMode` [defaults](@ref SentinelValues) to @ref WGPUVertexStepMode_Vertex.
 *
 * Default values can be set using @ref WGPU_VERTEX_BUFFER_LAYOUT_INIT as initializer.
 */
expect interface WGPUVertexBufferLayout {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to @ref WGPUVertexStepMode_Undefined.
     */
    var stepMode: WGPUVertexStepMode
    /**
     * The `INIT` macro sets this to `0`.
     */
    var arrayStride: ULong
    /**
     * Array count for `attributes`. The `INIT` macro sets this to 0.
     */
    var attributeCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var attributes: WGPUVertexAttribute?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUVertexBufferLayout
        fun allocate(allocator: MemoryAllocator): WGPUVertexBufferLayout
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexBufferLayout) -> Unit): ArrayHolder<WGPUVertexBufferLayout>
    }
}

/**
 * Default values can be set using @ref WGPU_BIND_GROUP_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUBindGroupDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var layout: WGPUBindGroupLayout?
    /**
     * Array count for `entries`. The `INIT` macro sets this to 0.
     */
    var entryCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var entries: WGPUBindGroupEntry?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupDescriptor) -> Unit): ArrayHolder<WGPUBindGroupDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_BIND_GROUP_LAYOUT_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUBindGroupLayoutDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * Array count for `entries`. The `INIT` macro sets this to 0.
     */
    var entryCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var entries: WGPUBindGroupLayoutEntry?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutDescriptor) -> Unit): ArrayHolder<WGPUBindGroupLayoutDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_COLOR_TARGET_STATE_INIT as initializer.
 */
expect interface WGPUColorTargetState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The texture format of the target. If @ref WGPUTextureFormat_Undefined,
     * indicates a "hole" in the parent @ref WGPUFragmentState `targets` array:
     * the pipeline does not output a value at this `location`.
     *
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var blend: WGPUBlendState?
    /**
     * The `INIT` macro sets this to @ref WGPUColorWriteMask_All.
     */
    var writeMask: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUColorTargetState
        fun allocate(allocator: MemoryAllocator): WGPUColorTargetState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUColorTargetState) -> Unit): ArrayHolder<WGPUColorTargetState>
    }
}

/**
 * Default values can be set using @ref WGPU_COMPUTE_PIPELINE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUComputePipelineDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var layout: WGPUPipelineLayout?
    /**
     * The `INIT` macro sets this to @ref WGPU_COMPUTE_STATE_INIT.
     */
    var compute: WGPUComputeState
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUComputePipelineDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUComputePipelineDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUComputePipelineDescriptor) -> Unit): ArrayHolder<WGPUComputePipelineDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_DEVICE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUDeviceDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * Array count for `requiredFeatures`. The `INIT` macro sets this to 0.
     */
    var requiredFeatureCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var requiredFeatures: NativeAddress?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var requiredLimits: WGPULimits?
    /**
     * The `INIT` macro sets this to @ref WGPU_QUEUE_DESCRIPTOR_INIT.
     */
    var defaultQueue: WGPUQueueDescriptor
    /**
     * The `INIT` macro sets this to @ref WGPU_DEVICE_LOST_CALLBACK_INFO_INIT.
     */
    var deviceLostCallbackInfo: WGPUDeviceLostCallbackInfo
    /**
     * Called when there is an uncaptured error on this device, from any thread.
     * See @ref ErrorScopes.
     *
     * **Important:** This callback does not have a configurable @ref WGPUCallbackMode; it may be called at any time (like @ref WGPUCallbackMode_AllowSpontaneous). As such, calls into the `webgpu.h` API from this callback are unsafe. See @ref CallbackReentrancy.
     *
     * The `INIT` macro sets this to @ref WGPU_UNCAPTURED_ERROR_CALLBACK_INFO_INIT.
     */
    var uncapturedErrorCallbackInfo: WGPUUncapturedErrorCallbackInfo
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUDeviceDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUDeviceDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceDescriptor) -> Unit): ArrayHolder<WGPUDeviceDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_PASS_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPURenderPassDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * Array count for `colorAttachments`. The `INIT` macro sets this to 0.
     */
    var colorAttachmentCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var colorAttachments: WGPURenderPassColorAttachment?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var depthStencilAttachment: WGPURenderPassDepthStencilAttachment?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var occlusionQuerySet: WGPUQuerySet?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var timestampWrites: WGPUPassTimestampWrites?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderPassDescriptor
        fun allocate(allocator: MemoryAllocator): WGPURenderPassDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPassDescriptor) -> Unit): ArrayHolder<WGPURenderPassDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_TEXTURE_VIEW_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPUTextureViewDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to @ref WGPUTextureFormat_Undefined.
     */
    var format: WGPUTextureFormat
    /**
     * The `INIT` macro sets this to @ref WGPUTextureViewDimension_Undefined.
     */
    var dimension: WGPUTextureViewDimension
    /**
     * The `INIT` macro sets this to `0`.
     */
    var baseMipLevel: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_MIP_LEVEL_COUNT_UNDEFINED.
     */
    var mipLevelCount: UInt
    /**
     * The `INIT` macro sets this to `0`.
     */
    var baseArrayLayer: UInt
    /**
     * The `INIT` macro sets this to @ref WGPU_ARRAY_LAYER_COUNT_UNDEFINED.
     */
    var arrayLayerCount: UInt
    /**
     * If set to @ref WGPUTextureAspect_Undefined,
     * [defaults](@ref SentinelValues) to @ref WGPUTextureAspect_All.
     *
     * The `INIT` macro sets this to @ref WGPUTextureAspect_Undefined.
     */
    var aspect: WGPUTextureAspect
    /**
     * The `INIT` macro sets this to @ref WGPUTextureUsage_None.
     */
    var usage: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUTextureViewDescriptor
        fun allocate(allocator: MemoryAllocator): WGPUTextureViewDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUTextureViewDescriptor) -> Unit): ArrayHolder<WGPUTextureViewDescriptor>
    }
}

/**
 * Default values can be set using @ref WGPU_VERTEX_STATE_INIT as initializer.
 */
expect interface WGPUVertexState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var module: WGPUShaderModule?
    /**
     * This is a \ref NullableInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var entryPoint: WGPUStringView
    /**
     * Array count for `constants`. The `INIT` macro sets this to 0.
     */
    var constantCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var constants: WGPUConstantEntry?
    /**
     * Array count for `buffers`. The `INIT` macro sets this to 0.
     */
    var bufferCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var buffers: WGPUVertexBufferLayout?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUVertexState
        fun allocate(allocator: MemoryAllocator): WGPUVertexState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUVertexState) -> Unit): ArrayHolder<WGPUVertexState>
    }
}

/**
 * Default values can be set using @ref WGPU_FRAGMENT_STATE_INIT as initializer.
 */
expect interface WGPUFragmentState {
    var nextInChain: WGPUChainedStruct?
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var module: WGPUShaderModule?
    /**
     * This is a \ref NullableInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var entryPoint: WGPUStringView
    /**
     * Array count for `constants`. The `INIT` macro sets this to 0.
     */
    var constantCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var constants: WGPUConstantEntry?
    /**
     * Array count for `targets`. The `INIT` macro sets this to 0.
     */
    var targetCount: ULong
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var targets: WGPUColorTargetState?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUFragmentState
        fun allocate(allocator: MemoryAllocator): WGPUFragmentState
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUFragmentState) -> Unit): ArrayHolder<WGPUFragmentState>
    }
}

/**
 * Default values can be set using @ref WGPU_RENDER_PIPELINE_DESCRIPTOR_INIT as initializer.
 */
expect interface WGPURenderPipelineDescriptor {
    var nextInChain: WGPUChainedStruct?
    /**
     * This is a \ref NonNullInputString.
     *
     * The `INIT` macro sets this to @ref WGPU_STRING_VIEW_INIT.
     */
    var label: WGPUStringView
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var layout: WGPUPipelineLayout?
    /**
     * The `INIT` macro sets this to @ref WGPU_VERTEX_STATE_INIT.
     */
    var vertex: WGPUVertexState
    /**
     * The `INIT` macro sets this to @ref WGPU_PRIMITIVE_STATE_INIT.
     */
    var primitive: WGPUPrimitiveState
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var depthStencil: WGPUDepthStencilState?
    /**
     * The `INIT` macro sets this to @ref WGPU_MULTISAMPLE_STATE_INIT.
     */
    var multisample: WGPUMultisampleState
    /**
     * The `INIT` macro sets this to `NULL`.
     */
    var fragment: WGPUFragmentState?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURenderPipelineDescriptor
        fun allocate(allocator: MemoryAllocator): WGPURenderPipelineDescriptor
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURenderPipelineDescriptor) -> Unit): ArrayHolder<WGPURenderPipelineDescriptor>
    }
}

/**
 * \defgroup GlobalFunctions Global Functions
 * \brief Functions that are not specific to an object.
 *
 * @{
 * /
 * / **
 * Create a WGPUInstance
 *
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuCreateInstance(descriptor: WGPUInstanceDescriptor?): WGPUInstance?

/**
 * Get the list of @ref WGPUInstanceFeatureName values supported by the instance.
 *
 * @param features
 * This parameter is @ref ReturnedWithOwnership.
 */
expect fun wgpuGetInstanceFeatures(features: WGPUSupportedInstanceFeatures?): Unit

/**
 * Get the limits supported by the instance.
 *
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuGetInstanceLimits(limits: WGPUInstanceLimits?): WGPUStatus

/**
 * Check whether a particular @ref WGPUInstanceFeatureName is supported by the instance.
 */
expect fun wgpuHasInstanceFeature(feature: WGPUInstanceFeatureName): UInt

/**
 * Returns the "procedure address" (function pointer) of the named function.
 * The result must be cast to the appropriate proc pointer type.
 */
expect fun wgpuGetProcAddress(procName: WGPUStringView): NativeAddress?

/**
 * \defgroup WGPUAdapterMethods WGPUAdapter methods
 * \brief Functions whose first argument has type WGPUAdapter.
 *
 * @{
 * /
 * / **
 * Get the list of @ref WGPUFeatureName values supported by the adapter.
 *
 * @param features
 * This parameter is @ref ReturnedWithOwnership.
 */
expect fun wgpuAdapterGetFeatures(adapter: WGPUAdapter?, features: WGPUSupportedFeatures?): Unit

/**
 * @param info
 * This parameter is @ref ReturnedWithOwnership.
 *
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuAdapterGetInfo(adapter: WGPUAdapter?, info: WGPUAdapterInfo?): WGPUStatus

/**
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuAdapterGetLimits(adapter: WGPUAdapter?, limits: WGPULimits?): WGPUStatus

expect fun wgpuAdapterHasFeature(adapter: WGPUAdapter?, feature: WGPUFeatureName): UInt

expect fun wgpuAdapterRequestDevice(adapter: WGPUAdapter?, descriptor: WGPUDeviceDescriptor?, callbackInfo: WGPURequestDeviceCallbackInfo): WGPUFuture

expect fun wgpuAdapterAddRef(adapter: WGPUAdapter?): Unit

expect fun wgpuAdapterRelease(adapter: WGPUAdapter?): Unit

/**
 * \defgroup WGPUAdapterInfoMethods WGPUAdapterInfo methods
 * \brief Functions whose first argument has type WGPUAdapterInfo.
 *
 * @{
 * /
 * / **
 * Frees members which were allocated by the API.
 */
expect fun wgpuAdapterInfoFreeMembers(adapterInfo: WGPUAdapterInfo): Unit

/**
 * \defgroup WGPUBindGroupMethods WGPUBindGroup methods
 * \brief Functions whose first argument has type WGPUBindGroup.
 *
 * @{
 */
expect fun wgpuBindGroupSetLabel(bindGroup: WGPUBindGroup?, label: WGPUStringView): Unit

expect fun wgpuBindGroupAddRef(bindGroup: WGPUBindGroup?): Unit

expect fun wgpuBindGroupRelease(bindGroup: WGPUBindGroup?): Unit

/**
 * \defgroup WGPUBindGroupLayoutMethods WGPUBindGroupLayout methods
 * \brief Functions whose first argument has type WGPUBindGroupLayout.
 *
 * @{
 */
expect fun wgpuBindGroupLayoutSetLabel(bindGroupLayout: WGPUBindGroupLayout?, label: WGPUStringView): Unit

expect fun wgpuBindGroupLayoutAddRef(bindGroupLayout: WGPUBindGroupLayout?): Unit

expect fun wgpuBindGroupLayoutRelease(bindGroupLayout: WGPUBindGroupLayout?): Unit

/**
 * \defgroup WGPUBufferMethods WGPUBuffer methods
 * \brief Functions whose first argument has type WGPUBuffer.
 *
 * @{
 */
expect fun wgpuBufferDestroy(buffer: WGPUBuffer?): Unit

/**
 * Returns a const pointer to beginning of the mapped range.
 * It must not be written; writing to this range causes undefined behavior.
 * See @ref MappedRangeBehavior for error conditions and guarantees.
 * This function is safe to call inside spontaneous callbacks (see @ref CallbackReentrancy).
 *
 * In Wasm, if `memcpy`ing from this range, prefer using @ref wgpuBufferReadMappedRange
 * instead for better performance.
 *
 * @param offset
 * Byte offset relative to the beginning of the buffer.
 *
 * @param size
 * Byte size of the range to get.
 * If this is @ref WGPU_WHOLE_MAP_SIZE, it defaults to `buffer.size - offset`.
 * The returned pointer is valid for exactly this many bytes.
 */
expect fun wgpuBufferGetConstMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress?

/**
 * Returns a mutable pointer to beginning of the mapped range.
 * See @ref MappedRangeBehavior for error conditions and guarantees.
 * This function is safe to call inside spontaneous callbacks (see @ref CallbackReentrancy).
 *
 * In Wasm, if `memcpy`ing into this range, prefer using @ref wgpuBufferWriteMappedRange
 * instead for better performance.
 *
 * @param offset
 * Byte offset relative to the beginning of the buffer.
 *
 * @param size
 * Byte size of the range to get.
 * If this is @ref WGPU_WHOLE_MAP_SIZE, it defaults to `buffer.size - offset`.
 * The returned pointer is valid for exactly this many bytes.
 */
expect fun wgpuBufferGetMappedRange(buffer: WGPUBuffer?, offset: ULong, size: ULong): NativeAddress?

expect fun wgpuBufferGetMapState(buffer: WGPUBuffer?): WGPUBufferMapState

expect fun wgpuBufferGetSize(buffer: WGPUBuffer?): ULong

expect fun wgpuBufferGetUsage(buffer: WGPUBuffer?): ULong

/**
 * @param mode
 * The mapping mode (read or write).
 *
 * @param offset
 * Byte offset relative to beginning of the buffer.
 *
 * @param size
 * Byte size of the region to map.
 * If this is @ref WGPU_WHOLE_MAP_SIZE, it defaults to `buffer.size - offset`.
 */
expect fun wgpuBufferMapAsync(buffer: WGPUBuffer?, mode: ULong, offset: ULong, size: ULong, callbackInfo: WGPUBufferMapCallbackInfo): WGPUFuture

/**
 * Copies a range of data from the buffer mapping into the provided destination pointer.
 * See @ref MappedRangeBehavior for error conditions and guarantees.
 * This function is safe to call inside spontaneous callbacks (see @ref CallbackReentrancy).
 *
 * In Wasm, this is more efficient than copying from a mapped range into a `malloc`'d range.
 *
 * @param offset
 * Byte offset relative to the beginning of the buffer.
 *
 * @param data
 * Destination, to read buffer data into.
 *
 * @param size
 * Number of bytes of data to read from the buffer.
 * (Note @ref WGPU_WHOLE_MAP_SIZE is *not* accepted here.)
 *
 * @returns
 * @ref WGPUStatus_Error if the copy did not occur.
 */
expect fun wgpuBufferReadMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus

expect fun wgpuBufferSetLabel(buffer: WGPUBuffer?, label: WGPUStringView): Unit

expect fun wgpuBufferUnmap(buffer: WGPUBuffer?): Unit

/**
 * Copies a range of data from the provided source pointer into the buffer mapping.
 * See @ref MappedRangeBehavior for error conditions and guarantees.
 * This function is safe to call inside spontaneous callbacks (see @ref CallbackReentrancy).
 *
 * In Wasm, this is more efficient than copying from a `malloc`'d range into a mapped range.
 *
 * @param offset
 * Byte offset relative to the beginning of the buffer.
 *
 * @param data
 * Source, to write buffer data from.
 *
 * @param size
 * Number of bytes of data to write to the buffer.
 * (Note @ref WGPU_WHOLE_MAP_SIZE is *not* accepted here.)
 *
 * @returns
 * @ref WGPUStatus_Error if the copy did not occur.
 */
expect fun wgpuBufferWriteMappedRange(buffer: WGPUBuffer?, offset: ULong, data: NativeAddress?, size: ULong): WGPUStatus

expect fun wgpuBufferAddRef(buffer: WGPUBuffer?): Unit

expect fun wgpuBufferRelease(buffer: WGPUBuffer?): Unit

/**
 * \defgroup WGPUCommandBufferMethods WGPUCommandBuffer methods
 * \brief Functions whose first argument has type WGPUCommandBuffer.
 *
 * @{
 */
expect fun wgpuCommandBufferSetLabel(commandBuffer: WGPUCommandBuffer?, label: WGPUStringView): Unit

expect fun wgpuCommandBufferAddRef(commandBuffer: WGPUCommandBuffer?): Unit

expect fun wgpuCommandBufferRelease(commandBuffer: WGPUCommandBuffer?): Unit

/**
 * \defgroup WGPUCommandEncoderMethods WGPUCommandEncoder methods
 * \brief Functions whose first argument has type WGPUCommandEncoder.
 *
 * @{
 * /
 * / **
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuCommandEncoderBeginComputePass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUComputePassDescriptor?): WGPUComputePassEncoder?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuCommandEncoderBeginRenderPass(commandEncoder: WGPUCommandEncoder?, descriptor: WGPURenderPassDescriptor?): WGPURenderPassEncoder?

expect fun wgpuCommandEncoderClearBuffer(commandEncoder: WGPUCommandEncoder?, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit

expect fun wgpuCommandEncoderCopyBufferToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUBuffer?, sourceOffset: ULong, destination: WGPUBuffer?, destinationOffset: ULong, size: ULong): Unit

expect fun wgpuCommandEncoderCopyBufferToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyBufferInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit

expect fun wgpuCommandEncoderCopyTextureToBuffer(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyBufferInfo?, copySize: WGPUExtent3D?): Unit

expect fun wgpuCommandEncoderCopyTextureToTexture(commandEncoder: WGPUCommandEncoder?, source: WGPUTexelCopyTextureInfo?, destination: WGPUTexelCopyTextureInfo?, copySize: WGPUExtent3D?): Unit

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuCommandEncoderFinish(commandEncoder: WGPUCommandEncoder?, descriptor: WGPUCommandBufferDescriptor?): WGPUCommandBuffer?

expect fun wgpuCommandEncoderInsertDebugMarker(commandEncoder: WGPUCommandEncoder?, markerLabel: WGPUStringView): Unit

expect fun wgpuCommandEncoderPopDebugGroup(commandEncoder: WGPUCommandEncoder?): Unit

expect fun wgpuCommandEncoderPushDebugGroup(commandEncoder: WGPUCommandEncoder?, groupLabel: WGPUStringView): Unit

expect fun wgpuCommandEncoderResolveQuerySet(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, firstQuery: UInt, queryCount: UInt, destination: WGPUBuffer?, destinationOffset: ULong): Unit

expect fun wgpuCommandEncoderSetLabel(commandEncoder: WGPUCommandEncoder?, label: WGPUStringView): Unit

expect fun wgpuCommandEncoderWriteTimestamp(commandEncoder: WGPUCommandEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit

expect fun wgpuCommandEncoderAddRef(commandEncoder: WGPUCommandEncoder?): Unit

expect fun wgpuCommandEncoderRelease(commandEncoder: WGPUCommandEncoder?): Unit

/**
 * \defgroup WGPUComputePassEncoderMethods WGPUComputePassEncoder methods
 * \brief Functions whose first argument has type WGPUComputePassEncoder.
 *
 * @{
 */
expect fun wgpuComputePassEncoderDispatchWorkgroups(computePassEncoder: WGPUComputePassEncoder?, workgroupCountX: UInt, workgroupCountY: UInt, workgroupCountZ: UInt): Unit

expect fun wgpuComputePassEncoderDispatchWorkgroupsIndirect(computePassEncoder: WGPUComputePassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit

expect fun wgpuComputePassEncoderEnd(computePassEncoder: WGPUComputePassEncoder?): Unit

expect fun wgpuComputePassEncoderInsertDebugMarker(computePassEncoder: WGPUComputePassEncoder?, markerLabel: WGPUStringView): Unit

expect fun wgpuComputePassEncoderPopDebugGroup(computePassEncoder: WGPUComputePassEncoder?): Unit

expect fun wgpuComputePassEncoderPushDebugGroup(computePassEncoder: WGPUComputePassEncoder?, groupLabel: WGPUStringView): Unit

expect fun wgpuComputePassEncoderSetBindGroup(computePassEncoder: WGPUComputePassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit

expect fun wgpuComputePassEncoderSetImmediates(computePassEncoder: WGPUComputePassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit

expect fun wgpuComputePassEncoderSetLabel(computePassEncoder: WGPUComputePassEncoder?, label: WGPUStringView): Unit

expect fun wgpuComputePassEncoderSetPipeline(computePassEncoder: WGPUComputePassEncoder?, pipeline: WGPUComputePipeline?): Unit

expect fun wgpuComputePassEncoderAddRef(computePassEncoder: WGPUComputePassEncoder?): Unit

expect fun wgpuComputePassEncoderRelease(computePassEncoder: WGPUComputePassEncoder?): Unit

/**
 * \defgroup WGPUComputePipelineMethods WGPUComputePipeline methods
 * \brief Functions whose first argument has type WGPUComputePipeline.
 *
 * @{
 * /
 * / **
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuComputePipelineGetBindGroupLayout(computePipeline: WGPUComputePipeline?, groupIndex: UInt): WGPUBindGroupLayout?

expect fun wgpuComputePipelineSetLabel(computePipeline: WGPUComputePipeline?, label: WGPUStringView): Unit

expect fun wgpuComputePipelineAddRef(computePipeline: WGPUComputePipeline?): Unit

expect fun wgpuComputePipelineRelease(computePipeline: WGPUComputePipeline?): Unit

/**
 * \defgroup WGPUDeviceMethods WGPUDevice methods
 * \brief Functions whose first argument has type WGPUDevice.
 *
 * @{
 * /
 * / **
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateBindGroup(device: WGPUDevice?, descriptor: WGPUBindGroupDescriptor?): WGPUBindGroup?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateBindGroupLayout(device: WGPUDevice?, descriptor: WGPUBindGroupLayoutDescriptor?): WGPUBindGroupLayout?

/**
 * TODO
 *
 * If @ref WGPUBufferDescriptor::mappedAtCreation is `true` and the mapping allocation fails,
 * returns `NULL`.
 *
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateBuffer(device: WGPUDevice?, descriptor: WGPUBufferDescriptor?): WGPUBuffer?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateCommandEncoder(device: WGPUDevice?, descriptor: WGPUCommandEncoderDescriptor?): WGPUCommandEncoder?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateComputePipeline(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?): WGPUComputePipeline?

expect fun wgpuDeviceCreateComputePipelineAsync(device: WGPUDevice?, descriptor: WGPUComputePipelineDescriptor?, callbackInfo: WGPUCreateComputePipelineAsyncCallbackInfo): WGPUFuture

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreatePipelineLayout(device: WGPUDevice?, descriptor: WGPUPipelineLayoutDescriptor?): WGPUPipelineLayout?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateQuerySet(device: WGPUDevice?, descriptor: WGPUQuerySetDescriptor?): WGPUQuerySet?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateRenderBundleEncoder(device: WGPUDevice?, descriptor: WGPURenderBundleEncoderDescriptor?): WGPURenderBundleEncoder?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateRenderPipeline(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?): WGPURenderPipeline?

expect fun wgpuDeviceCreateRenderPipelineAsync(device: WGPUDevice?, descriptor: WGPURenderPipelineDescriptor?, callbackInfo: WGPUCreateRenderPipelineAsyncCallbackInfo): WGPUFuture

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateSampler(device: WGPUDevice?, descriptor: WGPUSamplerDescriptor?): WGPUSampler?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateShaderModule(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?): WGPUShaderModule?

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceCreateTexture(device: WGPUDevice?, descriptor: WGPUTextureDescriptor?): WGPUTexture?

expect fun wgpuDeviceDestroy(device: WGPUDevice?): Unit

/**
 * @param adapterInfo
 * This parameter is @ref ReturnedWithOwnership.
 *
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuDeviceGetAdapterInfo(device: WGPUDevice?, adapterInfo: WGPUAdapterInfo?): WGPUStatus

/**
 * Get the list of @ref WGPUFeatureName values supported by the device.
 *
 * @param features
 * This parameter is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceGetFeatures(device: WGPUDevice?, features: WGPUSupportedFeatures?): Unit

/**
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuDeviceGetLimits(device: WGPUDevice?, limits: WGPULimits?): WGPUStatus

/**
 * @returns
 * The @ref WGPUFuture for the device-lost event of the device.
 */
expect fun wgpuDeviceGetLostFuture(device: WGPUDevice?): WGPUFuture

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuDeviceGetQueue(device: WGPUDevice?): WGPUQueue?

expect fun wgpuDeviceHasFeature(device: WGPUDevice?, feature: WGPUFeatureName): UInt

/**
 * Pops an error scope to the current thread's error scope stack,
 * asynchronously returning the result. See @ref ErrorScopes.
 */
expect fun wgpuDevicePopErrorScope(device: WGPUDevice?, callbackInfo: WGPUPopErrorScopeCallbackInfo): WGPUFuture

/**
 * Pushes an error scope to the current thread's error scope stack.
 * See @ref ErrorScopes.
 */
expect fun wgpuDevicePushErrorScope(device: WGPUDevice?, filter: WGPUErrorFilter): Unit

expect fun wgpuDeviceSetLabel(device: WGPUDevice?, label: WGPUStringView): Unit

expect fun wgpuDeviceAddRef(device: WGPUDevice?): Unit

expect fun wgpuDeviceRelease(device: WGPUDevice?): Unit

/**
 * \defgroup WGPUExternalTextureMethods WGPUExternalTexture methods
 * \brief Functions whose first argument has type WGPUExternalTexture.
 *
 * @{
 */
expect fun wgpuExternalTextureSetLabel(externalTexture: WGPUExternalTexture?, label: WGPUStringView): Unit

expect fun wgpuExternalTextureAddRef(externalTexture: WGPUExternalTexture?): Unit

expect fun wgpuExternalTextureRelease(externalTexture: WGPUExternalTexture?): Unit

/**
 * \defgroup WGPUInstanceMethods WGPUInstance methods
 * \brief Functions whose first argument has type WGPUInstance.
 *
 * @{
 * /
 * / **
 * Creates a @ref WGPUSurface, see @ref Surface-Creation for more details.
 *
 * @param descriptor
 * The description of the @ref WGPUSurface to create.
 *
 * @returns
 * A new @ref WGPUSurface for this descriptor (or an error @ref WGPUSurface).
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuInstanceCreateSurface(instance: WGPUInstance?, descriptor: WGPUSurfaceDescriptor?): WGPUSurface?

/**
 * Get the list of @ref WGPUWGSLLanguageFeatureName values supported by the instance.
 */
expect fun wgpuInstanceGetWGSLLanguageFeatures(instance: WGPUInstance?, features: WGPUSupportedWGSLLanguageFeatures?): Unit

expect fun wgpuInstanceHasWGSLLanguageFeature(instance: WGPUInstance?, feature: WGPUWGSLLanguageFeatureName): UInt

/**
 * Processes asynchronous events on this `WGPUInstance`, calling any callbacks for asynchronous operations created with @ref WGPUCallbackMode_AllowProcessEvents.
 *
 * See @ref Process-Events for more information.
 */
expect fun wgpuInstanceProcessEvents(instance: WGPUInstance?): Unit

expect fun wgpuInstanceRequestAdapter(instance: WGPUInstance?, options: WGPURequestAdapterOptions?, callbackInfo: WGPURequestAdapterCallbackInfo): WGPUFuture

/**
 * Wait for at least one WGPUFuture in `futures` to complete, and call callbacks of the respective completed asynchronous operations.
 *
 * See @ref Wait-Any for more information.
 */
expect fun wgpuInstanceWaitAny(instance: WGPUInstance?, futureCount: ULong, futures: WGPUFutureWaitInfo?, timeoutNS: ULong): WGPUWaitStatus

expect fun wgpuInstanceAddRef(instance: WGPUInstance?): Unit

expect fun wgpuInstanceRelease(instance: WGPUInstance?): Unit

/**
 * \defgroup WGPUPipelineLayoutMethods WGPUPipelineLayout methods
 * \brief Functions whose first argument has type WGPUPipelineLayout.
 *
 * @{
 */
expect fun wgpuPipelineLayoutSetLabel(pipelineLayout: WGPUPipelineLayout?, label: WGPUStringView): Unit

expect fun wgpuPipelineLayoutAddRef(pipelineLayout: WGPUPipelineLayout?): Unit

expect fun wgpuPipelineLayoutRelease(pipelineLayout: WGPUPipelineLayout?): Unit

/**
 * \defgroup WGPUQuerySetMethods WGPUQuerySet methods
 * \brief Functions whose first argument has type WGPUQuerySet.
 *
 * @{
 */
expect fun wgpuQuerySetDestroy(querySet: WGPUQuerySet?): Unit

expect fun wgpuQuerySetGetCount(querySet: WGPUQuerySet?): UInt

expect fun wgpuQuerySetGetType(querySet: WGPUQuerySet?): WGPUQueryType

expect fun wgpuQuerySetSetLabel(querySet: WGPUQuerySet?, label: WGPUStringView): Unit

expect fun wgpuQuerySetAddRef(querySet: WGPUQuerySet?): Unit

expect fun wgpuQuerySetRelease(querySet: WGPUQuerySet?): Unit

/**
 * \defgroup WGPUQueueMethods WGPUQueue methods
 * \brief Functions whose first argument has type WGPUQueue.
 *
 * @{
 */
expect fun wgpuQueueOnSubmittedWorkDone(queue: WGPUQueue?, callbackInfo: WGPUQueueWorkDoneCallbackInfo): WGPUFuture

expect fun wgpuQueueSetLabel(queue: WGPUQueue?, label: WGPUStringView): Unit

expect fun wgpuQueueSubmit(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): Unit

/**
 * Produces a @ref DeviceError both content-timeline (`size` alignment) and device-timeline
 * errors defined by the WebGPU specification.
 */
expect fun wgpuQueueWriteBuffer(queue: WGPUQueue?, buffer: WGPUBuffer?, bufferOffset: ULong, data: NativeAddress?, size: ULong): Unit

expect fun wgpuQueueWriteTexture(queue: WGPUQueue?, destination: WGPUTexelCopyTextureInfo?, data: NativeAddress?, dataSize: ULong, dataLayout: WGPUTexelCopyBufferLayout?, writeSize: WGPUExtent3D?): Unit

expect fun wgpuQueueAddRef(queue: WGPUQueue?): Unit

expect fun wgpuQueueRelease(queue: WGPUQueue?): Unit

/**
 * \defgroup WGPURenderBundleMethods WGPURenderBundle methods
 * \brief Functions whose first argument has type WGPURenderBundle.
 *
 * @{
 */
expect fun wgpuRenderBundleSetLabel(renderBundle: WGPURenderBundle?, label: WGPUStringView): Unit

expect fun wgpuRenderBundleAddRef(renderBundle: WGPURenderBundle?): Unit

expect fun wgpuRenderBundleRelease(renderBundle: WGPURenderBundle?): Unit

/**
 * \defgroup WGPURenderBundleEncoderMethods WGPURenderBundleEncoder methods
 * \brief Functions whose first argument has type WGPURenderBundleEncoder.
 *
 * @{
 */
expect fun wgpuRenderBundleEncoderDraw(renderBundleEncoder: WGPURenderBundleEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit

expect fun wgpuRenderBundleEncoderDrawIndexed(renderBundleEncoder: WGPURenderBundleEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit

expect fun wgpuRenderBundleEncoderDrawIndexedIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit

expect fun wgpuRenderBundleEncoderDrawIndirect(renderBundleEncoder: WGPURenderBundleEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit

/**
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuRenderBundleEncoderFinish(renderBundleEncoder: WGPURenderBundleEncoder?, descriptor: WGPURenderBundleDescriptor?): WGPURenderBundle?

expect fun wgpuRenderBundleEncoderInsertDebugMarker(renderBundleEncoder: WGPURenderBundleEncoder?, markerLabel: WGPUStringView): Unit

expect fun wgpuRenderBundleEncoderPopDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?): Unit

expect fun wgpuRenderBundleEncoderPushDebugGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupLabel: WGPUStringView): Unit

expect fun wgpuRenderBundleEncoderSetBindGroup(renderBundleEncoder: WGPURenderBundleEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit

expect fun wgpuRenderBundleEncoderSetImmediates(renderBundleEncoder: WGPURenderBundleEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit

expect fun wgpuRenderBundleEncoderSetIndexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit

expect fun wgpuRenderBundleEncoderSetLabel(renderBundleEncoder: WGPURenderBundleEncoder?, label: WGPUStringView): Unit

expect fun wgpuRenderBundleEncoderSetPipeline(renderBundleEncoder: WGPURenderBundleEncoder?, pipeline: WGPURenderPipeline?): Unit

expect fun wgpuRenderBundleEncoderSetVertexBuffer(renderBundleEncoder: WGPURenderBundleEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit

expect fun wgpuRenderBundleEncoderAddRef(renderBundleEncoder: WGPURenderBundleEncoder?): Unit

expect fun wgpuRenderBundleEncoderRelease(renderBundleEncoder: WGPURenderBundleEncoder?): Unit

/**
 * \defgroup WGPURenderPassEncoderMethods WGPURenderPassEncoder methods
 * \brief Functions whose first argument has type WGPURenderPassEncoder.
 *
 * @{
 */
expect fun wgpuRenderPassEncoderBeginOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?, queryIndex: UInt): Unit

expect fun wgpuRenderPassEncoderDraw(renderPassEncoder: WGPURenderPassEncoder?, vertexCount: UInt, instanceCount: UInt, firstVertex: UInt, firstInstance: UInt): Unit

expect fun wgpuRenderPassEncoderDrawIndexed(renderPassEncoder: WGPURenderPassEncoder?, indexCount: UInt, instanceCount: UInt, firstIndex: UInt, baseVertex: Int, firstInstance: UInt): Unit

expect fun wgpuRenderPassEncoderDrawIndexedIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit

expect fun wgpuRenderPassEncoderDrawIndirect(renderPassEncoder: WGPURenderPassEncoder?, indirectBuffer: WGPUBuffer?, indirectOffset: ULong): Unit

expect fun wgpuRenderPassEncoderEnd(renderPassEncoder: WGPURenderPassEncoder?): Unit

expect fun wgpuRenderPassEncoderEndOcclusionQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit

expect fun wgpuRenderPassEncoderExecuteBundles(renderPassEncoder: WGPURenderPassEncoder?, bundleCount: ULong, bundles: NativeAddress?): Unit

expect fun wgpuRenderPassEncoderInsertDebugMarker(renderPassEncoder: WGPURenderPassEncoder?, markerLabel: WGPUStringView): Unit

expect fun wgpuRenderPassEncoderPopDebugGroup(renderPassEncoder: WGPURenderPassEncoder?): Unit

expect fun wgpuRenderPassEncoderPushDebugGroup(renderPassEncoder: WGPURenderPassEncoder?, groupLabel: WGPUStringView): Unit

expect fun wgpuRenderPassEncoderSetBindGroup(renderPassEncoder: WGPURenderPassEncoder?, groupIndex: UInt, group: WGPUBindGroup?, dynamicOffsetCount: ULong, dynamicOffsets: NativeAddress?): Unit

/**
 * @param color
 * The RGBA blend constant. Represents an `f32` color using @ref DoubleAsSupertype.
 */
expect fun wgpuRenderPassEncoderSetBlendConstant(renderPassEncoder: WGPURenderPassEncoder?, color: WGPUColor?): Unit

expect fun wgpuRenderPassEncoderSetImmediates(renderPassEncoder: WGPURenderPassEncoder?, offset: UInt, data: NativeAddress?, size: ULong): Unit

expect fun wgpuRenderPassEncoderSetIndexBuffer(renderPassEncoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, format: WGPUIndexFormat, offset: ULong, size: ULong): Unit

expect fun wgpuRenderPassEncoderSetLabel(renderPassEncoder: WGPURenderPassEncoder?, label: WGPUStringView): Unit

expect fun wgpuRenderPassEncoderSetPipeline(renderPassEncoder: WGPURenderPassEncoder?, pipeline: WGPURenderPipeline?): Unit

expect fun wgpuRenderPassEncoderSetScissorRect(renderPassEncoder: WGPURenderPassEncoder?, x: UInt, y: UInt, width: UInt, height: UInt): Unit

expect fun wgpuRenderPassEncoderSetStencilReference(renderPassEncoder: WGPURenderPassEncoder?, reference: UInt): Unit

expect fun wgpuRenderPassEncoderSetVertexBuffer(renderPassEncoder: WGPURenderPassEncoder?, slot: UInt, buffer: WGPUBuffer?, offset: ULong, size: ULong): Unit

/**
 * TODO
 *
 * If any argument is non-finite, produces a @ref NonFiniteFloatValueError.
 */
expect fun wgpuRenderPassEncoderSetViewport(renderPassEncoder: WGPURenderPassEncoder?, x: Float, y: Float, width: Float, height: Float, minDepth: Float, maxDepth: Float): Unit

expect fun wgpuRenderPassEncoderAddRef(renderPassEncoder: WGPURenderPassEncoder?): Unit

expect fun wgpuRenderPassEncoderRelease(renderPassEncoder: WGPURenderPassEncoder?): Unit

/**
 * \defgroup WGPURenderPipelineMethods WGPURenderPipeline methods
 * \brief Functions whose first argument has type WGPURenderPipeline.
 *
 * @{
 * /
 * / **
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuRenderPipelineGetBindGroupLayout(renderPipeline: WGPURenderPipeline?, groupIndex: UInt): WGPUBindGroupLayout?

expect fun wgpuRenderPipelineSetLabel(renderPipeline: WGPURenderPipeline?, label: WGPUStringView): Unit

expect fun wgpuRenderPipelineAddRef(renderPipeline: WGPURenderPipeline?): Unit

expect fun wgpuRenderPipelineRelease(renderPipeline: WGPURenderPipeline?): Unit

/**
 * \defgroup WGPUSamplerMethods WGPUSampler methods
 * \brief Functions whose first argument has type WGPUSampler.
 *
 * @{
 */
expect fun wgpuSamplerSetLabel(sampler: WGPUSampler?, label: WGPUStringView): Unit

expect fun wgpuSamplerAddRef(sampler: WGPUSampler?): Unit

expect fun wgpuSamplerRelease(sampler: WGPUSampler?): Unit

/**
 * \defgroup WGPUShaderModuleMethods WGPUShaderModule methods
 * \brief Functions whose first argument has type WGPUShaderModule.
 *
 * @{
 */
expect fun wgpuShaderModuleGetCompilationInfo(shaderModule: WGPUShaderModule?, callbackInfo: WGPUCompilationInfoCallbackInfo): WGPUFuture

expect fun wgpuShaderModuleSetLabel(shaderModule: WGPUShaderModule?, label: WGPUStringView): Unit

expect fun wgpuShaderModuleAddRef(shaderModule: WGPUShaderModule?): Unit

expect fun wgpuShaderModuleRelease(shaderModule: WGPUShaderModule?): Unit

/**
 * \defgroup WGPUSupportedFeaturesMethods WGPUSupportedFeatures methods
 * \brief Functions whose first argument has type WGPUSupportedFeatures.
 *
 * @{
 * /
 * / **
 * Frees members which were allocated by the API.
 */
expect fun wgpuSupportedFeaturesFreeMembers(supportedFeatures: WGPUSupportedFeatures): Unit

/**
 * \defgroup WGPUSupportedInstanceFeaturesMethods WGPUSupportedInstanceFeatures methods
 * \brief Functions whose first argument has type WGPUSupportedInstanceFeatures.
 *
 * @{
 * /
 * / **
 * Frees members which were allocated by the API.
 */
expect fun wgpuSupportedInstanceFeaturesFreeMembers(supportedInstanceFeatures: WGPUSupportedInstanceFeatures): Unit

/**
 * \defgroup WGPUSupportedWGSLLanguageFeaturesMethods WGPUSupportedWGSLLanguageFeatures methods
 * \brief Functions whose first argument has type WGPUSupportedWGSLLanguageFeatures.
 *
 * @{
 * /
 * / **
 * Frees members which were allocated by the API.
 */
expect fun wgpuSupportedWGSLLanguageFeaturesFreeMembers(supportedWGSLLanguageFeatures: WGPUSupportedWGSLLanguageFeatures): Unit

/**
 * \defgroup WGPUSurfaceMethods WGPUSurface methods
 * \brief Functions whose first argument has type WGPUSurface.
 *
 * @{
 * /
 * / **
 * Configures parameters for rendering to `surface`.
 * Produces a @ref DeviceError for all content-timeline errors defined by the WebGPU specification.
 *
 * See @ref Surface-Configuration for more details.
 *
 * @param config
 * The new configuration to use.
 */
expect fun wgpuSurfaceConfigure(surface: WGPUSurface?, config: WGPUSurfaceConfiguration?): Unit

/**
 * Provides information on how `adapter` is able to use `surface`.
 * See @ref Surface-Capabilities for more details.
 *
 * @param adapter
 * The @ref WGPUAdapter to get capabilities for presenting to this @ref WGPUSurface.
 *
 * @param capabilities
 * The structure to fill capabilities in.
 * It may contain memory allocations so @ref wgpuSurfaceCapabilitiesFreeMembers must be called to avoid memory leaks.
 * This parameter is @ref ReturnedWithOwnership.
 *
 * @returns
 * Indicates if there was an @ref OutStructChainError.
 */
expect fun wgpuSurfaceGetCapabilities(surface: WGPUSurface?, adapter: WGPUAdapter?, capabilities: WGPUSurfaceCapabilities?): WGPUStatus

/**
 * Returns the @ref WGPUTexture to render to `surface` this frame along with metadata on the frame.
 * Returns `NULL` and @ref WGPUSurfaceGetCurrentTextureStatus_Error if the surface is not configured.
 *
 * See @ref Surface-Presenting for more details.
 *
 * @param surfaceTexture
 * The structure to fill the @ref WGPUTexture and metadata in.
 */
expect fun wgpuSurfaceGetCurrentTexture(surface: WGPUSurface?, surfaceTexture: WGPUSurfaceTexture?): Unit

/**
 * Shows `surface`'s current texture to the user.
 * See @ref Surface-Presenting for more details.
 *
 * @returns
 * Returns @ref WGPUStatus_Error if the surface doesn't have a current texture.
 */
expect fun wgpuSurfacePresent(surface: WGPUSurface?): WGPUStatus

/**
 * Modifies the label used to refer to `surface`.
 *
 * @param label
 * The new label.
 */
expect fun wgpuSurfaceSetLabel(surface: WGPUSurface?, label: WGPUStringView): Unit

/**
 * Removes the configuration for `surface`.
 * See @ref Surface-Configuration for more details.
 */
expect fun wgpuSurfaceUnconfigure(surface: WGPUSurface?): Unit

expect fun wgpuSurfaceAddRef(surface: WGPUSurface?): Unit

expect fun wgpuSurfaceRelease(surface: WGPUSurface?): Unit

/**
 * \defgroup WGPUSurfaceCapabilitiesMethods WGPUSurfaceCapabilities methods
 * \brief Functions whose first argument has type WGPUSurfaceCapabilities.
 *
 * @{
 * /
 * / **
 * Frees members which were allocated by the API.
 */
expect fun wgpuSurfaceCapabilitiesFreeMembers(surfaceCapabilities: WGPUSurfaceCapabilities): Unit

/**
 * \defgroup WGPUTextureMethods WGPUTexture methods
 * \brief Functions whose first argument has type WGPUTexture.
 *
 * @{
 * /
 * / **
 * @returns
 * This value is @ref ReturnedWithOwnership.
 */
expect fun wgpuTextureCreateView(texture: WGPUTexture?, descriptor: WGPUTextureViewDescriptor?): WGPUTextureView?

expect fun wgpuTextureDestroy(texture: WGPUTexture?): Unit

expect fun wgpuTextureGetDepthOrArrayLayers(texture: WGPUTexture?): UInt

expect fun wgpuTextureGetDimension(texture: WGPUTexture?): WGPUTextureDimension

expect fun wgpuTextureGetFormat(texture: WGPUTexture?): WGPUTextureFormat

expect fun wgpuTextureGetHeight(texture: WGPUTexture?): UInt

expect fun wgpuTextureGetMipLevelCount(texture: WGPUTexture?): UInt

expect fun wgpuTextureGetSampleCount(texture: WGPUTexture?): UInt

expect fun wgpuTextureGetTextureBindingViewDimension(texture: WGPUTexture?): WGPUTextureViewDimension

expect fun wgpuTextureGetUsage(texture: WGPUTexture?): ULong

expect fun wgpuTextureGetWidth(texture: WGPUTexture?): UInt

expect fun wgpuTextureSetLabel(texture: WGPUTexture?, label: WGPUStringView): Unit

expect fun wgpuTextureAddRef(texture: WGPUTexture?): Unit

expect fun wgpuTextureRelease(texture: WGPUTexture?): Unit

/**
 * \defgroup WGPUTextureViewMethods WGPUTextureView methods
 * \brief Functions whose first argument has type WGPUTextureView.
 *
 * @{
 */
expect fun wgpuTextureViewSetLabel(textureView: WGPUTextureView?, label: WGPUStringView): Unit

expect fun wgpuTextureViewAddRef(textureView: WGPUTextureView?): Unit

expect fun wgpuTextureViewRelease(textureView: WGPUTextureView?): Unit

typealias WGPUNativeSType = UInt
/**
 * Identifies @ref WGPUDeviceExtras.
 */
const val WGPUSType_DeviceExtras : WGPUNativeSType = 196609u
/**
 * Identifies @ref WGPUNativeLimits.
 */
const val WGPUSType_NativeLimits : WGPUNativeSType = 196610u
/**
 * Identifies @ref WGPUShaderSourceGLSL.
 */
const val WGPUSType_ShaderSourceGLSL : WGPUNativeSType = 196611u
/**
 * Identifies @ref WGPUInstanceExtras.
 */
const val WGPUSType_InstanceExtras : WGPUNativeSType = 196612u
/**
 * Identifies @ref WGPUBindGroupEntryExtras.
 */
const val WGPUSType_BindGroupEntryExtras : WGPUNativeSType = 196613u
/**
 * Identifies @ref WGPUBindGroupLayoutEntryExtras.
 */
const val WGPUSType_BindGroupLayoutEntryExtras : WGPUNativeSType = 196614u
/**
 * Identifies @ref WGPUQuerySetDescriptorExtras.
 */
const val WGPUSType_QuerySetDescriptorExtras : WGPUNativeSType = 196615u
/**
 * Identifies @ref WGPUSurfaceConfigurationExtras.
 */
const val WGPUSType_SurfaceConfigurationExtras : WGPUNativeSType = 196616u
/**
 * Identifies @ref WGPUSurfaceSourceSwapChainPanel.
 */
const val WGPUSType_SurfaceSourceSwapChainPanel : WGPUNativeSType = 196617u
/**
 * Identifies @ref WGPUPrimitiveStateExtras.
 */
const val WGPUSType_PrimitiveStateExtras : WGPUNativeSType = 196618u
/**
 * Identifies @ref WGPUSamplerDescriptorExtras.
 */
const val WGPUSType_SamplerDescriptorExtras : WGPUNativeSType = 196619u
/**
 * Identifies @ref WGPUSamplerDescriptorExtras.
 */
const val WGPUNativeSType_Force32 : WGPUNativeSType = 2147483647u

/**
 * Additional surface-get-current-texture status codes defined by wgpu-native.
 *
 * These extend the standard @c WGPUSurfaceGetCurrentTextureStatus values.
 */
typealias WGPUNativeSurfaceGetCurrentTextureStatus = UInt
/**
 * The surface texture was not acquired because the window is occluded
 * (e.g. minimized or fully covered by another window).
 *
 * No texture is returned and the @c texture field of
 * @c WGPUSurfaceTexture will be NULL. The surface and swapchain remain
 * valid -- there is no need to reconfigure or recreate the surface.
 *
 * Applications should skip rendering for the current frame and try
 * again once the window is no longer occluded. If you are using a
 * windowing library such as winit, listen for the window's "occluded"
 * event and request a new redraw when the window becomes visible again.
 *
 * When does this occur?
 *
 * Currently this status is only produced by the Metal backend on macOS.
 * When a window is not visible (checked via the @c NSWindow
 * @c occlusionState property), acquiring the next drawable would block
 * for up to one second waiting for vsync. wgpu-native returns
 * @c Occluded instead to avoid that hang.
 *
 * Other backends (Vulkan, DX12, GL) do not currently report this
 * status; an occluded window on those backends may produce
 * @c WGPUSurfaceGetCurrentTextureStatus_Timeout or simply succeed
 * normally.
 */
const val WGPUSurfaceGetCurrentTextureStatus_Occluded : WGPUNativeSurfaceGetCurrentTextureStatus = 196609u
/**
 * The surface texture was not acquired because the window is occluded
 * (e.g. minimized or fully covered by another window).
 *
 * No texture is returned and the @c texture field of
 * @c WGPUSurfaceTexture will be NULL. The surface and swapchain remain
 * valid -- there is no need to reconfigure or recreate the surface.
 *
 * Applications should skip rendering for the current frame and try
 * again once the window is no longer occluded. If you are using a
 * windowing library such as winit, listen for the window's "occluded"
 * event and request a new redraw when the window becomes visible again.
 *
 * When does this occur?
 *
 * Currently this status is only produced by the Metal backend on macOS.
 * When a window is not visible (checked via the @c NSWindow
 * @c occlusionState property), acquiring the next drawable would block
 * for up to one second waiting for vsync. wgpu-native returns
 * @c Occluded instead to avoid that hang.
 *
 * Other backends (Vulkan, DX12, GL) do not currently report this
 * status; an occluded window on those backends may produce
 * @c WGPUSurfaceGetCurrentTextureStatus_Timeout or simply succeed
 * normally.
 */
const val WGPUNativeSurfaceGetCurrentTextureStatus_Force32 : WGPUNativeSurfaceGetCurrentTextureStatus = 2147483647u

/**
 * Native-only device features.
 *
 * These extend the standard @c WGPUFeatureName values and can be passed to
 * @c WGPUDeviceDescriptor::requiredFeatures to request additional
 * capabilities when creating a device.
 */
typealias WGPUNativeFeature = UInt
/**
 * Allows the use of immediate data: small, fast blocks of memory
 * that can be updated inside a render pass, compute pass, or render
 * bundle encoder.
 *
 * Enables @ref wgpuRenderPassEncoderSetImmediates,
 * @ref wgpuComputePassEncoderSetImmediates,
 * @ref wgpuRenderBundleEncoderSetImmediates,
 * non-zero @c immediateSize in @ref WGPUPipelineLayout,
 * and non-zero @c maxImmediateSize in @ref WGPULimits.
 *
 * A block of immediate data can be declared in WGSL with
 * @c var<immediate>:
 * @code
 * struct Immediates { example: f32, }
 * var<immediate> c: Immediates;
 * @endcode
 *
 * In GLSL, this corresponds to @c layout(immediates) @c uniform @c Name @c {..}.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan
 * - Metal
 * - OpenGL (emulated with uniforms)
 * - WebGPU
 *
 * This is a web and native feature.
 */
const val WGPUNativeFeature_Immediates : WGPUNativeFeature = 196609u
/**
 * Enables device-specific texture format features.
 *
 * By default only texture format properties as defined by the WebGPU
 * specification are allowed. Enabling this feature flag extends the
 * features of each format to the ones supported by the current device.
 * Note that without this flag, read/write storage access is not allowed
 * at all.
 *
 * This extension does not enable additional formats.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureAdapterSpecificFormatFeatures : WGPUNativeFeature = 196610u
/**
 * Allows the use of a buffer containing the actual number of draw calls.
 *
 * Enables @ref wgpuRenderPassEncoderMultiDrawIndirectCount and
 * @ref wgpuRenderPassEncoderMultiDrawIndexedIndirectCount.
 *
 * This feature being present also implies that all calls to
 * @ref wgpuRenderPassEncoderMultiDrawIndirect and
 * @ref wgpuRenderPassEncoderMultiDrawIndexedIndirect are not being
 * emulated with a series of @c draw_indirect calls.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan 1.2+ (or VK_KHR_draw_indirect_count)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_MultiDrawIndirectCount : WGPUNativeFeature = 196612u
/**
 * Enables bindings of writable storage buffers and textures visible
 * to vertex shaders.
 *
 * Note: some (tiled-based) platforms do not support vertex shaders
 * with any side-effects.
 *
 * Supported platforms:
 * - All
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_VertexWritableStorage : WGPUNativeFeature = 196613u
/**
 * Allows the user to create uniform arrays of textures in shaders:
 *
 * - WGSL: @c var @c textures: @c binding_array<texture_2d<f32>, @c 10>
 * - GLSL: @c uniform @c texture2D @c textures[10]
 *
 * If @ref WGPUNativeFeature_StorageResourceBindingArray is supported
 * as well as this, the user may also create uniform arrays of storage
 * textures.
 *
 * This capability allows them to exist and to be indexed by dynamically
 * uniform values.
 *
 * Supported platforms:
 * - DX12
 * - Metal (with MSL 2.0+ on macOS 10.13+)
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureBindingArray : WGPUNativeFeature = 196614u
/**
 * Allows shaders to index sampled texture and storage buffer resource
 * arrays with dynamically non-uniform values:
 *
 * e.g. @c texture_array[vertex_data]
 *
 * In order to use this capability, the corresponding GLSL extension must
 * be enabled:
 *
 * @c \#extension @c GL_EXT_nonuniform_qualifier @c : @c require
 *
 * and then used either as @c nonuniformEXT qualifier in variable
 * declaration or as @c nonuniformEXT constructor.
 *
 * WGSL and HLSL do not need any extension.
 *
 * Supported platforms:
 * - DX12
 * - Metal (with MSL 2.0+ on macOS 10.13+)
 * - Vulkan 1.2+ (or VK_EXT_descriptor_indexing)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_SampledTextureAndStorageBufferArrayNonUniformIndexing : WGPUNativeFeature = 196615u
/**
 * Enables use of Pipeline Statistics Queries. These queries report the
 * count of various operations performed between the start and stop call.
 *
 * Use @ref wgpuRenderPassEncoderBeginPipelineStatisticsQuery /
 * @ref wgpuRenderPassEncoderEndPipelineStatisticsQuery (or the compute
 * pass equivalents) to start and stop a query.
 *
 * They must be resolved using @c wgpuCommandEncoderResolveQuerySet into
 * a buffer. See @ref WGPUPipelineStatisticName for the list of available
 * statistics.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_PipelineStatisticsQuery : WGPUNativeFeature = 196616u
/**
 * Allows the user to create uniform arrays of storage buffers or
 * textures in shaders, if @ref WGPUNativeFeature_BufferBindingArray
 * or @ref WGPUNativeFeature_TextureBindingArray (respectively)
 * is also supported.
 *
 * This capability allows them to exist and to be indexed by dynamically
 * uniform values.
 *
 * Supported platforms:
 * - Metal (with MSL 2.2+ on macOS 10.13+)
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_StorageResourceBindingArray : WGPUNativeFeature = 196617u
/**
 * Allows the user to create bind groups containing arrays with fewer
 * bindings than the @c WGPUBindGroupLayout requires.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_PartiallyBoundBindingArray : WGPUNativeFeature = 196618u
/**
 * Enables normalized 16-bit texture formats:
 * @ref WGPUTextureFormat_R16Unorm, @ref WGPUTextureFormat_R16Snorm,
 * @ref WGPUTextureFormat_RG16Unorm, @ref WGPUTextureFormat_RG16Snorm,
 * @ref WGPUTextureFormat_RGBA16Unorm, @ref WGPUTextureFormat_RGBA16Snorm.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureFormat16bitNorm : WGPUNativeFeature = 196619u
/**
 * Enables ASTC HDR family of compressed textures.
 *
 * Compressed textures sacrifice some quality in exchange for
 * significantly reduced bandwidth usage.
 *
 * Support for this feature guarantees availability of
 * @c COPY_SRC | @c COPY_DST | @c TEXTURE_BINDING for ASTC formats
 * with the HDR channel type.
 * @ref WGPUNativeFeature_TextureAdapterSpecificFormatFeatures may
 * enable additional usages.
 *
 * Supported platforms:
 * - Metal
 * - Vulkan
 * - OpenGL
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureCompressionAstcHdr : WGPUNativeFeature = 196620u
/**
 * Removes the WebGPU restriction that @c MAP_READ and @c MAP_WRITE
 * buffer usages must be paired exclusively with @c COPY_DST and
 * @c COPY_SRC respectively.
 *
 * This is only beneficial on systems that share memory between CPU and
 * GPU. If enabled on a system that doesn't, this can severely hinder
 * performance. Only use if you understand the consequences.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_MappablePrimaryBuffers : WGPUNativeFeature = 196622u
/**
 * Allows the user to create arrays of buffers in shaders:
 *
 * - WGSL: @c var<uniform> @c buffer_array: @c array<MyBuffer, @c 10>
 * - GLSL: @c uniform @c myBuffer @c { @c ... @c } @c buffer_array[10]
 *
 * This capability allows them to exist and to be indexed by dynamically
 * uniform values.
 *
 * If @ref WGPUNativeFeature_StorageResourceBindingArray is supported as
 * well as this, the user may also create arrays of storage buffers.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_BufferBindingArray : WGPUNativeFeature = 196623u
/**
 * Allows shaders to index storage texture resource
 * arrays with dynamically non-uniform values.
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_StorageTextureArrayNonUniformIndexing : WGPUNativeFeature = 196624u
/**
 * Allows shaders to index storage texture resource
 * arrays with dynamically non-uniform values.
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_AddressModeClampToZero : WGPUNativeFeature = 196625u
/**
 * Allows shaders to index storage texture resource
 * arrays with dynamically non-uniform values.
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_AddressModeClampToBorder : WGPUNativeFeature = 196626u
/**
 * Allows the user to set @ref WGPUPolygonMode_Line in
 * @ref WGPUPrimitiveStateExtras::polygonMode.
 *
 * This allows drawing polygons/triangles as lines (wireframe) instead
 * of filled.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_PolygonModeLine : WGPUNativeFeature = 196627u
/**
 * Allows the user to set @ref WGPUPolygonMode_Point in
 * @ref WGPUPrimitiveStateExtras::polygonMode.
 *
 * This allows only drawing the vertices of polygons/triangles instead
 * of filled.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_PolygonModePoint : WGPUNativeFeature = 196628u
/**
 * Allows the user to enable overestimation conservative rasterization
 * via @ref WGPUPrimitiveStateExtras::conservative.
 *
 * Processing of degenerate triangles/lines is hardware specific.
 * Only triangles are supported.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ConservativeRasterization : WGPUNativeFeature = 196629u
/**
 * Enables clear to zero for textures.
 *
 * Supported platforms:
 * - All
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ClearTexture : WGPUNativeFeature = 196630u
/**
 * Enables multiview render passes and `builtin(view_index)` in vertex/mesh shaders.
 *
 * Supported platforms:
 * - Vulkan
 * - Metal
 * - DX12
 * - OpenGL (web only)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_Multiview : WGPUNativeFeature = 196632u
/**
 * Enables using 64-bit types for vertex attributes.
 *
 * Requires @ref WGPUNativeFeature_ShaderF64.
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_VertexAttribute64bit : WGPUNativeFeature = 196633u
/**
 * Allows for creation of textures of format
 * @ref WGPUNativeTextureFormat_NV12.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureFormatNv12 : WGPUNativeFeature = 196634u
/**
 * Allows for the creation of ray-tracing queries within shaders.
 *
 * @b EXPERIMENTAL: Features enabled by this may have major bugs and are
 * expected to be subject to breaking changes.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_RayQuery : WGPUNativeFeature = 196636u
/**
 * Enables 64-bit floating point types in SPIR-V shaders.
 *
 * Note: even when supported by GPU hardware, 64-bit floating point
 * operations are frequently between 16 and 64 @e times slower than
 * equivalent operations on 32-bit floats.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderF64 : WGPUNativeFeature = 196637u
/**
 * Allows shaders to use i16. Not currently supported in naga, only
 * available through SPIR-V passthrough.
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderI16 : WGPUNativeFeature = 196638u
/**
 * Allows shaders to use the @c early_depth_test attribute.
 *
 * The attribute is applied to the fragment shader entry point and can be
 * used in two ways:
 *
 * 1. Force early depth/stencil tests:
 * - WGSL: @c \@early_depth_test(force)
 * - GLSL: @c layout(early_fragment_tests) @c in;
 *
 * 2. Provide a conservative depth specifier that allows an additional
 * early depth test under certain conditions:
 * - WGSL: @c \@early_depth_test(greater_equal/less_equal/unchanged)
 * - GLSL: @c layout(depth_<greater/less/unchanged>) @c out @c float @c gl_FragDepth;
 *
 * Supported platforms:
 * - Vulkan
 * - GLES 3.1+
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderEarlyDepthTest : WGPUNativeFeature = 196640u
/**
 * Allows compute and fragment shaders to use the subgroup operation
 * built-ins and perform subgroup operations (except barriers).
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_Subgroup : WGPUNativeFeature = 196641u
/**
 * Allows vertex shaders to use the subgroup operation built-ins and
 * perform subgroup operations (except barriers).
 *
 * Supported platforms:
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_SubgroupVertex : WGPUNativeFeature = 196642u
/**
 * Allows compute shaders to use the subgroup barrier.
 *
 * Requires @ref WGPUNativeFeature_Subgroup. Without it, enables nothing.
 *
 * Supported platforms:
 * - Vulkan
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_SubgroupBarrier : WGPUNativeFeature = 196643u
/**
 * Allows for timestamp queries directly on command encoders.
 *
 * Implies @c WGPUFeatureName_TimestampQuery is supported.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal
 * - OpenGL (with GL_ARB_timer_query)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TimestampQueryInsideEncoders : WGPUNativeFeature = 196644u
/**
 * Allows for timestamp queries inside render and compute passes.
 *
 * Implies @c WGPUFeatureName_TimestampQuery and
 * @ref WGPUNativeFeature_TimestampQueryInsideEncoders are supported.
 *
 * Enables @ref wgpuRenderPassEncoderWriteTimestamp and
 * @ref wgpuComputePassEncoderWriteTimestamp.
 *
 * This is generally not available on tile-based rasterization GPUs.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal (AMD & Intel, not Apple GPUs)
 * - OpenGL (with GL_ARB_timer_query)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TimestampQueryInsidePasses : WGPUNativeFeature = 196645u
/**
 * Allows shaders to use i64 and u64.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12 (DXC only)
 * - Metal (with MSL 2.3+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderInt64 : WGPUNativeFeature = 196646u
/**
 * Allows shaders to use f32 atomic load, store, add, sub, and exchange.
 *
 * Supported platforms:
 * - Metal (with MSL 3.0+ and Apple7+/Mac2)
 * - Vulkan (with [VK_EXT_shader_atomic_float])
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderFloat32Atomic : WGPUNativeFeature = 196647u
/**
 * Enables image atomic fetch add, and, xor, or, min, and max for R32Uint and R32Sint textures.
 *
 * Supported platforms:
 * - Vulkan
 * - DX12
 * - Metal (with MSL 3.1+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureAtomic : WGPUNativeFeature = 196648u
/**
 * Allows for creation of textures of format
 * @ref WGPUNativeTextureFormat_P010.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureFormatP010 : WGPUNativeFeature = 196649u
/**
 * Allows the use of pipeline cache objects
 *
 * Supported platforms:
 * - Vulkan
 *
 * Unimplemented Platforms:
 * - DX12
 * - Metal
 */
const val WGPUNativeFeature_PipelineCache : WGPUNativeFeature = 196651u
/**
 * Allows shaders to use i64 and u64 atomic min and max.
 *
 * Supported platforms:
 * - Vulkan (with VK_KHR_shader_atomic_int64)
 * - DX12 (with SM 6.6+)
 * - Metal (with MSL 2.4+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderInt64AtomicMinMax : WGPUNativeFeature = 196652u
/**
 * Allows shaders to use all i64 and u64 atomic operations.
 *
 * Supported platforms:
 * - Vulkan (with VK_KHR_shader_atomic_int64)
 * - DX12 (with SM 6.6+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderInt64AtomicAllOps : WGPUNativeFeature = 196653u
/**
 * Enables R64Uint image atomic min and max.
 *
 * Supported platforms:
 * - Vulkan (with VK_EXT_shader_image_atomic_int64)
 * - DX12 (with SM 6.6+)
 * - Metal (with MSL 3.1+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_TextureInt64Atomic : WGPUNativeFeature = 196656u
/**
 * Enables shader barycentric coordinates.
 *
 * Supported platforms:
 * - Vulkan (with VK_KHR_fragment_shader_barycentric)
 * - DX12 (with SM 6.1+)
 * - Metal (with MSL 2.2+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderBarycentrics : WGPUNativeFeature = 196663u
/**
 * Enables using multiview where not all texture array layers are rendered to in a single render pass/render pipeline. Making
 * use of this feature also requires enabling `Features::MULTIVIEW`.
 *
 * Supported platforms
 * - Vulkan
 * - DX12
 *
 * While metal supports this in theory, the behavior of `view_index` differs from vulkan and dx12 so the feature isn't exposed.
 */
const val WGPUNativeFeature_SelectiveMultiview : WGPUNativeFeature = 196664u
/**
 * Enables using multiview where not all texture array layers are rendered to in a single render pass/render pipeline. Making
 * use of this feature also requires enabling `Features::MULTIVIEW`.
 *
 * Supported platforms
 * - Vulkan
 * - DX12
 *
 * While metal supports this in theory, the behavior of `view_index` differs from vulkan and dx12 so the feature isn't exposed.
 */
const val WGPUNativeFeature_MultisampleArray : WGPUNativeFeature = 196666u
/**
 * Enables cooperative matrix operations (also known as tensor cores on NVIDIA GPUs
 * or simdgroup matrix operations on Apple GPUs).
 *
 * Cooperative matrices allow a workgroup to collectively load, store, and perform
 * matrix multiply-accumulate operations on small tiles of data, enabling
 * hardware-accelerated matrix math.
 *
 * @b EXPERIMENTAL: Features enabled by this may have major bugs and are
 * expected to be subject to breaking changes.
 *
 * **Current limitations:** The implementation currently only supports 8x8 f32 matrices.
 * On Vulkan, support is determined by querying `vkGetPhysicalDeviceCooperativeMatrixPropertiesKHR`
 * for configurations matching 8x8x8 f32. Most Vulkan implementations (NVIDIA, AMD) primarily
 * support f16 inputs at larger sizes (e.g., 16x16), so Vulkan support may be limited.
 *
 * Supported platforms:
 * - Metal (with MSL 2.3+ and Apple7+/Mac2+, using simdgroup matrix operations)
 * - Vulkan (with [VK_KHR_cooperative_matrix](https://registry.khronos.org/vulkan/specs/latest/man/html/VK_KHR_cooperative_matrix.html), if 8x8 f32 is supported)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_CooperativeMatrix : WGPUNativeFeature = 196667u
/**
 * Enables shader per-vertex attributes.
 *
 * Supported platforms:
 * - Vulkan (with VK_KHR_fragment_shader_barycentric)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderPerVertex : WGPUNativeFeature = 196668u
/**
 * Enables shader `draw_index` builtin.
 *
 * Supported platforms:
 * - GLES
 * - Vulkan
 *
 * Potential platforms:
 * - DX12
 * - Metal
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_ShaderDrawIndex : WGPUNativeFeature = 196669u
/**
 * Allows the user to create arrays of acceleration structures in shaders:
 *
 * ex.
 * - `var tlas: binding_array<acceleration_structure, 10>` (WGSL)
 *
 * This capability allows them to exist and to be indexed by dynamically uniform values.
 *
 * Supported platforms:
 * - DX12
 * - Vulkan
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_AccelerationStructureBindingArray : WGPUNativeFeature = 196670u
/**
 * Enables the `@coherent` memory decoration on storage buffer variables.
 *
 * Backend mapping:
 * - Vulkan
 * - DX12
 * - Metal (3.2+)
 * - GLES (ES 3.1+ / GL 4.3+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_MemoryDecorationCoherent : WGPUNativeFeature = 196671u
/**
 * Enables the `@volatile` memory decoration on storage buffer variables.
 *
 * Backend mapping:
 * - Vulkan
 * - GLES (ES 3.1+ / GL 4.3+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_MemoryDecorationVolatile : WGPUNativeFeature = 196672u
/**
 * Enables the `@volatile` memory decoration on storage buffer variables.
 *
 * Backend mapping:
 * - Vulkan
 * - GLES (ES 3.1+ / GL 4.3+)
 *
 * This is a native only feature.
 */
const val WGPUNativeFeature_Force32 : WGPUNativeFeature = 2147483647u

typealias WGPULogLevel = UInt
const val WGPULogLevel_Off : WGPULogLevel = 0u
/**
 * Only error messages.
 */
const val WGPULogLevel_Error : WGPULogLevel = 1u
/**
 * Errors and warnings.
 */
const val WGPULogLevel_Warn : WGPULogLevel = 2u
/**
 * Errors, warnings, and informational messages.
 */
const val WGPULogLevel_Info : WGPULogLevel = 3u
/**
 * Errors, warnings, informational, and debug messages.
 */
const val WGPULogLevel_Debug : WGPULogLevel = 4u
/**
 * All messages, including very verbose trace-level output.
 */
const val WGPULogLevel_Trace : WGPULogLevel = 5u
/**
 * All messages, including very verbose trace-level output.
 */
const val WGPULogLevel_Force32 : WGPULogLevel = 2147483647u

typealias WGPUDx12Compiler = UInt
const val WGPUDx12Compiler_Undefined : WGPUDx12Compiler = 0u
/**
 * Use the FXC (D3DCompile) shader compiler.
 *
 * The FXC compiler is old, slow, and unmaintained. However, it doesn't
 * require any additional DLLs to be shipped with the application.
 */
const val WGPUDx12Compiler_Fxc : WGPUDx12Compiler = 1u
/**
 * Use the DXC (DirectX Shader Compiler).
 *
 * The DXC compiler is new, fast, and maintained. However, it requires
 * @c dxcompiler.dll to be available. The path to this DLL can be
 * specified via @ref WGPUInstanceExtras::dxcPath.
 *
 * Minimum supported version: v1.8.2502.
 * Requires WDDM 2.1 (Windows 10 version 1607).
 */
const val WGPUDx12Compiler_Dxc : WGPUDx12Compiler = 2u
/**
 * Use the DXC (DirectX Shader Compiler).
 *
 * The DXC compiler is new, fast, and maintained. However, it requires
 * @c dxcompiler.dll to be available. The path to this DLL can be
 * specified via @ref WGPUInstanceExtras::dxcPath.
 *
 * Minimum supported version: v1.8.2502.
 * Requires WDDM 2.1 (Windows 10 version 1607).
 */
const val WGPUDx12Compiler_Force32 : WGPUDx12Compiler = 2147483647u

typealias WGPUGles3MinorVersion = UInt
const val WGPUGles3MinorVersion_Automatic : WGPUGles3MinorVersion = 0u
/**
 * Request an ES 3.0 context.
 */
const val WGPUGles3MinorVersion_Version0 : WGPUGles3MinorVersion = 1u
/**
 * Request an ES 3.1 context.
 */
const val WGPUGles3MinorVersion_Version1 : WGPUGles3MinorVersion = 2u
/**
 * Request an ES 3.2 context.
 */
const val WGPUGles3MinorVersion_Version2 : WGPUGles3MinorVersion = 3u
/**
 * Request an ES 3.2 context.
 */
const val WGPUGles3MinorVersion_Force32 : WGPUGles3MinorVersion = 2147483647u

typealias WGPUPipelineStatisticName = UInt
const val WGPUPipelineStatisticName_VertexShaderInvocations : WGPUPipelineStatisticName = 0u
/**
 * Number of times the clipper is invoked. This is also the number of
 * triangles output by the vertex shader.
 */
const val WGPUPipelineStatisticName_ClipperInvocations : WGPUPipelineStatisticName = 1u
/**
 * Number of primitives that are not culled by the clipper. This is the
 * number of triangles that are actually on screen and will be rasterized
 * and rendered.
 */
const val WGPUPipelineStatisticName_ClipperPrimitivesOut : WGPUPipelineStatisticName = 2u
/**
 * Number of times the fragment shader is invoked. Accounts for fragment
 * shaders running in 2x2 blocks in order to get derivatives.
 */
const val WGPUPipelineStatisticName_FragmentShaderInvocations : WGPUPipelineStatisticName = 3u
/**
 * Number of times a compute shader is invoked. This will be equivalent
 * to the dispatch count times the workgroup size.
 */
const val WGPUPipelineStatisticName_ComputeShaderInvocations : WGPUPipelineStatisticName = 4u
/**
 * Number of times a compute shader is invoked. This will be equivalent
 * to the dispatch count times the workgroup size.
 */
const val WGPUPipelineStatisticName_Force32 : WGPUPipelineStatisticName = 2147483647u

typealias WGPUNativeQueryType = UInt
const val WGPUNativeQueryType_PipelineStatistics : WGPUNativeQueryType = 196608u
const val WGPUNativeQueryType_Force32 : WGPUNativeQueryType = 2147483647u

typealias WGPUDxcMaxShaderModel = UInt
const val WGPUDxcMaxShaderModel_V6_0 : WGPUDxcMaxShaderModel = 0u
/**
 * Shader Model 6.1
 */
const val WGPUDxcMaxShaderModel_V6_1 : WGPUDxcMaxShaderModel = 1u
/**
 * Shader Model 6.2
 */
const val WGPUDxcMaxShaderModel_V6_2 : WGPUDxcMaxShaderModel = 2u
/**
 * Shader Model 6.3
 */
const val WGPUDxcMaxShaderModel_V6_3 : WGPUDxcMaxShaderModel = 3u
/**
 * Shader Model 6.4
 */
const val WGPUDxcMaxShaderModel_V6_4 : WGPUDxcMaxShaderModel = 4u
/**
 * Shader Model 6.5
 */
const val WGPUDxcMaxShaderModel_V6_5 : WGPUDxcMaxShaderModel = 5u
/**
 * Shader Model 6.6
 */
const val WGPUDxcMaxShaderModel_V6_6 : WGPUDxcMaxShaderModel = 6u
/**
 * Shader Model 6.7
 */
const val WGPUDxcMaxShaderModel_V6_7 : WGPUDxcMaxShaderModel = 7u
/**
 * Shader Model 6.7
 */
const val WGPUDxcMaxShaderModel_Force32 : WGPUDxcMaxShaderModel = 2147483647u

typealias WGPUGLFenceBehaviour = UInt
const val WGPUGLFenceBehaviour_Normal : WGPUGLFenceBehaviour = 0u
/**
 * Fences are short-circuited to always report completion immediately.
 *
 * This solves a specific issue that arose due to a bug in wgpu-core that
 * made many WebGL programs work when they shouldn't have. If you have
 * code that calls @ref wgpuDevicePoll with @c wait=true on WebGL, you
 * may need to enable this option for "wait" to behave how you expect.
 *
 * When this is set, @c wgpuQueueOnCompletedWorkDone callbacks will fire
 * the next time the device is polled, not when work is actually done on
 * the GPU.
 */
const val WGPUGLFenceBehaviour_AutoFinish : WGPUGLFenceBehaviour = 1u
/**
 * Fences are short-circuited to always report completion immediately.
 *
 * This solves a specific issue that arose due to a bug in wgpu-core that
 * made many WebGL programs work when they shouldn't have. If you have
 * code that calls @ref wgpuDevicePoll with @c wait=true on WebGL, you
 * may need to enable this option for "wait" to behave how you expect.
 *
 * When this is set, @c wgpuQueueOnCompletedWorkDone callbacks will fire
 * the next time the device is polled, not when work is actually done on
 * the GPU.
 */
const val WGPUGLFenceBehaviour_Force32 : WGPUGLFenceBehaviour = 2147483647u

typealias WGPUDx12SwapchainKind = UInt
const val WGPUDx12SwapchainKind_Undefined : WGPUDx12SwapchainKind = 0u
/**
 * Use a DXGI swapchain created directly from the window's HWND.
 *
 * This does not support transparency but has better support from
 * developer tooling such as RenderDoc.
 */
const val WGPUDx12SwapchainKind_DxgiFromHwnd : WGPUDx12SwapchainKind = 1u
/**
 * Use a DXGI swapchain created from a DirectComposition visual made
 * automatically from the window's HWND.
 *
 * This creates a single @c IDCompositionVisual over the entire window.
 * Supports transparency. If you want to manage the composition tree
 * yourself, create your own device and composition and pass the relevant
 * visual via the surface target.
 */
const val WGPUDx12SwapchainKind_DxgiFromVisual : WGPUDx12SwapchainKind = 2u
/**
 * Use a DXGI swapchain created from a DirectComposition visual made
 * automatically from the window's HWND.
 *
 * This creates a single @c IDCompositionVisual over the entire window.
 * Supports transparency. If you want to manage the composition tree
 * yourself, create your own device and composition and pass the relevant
 * visual via the surface target.
 */
const val WGPUDx12SwapchainKind_Force32 : WGPUDx12SwapchainKind = 2147483647u

/**
 * Discriminant for @ref WGPUNativeDisplayHandle.
 *
 * Identifies which platform's display connection is stored in the tagged union.
 * Use @ref WGPUNativeDisplayHandleType_None (the default when zero-initialized) when
 * no display handle is needed. Platforms with no display connection data (Windows,
 * macOS, iOS, Android) should use @ref WGPUNativeDisplayHandleType_None.
 */
typealias WGPUNativeDisplayHandleType = UInt
/**
 * No display handle provided.
 */
const val WGPUNativeDisplayHandleType_None : WGPUNativeDisplayHandleType = 0u
/**
 * X11 display connection via Xlib. See @ref WGPUXlibDisplayHandle.
 */
const val WGPUNativeDisplayHandleType_Xlib : WGPUNativeDisplayHandleType = 1u
/**
 * X11 display connection via XCB. See @ref WGPUXcbDisplayHandle.
 */
const val WGPUNativeDisplayHandleType_Xcb : WGPUNativeDisplayHandleType = 2u
/**
 * Wayland display connection. See @ref WGPUWaylandDisplayHandle.
 */
const val WGPUNativeDisplayHandleType_Wayland : WGPUNativeDisplayHandleType = 3u
/**
 * Wayland display connection. See @ref WGPUWaylandDisplayHandle.
 */
const val WGPUNativeDisplayHandleType_Force32 : WGPUNativeDisplayHandleType = 2147483647u

/**
 * Xlib display connection data for @ref WGPUNativeDisplayHandle.
 */
expect interface WGPUXlibDisplayHandle {
    /**
     * Pointer to the X11 @c Display (i.e. @c Display*). Must not be NULL.
     */
    var display: NativeAddress?
    /**
     * X11 screen number.
     */
    var screen: Int
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUXlibDisplayHandle
        fun allocate(allocator: MemoryAllocator): WGPUXlibDisplayHandle
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXlibDisplayHandle) -> Unit): ArrayHolder<WGPUXlibDisplayHandle>
    }
}

/**
 * XCB display connection data for @ref WGPUNativeDisplayHandle.
 */
expect interface WGPUXcbDisplayHandle {
    /**
     * Pointer to the XCB connection (i.e. @c xcb_connection_t*). Must not be NULL.
     */
    var connection: NativeAddress?
    /**
     * X11 screen number.
     */
    var screen: Int
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUXcbDisplayHandle
        fun allocate(allocator: MemoryAllocator): WGPUXcbDisplayHandle
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUXcbDisplayHandle) -> Unit): ArrayHolder<WGPUXcbDisplayHandle>
    }
}

/**
 * Wayland display connection data for @ref WGPUNativeDisplayHandle.
 */
expect interface WGPUWaylandDisplayHandle {
    /**
     * Pointer to the Wayland display (i.e. @c wl_display*). Must not be NULL.
     */
    var display: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUWaylandDisplayHandle
        fun allocate(allocator: MemoryAllocator): WGPUWaylandDisplayHandle
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUWaylandDisplayHandle) -> Unit): ArrayHolder<WGPUWaylandDisplayHandle>
    }
}

/**
 * Platform display connection, passed as a field of @ref WGPUInstanceExtras.
 *
 * This is a tagged union. Set @c type to indicate which variant is active, then
 * populate the corresponding field in @c data. Zero-initialization yields
 * @ref WGPUNativeDisplayHandleType_None, meaning no display handle is provided.
 *
 * Currently required by the GLES backend when presenting on Wayland. Other
 * backends ignore this field. If the instance is created with a display handle,
 * all surfaces created from it must use the same display connection.
 */
expect interface WGPUNativeDisplayHandle {
    var type: WGPUNativeDisplayHandleType
    val xlib: WGPUXlibDisplayHandle?
    fun setXlib(value: WGPUXlibDisplayHandle)
    val xcb: WGPUXcbDisplayHandle?
    fun setXcb(value: WGPUXcbDisplayHandle)
    val wayland: WGPUWaylandDisplayHandle?
    fun setWayland(value: WGPUWaylandDisplayHandle)
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUNativeDisplayHandle
        fun allocate(allocator: MemoryAllocator): WGPUNativeDisplayHandle
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeDisplayHandle) -> Unit): ArrayHolder<WGPUNativeDisplayHandle>
    }
}

expect interface WGPUInstanceExtras {
    var chain: WGPUChainedStruct
    /**
     * Which backends to enable.
     * Zero (@ref WGPUInstanceBackend_All) enables all backends.
     */
    var backends: ULong
    /**
     * Flags controlling debug/validation behavior.
     * See @ref WGPUInstanceFlag for available flags.
     */
    var flags: ULong
    /**
     * Which DX12 shader compiler to use.
     * See @ref WGPUDx12Compiler. Ignored on non-DX12 backends.
     */
    var dx12ShaderCompiler: WGPUDx12Compiler
    /**
     * Which OpenGL ES 3 minor version to request.
     * See @ref WGPUGles3MinorVersion. Ignored on non-GL backends.
     */
    var gles3MinorVersion: WGPUGles3MinorVersion
    /**
     * Controls OpenGL fence synchronization behavior.
     * See @ref WGPUGLFenceBehaviour. Ignored on non-GL backends.
     */
    var glFenceBehaviour: WGPUGLFenceBehaviour
    /**
     * File system path to @c dxcompiler.dll for dynamic DXC loading.
     * Only used when @c dx12ShaderCompiler is @ref WGPUDx12Compiler_Dxc.
     * An empty/undefined string view means the DLL will be searched for
     * on the system PATH.
     */
    var dxcPath: WGPUStringView
    /**
     * Maximum HLSL shader model version that DXC should target.
     * See @ref WGPUDxcMaxShaderModel. Only used with the DXC compiler.
     */
    var dxcMaxShaderModel: WGPUDxcMaxShaderModel
    /**
     * Which DX12 presentation system (swapchain kind) to use.
     * See @ref WGPUDx12SwapchainKind. Ignored on non-DX12 backends.
     */
    var dx12PresentationSystem: WGPUDx12SwapchainKind
    var budgetForDeviceCreation: NativeAddress?
    var budgetForDeviceLoss: NativeAddress?
    /**
     * Platform display connection to associate with this instance.
     * Zero-initialized yields @ref WGPUNativeDisplayHandleType_None (no handle).
     */
    var displayHandle: WGPUNativeDisplayHandle
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUInstanceExtras
        fun allocate(allocator: MemoryAllocator): WGPUInstanceExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceExtras) -> Unit): ArrayHolder<WGPUInstanceExtras>
    }
}

expect interface WGPUDeviceExtras {
    var chain: WGPUChainedStruct
    /**
     * File system path for API trace output.
     *
     * When set to a non-empty path, wgpu will record all API calls to
     * the given directory, which can later be replayed for debugging.
     * An empty/undefined string view disables tracing.
     */
    var tracePath: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUDeviceExtras
        fun allocate(allocator: MemoryAllocator): WGPUDeviceExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUDeviceExtras) -> Unit): ArrayHolder<WGPUDeviceExtras>
    }
}

expect interface WGPUNativeLimits {
    /**
     * This struct chain is used as mutable in some places and immutable in others.
     */
    var chain: WGPUChainedStruct
    /**
     * Maximum number of live non-sampler bindings.
     *
     * Default is 1,000,000. Only meaningful on D3D12.
     *
     * @b Warning: On integrated GPUs, large values can cause significant
     * system RAM consumption.
     */
    var maxNonSamplerBindings: UInt
    /**
     * Maximum number of individual resources within binding arrays that can be accessed
     * in a single shader stage. Applies to all types of bindings except samplers.
     */
    var maxBindingArrayElementsPerShaderStage: UInt
    /**
     * Maximum number of individual samplers within binding arrays that
     * can be accessed in a single shader stage.
     */
    var maxBindingArraySamplerElementsPerShaderStage: UInt
    /**
     * The maximum number of views that can be used in multiview rendering.
     */
    var maxMultiviewViewCount: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUNativeLimits
        fun allocate(allocator: MemoryAllocator): WGPUNativeLimits
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUNativeLimits) -> Unit): ArrayHolder<WGPUNativeLimits>
    }
}

expect interface WGPUShaderDefine {
    var name: WGPUStringView
    /**
     * The value of the preprocessor macro (e.g. @c "1").
     */
    var value: WGPUStringView
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderDefine
        fun allocate(allocator: MemoryAllocator): WGPUShaderDefine
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderDefine) -> Unit): ArrayHolder<WGPUShaderDefine>
    }
}

expect interface WGPUShaderSourceGLSL {
    var chain: WGPUChainedStruct
    /**
     * The shader stage this GLSL source targets.
     */
    var stage: ULong
    /**
     * GLSL source code.
     */
    var code: WGPUStringView
    /**
     * Number of entries in @c defines.
     */
    var defineCount: UInt
    var defines: WGPUShaderDefine?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderSourceGLSL
        fun allocate(allocator: MemoryAllocator): WGPUShaderSourceGLSL
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderSourceGLSL) -> Unit): ArrayHolder<WGPUShaderSourceGLSL>
    }
}

expect interface WGPUShaderModuleDescriptorSpirV {
    var label: WGPUStringView
    /**
     * Number of 32-bit words in @c source.
     */
    var sourceSize: UInt
    var source: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUShaderModuleDescriptorSpirV
        fun allocate(allocator: MemoryAllocator): WGPUShaderModuleDescriptorSpirV
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUShaderModuleDescriptorSpirV) -> Unit): ArrayHolder<WGPUShaderModuleDescriptorSpirV>
    }
}

expect interface WGPURegistryReport {
    var numAllocated: ULong
    var numKeptFromUser: ULong
    var numReleasedFromUser: ULong
    var elementSize: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPURegistryReport
        fun allocate(allocator: MemoryAllocator): WGPURegistryReport
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPURegistryReport) -> Unit): ArrayHolder<WGPURegistryReport>
    }
}

expect interface WGPUHubReport {
    var adapters: WGPURegistryReport
    var devices: WGPURegistryReport
    var queues: WGPURegistryReport
    var pipelineLayouts: WGPURegistryReport
    var shaderModules: WGPURegistryReport
    var bindGroupLayouts: WGPURegistryReport
    var bindGroups: WGPURegistryReport
    var commandBuffers: WGPURegistryReport
    var renderBundles: WGPURegistryReport
    var renderPipelines: WGPURegistryReport
    var computePipelines: WGPURegistryReport
    var pipelineCaches: WGPURegistryReport
    var querySets: WGPURegistryReport
    var buffers: WGPURegistryReport
    var textures: WGPURegistryReport
    var textureViews: WGPURegistryReport
    var samplers: WGPURegistryReport
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUHubReport
        fun allocate(allocator: MemoryAllocator): WGPUHubReport
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUHubReport) -> Unit): ArrayHolder<WGPUHubReport>
    }
}

expect interface WGPUGlobalReport {
    var surfaces: WGPURegistryReport
    /**
     * Statistics for all other resource types, grouped by backend hub.
     */
    var hub: WGPUHubReport
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUGlobalReport
        fun allocate(allocator: MemoryAllocator): WGPUGlobalReport
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUGlobalReport) -> Unit): ArrayHolder<WGPUGlobalReport>
    }
}

expect interface WGPUInstanceEnumerateAdapterOptions {
    var nextInChain: WGPUChainedStruct?
    var backends: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUInstanceEnumerateAdapterOptions
        fun allocate(allocator: MemoryAllocator): WGPUInstanceEnumerateAdapterOptions
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUInstanceEnumerateAdapterOptions) -> Unit): ArrayHolder<WGPUInstanceEnumerateAdapterOptions>
    }
}

expect interface WGPUBindGroupEntryExtras {
    var chain: WGPUChainedStruct
    var buffers: NativeAddress?
    var bufferCount: ULong
    var samplers: NativeAddress?
    var samplerCount: ULong
    var textureViews: NativeAddress?
    var textureViewCount: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupEntryExtras
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupEntryExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupEntryExtras>
    }
}

expect interface WGPUBindGroupLayoutEntryExtras {
    var chain: WGPUChainedStruct
    /**
     * Number of resources in this binding array slot. Corresponds to the
     * array size in the shader (e.g. @c binding_array<T, @c N>).
     */
    var count: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUBindGroupLayoutEntryExtras
        fun allocate(allocator: MemoryAllocator): WGPUBindGroupLayoutEntryExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUBindGroupLayoutEntryExtras) -> Unit): ArrayHolder<WGPUBindGroupLayoutEntryExtras>
    }
}

expect interface WGPUQuerySetDescriptorExtras {
    var chain: WGPUChainedStruct
    var pipelineStatistics: NativeAddress?
    var pipelineStatisticCount: ULong
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUQuerySetDescriptorExtras
        fun allocate(allocator: MemoryAllocator): WGPUQuerySetDescriptorExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUQuerySetDescriptorExtras) -> Unit): ArrayHolder<WGPUQuerySetDescriptorExtras>
    }
}

expect interface WGPUSurfaceConfigurationExtras {
    var chain: WGPUChainedStruct
    /**
     * Desired maximum number of frames in flight (i.e. the number of monitor
     * refreshes between @c wgpuSurfaceGetCurrentTexture and presentation).
     *
     * - 1: Minimize latency (CPU and GPU cannot run in parallel).
     * - 2: Balance between latency and throughput (the default).
     * - 3+: Maximize throughput.
     */
    var desiredMaximumFrameLatency: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceConfigurationExtras
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceConfigurationExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceConfigurationExtras) -> Unit): ArrayHolder<WGPUSurfaceConfigurationExtras>
    }
}

/**
 * Chained in @ref WGPUSurfaceDescriptor to make a @ref WGPUSurface wrapping a WinUI [`SwapChainPanel`](https://learn.microsoft.com/en-us/windows/windows-app-sdk/api/winrt/microsoft.ui.xaml.controls.swapchainpanel).
 */
expect interface WGPUSurfaceSourceSwapChainPanel {
    var chain: WGPUChainedStruct
    /**
     * A pointer to the [`ISwapChainPanelNative`](https://learn.microsoft.com/en-us/windows/windows-app-sdk/api/win32/microsoft.ui.xaml.media.dxinterop/nn-microsoft-ui-xaml-media-dxinterop-iswapchainpanelnative)
     * interface of the SwapChainPanel that will be wrapped by the @ref WGPUSurface.
     */
    var panelNative: NativeAddress?
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSurfaceSourceSwapChainPanel
        fun allocate(allocator: MemoryAllocator): WGPUSurfaceSourceSwapChainPanel
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSurfaceSourceSwapChainPanel) -> Unit): ArrayHolder<WGPUSurfaceSourceSwapChainPanel>
    }
}

typealias WGPUPolygonMode = UInt
const val WGPUPolygonMode_Fill : WGPUPolygonMode = 0u
/**
 * Polygons are drawn as line segments (wireframe).
 * Requires @ref WGPUNativeFeature_PolygonModeLine.
 */
const val WGPUPolygonMode_Line : WGPUPolygonMode = 1u
/**
 * Polygons are drawn as points (vertices only).
 * Requires @ref WGPUNativeFeature_PolygonModePoint.
 */
const val WGPUPolygonMode_Point : WGPUPolygonMode = 2u

expect interface WGPUPrimitiveStateExtras {
    var chain: WGPUChainedStruct
    /**
     * Controls the way each polygon is rasterized.
     * See @ref WGPUPolygonMode. Defaults to @ref WGPUPolygonMode_Fill.
     */
    var polygonMode: WGPUPolygonMode
    /**
     * If set to true, the primitives are rendered with conservative
     * overestimation. Only valid when @c polygonMode is
     * @ref WGPUPolygonMode_Fill.
     * Requires @ref WGPUNativeFeature_ConservativeRasterization.
     */
    var conservative: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUPrimitiveStateExtras
        fun allocate(allocator: MemoryAllocator): WGPUPrimitiveStateExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUPrimitiveStateExtras) -> Unit): ArrayHolder<WGPUPrimitiveStateExtras>
    }
}

typealias WGPUNativeTextureFormat = UInt
/**
 * YUV 4:2:0 chroma subsampled format (NV12).
 * Plane 0 contains R8Unorm luminance (Y), Plane 1 contains Rg8Unorm
 * chrominance (UV) at half width and half height.
 * Requires @ref WGPUNativeFeature_TextureFormatNv12.
 */
const val WGPUNativeTextureFormat_NV12 : WGPUNativeTextureFormat = 196615u
/**
 * YUV 4:2:0 with 10 bits used from 16-bit channels (P010).
 * Plane 0 contains R16Unorm luminance (Y), Plane 1 contains Rg16Unorm
 * chrominance (UV) at half width and half height.
 */
const val WGPUNativeTextureFormat_P010 : WGPUNativeTextureFormat = 196616u

expect interface WGPUImageSubresourceRange {
    var aspect: WGPUTextureAspect
    var baseMipLevel: UInt
    var mipLevelCount: UInt
    var baseArrayLayer: UInt
    var arrayLayerCount: UInt
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUImageSubresourceRange
        fun allocate(allocator: MemoryAllocator): WGPUImageSubresourceRange
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUImageSubresourceRange) -> Unit): ArrayHolder<WGPUImageSubresourceRange>
    }
}

typealias WGPUNativeAddressMode = UInt
const val WGPUNativeAddressMode_ClampToBorder : WGPUNativeAddressMode = 4u
const val WGPUNativeAddressMode_Force32 : WGPUNativeAddressMode = 2147483647u

typealias WGPUSamplerBorderColor = UInt
const val WGPUSamplerBorderColor_Undefined : WGPUSamplerBorderColor = 0u
const val WGPUSamplerBorderColor_TransparentBlack : WGPUSamplerBorderColor = 1u
const val WGPUSamplerBorderColor_OpaqueBlack : WGPUSamplerBorderColor = 2u
const val WGPUSamplerBorderColor_OpaqueWhite : WGPUSamplerBorderColor = 3u
const val WGPUSamplerBorderColor_Zero : WGPUSamplerBorderColor = 4u
const val WGPUSamplerBorderColor_Force32 : WGPUSamplerBorderColor = 2147483647u

expect interface WGPUSamplerDescriptorExtras {
    var chain: WGPUChainedStruct
    var samplerBorderColor: WGPUSamplerBorderColor
    val handler: NativeAddress
    companion object {
        operator fun invoke(address: NativeAddress): WGPUSamplerDescriptorExtras
        fun allocate(allocator: MemoryAllocator): WGPUSamplerDescriptorExtras
        fun allocateArray(allocator: MemoryAllocator, size: UInt, provider: (UInt, WGPUSamplerDescriptorExtras) -> Unit): ArrayHolder<WGPUSamplerDescriptorExtras>
    }
}

expect fun wgpuGenerateReport(instance: WGPUInstance?, report: WGPUGlobalReport?): Unit

expect fun wgpuInstanceEnumerateAdapters(instance: WGPUInstance?, options: WGPUInstanceEnumerateAdapterOptions?, adapters: NativeAddress?): ULong

expect fun wgpuQueueSubmitForIndex(queue: WGPUQueue?, commandCount: ULong, commands: NativeAddress?): ULong

expect fun wgpuQueueGetTimestampPeriod(queue: WGPUQueue?): Float

expect fun wgpuDevicePoll(device: WGPUDevice?, wait: UInt, submissionIndex: NativeAddress?): UInt

expect fun wgpuDeviceCreateShaderModuleSpirV(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptorSpirV?): WGPUShaderModule?

expect fun wgpuSetLogCallback(callback: NativeAddress?, userdata: NativeAddress?): Unit

expect fun wgpuSetLogLevel(level: WGPULogLevel): Unit

expect fun wgpuGetVersion(): UInt

/**
 * Returns the backend-native `id<MTLDevice>` as an opaque pointer.
 *
 * The returned pointer is borrowed and remains valid only while `device` is alive.
 * Ownership is retained by wgpu-native; callers must not release or destroy it.
 * Returns NULL when the active backend is not Metal or when the handle is unavailable.
 */
expect fun wgpuDeviceGetNativeMetalDevice(device: WGPUDevice?): NativeAddress?

/**
 * Returns the backend-native `id<MTLCommandQueue>` as an opaque pointer.
 *
 * The returned pointer is borrowed and remains valid only while `queue` is alive.
 * Ownership is retained by wgpu-native; callers must not release or destroy it.
 * Returns NULL when the active backend is not Metal or when the handle is unavailable.
 */
expect fun wgpuQueueGetNativeMetalCommandQueue(queue: WGPUQueue?): NativeAddress?

/**
 * Returns the backend-native `id<MTLTexture>` as an opaque pointer.
 *
 * The returned pointer is borrowed and remains valid only while `texture` is alive.
 * Ownership is retained by wgpu-native; callers must not release or destroy it.
 * Returns NULL when the active backend is not Metal or when the handle is unavailable.
 */
expect fun wgpuTextureGetNativeMetalTexture(texture: WGPUTexture?): NativeAddress?

expect fun wgpuRenderPassEncoderMultiDrawIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit

expect fun wgpuRenderPassEncoderMultiDrawIndexedIndirect(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count: UInt): Unit

expect fun wgpuRenderPassEncoderMultiDrawIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit

expect fun wgpuRenderPassEncoderMultiDrawIndexedIndirectCount(encoder: WGPURenderPassEncoder?, buffer: WGPUBuffer?, offset: ULong, count_buffer: WGPUBuffer?, count_buffer_offset: ULong, max_count: UInt): Unit

expect fun wgpuComputePassEncoderBeginPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit

expect fun wgpuComputePassEncoderEndPipelineStatisticsQuery(computePassEncoder: WGPUComputePassEncoder?): Unit

expect fun wgpuRenderPassEncoderBeginPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit

expect fun wgpuRenderPassEncoderEndPipelineStatisticsQuery(renderPassEncoder: WGPURenderPassEncoder?): Unit

expect fun wgpuComputePassEncoderWriteTimestamp(computePassEncoder: WGPUComputePassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit

expect fun wgpuRenderPassEncoderWriteTimestamp(renderPassEncoder: WGPURenderPassEncoder?, querySet: WGPUQuerySet?, queryIndex: UInt): Unit

expect fun wgpuDeviceStartGraphicsDebuggerCapture(device: WGPUDevice?): UInt

expect fun wgpuDeviceStopGraphicsDebuggerCapture(device: WGPUDevice?): Unit

expect fun wgpuCommandEncoderClearTexture(commandEncoder: WGPUCommandEncoder?, texture: WGPUTexture?, range: WGPUImageSubresourceRange?): Unit

expect fun wgpuDeviceCreateShaderModuleTrusted(device: WGPUDevice?, descriptor: WGPUShaderModuleDescriptor?, runtimeChecks: ULong): WGPUShaderModule?

/**
 * @}
 */
fun interface WGPUProc : Callback {
    fun invoke()
    companion object
}

@CallbackRuntimeApi
internal val WGPUProcType: CallbackType<WGPUProc> = CallbackType(
    canonicalId = "typedef:WGPUProc",
    hasRoutingUserdata = false,
)

expect fun WGPUProc.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUProc,
): CallbackRegistration<WGPUProc>

@CallbackRuntimeApi
internal expect fun WGPUProc.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUProc,
): PreparedCallbackRegistration<WGPUProc>

@UnsafeCallbackRearmApi
expect fun WGPUProc.Companion.rearmAfterNativeQuiescence(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUProc,
): CallbackRegistration<WGPUProc>

/**
 * See also @ref CallbackError.
 *
 * @param message
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUBufferMapCallback : Callback {
    fun invoke(
        status: WGPUMapAsyncStatus,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUBufferMapCallbackType: CallbackType<WGPUBufferMapCallback> = CallbackType(
    canonicalId = "typedef:WGPUBufferMapCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUBufferMapCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUBufferMapCallback,
): CallbackRegistration<WGPUBufferMapCallback>

@CallbackRuntimeApi
internal expect fun WGPUBufferMapCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUBufferMapCallback,
): PreparedCallbackRegistration<WGPUBufferMapCallback>

/**
 * See also @ref CallbackError.
 *
 * @param compilationInfo
 * This argument contains multiple @ref ImplementationAllocatedStructChain roots.
 * Arbitrary chains must be handled gracefully by the application!
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUCompilationInfoCallback : Callback {
    fun invoke(
        status: WGPUCompilationInfoRequestStatus,
        compilationInfo: NativeAddress?,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUCompilationInfoCallbackType: CallbackType<WGPUCompilationInfoCallback> = CallbackType(
    canonicalId = "typedef:WGPUCompilationInfoCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUCompilationInfoCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCompilationInfoCallback,
): CallbackRegistration<WGPUCompilationInfoCallback>

@CallbackRuntimeApi
internal expect fun WGPUCompilationInfoCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCompilationInfoCallback,
): PreparedCallbackRegistration<WGPUCompilationInfoCallback>

/**
 * See also @ref CallbackError.
 *
 * @param pipeline
 * This parameter is @ref PassedWithOwnership.
 */
fun interface WGPUCreateComputePipelineAsyncCallback : Callback {
    fun invoke(
        status: WGPUCreatePipelineAsyncStatus,
        pipeline: WGPUComputePipeline?,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUCreateComputePipelineAsyncCallbackType: CallbackType<WGPUCreateComputePipelineAsyncCallback> = CallbackType(
    canonicalId = "typedef:WGPUCreateComputePipelineAsyncCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUCreateComputePipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCreateComputePipelineAsyncCallback,
): CallbackRegistration<WGPUCreateComputePipelineAsyncCallback>

@CallbackRuntimeApi
internal expect fun WGPUCreateComputePipelineAsyncCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCreateComputePipelineAsyncCallback,
): PreparedCallbackRegistration<WGPUCreateComputePipelineAsyncCallback>

/**
 * See also @ref CallbackError.
 *
 * @param pipeline
 * This parameter is @ref PassedWithOwnership.
 */
fun interface WGPUCreateRenderPipelineAsyncCallback : Callback {
    fun invoke(
        status: WGPUCreatePipelineAsyncStatus,
        pipeline: WGPURenderPipeline?,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUCreateRenderPipelineAsyncCallbackType: CallbackType<WGPUCreateRenderPipelineAsyncCallback> = CallbackType(
    canonicalId = "typedef:WGPUCreateRenderPipelineAsyncCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUCreateRenderPipelineAsyncCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCreateRenderPipelineAsyncCallback,
): CallbackRegistration<WGPUCreateRenderPipelineAsyncCallback>

@CallbackRuntimeApi
internal expect fun WGPUCreateRenderPipelineAsyncCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUCreateRenderPipelineAsyncCallback,
): PreparedCallbackRegistration<WGPUCreateRenderPipelineAsyncCallback>

/**
 * See also @ref CallbackError.
 *
 * @param device
 * Pointer to the device which was lost. This is always a non-null pointer.
 * The pointed-to @ref WGPUDevice will be null if, and only if, either:
 * (1) The `reason` is @ref WGPUDeviceLostReason_FailedCreation.
 * (2) The last ref of the device has been (or is being) released: see @ref DeviceRelease.
 * This parameter is @ref PassedWithoutOwnership.
 *
 * @param reason
 * An error code explaining why the device was lost.
 *
 * @param message
 * A @ref LocalizableHumanReadableMessageString describing why the device was lost.
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUDeviceLostCallback : Callback {
    fun invoke(
        device: NativeAddress?,
        reason: WGPUDeviceLostReason,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUDeviceLostCallbackType: CallbackType<WGPUDeviceLostCallback> = CallbackType(
    canonicalId = "typedef:WGPUDeviceLostCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUDeviceLostCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUDeviceLostCallback,
): CallbackRegistration<WGPUDeviceLostCallback>

@CallbackRuntimeApi
internal expect fun WGPUDeviceLostCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUDeviceLostCallback,
): PreparedCallbackRegistration<WGPUDeviceLostCallback>

/**
 * See also @ref CallbackError.
 *
 * @param status
 * See @ref WGPUPopErrorScopeStatus.
 *
 * @param type
 * The type of the error caught by the scope, or @ref WGPUErrorType_NoError if there was none.
 * If the `status` is not @ref WGPUPopErrorScopeStatus_Success, this is @ref WGPUErrorType_NoError.
 *
 * @param message
 * If the `status` is not @ref WGPUPopErrorScopeStatus_Success **or**
 * the `type` is not @ref WGPUErrorType_NoError, this is a non-empty
 * @ref LocalizableHumanReadableMessageString;
 * otherwise, this is an empty string.
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUPopErrorScopeCallback : Callback {
    fun invoke(
        status: WGPUPopErrorScopeStatus,
        type: WGPUErrorType,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUPopErrorScopeCallbackType: CallbackType<WGPUPopErrorScopeCallback> = CallbackType(
    canonicalId = "typedef:WGPUPopErrorScopeCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUPopErrorScopeCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUPopErrorScopeCallback,
): CallbackRegistration<WGPUPopErrorScopeCallback>

@CallbackRuntimeApi
internal expect fun WGPUPopErrorScopeCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUPopErrorScopeCallback,
): PreparedCallbackRegistration<WGPUPopErrorScopeCallback>

/**
 * See also @ref CallbackError.
 *
 * @param status
 * See @ref WGPUQueueWorkDoneStatus.
 *
 * @param message
 * If the `status` is not @ref WGPUQueueWorkDoneStatus_Success,
 * this is a non-empty @ref LocalizableHumanReadableMessageString;
 * otherwise, this is an empty string.
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUQueueWorkDoneCallback : Callback {
    fun invoke(
        status: WGPUQueueWorkDoneStatus,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUQueueWorkDoneCallbackType: CallbackType<WGPUQueueWorkDoneCallback> = CallbackType(
    canonicalId = "typedef:WGPUQueueWorkDoneCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUQueueWorkDoneCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUQueueWorkDoneCallback,
): CallbackRegistration<WGPUQueueWorkDoneCallback>

@CallbackRuntimeApi
internal expect fun WGPUQueueWorkDoneCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUQueueWorkDoneCallback,
): PreparedCallbackRegistration<WGPUQueueWorkDoneCallback>

/**
 * See also @ref CallbackError.
 *
 * @param adapter
 * This parameter is @ref PassedWithOwnership.
 *
 * @param message
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPURequestAdapterCallback : Callback {
    fun invoke(
        status: WGPURequestAdapterStatus,
        adapter: WGPUAdapter?,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPURequestAdapterCallbackType: CallbackType<WGPURequestAdapterCallback> = CallbackType(
    canonicalId = "typedef:WGPURequestAdapterCallback",
    hasRoutingUserdata = true,
)

expect fun WGPURequestAdapterCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPURequestAdapterCallback,
): CallbackRegistration<WGPURequestAdapterCallback>

@CallbackRuntimeApi
internal expect fun WGPURequestAdapterCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPURequestAdapterCallback,
): PreparedCallbackRegistration<WGPURequestAdapterCallback>

/**
 * See also @ref CallbackError.
 *
 * @param device
 * This parameter is @ref PassedWithOwnership.
 *
 * @param message
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPURequestDeviceCallback : Callback {
    fun invoke(
        status: WGPURequestDeviceStatus,
        device: WGPUDevice?,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPURequestDeviceCallbackType: CallbackType<WGPURequestDeviceCallback> = CallbackType(
    canonicalId = "typedef:WGPURequestDeviceCallback",
    hasRoutingUserdata = true,
)

expect fun WGPURequestDeviceCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPURequestDeviceCallback,
): CallbackRegistration<WGPURequestDeviceCallback>

@CallbackRuntimeApi
internal expect fun WGPURequestDeviceCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPURequestDeviceCallback,
): PreparedCallbackRegistration<WGPURequestDeviceCallback>

/**
 * See also @ref CallbackError.
 *
 * @param device
 * This parameter is @ref PassedWithoutOwnership.
 *
 * @param message
 * This parameter is @ref PassedWithoutOwnership.
 */
fun interface WGPUUncapturedErrorCallback : Callback {
    fun invoke(
        device: NativeAddress?,
        type: WGPUErrorType,
        message: WGPUStringView,
        userdata1: NativeAddress?,
    )

    companion object
}

@CallbackRuntimeApi
internal val WGPUUncapturedErrorCallbackType: CallbackType<WGPUUncapturedErrorCallback> = CallbackType(
    canonicalId = "typedef:WGPUUncapturedErrorCallback",
    hasRoutingUserdata = true,
)

expect fun WGPUUncapturedErrorCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUUncapturedErrorCallback,
): CallbackRegistration<WGPUUncapturedErrorCallback>

@CallbackRuntimeApi
internal expect fun WGPUUncapturedErrorCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPUUncapturedErrorCallback,
): PreparedCallbackRegistration<WGPUUncapturedErrorCallback>

fun interface WGPULogCallback : Callback {
    fun invoke(level: WGPULogLevel, message: WGPUStringView)
    companion object
}

@CallbackRuntimeApi
internal val WGPULogCallbackType: CallbackType<WGPULogCallback> = CallbackType(
    canonicalId = "typedef:WGPULogCallback",
    hasRoutingUserdata = true,
)

expect fun WGPULogCallback.Companion.register(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPULogCallback,
): CallbackRegistration<WGPULogCallback>

@CallbackRuntimeApi
internal expect fun WGPULogCallback.Companion.prepare(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPULogCallback,
): PreparedCallbackRegistration<WGPULogCallback>

internal expect fun wgpuSetLogCallbackCallbackBindingPreflight(): (NativeAddress?, NativeAddress?) -> Unit

@OptIn(CallbackRuntimeApi::class)
fun wgpuSetLogCallback(
    policy: CallbackPolicy,
    onError: CallbackExceptionHandler = CallbackExceptionHandler.Default,
    callback: WGPULogCallback,
): CallbackRegistration<WGPULogCallback> {
    val preparedCall = wgpuSetLogCallbackCallbackBindingPreflight()
    val prepared = WGPULogCallback.prepare(
        policy = policy,
        onError = onError,
        callback = callback,
    )
    return CallbackRuntime.activateForNativeCall(prepared) { registration ->
        preparedCall(registration.callback, registration.userdata)
    }
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUBufferMapCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUBufferMapCallback>,
    userdata1: NativeAddress? = null,
): WGPUBufferMapCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUCompilationInfoCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUCompilationInfoCallback>,
    userdata1: NativeAddress? = null,
): WGPUCompilationInfoCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUCreateComputePipelineAsyncCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUCreateComputePipelineAsyncCallback>,
    userdata1: NativeAddress? = null,
): WGPUCreateComputePipelineAsyncCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUCreateRenderPipelineAsyncCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUCreateRenderPipelineAsyncCallback>,
    userdata1: NativeAddress? = null,
): WGPUCreateRenderPipelineAsyncCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUDeviceLostCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUDeviceLostCallback>,
    userdata1: NativeAddress? = null,
): WGPUDeviceLostCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUPopErrorScopeCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUPopErrorScopeCallback>,
    userdata1: NativeAddress? = null,
): WGPUPopErrorScopeCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUQueueWorkDoneCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPUQueueWorkDoneCallback>,
    userdata1: NativeAddress? = null,
): WGPUQueueWorkDoneCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPURequestAdapterCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPURequestAdapterCallback>,
    userdata1: NativeAddress? = null,
): WGPURequestAdapterCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPURequestDeviceCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    mode: WGPUCallbackMode,
    registration: CallbackRegistration<WGPURequestDeviceCallback>,
    userdata1: NativeAddress? = null,
): WGPURequestDeviceCallbackInfo {
    require(
        mode == WGPUCallbackMode_WaitAnyOnly ||
            mode == WGPUCallbackMode_AllowProcessEvents ||
            mode == WGPUCallbackMode_AllowSpontaneous,
    )
    val info = allocate(allocator)
    info.mode = mode
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

/**
 * CONSUMED_DURING_CALL: the owning native call copies the callback-info value or containing descriptor, so the allocator scope may close after the call while the registration remains live.
 *
 * This factory does not own [registration].
 */
fun WGPUUncapturedErrorCallbackInfo.Companion.allocate(
    allocator: MemoryAllocator,
    registration: CallbackRegistration<WGPUUncapturedErrorCallback>,
    userdata1: NativeAddress? = null,
): WGPUUncapturedErrorCallbackInfo {
    val info = allocate(allocator)
    info.callback = registration.callback
    info.userdata2 = registration.userdata
    info.userdata1 = userdata1
    return info
}

