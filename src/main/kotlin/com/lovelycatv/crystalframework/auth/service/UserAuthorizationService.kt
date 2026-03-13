package com.lovelycatv.crystalframework.auth.service

import com.lovelycatv.crystalframework.auth.service.result.LoginSuccessResponseData
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

interface UserAuthorizationService {
    suspend fun refreshUserAuthorityCache(userId: Long, tenantId: Long?)

    fun buildLoginSuccessResponse(userEntity: UserEntity): LoginSuccessResponseData

    fun processOAuth2AuthenticationSuccess(authentication: Authentication): Mono<ApiResponse<*>>
}