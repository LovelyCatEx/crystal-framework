package com.lovelycatv.crystalframework.ai.controller.manager.playground.dto

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
)
