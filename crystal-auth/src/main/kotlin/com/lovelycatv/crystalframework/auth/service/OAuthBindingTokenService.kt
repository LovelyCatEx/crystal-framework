package com.lovelycatv.crystalframework.auth.service

interface OAuthBindingTokenService {
    suspend fun issue(oauthAccountId: Long): String

    suspend fun consume(token: String): Long
}
