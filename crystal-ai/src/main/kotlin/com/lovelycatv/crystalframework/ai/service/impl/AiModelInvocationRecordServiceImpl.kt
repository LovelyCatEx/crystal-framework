package com.lovelycatv.crystalframework.ai.service.impl

import com.jayway.jsonpath.JsonPath
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiModelInvocationRecordEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelInvocationRecordRepository
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.types.AiEndpointResponseConfig
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import com.lovelycatv.crystalframework.ai.types.AiModelInvocationStatus
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderResponseConfig
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.openai.models.completions.CompletionUsage
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

    override suspend fun recordInvocation(
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
            isStreaming = isStreaming,
            timeToFirstTokenMs = timeToFirstTokenMs,
            responseConfig = chatConfig,
            rawRequestBody = rawRequestBody,
            rawResponseBody = rawResponseBody,
        )

        repository.save(entity).awaitFirstOrNull()
    }

    override suspend fun recordInvocationFromContext(
        context: AiInvocationContext,
        model: AiModelEntity,
        provider: AiProviderEntity,
    ) {
        val responseConfig = provider.getResponseConfigObject<AiProviderResponseConfig>()
        val chatConfig = responseConfig.chatCompletions

        val promptTokens = extractTokenCount(context.rawResponseBody, chatConfig?.usage?.inputTokensPath, "$.usage.prompt_tokens")
        val completionTokens = extractTokenCount(context.rawResponseBody, chatConfig?.usage?.outputTokensPath, "$.usage.completion_tokens")
        val cachedPromptTokens = extractTokenCount(context.rawResponseBody, chatConfig?.usage?.cacheReadTokensPath, "$.usage.prompt_tokens_details.cached_tokens")
        val cacheCreationTokens = extractTokenCount(context.rawResponseBody, chatConfig?.usage?.cacheWriteTokensPath, "$.usage.prompt_tokens_details.cache_creation_input_tokens")
        val reasoningTokens = extractTokenCount(context.rawResponseBody, null, "$.usage.completion_tokens_details.reasoning_tokens")
        val stopReason = extractStopReason(context.rawResponseBody, chatConfig?.finishReasonPath)
        val queueWaitMs = extractLongValue(context.rawResponseBody, "$.usage.queue_time")

        val requestSizeBytes = context.rawRequestBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()
        val responseSizeBytes = context.rawResponseBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()

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
        val temperature = modelRequestConfig.temperature?.toDouble()
        val topP = 0.0
        val maxTokens = (modelRequestConfig.maxOutputTokens ?: model.maxOutputTokens)?.toInt()

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
            toolCallsCount = 0, // Context 中没有 toolCalls 信息
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
        rawRequestBody: String?,
        rawResponseBody: String?,
    ): AiModelInvocationRecordEntity {
        val promptTokens = extractTokenCount(rawResponseBody, responseConfig?.usage?.inputTokensPath, "$.usage.prompt_tokens")
        val completionTokens = extractTokenCount(rawResponseBody, responseConfig?.usage?.outputTokensPath, "$.usage.completion_tokens")
        val cachedPromptTokens = extractTokenCount(rawResponseBody, responseConfig?.usage?.cacheReadTokensPath, "$.usage.prompt_tokens_details.cached_tokens")
        val cacheCreationTokens = extractTokenCount(rawResponseBody, responseConfig?.usage?.cacheWriteTokensPath, "$.usage.prompt_tokens_details.cache_creation_input_tokens")

        val reasoningTokens = extractTokenCount(rawResponseBody, null, "$.usage.completion_tokens_details.reasoning_tokens")

        val toolCallsCount = response.result?.output?.toolCalls?.size ?: 0

        val stopReason = extractStopReason(rawResponseBody, responseConfig?.finishReasonPath)

        // Extract queue wait time from raw response if available
        val queueWaitMs = extractLongValue(rawResponseBody, "$.usage.queue_time")

        // Calculate request and response sizes from raw bodies
        val requestSizeBytes = rawRequestBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()
        val responseSizeBytes = rawResponseBody?.toByteArray(Charsets.UTF_8)?.size?.toLong()

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
            queueWaitMs = queueWaitMs,
            tokensPerSecond = tokensPerSecond,
            requestSizeBytes = requestSizeBytes,
            responseSizeBytes = responseSizeBytes,
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
        rawResponseBody: String?,
        jsonPath: String?,
        fallbackPath: String,
    ): Int {
        if (rawResponseBody.isNullOrBlank()) return 0

        val pathToUse = jsonPath ?: fallbackPath

        return try {
            val value = JsonPath.read<Any>(rawResponseBody, pathToUse)
            (value as? Number)?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun extractStopReason(
        rawResponseBody: String?,
        jsonPath: String?,
    ): String? {
        if (rawResponseBody.isNullOrBlank()) return null

        if (!jsonPath.isNullOrBlank()) {
            try {
                return JsonPath.read<String>(rawResponseBody, jsonPath)
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Fallback: try common paths
        return try {
            JsonPath.read<String>(rawResponseBody, "$.choices[0].finish_reason")
        } catch (e: Exception) {
            try {
                JsonPath.read<String>(rawResponseBody, "$.choices[0].stop_reason")
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun extractLongValue(
        rawResponseBody: String?,
        jsonPath: String,
    ): Long? {
        if (rawResponseBody.isNullOrBlank()) return null

        return try {
            val value = JsonPath.read<Any>(rawResponseBody, jsonPath)
            (value as? Number)?.toLong()
        } catch (e: Exception) {
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
