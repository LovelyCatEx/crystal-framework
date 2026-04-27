package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.config.AuthConfiguration
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.auth.service.result.LoginSuccessResponseData
import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.user.service.UserService
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Service
class UserAuthorizationServiceImpl(
    private val userService: UserService,
    private val oAuthAccountService: OAuthAccountService,
    private val jwtSignKeyStore: JWTSignKeyStore,
    private val userRbacQueryService: UserRbacQueryService,
    private val authConfiguration: AuthConfiguration,
) : UserAuthorizationService {
    override suspend fun clearUserAuthorityCache(userId: Long) {
        userRbacQueryService.clearUserAuthoritiesCache(userId)
    }

    override fun buildLoginSuccessResponse(userEntity: UserEntity): LoginSuccessResponseData {
        return LoginSuccessResponseData().apply {
            val expiration = authConfiguration.jwt.expiration.toMillis()

            val signKey = jwtSignKeyStore.getSignKey()

            token = JwtUtil.buildJwtToken(
                signKey = signKey,
                subject = userEntity.username,
                authorities = userEntity.authorities.map { it.toString() }.toSet(),
                expiration = expiration
            ) {
                this.claim("userId", userEntity.id.toString())
                userEntity.getAuthenticatedTenant()?.id?.let {
                    this.claim("tenantId", it.toString())
                }
            }

            expiresIn = expiration
        }
    }

    override fun processOAuth2AuthenticationSuccess(authentication: Authentication): Mono<ApiResponse<*>> {
        return if (authentication !is OAuth2AuthenticationToken) {
            ApiResponse.internalServerError<Nothing>("could not process oauth2 login").toMono()
        } else {
            val account = mono {
                oAuthAccountService.getAccountFromOAuth2AuthenticationToken(authentication)
            }

            account
                .flatMap {
                    val userEntity = it.userId?.let {
                        userService.getRepository().findById(it)
                    }

                    if (userEntity != null) {
                        userEntity.map { userEntity ->
                            ApiResponse.success(
                                buildLoginSuccessResponse(userEntity)
                            ) as ApiResponse<*>
                        }
                    } else {
                        ApiResponse.success(mapOf(
                            "oauthAccountId" to it.id.toString(),
                            "platform" to it.getRealPlatform().name,
                            "identifier" to it.identifier,
                            "nickname" to it.nickname,
                            "avatar" to it.avatar
                        )).toMono()
                    }
                }
        }
    }
}