package com.lovelycatv.crystalframework.ai.types

import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse
import java.io.Serializable

data class AiInvocationContext(
    val userId: Long,
    val tenantId: Long?,
    val modelId: Long,
    val providerId: Long,
    val messageCount: Int,
    val responseMetadataId: String,
    val durationMs: Long,
    val isStreaming: Boolean,
    val timeToFirstTokenMs: Long?,
    val rawRequestBody: String?,
    val rawResponseBody: String?,
) : Serializable {
    companion object {
        fun fromInvocation(
            userId: Long,
            tenantId: Long?,
            modelId: Long,
            providerId: Long,
            messages: List<Message>,
            response: ChatResponse,
            durationMs: Long,
            isStreaming: Boolean,
            timeToFirstTokenMs: Long?,
            rawRequestBody: String?,
            rawResponseBody: String?,
        ): AiInvocationContext {
            return AiInvocationContext(
                userId = userId,
                tenantId = tenantId,
                modelId = modelId,
                providerId = providerId,
                messageCount = messages.size,
                responseMetadataId = response.metadata.id,
                durationMs = durationMs,
                isStreaming = isStreaming,
                timeToFirstTokenMs = timeToFirstTokenMs,
                rawRequestBody = rawRequestBody,
                rawResponseBody = rawResponseBody,
            )
        }
    }
}
