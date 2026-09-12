package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse

interface AiModelInvocationRecordService {
    suspend fun recordInvocation(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
        isStreaming: Boolean,
        timeToFirstTokenMs: Long?,
        rawRequestBody: String?,
        rawResponseBody: String?,
    )

    suspend fun recordInvocationFromContext(
        context: AiInvocationContext,
        model: AiModelEntity,
        provider: AiProviderEntity,
    )

    suspend fun recordFailedInvocation(
        userId: Long,
        tenantId: Long?,
        modelId: Long,
        providerId: Long,
        messages: List<Message>,
        durationMs: Long,
        errorCode: String,
        errorMessage: String,
        isStreaming: Boolean,
    )
}
