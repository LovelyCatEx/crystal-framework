package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class BindOAuthAccountDTO(
    val oauthAccountId: Long,

    @field:Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Username can only contain letters, numbers, underscores, hyphens, dots, and @")
    val username: String?,

    val password: String?,
)
