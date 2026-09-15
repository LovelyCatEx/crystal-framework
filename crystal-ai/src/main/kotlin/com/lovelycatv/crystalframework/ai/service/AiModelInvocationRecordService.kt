/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
        sessionId: String?,
        clientIp: String?,
        userAgent: String?,
        groupId: Long,
    )
}
