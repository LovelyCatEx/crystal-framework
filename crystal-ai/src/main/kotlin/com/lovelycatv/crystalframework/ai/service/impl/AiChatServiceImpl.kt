package com.lovelycatv.crystalframework.ai.service.impl

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.service.factory.AiLlmClientFactory
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import com.lovelycatv.crystalframework.ai.types.AiModelRequestConfig
import com.lovelycatv.crystalframework.ai.types.AiProviderProtocolType
import com.lovelycatv.crystalframework.shared.context.CurrentTenantId
import com.lovelycatv.crystalframework.shared.context.CurrentUserId
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.vertex.ai.llm.ChatRequest
import com.lovelycatv.vertex.ai.llm.ChatResponse
import com.lovelycatv.vertex.ai.llm.ErrorChatResponse
import com.lovelycatv.vertex.ai.llm.ErrorStreamChatResponse
import com.lovelycatv.vertex.ai.llm.ReasoningEffort
import com.lovelycatv.vertex.ai.llm.StreamChatResponse
import com.lovelycatv.vertex.ai.llm.message.ChatMessage
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class AiChatServiceImpl(
    private val aiModelManagerService: AiModelManagerService,
    private val aiProviderManagerService: AiProviderManagerService,
    private val invocationRecordService: AiModelInvocationRecordService,
    private val aiLlmClientFactory: AiLlmClientFactory,
) : AiChatService {
    private val logger = logger()

    private val recordScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun chatCompletionSync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort?
    ): ChatResponse {
        val subject = resolveSubject(modelId, messages.size)

        val client = aiLlmClientFactory.getClient(subject.provider)
        val chatRequest = buildChatRequest(subject.model, subject.provider, messages, false, reasoningEffort)
        val rawRequestBody = client.transformRequestBody(chatRequest)

        val response = try {
            client.chatCompletion(chatRequest)
        } catch (expected: Exception) {
            recordFailure(subject, expected.javaClass.simpleName, describe(expected), isStreaming = false)
            throw BusinessException(failureMessage(subject.provider, describe(expected)))
        }

        // A refused call comes back as a well-formed error envelope rather than an exception.
        if (response is ErrorChatResponse) {
            recordFailure(subject, response.javaClass.simpleName, response.errorMessage, isStreaming = false)
            throw BusinessException(failureMessage(subject.provider, response.errorMessage))
        }

        val assistantMessage = response.choices.firstOrNull()?.message

        launchRecording {
            invocationRecordService.recordInvocationFromContext(
                context = AiInvocationContext(
                    userId = subject.userId,
                    tenantId = subject.tenantId,
                    modelId = subject.model.id,
                    providerId = subject.provider.id,
                    messageCount = subject.messageCount,
                    toolCallsCount = assistantMessage?.toolCalls?.size ?: 0,
                    responseMetadataId = response.id,
                    durationMs = subject.durationMs,
                    isStreaming = false,
                    timeToFirstTokenMs = null,
                    usage = response.usage,
                    stopReason = assistantMessage?.stopReasonString,
                    rawRequestBody = rawRequestBody,
                    rawResponseBody = response.originalResponse,
                ),
                model = subject.model,
            )
        }

        return response
    }

    override suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort?
    ): Flow<StreamChatResponse> {
        val subject = resolveSubject(modelId, messages.size)

        val client = aiLlmClientFactory.getClient(subject.provider)
        val chatRequest = buildChatRequest(subject.model, subject.provider, messages, true, reasoningEffort)
        val rawRequestBody = client.transformRequestBody(chatRequest)

        return flow {
            val merged = StreamingMerge()
            var firstTokenTime: Long? = null

            try {
                // Called here, inside the flow builder, and not before it: the client issues the HTTP
                // request as soon as it is invoked, while only the flow it returns closes the
                // response body. Calling it outside would leave an unconsumed body behind whenever
                // the caller never collects.
                client.chatCompletionAsync(chatRequest).collect { frame ->
                    if (frame is ErrorStreamChatResponse) {
                        throw BusinessException(frame.errorMessage)
                    }

                    if (firstTokenTime == null) {
                        firstTokenTime = System.currentTimeMillis()
                    }

                    merged.accept(frame)
                    emit(frame)
                }

                // A rejected call carries no `data:` line at all — just a JSON or HTML error body —
                // so the reader runs out of input and the flow completes normally. Producing nothing
                // is therefore the only signal that it failed.
                if (!merged.receivedAnyFrame) {
                    throw BusinessException("provider returned no stream events")
                }
            } catch (expected: Exception) {
                recordFailure(subject, expected.javaClass.simpleName, describe(expected), isStreaming = true)
                throw BusinessException(failureMessage(subject.provider, describe(expected)))
            }

            launchRecording {
                invocationRecordService.recordInvocationFromContext(
                    context = AiInvocationContext(
                        userId = subject.userId,
                        tenantId = subject.tenantId,
                        modelId = subject.model.id,
                        providerId = subject.provider.id,
                        messageCount = subject.messageCount,
                        toolCallsCount = merged.toolCallsCount,
                        responseMetadataId = merged.responseId.orEmpty(),
                        durationMs = subject.durationMs,
                        isStreaming = true,
                        timeToFirstTokenMs = firstTokenTime?.let { it - subject.startedAt },
                        usage = merged.usage,
                        stopReason = merged.stopReason,
                        rawRequestBody = rawRequestBody,
                        rawResponseBody = merged.rawResponseBody,
                    ),
                    model = subject.model,
                )
            }
        }
    }

    private suspend fun resolveSubject(modelId: Long, messageCount: Int): InvocationSubject {
        val startedAt = System.currentTimeMillis()

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

        return InvocationSubject(
            // Resolved here and not on recordScope: that scope does not inherit the Reactor
            // context, so reading the caller from inside it would always come back empty.
            userId = CurrentUserId.current() ?: 0,
            tenantId = CurrentTenantId.current(),
            model = model,
            provider = provider,
            messageCount = messageCount,
            startedAt = startedAt,
        )
    }

    private fun buildChatRequest(
        model: AiModelEntity,
        provider: AiProviderEntity,
        messages: List<ChatMessage>,
        stream: Boolean,
        reasoningEffort: ReasoningEffort?
    ): ChatRequest {
        val modelRequestConfig = model.getRequestConfigObject<AiModelRequestConfig>()

        return ChatRequest(
            model = model.key,
            messages = messages,
            stream = stream,
            reasoningEffort = reasoningEffort ?: provider.getRealProtocolType().defaultReasoningEffort(),
            maxCompletionTokens = resolveMaxCompletionTokens(model, modelRequestConfig),
            temperature = modelRequestConfig.temperature?.toFloat(),
            // Passed through untouched, nulls included: a null entry is VertexLib's way of dropping
            // the field from the body, which is how a provider that rejects a derived field is
            // configured.
            extraBody = modelRequestConfig.additionalBody,
        )
    }

    private fun resolveMaxCompletionTokens(
        model: AiModelEntity,
        modelRequestConfig: AiModelRequestConfig
    ): Int? {
        val maxOutputTokens = modelRequestConfig.maxOutputTokens ?: model.maxOutputTokens ?: return null

        if (maxOutputTokens > Int.MAX_VALUE) {
            throw BusinessException("AI model max output tokens exceed supported range")
        }

        return maxOutputTokens.toInt()
    }

    /**
     * The effort level a caller did not ask for, picked so it changes the least about what the
     * request used to look like.
     *
     * [ReasoningEffort.AUTO] renders as no `reasoning_effort` field at all, which is what the
     * OpenAI-compatible path sent before — and `"none"`, its [ReasoningEffort.DISABLED] rendering,
     * is rejected by models older than GPT-5.1 and by endpoints that only claim compatibility. The
     * Messages path has no such option: omitting `thinking` leaves it running on models that
     * default to it, so turning it off has to be explicit.
     */
    private fun AiProviderProtocolType.defaultReasoningEffort(): ReasoningEffort {
        return when (this) {
            AiProviderProtocolType.OPENAI_COMPATIBLE -> ReasoningEffort.AUTO
            AiProviderProtocolType.ANTHROPIC_MESSAGES -> ReasoningEffort.DISABLED
        }
    }

    private fun recordFailure(
        subject: InvocationSubject,
        errorCode: String,
        errorMessage: String,
        isStreaming: Boolean
    ) {
        launchRecording {
            invocationRecordService.recordFailedInvocation(
                userId = subject.userId,
                tenantId = subject.tenantId,
                modelId = subject.model.id,
                providerId = subject.provider.id,
                messageCount = subject.messageCount,
                durationMs = subject.durationMs,
                errorCode = errorCode,
                errorMessage = errorMessage,
                isStreaming = isStreaming,
            )
        }
    }

    /** Audit writes must never take the call down with them. */
    private fun launchRecording(block: suspend () -> Unit) {
        recordScope.launch {
            try {
                block()
            } catch (expected: Exception) {
                logger.error("Failed to record AI invocation: ${expected.message}", expected)
            }
        }
    }

    private fun describe(cause: Exception): String {
        return cause.localizedMessage ?: cause.message ?: cause.javaClass.simpleName
    }

    private fun failureMessage(provider: AiProviderEntity, reason: String): String {
        return "AI provider '${provider.name}' call failed: $reason"
    }

    /**
     * Which invocation is being recorded — the part that is identical whether the call succeeds or
     * fails, resolved once up front.
     */
    private data class InvocationSubject(
        val userId: Long,
        val tenantId: Long?,
        val model: AiModelEntity,
        val provider: AiProviderEntity,
        val messageCount: Int,
        val startedAt: Long,
    ) {
        val durationMs: Long get() = System.currentTimeMillis() - startedAt
    }

    /**
     * Folds a stream into the single set of numbers an audit record needs.
     *
     * Each field is taken from the last frame that actually carries a value, not from the last
     * frame outright: a stream ends on `message_stop`, which has no delta and no usage, and
     * Anthropic splits usage across `message_start` (prompt) and `message_delta` (completion).
     * These counters are running totals, so keeping the largest value seen lands on the final one
     * without having to know which event type reports which field.
     */
    private class StreamingMerge {
        var receivedAnyFrame: Boolean = false
            private set

        var responseId: String? = null
            private set

        var stopReason: String? = null
            private set

        var rawResponseBody: String? = null
            private set

        var toolCallsCount: Int = 0
            private set

        var usage: ChatResponse.Usage = ChatResponse.Usage()
            private set

        private val completedToolCallKeys = mutableSetOf<String>()

        fun accept(frame: StreamChatResponse) {
            receivedAnyFrame = true

            if (frame.id.isNotEmpty()) {
                responseId = frame.id
            }

            if (frame.originalResponse.isNotBlank()) {
                rawResponseBody = frame.originalResponse
            }

            usage = ChatResponse.Usage(
                promptTokens = maxOf(usage.promptTokens, frame.usage.promptTokens),
                completionTokens = maxOf(usage.completionTokens, frame.usage.completionTokens),
                reasoningTokens = maxOf(usage.reasoningTokens, frame.usage.reasoningTokens),
                cachedPromptTokens = maxOf(usage.cachedPromptTokens, frame.usage.cachedPromptTokens),
                cacheCreationTokens = maxOf(usage.cacheCreationTokens, frame.usage.cacheCreationTokens),
            )

            val message = frame.choices.firstOrNull()?.message ?: return

            message.stopReasonString?.let { stopReason = it }

            // A tool call only counts once its arguments have finished streaming, keyed by id so the
            // frames carrying its remaining deltas do not count it again.
            message.toolCalls
                ?.filter { !it.streaming }
                ?.forEach { completedToolCallKeys += it.id.ifBlank { "index-${it.index}" } }

            toolCallsCount = completedToolCallKeys.size
        }
    }
}
