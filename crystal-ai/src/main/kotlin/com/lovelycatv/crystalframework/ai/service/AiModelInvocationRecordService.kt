package com.lovelycatv.crystalframework.ai.service

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext

interface AiModelInvocationRecordService {
    suspend fun recordInvocationFromContext(
        context: AiInvocationContext,
        model: AiModelEntity,
    )

    suspend fun recordFailedInvocation(
        userId: Long,
        tenantId: Long?,
        modelId: Long,
        providerId: Long,
        messageCount: Int,
        durationMs: Long,
        errorCode: String,
        errorMessage: String,
        isStreaming: Boolean,
    )
}
