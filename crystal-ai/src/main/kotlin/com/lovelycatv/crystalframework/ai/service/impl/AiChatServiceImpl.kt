package com.lovelycatv.crystalframework.ai.service.impl

import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.ai.service.AiModelInvocationRecordService
import com.lovelycatv.crystalframework.ai.service.factory.AiLlmClientFactory
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.ai.tool.CommonAiTools
import com.lovelycatv.crystalframework.ai.types.AiChatCompletionResult
import com.lovelycatv.crystalframework.ai.types.AiChatStreamEvent
import com.lovelycatv.crystalframework.ai.types.AiInvocationContext
import com.lovelycatv.crystalframework.ai.types.AiToolCallResult
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
import com.lovelycatv.vertex.ai.llm.message.AssistantChatMessage
import com.lovelycatv.vertex.ai.llm.message.ChatMessage
import com.lovelycatv.vertex.ai.llm.message.ToolCall
import com.lovelycatv.vertex.ai.llm.message.ToolChatMessage
import com.lovelycatv.vertex.ai.llm.tool.ToolDeclaration
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
    ): AiChatCompletionResult {
        val subject = resolveSubject(modelId, messages.size)

        val client = aiLlmClientFactory.getClient(subject.provider)
        val tools = CommonAiTools.declarations

        var currentMessages = messages
        var accumulatedUsage = ChatResponse.Usage()
        var totalToolCalls = 0
        var rawRequestBody: String? = null
        val toolCallResults = mutableListOf<AiToolCallResult>()

        val finalResponse = try {
            var response: ChatResponse? = null
            var assistantMessage: AssistantChatMessage? = null

            for (iteration in 0 until MAX_TOOL_ITERATIONS) {
                val chatRequest = buildChatRequest(subject.model, subject.provider, currentMessages, false, reasoningEffort, tools)
                rawRequestBody = client.transformRequestBody(chatRequest)

                response = client.chatCompletion(chatRequest)

                if (response is ErrorChatResponse) {
                    throw BusinessException(failureMessage(subject.provider, response.errorMessage))
                }

                assistantMessage = response.choices.firstOrNull()?.message
                accumulatedUsage += response.usage
                totalToolCalls += assistantMessage?.toolCalls?.size ?: 0

                if (assistantMessage == null || !assistantMessage.hasToolCall) {
                    break
                }

                val executed = executeToolCalls(assistantMessage)
                toolCallResults.addAll(executed.map { it.result })
                currentMessages = currentMessages + assistantMessage + executed.map { it.toolMessage }
            }

            if (assistantMessage?.hasToolCall == true) {
                throw BusinessException("Exceeded max tool iterations")
            }

            response ?: throw BusinessException("No response from model")
        } catch (expected: Exception) {
            recordFailure(subject, expected.javaClass.simpleName, describe(expected), isStreaming = false)
            throw if (expected is BusinessException) {
                expected
            } else {
                BusinessException(failureMessage(subject.provider, describe(expected)))
            }
        }

        val assistantMessage = finalResponse.choices.firstOrNull()?.message

        launchRecording {
            invocationRecordService.recordInvocationFromContext(
                context = AiInvocationContext(
                    userId = subject.userId,
                    tenantId = subject.tenantId,
                    modelId = subject.model.id,
                    providerId = subject.provider.id,
                    messageCount = subject.messageCount,
                    toolCallsCount = totalToolCalls,
                    responseMetadataId = finalResponse.id,
                    durationMs = subject.durationMs,
                    isStreaming = false,
                    timeToFirstTokenMs = null,
                    usage = accumulatedUsage,
                    stopReason = assistantMessage?.stopReasonString,
                    rawRequestBody = rawRequestBody,
                    rawResponseBody = finalResponse.originalResponse,
                ),
                model = subject.model,
            )
        }

        return AiChatCompletionResult(
            response = ChatResponse(
                success = finalResponse.success,
                id = finalResponse.id,
                timestamp = finalResponse.timestamp,
                model = finalResponse.model,
                choices = finalResponse.choices,
                usage = accumulatedUsage,
                originalResponse = finalResponse.originalResponse,
            ),
            toolCalls = toolCallResults,
        )
    }

    override suspend fun chatCompletionAsync(
        modelId: Long,
        messages: List<ChatMessage>,
        reasoningEffort: ReasoningEffort?
    ): Flow<AiChatStreamEvent> {
        val subject = resolveSubject(modelId, messages.size)

        val client = aiLlmClientFactory.getClient(subject.provider)
        val tools = CommonAiTools.declarations

        return flow {
            var currentMessages = messages
            var accumulatedUsage = ChatResponse.Usage()
            var totalToolCalls = 0
            var firstTokenTime: Long? = null
            var rawRequestBody: String? = null
            var responseMetadataId = ""
            var stopReason: String? = null
            var rawResponseBody: String? = null

            try {
                var finalized = false

                for (iteration in 0 until MAX_TOOL_ITERATIONS) {
                    val chatRequest = buildChatRequest(subject.model, subject.provider, currentMessages, true, reasoningEffort, tools)
                    rawRequestBody = client.transformRequestBody(chatRequest)

                    val round = StreamingRound()
                    // Called here, inside the flow builder, and not before it: the client issues the
                    // HTTP request as soon as it is invoked, while only the flow it returns closes the
                    // response body. Calling it outside would leave an unconsumed body behind whenever
                    // the caller never collects.
                    client.chatCompletionAsync(chatRequest).collect { frame ->
                        if (frame is ErrorStreamChatResponse) {
                            throw BusinessException(frame.errorMessage)
                        }

                        if (firstTokenTime == null) {
                            firstTokenTime = System.currentTimeMillis()
                        }

                        round.accept(frame)
                        emit(AiChatStreamEvent.Chunk(frame))
                    }

                    // A rejected call carries no `data:` line at all — just a JSON or HTML error body —
                    // so the reader runs out of input and the flow completes normally. Producing nothing
                    // is therefore the only signal that it failed.
                    if (!round.receivedAnyFrame) {
                        throw BusinessException("provider returned no stream events")
                    }

                    accumulatedUsage += round.usage
                    totalToolCalls += round.toolCalls.size

                    if (!round.hasToolCall) {
                        responseMetadataId = round.responseId.orEmpty()
                        stopReason = round.stopReason
                        rawResponseBody = round.rawResponseBody
                        finalized = true
                        break
                    }

                    val assistantMessage = round.toAssistantMessage()
                    val executed = executeToolCalls(assistantMessage)
                    executed.forEach { emit(AiChatStreamEvent.ToolCall(it.result)) }
                    currentMessages = currentMessages + assistantMessage + executed.map { it.toolMessage }
                }

                if (!finalized) {
                    throw BusinessException("Exceeded max tool iterations")
                }
            } catch (expected: Exception) {
                recordFailure(subject, expected.javaClass.simpleName, describe(expected), isStreaming = true)
                throw expected as? BusinessException ?: BusinessException(failureMessage(subject.provider, describe(expected)))
            }

            launchRecording {
                invocationRecordService.recordInvocationFromContext(
                    context = AiInvocationContext(
                        userId = subject.userId,
                        tenantId = subject.tenantId,
                        modelId = subject.model.id,
                        providerId = subject.provider.id,
                        messageCount = subject.messageCount,
                        toolCallsCount = totalToolCalls,
                        responseMetadataId = responseMetadataId,
                        durationMs = subject.durationMs,
                        isStreaming = true,
                        timeToFirstTokenMs = firstTokenTime?.let { it - subject.startedAt },
                        usage = accumulatedUsage,
                        stopReason = stopReason,
                        rawRequestBody = rawRequestBody,
                        rawResponseBody = rawResponseBody,
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
        reasoningEffort: ReasoningEffort?,
        tools: List<ToolDeclaration>?
    ): ChatRequest {
        val modelRequestConfig = model.getRequestConfigObject<AiModelRequestConfig>()

        return ChatRequest(
            model = model.key,
            messages = messages,
            stream = stream,
            tools = tools,
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
     * Runs each tool call the model produced, feeding the result back as a [ToolChatMessage]. A tool
     * that throws is turned into an error result instead of aborting the loop, so the model can see
     * the failure and correct itself.
     */
    private fun executeToolCalls(message: AssistantChatMessage): List<ExecutedTool> {
        return message.toolCalls.orEmpty().map { call ->
            val result = try {
                CommonAiTools.execute(call.toolName, call.arguments)
            } catch (expected: Exception) {
                "Error: ${expected.message}"
            }
            ExecutedTool(
                toolMessage = ToolChatMessage(call.id, result),
                result = AiToolCallResult(call.toolName, call.arguments, result),
            )
        }
    }

    private data class ExecutedTool(
        val toolMessage: ToolChatMessage,
        val result: AiToolCallResult,
    )

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

    private operator fun ChatResponse.Usage.plus(other: ChatResponse.Usage): ChatResponse.Usage {
        return ChatResponse.Usage(
            promptTokens = promptTokens + other.promptTokens,
            completionTokens = completionTokens + other.completionTokens,
            reasoningTokens = reasoningTokens + other.reasoningTokens,
            cachedPromptTokens = cachedPromptTokens + other.cachedPromptTokens,
            cacheCreationTokens = cacheCreationTokens + other.cacheCreationTokens,
        )
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
     * Folds one stream round into the shape a tool-calling loop needs: the concatenated text and
     * the finalized tool calls. Frames are forwarded to the caller as they arrive rather than
     * buffered here, so the stream stays live across tool-calling rounds.
     *
     * Each usage field is a running total within the round, so the merged value is the largest seen
     * for that field — Anthropic splits usage across `message_start` (prompt) and `message_delta`
     * (completion), OpenAI reports everything on the final frame.
     */
    private class StreamingRound {
        val toolCalls = mutableListOf<ToolCall>()

        var receivedAnyFrame: Boolean = false
            private set

        var responseId: String? = null
            private set

        var stopReason: String? = null
            private set

        var rawResponseBody: String? = null
            private set

        var usage: ChatResponse.Usage = ChatResponse.Usage()
            private set

        private val contentBuilder = StringBuilder()
        private val reasoningBuilder = StringBuilder()

        val hasToolCall: Boolean get() = toolCalls.isNotEmpty()

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

            message.content?.let { contentBuilder.append(it) }
            message.reasoningContent?.let { reasoningBuilder.append(it) }
            message.stopReasonString?.let { stopReason = it }

            // Only a finalized (non-streaming) tool call has complete arguments; the frames carrying
            // its partial deltas are skipped.
            message.toolCalls
                ?.filter { !it.streaming }
                ?.forEach { toolCalls += it }
        }

        fun toAssistantMessage(): AssistantChatMessage {
            return AssistantChatMessage(
                content = contentBuilder.toString().ifEmpty { null },
                reasoningContent = reasoningBuilder.toString().ifEmpty { null },
                toolCalls = toolCalls.ifEmpty { null },
                stopReason = null,
                stopReasonString = null,
            )
        }
    }

    companion object {
        private const val MAX_TOOL_ITERATIONS = 5
    }
}
