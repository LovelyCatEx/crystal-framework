




package com.lovelycatv.crystalframework.user.controller.dto

data class RegisterFromOAuthAccountDTO(
    val oauthAccountId: Long,
    val username: String,
    val password: String,
    val nickname: String
)
