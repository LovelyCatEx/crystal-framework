package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserRegisterDTO(
    @field:NotBlank(message = "Username is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @field:Size(max = 64, message = "Username length cannot exceed 64 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$", message = "Password must be at least 8 characters and contain both letters and numbers")
    @field:Size(max = 128, message = "Password length cannot exceed 128 characters")
    val password: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String,

    @field:NotBlank(message = "Email verification code is required")
    val emailCode: String
)
