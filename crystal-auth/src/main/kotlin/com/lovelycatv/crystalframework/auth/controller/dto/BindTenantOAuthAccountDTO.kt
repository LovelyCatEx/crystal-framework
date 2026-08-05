package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotBlank

data class BindTenantOAuthAccountDTO(
    @field:NotBlank(message = "OAuth bind token is required")
    val oauthBindToken: String
)
