package com.lovelycatv.crystalframework.auth.service

import com.lovelycatv.crystalframework.auth.service.result.LoginSuccessResponseData
import com.lovelycatv.crystalframework.auth.types.ProcessOAuth2AuthenticationSuccessResult
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

interface UserAuthorizationService {
    suspend fun clearUserAuthorityCache(userId: Long)

    fun buildLoginSuccessResponse(userEntity: UserEntity): LoginSuccessResponseData

    fun processOAuth2AuthenticationSuccess(authentication: Authentication): Mono<ProcessOAuth2AuthenticationSuccessResult>
}