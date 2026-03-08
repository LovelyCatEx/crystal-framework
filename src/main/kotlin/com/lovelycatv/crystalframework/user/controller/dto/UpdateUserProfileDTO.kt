package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateUserProfileDTO(
    @field:NotBlank(message = "Nickname is required")
    @field:Size(max = 32, message = "Nickname length cannot exceed 32 characters")
    val nickname: String?,
)
