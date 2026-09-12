package com.lovelycatv.crystalframework.ai.controller.manager.playground

import com.lovelycatv.crystalframework.ai.constants.AiPermission
import com.lovelycatv.crystalframework.ai.controller.manager.playground.dto.ManagerAiPlaygroundChatDTO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.dto.ManagerAiPlaygroundMessageDTO
import com.lovelycatv.crystalframework.ai.controller.manager.playground.vo.ManagerAiPlaygroundChatVO
import com.lovelycatv.crystalframework.ai.service.AiChatService
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.vertex.ai.llm.message.AssistantChatMessage
import com.lovelycatv.vertex.ai.llm.message.ChatMessage
import com.lovelycatv.vertex.ai.llm.message.ChatMessageType
import com.lovelycatv.vertex.ai.llm.message.SystemChatMessage
import com.lovelycatv.vertex.ai.llm.message.UserChatMessage
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        val assistantMessage = aiChatService.chatCompletionSync(modelId, messages, dto.reasoningEffort)
            .choices
            .firstOrNull()
            ?.message

        return ApiResponse.success(
            ManagerAiPlaygroundChatVO(
                content = assistantMessage?.content ?: "",
                reasoningContent = assistantMessage?.reasoningContent,
            )
        )
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
}
