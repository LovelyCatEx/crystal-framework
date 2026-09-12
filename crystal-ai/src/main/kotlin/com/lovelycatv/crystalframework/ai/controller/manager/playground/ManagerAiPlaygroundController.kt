package com.lovelycatv.crystalframework.ai.controller.manager.playground

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.playground.dto.ManagerAiPlaygroundChatDTO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.dto.ManagerAiPlaygroundMessageDTO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundChatVO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundStreamChunkVO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundUsageVO
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.vertex.ai.llm.ChatResponse
import com.lovelycatv.vertex.ai.llm.StreamChatResponse
import com.lovelycatv.vertex.ai.llm.message.AssistantChatMessage
import com.lovelycatv.vertex.ai.llm.message.ChatMessage
import com.lovelycatv.vertex.ai.llm.message.ChatMessageType
import com.lovelycatv.vertex.ai.llm.message.SystemChatMessage
import com.lovelycatv.vertex.ai.llm.message.UserChatMessage
import jakarta.validation.Valid
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.reactor.asFlux
import org.springframework.http.codec.ServerSentEvent
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/ai/playground")
class ManagerAiPlaygroundController(
    private val aiChatService: AiChatService,
) {
    @PostMapping("/chat", version = "1")
    @RequiresAuthority(
        anyOf = [AiPermission.ACTION_SYSTEM_AI_PLAYGROUND_CHAT_NAME],
        scope = ResourceScope.SYSTEM,
    )
    suspend fun chat(
        @Valid @RequestBody dto: ManagerAiPlaygroundChatDTO,
    ): ApiResponse<ManagerAiPlaygroundChatVO> {
        val modelId = dto.modelId.toLongOrNull()?.takeIf { it > 0 }
            ?: throw BusinessException("Invalid AI model ID")

        val messages = dto.messages.map(::toMessage)
        val response = aiChatService.chatCompletionSync(modelId, messages, dto.reasoningEffort)
        val assistantMessage = response.choices.firstOrNull()?.message

        return ApiResponse.success(
            ManagerAiPlaygroundChatVO(
                content = assistantMessage?.content ?: "",
                reasoningContent = assistantMessage?.reasoningContent,
                usage = response.usage.toUsageVO(),
            )
        )
    }

    /**
     * Streams the same chat as [chat], one SSE frame per chunk.
     *
     * Authorisation is checked in-method rather than through [RequiresAuthority]: the
     * `RequiresAuthorityAspect` casts every intercepted return value to `Mono`, so an endpoint
     * returning a `Flux` would fail there with a `ClassCastException`.
     */
    @PostMapping("/chat-stream", version = "1")
    suspend fun chatStream(
        @Valid @RequestBody dto: ManagerAiPlaygroundChatDTO,
    ): Flux<ServerSentEvent<ManagerAiPlaygroundStreamChunkVO>> {
        val modelId = dto.modelId.toLongOrNull()?.takeIf { it > 0 }
            ?: throw BusinessException("Invalid AI model ID")

        if (!RbacUtils.hasAnyAuthority(AiPermission.ACTION_SYSTEM_AI_PLAYGROUND_CHAT_NAME)) {
            throw ForbiddenException(
                "Access denied",
                context = ForbiddenContext(
                    reason = ForbiddenReason.MISSING_PERMISSION,
                    requiredPermissions = listOf(AiPermission.ACTION_SYSTEM_AI_PLAYGROUND_CHAT_NAME),
                    scope = ResourceScope.SYSTEM,
                ),
            )
        }

        val messages = dto.messages.map(::toMessage)

        return aiChatService.chatCompletionAsync(modelId, messages, dto.reasoningEffort)
            .scan<StreamChatResponse, StreamFrame?>(null) { acc, chunk ->
                StreamFrame(chunk, mergeUsage(acc?.usage, chunk.usage))
            }
            .filterNotNull()
            .map { frame ->
                val message = frame.chunk.choices.firstOrNull()?.message
                ServerSentEvent.builder(
                    ManagerAiPlaygroundStreamChunkVO(
                        content = message?.content,
                        reasoningContent = message?.reasoningContent,
                        finished = frame.chunk.finished,
                        usage = frame.usage.toUsageVO(),
                    )
                ).build()
            }
            .asFlux()
    }

    private fun toMessage(dto: ManagerAiPlaygroundMessageDTO): ChatMessage {
        val type = ChatMessageType.entries.firstOrNull { it.name.equals(dto.role, ignoreCase = true) }
            ?: throw BusinessException("Unsupported AI message role: ${dto.role}")

        return when (type) {
            ChatMessageType.SYSTEM -> SystemChatMessage(dto.content)

            ChatMessageType.USER -> UserChatMessage(dto.content)

            ChatMessageType.ASSISTANT -> AssistantChatMessage(
                content = dto.content,
                reasoningContent = null,
                toolCalls = null,
                stopReason = null,
                stopReasonString = null,
            )

            // Tool results belong to a request the caller is building from a model's tool call, not
            // to a message a person typed into the playground.
            ChatMessageType.TOOL -> throw BusinessException("Unsupported AI message role: ${dto.role}")
        }
    }

    private fun ChatResponse.Usage.toUsageVO() = ManagerAiPlaygroundUsageVO(
        promptTokens = promptTokens,
        completionTokens = completionTokens,
        reasoningTokens = reasoningTokens,
        cachedPromptTokens = cachedPromptTokens,
        cacheCreationTokens = cacheCreationTokens,
    )

    /**
     * Streaming usage is split across frames (Anthropic reports prompt on `message_start` and
     * completion on `message_delta`; OpenAI reports everything on the final frame). Each field is a
     * running total, so the merged value is the largest seen for that field.
     */
    private fun mergeUsage(acc: ChatResponse.Usage?, current: ChatResponse.Usage): ChatResponse.Usage {
        return ChatResponse.Usage(
            promptTokens = maxOf(acc?.promptTokens ?: 0, current.promptTokens),
            completionTokens = maxOf(acc?.completionTokens ?: 0, current.completionTokens),
            reasoningTokens = maxOf(acc?.reasoningTokens ?: 0, current.reasoningTokens),
            cachedPromptTokens = maxOf(acc?.cachedPromptTokens ?: 0, current.cachedPromptTokens),
            cacheCreationTokens = maxOf(acc?.cacheCreationTokens ?: 0, current.cacheCreationTokens),
        )
    }

    private data class StreamFrame(
        val chunk: StreamChatResponse,
        val usage: ChatResponse.Usage,
    )
}
