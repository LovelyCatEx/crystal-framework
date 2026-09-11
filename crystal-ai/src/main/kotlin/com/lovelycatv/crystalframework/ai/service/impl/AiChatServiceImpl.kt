package com.lovelycatv.crystalframework.ai.service.impl

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType
import com.lovelycatv.crystalframework.ai.types.AiProviderRequestConfig
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service
import kotlin.coroutines.resume

@Service
class AiChatServiceImpl(
    private val aiModelManagerService: AiModelManagerService,
    private val aiProviderManagerService: AiProviderManagerService
) : AiChatService {

    override suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<Message>
    ): ChatResponse {
        val (model, provider) = getModelAndProvider(modelId)

        if (provider.getRealProtocolType() != AiProviderProtocolType.OPENAI_COMPATIBLE) {
            throw BusinessException("Only OpenAI compatible providers are currently supported")
        }

        val chatModel = buildChatModel(model, provider)
        val prompt = Prompt(messages)

        return try {
            suspendCancellableCoroutine { continuation ->
                continuation.resume(chatModel.call(prompt))
            }
        } catch (e: Exception) {
            throw BusinessException(e.localizedMessage ?: e.message ?: "")
        }
    }

    override suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<Message>
    ): Flow<ChatResponse> {
        val (model, provider) = getModelAndProvider(modelId)

        if (provider.getRealProtocolType() != AiProviderProtocolType.OPENAI_COMPATIBLE) {
            throw BusinessException("Only OpenAI compatible providers are currently supported")
        }

        val chatModel = buildChatModel(model, provider)
        val prompt = Prompt(messages)

        return try {
            chatModel.stream(prompt).asFlow()
        } catch (e: Exception) {
            throw BusinessException(e.localizedMessage ?: e.message ?: "")
        }
    }

    private suspend fun getModelAndProvider(modelId: Long): Pair<AiModelEntity, AiProviderEntity> {
        val model = aiModelManagerService.getByIdOrNull(modelId)
            ?: throw BusinessException("Model not found: $modelId")

        if (!model.enabled) {
            throw BusinessException("Model is disabled: ${model.displayName}")
        }

        val provider = aiProviderManagerService.getByIdOrNull(model.providerId)
            ?: throw BusinessException("Provider not found: ${model.providerId}")

        if (!provider.enabled) {
            throw BusinessException("Provider is disabled: ${provider.name}")
        }

        return model to provider
    }

    private fun buildChatModel(
        model: AiModelEntity,
        provider: AiProviderEntity
    ): OpenAiChatModel {
        val providerRequestConfig = provider.getRequestConfigObject<AiProviderRequestConfig>()
        val modelRequestConfig = model.getRequestConfigObject<AiModelRequestConfig>()

        val chatOptionsBuilder = OpenAiChatOptions.builder()
            .model(model.key)
            .baseUrl(provider.baseUrl)
            .apiKey(provider.apiKey)

        // Apply provider headers
        if (providerRequestConfig.headers.isNotEmpty()) {
            chatOptionsBuilder.customHeaders(providerRequestConfig.headers)
        }

        // Apply model temperature
        modelRequestConfig.temperature?.let {
            chatOptionsBuilder.temperature(it.toDouble())
        }

        // Apply model max output tokens
        val maxTokens = modelRequestConfig.maxOutputTokens ?: model.maxOutputTokens
        maxTokens?.let {
            if (it > Int.MAX_VALUE) {
                throw BusinessException("AI model max output tokens exceed supported range")
            }
            chatOptionsBuilder.maxCompletionTokens(it.toInt())
        }

        if (modelRequestConfig.additionalBody.isNotEmpty()) {
            chatOptionsBuilder.extraBody(modelRequestConfig.additionalBody.mapNotNull { (key, value) ->
                value?.let { key to it }
            }.toMap())
        }

        val chatOptions = chatOptionsBuilder.build()

        return OpenAiChatModel.builder()
            .options(chatOptions)
            .build()
    }
}
