package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse

interface AiModelInvocationRecordService {
    suspend fun recordSyncInvocation(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
    )

    suspend fun recordStreamInvocation(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
        timeToFirstTokenMs: Long,
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
