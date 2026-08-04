package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BindOAuthByAccountIdDTO(
    @field:NotBlank(message = "OAuth bind token is required")
    val oauthBindToken: String,

    @field:NotNull(message = "Binding scope is required")
    val scope: Int,
)
