package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotNull

data class BindOAuthByAccountIdDTO(
    @field:NotNull(message = "OAuth account ID is required")
    val oauthAccountId: Long,

    @field:NotNull(message = "Binding scope is required")
    val scope: Int,
)
