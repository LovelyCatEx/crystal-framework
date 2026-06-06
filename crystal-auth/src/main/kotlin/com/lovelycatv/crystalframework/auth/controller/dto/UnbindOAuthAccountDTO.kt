package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotNull

data class UnbindOAuthAccountDTO(
    @field:NotNull(message = "OAuth account ID is required")
    val oauthAccountId: Long
)
