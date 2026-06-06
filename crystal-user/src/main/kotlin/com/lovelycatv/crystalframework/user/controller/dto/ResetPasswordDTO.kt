package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ResetPasswordDTO(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String,

    @field:NotBlank(message = "Email verification code is required")
    val emailCode: String,

    @field:NotBlank(message = "New password is required")
    @field:Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "Password must be at least 8 characters and contain both letters and numbers")
    @field:Size(max = 128, message = "Password length cannot exceed 128 characters")
    val newPassword: String
)
