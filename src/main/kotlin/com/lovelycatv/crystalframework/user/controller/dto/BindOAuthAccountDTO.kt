package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class BindOAuthAccountDTO(
    val oauthAccountId: Long,

    @field:NotBlank(message = "Username or email is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Username can only contain letters, numbers, underscores, hyphens, dots, and @")
    val username: String?,

    @field:NotBlank(message = "Password is required")
    val password: String?,
)
