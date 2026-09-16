/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.service.impl

import com.jayway.jsonpath.JsonPath
import com.lovelycatv.crystalframework.ai.constants.AiInvocationRecordConstants
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupManagerService
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import com.lovelycatv.crystalframework.ai.types.AiModelInvocationStatus
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.economy.constants.CurrencyConstants
import com.lovelycatv.crystalframework.economy.service.EconomyWalletService
import com.lovelycatv.crystalframework.economy.types.EconomyChargeResult
import com.lovelycatv.crystalframework.economy.types.EconomyReferenceType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class AiModelInvocationRecordServiceImpl(
    private val repository: AiModelInvocationRecordRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val aiUserGroupManagerService: AiUserGroupManagerService,
    private val economyWalletService: EconomyWalletService,
) : AiModelInvocationRecordService {
    companion object {
        private val logger = LoggerFactory.getLogger(AiModelInvocationRecordServiceImpl::class.java)
    }

    override suspend fun recordInvocationFromContext(
        context: AiInvocationContext,
        model: AiModelEntity,
    ) {
        val promptTokens = context.usage.promptTokens
        val completionTokens = context.usage.completionTokens
        val cachedPromptTokens = context.usage.cachedPromptTokens
        val cacheCreationTokens = context.usage.cacheCreationTokens
        val reasoningTokens = context.usage.reasoningTokens

        val queueWaitMs = extractQueueWaitMs(context.rawResponseBody)

        val requestSizeBytes = context.rawRequestBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()
        val responseSizeBytes = context.responseSizeBytes
            ?: context.rawResponseBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()

        val promptPrice = model.inputPricePerMillion.toDouble()
        val completionPrice = model.outputPricePerMillion.toDouble()
        val cacheReadPrice = model.cacheReadPricePerMillion?.toDouble() ?: 0.0
        val cacheWritePrice = model.cacheWritePricePerMillion?.toDouble() ?: 0.0

        val rawCost = calculateCost(
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            cachedPromptTokens = cachedPromptTokens,
            cacheCreationTokens = cacheCreationTokens,
            promptPrice = promptPrice,
            completionPrice = completionPrice,
            cacheReadPrice = cacheReadPrice,
            cacheWritePrice = cacheWritePrice,
        )

        val tokensPerSecond = if (context.durationMs > 0) {
            (completionTokens.toDouble() / context.durationMs) * 1000
        } else null

        val modelRequestConfig = model.getRequestConfigObject<AiModelRequestConfig>()
        val groupMultiplier = resolveGroupMultiplier(context.groupId)

        val entity = AiModelInvocationRecordEntity(
            id = snowIdGenerator.nextId(),
            requestId = context.responseMetadataId,
            userId = context.userId,
            tenantId = context.tenantId,
            providerId = context.providerId,
            modelId = context.modelId,
            promptTokens = promptTokens,
            cachedPromptTokens = cachedPromptTokens,
            completionTokens = completionTokens,
            reasoningTokens = reasoningTokens,
            cacheCreationTokens = cacheCreationTokens,
            toolCallsCount = context.toolCallsCount,
            messageCount = context.messageCount,
            isStreaming = context.isStreaming,
            timeToFirstTokenMs = context.timeToFirstTokenMs ?: 0,
            totalDurationMs = context.durationMs,
            queueWaitMs = queueWaitMs,
            tokensPerSecond = tokensPerSecond,
            requestSizeBytes = requestSizeBytes,
            responseSizeBytes = responseSizeBytes,
            promptUnitPrice = promptPrice,
            completionUnitPrice = completionPrice,
            cacheReadUnitPrice = cacheReadPrice,
            cacheWriteUnitPrice = cacheWritePrice,
            rawCost = rawCost,
            finalCost = rawCost * groupMultiplier,
            currencyId = model.currencyId,
            temperature = modelRequestConfig.temperature?.toDouble(),
            topP = 0.0,
            maxTokens = (modelRequestConfig.maxOutputTokens ?: model.maxOutputTokens)?.toInt(),
            status = AiModelInvocationStatus.SUCCESS.typeId,
            stopReason = context.stopReason,
            sessionId = context.sessionId,
            clientIp = context.clientIp,
            userAgent = context.userAgent,
            groupId = context.groupId,
            groupMultiplier = groupMultiplier,
        ).apply {
            newEntity()
        }

        repository.save(entity).awaitFirstOrNull()

        chargeForInvocation(entity, model)
    }

    override suspend fun recordFailedInvocation(
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
    ) {
        val entity = AiModelInvocationRecordEntity(
            id = snowIdGenerator.nextId(),
            requestId = UUID.randomUUID().toString(),
            userId = userId,
            tenantId = tenantId,
            providerId = providerId,
            modelId = modelId,
            messageCount = messageCount,
            isStreaming = isStreaming,
            totalDurationMs = durationMs,
            status = AiModelInvocationStatus.FAILED.typeId,
            errorCode = errorCode,
            errorMessage = errorMessage,
            sessionId = sessionId,
            clientIp = clientIp,
            userAgent = userAgent,
            groupId = groupId,
        )

        repository.save(entity).awaitFirstOrNull()
    }

    private suspend fun chargeForInvocation(entity: AiModelInvocationRecordEntity, model: AiModelEntity) {
        if (entity.finalCost <= 0.0 || model.currencyId <= CurrencyConstants.NO_CURRENCY_ID) return

        try {
            val result = economyWalletService.charge(
                userId = entity.userId,
                tenantId = entity.tenantId,
                currencyId = model.currencyId,
                amount = BigDecimal.valueOf(entity.finalCost),
                referenceType = EconomyReferenceType.AI_INVOCATION.typeId,
                referenceId = entity.id,
                requestId = entity.requestId,
            )
            if (result != EconomyChargeResult.CHARGED) {
                logger.warn("Wallet charge for AI invocation {} returned {}", entity.requestId, result)
            }
        } catch (e: Exception) {
            logger.error("Failed to charge wallet for AI invocation {}", entity.requestId, e)
        }
    }

    /**
     * Resolves the billing multiplier for a user group. The group is required — there is no
     * fallback to a 1x multiplier.
     */
    private suspend fun resolveGroupMultiplier(groupId: Long): Double {
        val group = aiUserGroupManagerService.getByIdOrNull(groupId)
            ?: throw BusinessException("User group not found: $groupId")
        return group.billingMultiplier.toDouble()
    }

    /**
     * The one field still read straight out of the raw body: VertexLib resolves usage through the
     * provider's response config, and `queue_time` is a gateway addition with no place in it.
     */
    private fun extractQueueWaitMs(rawResponseBody: String?): Long? {
        if (rawResponseBody.isNullOrBlank()) return null

        return try {
            (JsonPath.read<Any>(rawResponseBody, AiInvocationRecordConstants.QUEUE_TIME_JSON_PATH) as? Number)?.toLong()
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateCost(
        promptTokens: Int,
        completionTokens: Int,
        cachedPromptTokens: Int,
        cacheCreationTokens: Int,
        promptPrice: Double,
        completionPrice: Double,
        cacheReadPrice: Double,
        cacheWritePrice: Double,
    ): Double {
        val promptCost = (promptTokens / 1_000_000.0) * promptPrice
        val completionCost = (completionTokens / 1_000_000.0) * completionPrice
        val cacheReadCost = (cachedPromptTokens / 1_000_000.0) * cacheReadPrice
        val cacheWriteCost = (cacheCreationTokens / 1_000_000.0) * cacheWritePrice

        return promptCost + completionCost + cacheReadCost + cacheWriteCost
    }
}
