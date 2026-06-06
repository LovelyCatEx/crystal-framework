package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterFromOAuthAccountDTO(
    val oauthAccountId: Long,

    @field:NotBlank(message = "Username is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    @field:Size(max = 64, message = "Username length cannot exceed 64 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(max = 256, message = "Password length cannot exceed 256 characters")
    val password: String,

    @field:NotBlank(message = "Nickname is required")
    @field:Size(max = 32, message = "Nickname length cannot exceed 32 characters")
    val nickname: String
)
