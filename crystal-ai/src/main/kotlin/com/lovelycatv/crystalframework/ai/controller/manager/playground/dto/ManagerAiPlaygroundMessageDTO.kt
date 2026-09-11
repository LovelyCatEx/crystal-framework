package com.lovelycatv.crystalframework.ai.controller.manager.playground.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerAiPlaygroundMessageDTO(
    @field:NotBlank(message = "Message role is required")
    val role: String,
    @field:NotBlank(message = "Message content is required")
    @field:Size(max = 100_000, message = "Message content cannot exceed 100000 characters")
    val content: String,
)
