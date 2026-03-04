package com.lovelycatv.crystalframework.user.controller.dto

data class BindOAuthAccountDTO(
    val oauthAccountId: Long,
    val username: String?,
    val password: String?,
)
