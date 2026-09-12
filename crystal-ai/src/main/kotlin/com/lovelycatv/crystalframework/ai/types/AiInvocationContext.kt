package com.lovelycatv.crystalframework.ai.types

import com.lovelycatv.vertex.ai.llm.ChatResponse

/**
 * Everything an audit record needs about one chat completion.
 *
 * [usage] and [stopReason] are carried resolved rather than re-parsed from [rawResponseBody]: the
 * caller has already merged them across the stream, and VertexLib parsed them through the
 * provider's response config. For a stream the raw body of a single frame is not a complete
 * response, so parsing it again would under-report.
 *
 * Handed straight to `AiModelInvocationRecordService` within the same process. It used to be
 * `Serializable`, which it no longer is now that it carries a library type.
 */
data class AiInvocationContext(
    val userId: Long,
    val tenantId: Long?,
    val modelId: Long,
    val providerId: Long,
    val messageCount: Int,
    val toolCallsCount: Int,
    val responseMetadataId: String,
    val durationMs: Long,
    val isStreaming: Boolean,
    val timeToFirstTokenMs: Long?,
    val usage: ChatResponse.Usage,
    val stopReason: String?,
    val rawRequestBody: String?,
    val rawResponseBody: String?,
)
