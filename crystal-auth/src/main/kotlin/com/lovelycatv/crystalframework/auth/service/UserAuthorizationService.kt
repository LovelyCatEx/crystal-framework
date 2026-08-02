package com.lovelycatv.crystalframework.auth.service

import com.lovelycatv.crystalframework.auth.service.result.LoginSuccessResponseData
import com.lovelycatv.crystalframework.auth.types.ProcessOAuth2AuthenticationSuccessResult
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

interface UserAuthorizationService {
    suspend fun clearUserAuthorityCache(userId: Long)

    /**
     * Single convergent login guard that every token-issuing path must pass. Rejects banned
     * accounts ([com.lovelycatv.crystalframework.shared.exception.AccountBannedException]) and
     * disabled accounts ([org.springframework.security.authentication.DisabledException]).
     *
     * Invoked inside [buildLoginSuccessResponse] so no token can be issued to a banned/disabled
     * user regardless of which flow (password / OAuth2 login / OAuth binding / tenant switch)
     * reaches it. Also reused by the password path ([com.lovelycatv.crystalframework.auth.service.impl.CustomUserDetailsService])
     * so the failure can surface through Spring Security's failure handler.
     */
    suspend fun assertLoginable(userEntity: UserEntity)

    suspend fun buildLoginSuccessResponse(userEntity: UserEntity): LoginSuccessResponseData

    fun processOAuth2AuthenticationSuccess(authentication: Authentication): Mono<ProcessOAuth2AuthenticationSuccessResult>
}