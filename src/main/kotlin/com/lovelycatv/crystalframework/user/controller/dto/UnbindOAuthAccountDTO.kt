package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.NotNull

data class UnbindOAuthAccountDTO(
    @field:NotNull(message = "OAuth account ID is required")
    val oauthAccountId: Long
)
