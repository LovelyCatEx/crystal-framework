package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

 data class BindOAuthAccountDTO(
    @field:NotBlank(message = "OAuth bind token is required")
    val oauthBindToken: String,

    @field:Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Username can only contain letters, numbers, underscores, hyphens, dots, and @")
    val username: String?,

    val password: String?,
)
