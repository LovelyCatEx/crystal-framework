package com.lovelycatv.crystalframework.ai.service.impl

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.interceptor.RawResponseCapturingInterceptor
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType
import com.lovelycatv.crystalframework.ai.types.AiProviderRequestConfig
import com.lovelycatv.crystalframework.shared.context.CurrentTenantId
import com.lovelycatv.crystalframework.shared.context.CurrentUserId
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

@Service
class AiChatServiceImpl(
    private val aiModelManagerService: AiModelManagerService,
    private val aiProviderManagerService: AiProviderManagerService,
    private val invocationRecordService: AiModelInvocationRecordService,
) : AiChatService {
    private val recordScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<Message>
    ): ChatResponse {
        val startTime = System.currentTimeMillis()
        val (model, provider) = getModelAndProvider(modelId)

        if (provider.getRealProtocolType() != AiProviderProtocolType.OPENAI_COMPATIBLE) {
            throw BusinessException("Only OpenAI compatible providers are currently supported")
        }

        val rawRequestBodyHolder = AtomicReference<String?>()
        val rawResponseBodyHolder = AtomicReference<String?>()
        val chatModel = buildChatModel(model, provider, rawRequestBodyHolder, rawResponseBodyHolder)
        val prompt = Prompt(messages)

        return try {
            val response = suspendCancellableCoroutine { continuation ->
                continuation.resume(chatModel.call(prompt))
            }
            val duration = System.currentTimeMillis() - startTime
            val rawRequestBody = rawRequestBodyHolder.getAndSet(null)
            val rawResponseBody = rawResponseBodyHolder.getAndSet(null)

            recordScope.launch {
                try {
                    val context = AiInvocationContext.fromInvocation(
                        userId = CurrentUserId.current() ?: 0,
                        tenantId = CurrentTenantId.current(),
                        modelId = model.id,
                        providerId = provider.id,
                        messages = messages,
                        response = response,
                        durationMs = duration,
                        isStreaming = false,
                        timeToFirstTokenMs = null,
                        rawRequestBody = rawRequestBody,
                        rawResponseBody = rawResponseBody,
                    )

                    invocationRecordService.recordInvocationFromContext(
                        context = context,
                        model = model,
                        provider = provider,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            response
        } catch (e: Exception) {
            rawRequestBodyHolder.set(null)
            rawResponseBodyHolder.set(null)
            val duration = System.currentTimeMillis() - startTime

            recordScope.launch {
                try {
                    invocationRecordService.recordFailedInvocation(
                        userId = CurrentUserId.current() ?: 0,
                        tenantId = CurrentTenantId.current(),
                        modelId = modelId,
                        providerId = provider.id,
                        messages = messages,
                        durationMs = duration,
                        errorCode = e.javaClass.simpleName,
                        errorMessage = e.localizedMessage ?: e.message ?: "",
                        isStreaming = false,
                    )
                } catch (recordException: Exception) {
                    recordException.printStackTrace()
                }
            }

            throw BusinessException(e.localizedMessage ?: e.message ?: "")
        }
    }

    override suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<Message>
    ): Flow<ChatResponse> {
        val startTime = System.currentTimeMillis()
        var firstTokenTime: Long? = null
        var lastResponse: ChatResponse? = null

        val (model, provider) = getModelAndProvider(modelId)

        if (provider.getRealProtocolType() != AiProviderProtocolType.OPENAI_COMPATIBLE) {
            throw BusinessException("Only OpenAI compatible providers are currently supported")
        }

        val rawRequestBodyHolder = AtomicReference<String?>()
        val rawResponseBodyHolder = AtomicReference<String?>()
        val chatModel = buildChatModel(model, provider, rawRequestBodyHolder, rawResponseBodyHolder)
        val prompt = Prompt(messages)

        return try {
            chatModel.stream(prompt).asFlow()
                .onEach { response ->
                    if (firstTokenTime == null) {
                        firstTokenTime = System.currentTimeMillis()
                    }
                    lastResponse = response
                }
                .onCompletion { cause ->
                    val duration = System.currentTimeMillis() - startTime
                    val timeToFirst = firstTokenTime?.let { it - startTime } ?: 0L
                    val rawRequestBody = rawRequestBodyHolder.getAndSet(null)
                    val rawResponseBody = rawResponseBodyHolder.getAndSet(null)

                    if (cause == null && lastResponse != null) {
                        recordScope.launch {
                            try {
                                val context = AiInvocationContext.fromInvocation(
                                    userId = CurrentUserId.current() ?: 0,
                                    tenantId = CurrentTenantId.current(),
                                    modelId = model.id,
                                    providerId = provider.id,
                                    messages = messages,
                                    response = lastResponse,
                                    durationMs = duration,
                                    isStreaming = true,
                                    timeToFirstTokenMs = timeToFirst,
                                    rawRequestBody = rawRequestBody,
                                    rawResponseBody = rawResponseBody,
                                )

                                invocationRecordService.recordInvocationFromContext(
                                    context = context,
                                    model = model,
                                    provider = provider,
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else if (cause != null) {
                        recordScope.launch {
                            try {
                                invocationRecordService.recordFailedInvocation(
                                    userId = CurrentUserId.current() ?: 0,
                                    tenantId = CurrentTenantId.current(),
                                    modelId = modelId,
                                    providerId = provider.id,
                                    messages = messages,
                                    durationMs = duration,
                                    errorCode = cause.javaClass.simpleName,
                                    errorMessage = cause.localizedMessage ?: cause.message ?: "",
                                    isStreaming = true,
                                )
                            } catch (recordException: Exception) {
                                recordException.printStackTrace()
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            rawRequestBodyHolder.set(null)
            rawResponseBodyHolder.set(null)
            val duration = System.currentTimeMillis() - startTime

            recordScope.launch {
                try {
                    invocationRecordService.recordFailedInvocation(
                        userId = CurrentUserId.current() ?: 0,
                        tenantId = CurrentTenantId.current(),
                        modelId = modelId,
                        providerId = provider.id,
                        messages = messages,
                        durationMs = duration,
                        errorCode = e.javaClass.simpleName,
                        errorMessage = e.localizedMessage ?: e.message ?: "",
                        isStreaming = true,
                    )
                } catch (recordException: Exception) {
                    recordException.printStackTrace()
                }
            }

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
        provider: AiProviderEntity,
        rawRequestBodyHolder: AtomicReference<String?>,
        rawResponseBodyHolder: AtomicReference<String?>
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
            .httpClientBuilderCustomizer { httpClientBuilder ->
                httpClientBuilder.interceptor(RawResponseCapturingInterceptor(rawRequestBodyHolder, rawResponseBodyHolder))
            }
            .build()
    }
}
