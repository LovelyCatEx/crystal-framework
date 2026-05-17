package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetEmailDTO(
    @field:NotBlank(message = "Email verification code is required")
    val emailCode: String,

    @field:NotBlank(message = "New email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val newEmail: String
)
