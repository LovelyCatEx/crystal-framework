package com.lovelycatv.crystalframework.ai.service.impl

import com.jayway.jsonpath.JsonPath
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.types.AiEndpointResponseConfig
import com.lovelycatv.crystalframework.ai.types.AiModelInvocationStatus
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderResponseConfig
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper
import java.util.*

@Service
class AiModelInvocationRecordServiceImpl(
    private val repository: AiModelInvocationRecordRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val objectMapper: ObjectMapper,
) : AiModelInvocationRecordService {

    override suspend fun recordSyncInvocation(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
    ) {
        val responseConfig = provider.getResponseConfigObject<AiProviderResponseConfig>()
        val chatConfig = responseConfig.chatCompletions

        val entity = buildBaseRecord(
            userId = userId,
            tenantId = tenantId,
            model = model,
            provider = provider,
            messages = messages,
            response = response,
            durationMs = durationMs,
            isStreaming = false,
            timeToFirstTokenMs = null,
            responseConfig = chatConfig,
        )

        repository.save(entity).awaitFirstOrNull()
    }

    override suspend fun recordStreamInvocation(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
        timeToFirstTokenMs: Long,
    ) {
        val responseConfig = provider.getResponseConfigObject<AiProviderResponseConfig>()
        val chatConfig = responseConfig.chatCompletions

        val entity = buildBaseRecord(
            userId = userId,
            tenantId = tenantId,
            model = model,
            provider = provider,
            messages = messages,
            response = response,
            durationMs = durationMs,
            isStreaming = true,
            timeToFirstTokenMs = timeToFirstTokenMs,
            responseConfig = chatConfig,
        )

        repository.save(entity).awaitFirstOrNull()
    }

    override suspend fun recordFailedInvocation(
        userId: Long,
        tenantId: Long?,
        modelId: Long,
        providerId: Long,
        messages: List<Message>,
        durationMs: Long,
        errorCode: String,
        errorMessage: String,
        isStreaming: Boolean,
    ) {
        val entity = AiModelInvocationRecordEntity(
            id = snowIdGenerator.nextId(),
            requestId = UUID.randomUUID().toString(),
            userId = userId,
            tenantId = tenantId,
            providerId = providerId,
            modelId = modelId,
            messageCount = messages.size,
            isStreaming = isStreaming,
            totalDurationMs = durationMs,
            status = AiModelInvocationStatus.FAILED.typeId,
            errorCode = errorCode,
            errorMessage = errorMessage,
        )

        repository.save(entity).awaitFirstOrNull()
    }

    private fun buildBaseRecord(
        userId: Long,
        tenantId: Long?,
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<Message>,
        response: ChatResponse,
        durationMs: Long,
        isStreaming: Boolean,
        timeToFirstTokenMs: Long?,
        responseConfig: AiEndpointResponseConfig?,
    ): AiModelInvocationRecordEntity {
        val metadata = response.metadata
        val additionalProperties = metadata["_additionalProperties"] as? Map<*, *>

        val promptTokens = extractTokenCount(additionalProperties, responseConfig?.usage?.inputTokensPath, "prompt_tokens")
        val completionTokens = extractTokenCount(additionalProperties, responseConfig?.usage?.outputTokensPath, "completion_tokens")
        val cachedPromptTokens = extractTokenCount(additionalProperties, responseConfig?.usage?.cacheReadTokensPath, "cached_tokens")
        val cacheCreationTokens = extractTokenCount(additionalProperties, responseConfig?.usage?.cacheWriteTokensPath, "cache_creation_input_tokens")

        val reasoningTokens = (additionalProperties?.get("reasoning_tokens") as? Number)?.toInt() ?: 0

        val toolCallsCount = response.result?.output?.toolCalls?.size ?: 0

        val stopReason = extractStopReason(additionalProperties, responseConfig?.finishReasonPath)

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

        val tokensPerSecond = if (durationMs > 0) {
            (completionTokens.toDouble() / durationMs) * 1000
        } else null

        val modelRequestConfig = model.getRequestConfigObject<AiModelRequestConfig>()
        val temperature = modelRequestConfig.temperature?.toDouble()
        val topP = 0.0
        val maxTokens = (modelRequestConfig.maxOutputTokens ?: model.maxOutputTokens)?.toInt()

        return AiModelInvocationRecordEntity(
            id = snowIdGenerator.nextId(),
            requestId = response.metadata.id,
            userId = userId,
            tenantId = tenantId,
            providerId = provider.id,
            modelId = model.id,
            promptTokens = promptTokens,
            cachedPromptTokens = cachedPromptTokens,
            completionTokens = completionTokens,
            reasoningTokens = reasoningTokens,
            cacheCreationTokens = cacheCreationTokens,
            toolCallsCount = toolCallsCount,
            messageCount = messages.size,
            isStreaming = isStreaming,
            timeToFirstTokenMs = timeToFirstTokenMs ?: 0,
            totalDurationMs = durationMs,
            tokensPerSecond = tokensPerSecond,
            promptUnitPrice = promptPrice,
            completionUnitPrice = completionPrice,
            cacheReadUnitPrice = cacheReadPrice,
            cacheWriteUnitPrice = cacheWritePrice,
            rawCost = rawCost,
            finalCost = rawCost,
            currency = model.currency,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            status = AiModelInvocationStatus.SUCCESS.typeId,
            stopReason = stopReason,
        ).apply {
            newEntity()
        }
    }

    private fun extractTokenCount(
        additionalProperties: Map<*, *>?,
        jsonPath: String?,
        fallbackKey: String,
    ): Int {
        if (additionalProperties == null) return 0

        if (!jsonPath.isNullOrBlank()) {
            try {
                val json = objectMapper.writeValueAsString(additionalProperties)
                val value = JsonPath.read<Any>(json, jsonPath)
                return (value as? Number)?.toInt() ?: 0
            } catch (e: Exception) {
                // Fall back to direct map access
            }
        }

        return (additionalProperties[fallbackKey] as? Number)?.toInt() ?: 0
    }

    private fun extractStopReason(
        additionalProperties: Map<*, *>?,
        jsonPath: String?,
    ): String? {
        if (additionalProperties == null) return null

        if (!jsonPath.isNullOrBlank()) {
            try {
                val json = objectMapper.writeValueAsString(additionalProperties)
                return JsonPath.read<String>(json, jsonPath)
            } catch (e: Exception) {
                // Ignore
            }
        }

        return (additionalProperties["finish_reason"] as? String)
            ?: (additionalProperties["stop_reason"] as? String)
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
