package com.lovelycatv.crystalframework.ai.controller.manager.playground.dto

import com.lovelycatv.vertex.ai.llm.ReasoningEffort
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class ManagerAiPlaygroundChatDTO(
    @field:NotBlank(message = "Model ID is required")
    val modelId: String,
    @field:NotEmpty(message = "Messages are required")
    @field:Size(max = 100, message = "Messages cannot exceed 100 items")
    @field:Valid
    val messages: List<ManagerAiPlaygroundMessageDTO>,
    /**
     * Left unset to keep whatever the provider's protocol picks by default. The enum carries no
     * `typeId`, so it travels by name the way `ForbiddenReason` does.
     */
    val reasoningEffort: ReasoningEffort? = null,
)
