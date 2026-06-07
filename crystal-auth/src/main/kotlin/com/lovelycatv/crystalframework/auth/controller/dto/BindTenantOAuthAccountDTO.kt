package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotNull

data class BindTenantOAuthAccountDTO(
    @field:NotNull(message = "OAuth account ID is required")
    val oauthAccountId: Long
)
