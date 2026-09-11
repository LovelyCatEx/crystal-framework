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
import jakarta.validation.Valid
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
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
    companion object {
        private const val REASONING_CONTENT_METADATA_KEY = "reasoningContent"
    }

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
        val response = aiChatService.chatCompletionSync(modelId, messages)
        val assistantMessage = response.result?.output
        val content = assistantMessage?.text ?: ""
        val reasoningContent = assistantMessage?.metadata?.get(REASONING_CONTENT_METADATA_KEY) as? String

        return ApiResponse.success(ManagerAiPlaygroundChatVO(content, reasoningContent))
    }

    private fun toMessage(dto: ManagerAiPlaygroundMessageDTO): Message = when (dto.role.lowercase()) {
        "system" -> SystemMessage(dto.content)
        "user" -> UserMessage(dto.content)
        "assistant" -> AssistantMessage(dto.content)
        else -> throw BusinessException("Unsupported AI message role: ${dto.role}")
    }
}
