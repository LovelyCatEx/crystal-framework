package com.lovelycatv.crystalframework.auth.filter

import com.lovelycatv.crystalframework.auth.event.LoginMethod
import com.lovelycatv.crystalframework.auth.event.UserLoginEvent
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class CustomLoginFilter(
    defaultFilterProcessesUrl: String,
    authenticationManager: ReactiveAuthenticationManager,
    userAuthorizationService: UserAuthorizationService,
    private val eventPublisher: ApplicationEventPublisher
) : AuthenticationWebFilter(authenticationManager) {
    private val logger = logger()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        setRequiresAuthenticationMatcher(
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, defaultFilterProcessesUrl)
        )

        setServerAuthenticationConverter { exchange ->
            exchange.formData
                .flatMap { params ->
                    val username = params["username"]?.firstOrNull()
                    val password = params["password"]?.firstOrNull()
                    val tenantId = params["tenantId"]?.firstOrNull() ?: 0

                    if (username == null || password == null) {
                        return@flatMap Mono.error(BusinessException("username or password is missing"))
                    }

                    UsernamePasswordAuthenticationToken(
                        "${username}:${tenantId}",
                        password,
                    ).toMono()
                }
        }

        setAuthenticationSuccessHandler { exchange, authentication ->
            val loggedUser = authentication.principal as UserEntity

            val data = userAuthorizationService.buildLoginSuccessResponse(loggedUser)

            logger.info("User ${loggedUser.username}#${loggedUser.id} is logged in with password")

            coroutineScope.launch {
                userAuthorizationService.clearUserAuthorityCache(loggedUser.id)
            }

            val remoteIp = exchange.exchange.request.remoteAddress?.address?.hostAddress
            val userAgent = exchange.exchange.request.headers.getFirst("User-Agent")

            eventPublisher.publishEvent(
                UserLoginEvent(
                    source = this,
                    userId = loggedUser.id,
                    username = loggedUser.username,
                    tenantId = loggedUser.getAuthenticatedTenant()?.id,
                    loginMethod = LoginMethod.PASSWORD.code,
                    oauth2Type = null,
                    oauth2Username = null,
                    oauth2AccountId = null,
                    success = true,
                    errorMessage = null,
                    remoteIp = remoteIp,
                    userAgent = userAgent
                )
            )

            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    ApiResponse.success(data).toJSONString().toByteArray()
                ).toMono()
            )
        }

        setAuthenticationFailureHandler { exchange, exception ->
            val remoteIp = exchange.exchange.request.remoteAddress?.address?.hostAddress
            val userAgent = exchange.exchange.request.headers.getFirst("User-Agent")

            eventPublisher.publishEvent(
                UserLoginEvent(
                    source = this,
                    userId = null,
                    username = null,
                    tenantId = null,
                    loginMethod = LoginMethod.PASSWORD.code,
                    oauth2Type = null,
                    oauth2Username = null,
                    oauth2AccountId = null,
                    success = false,
                    errorMessage = exception.localizedMessage,
                    remoteIp = remoteIp,
                    userAgent = userAgent
                )
            )

            exchange.exchange.response.statusCode = HttpStatus.OK
            exchange.exchange.response.writeWith(
                exchange.exchange.response.bufferFactory().wrap(
                    ApiResponse
                        .unauthorized<Nothing>(exception.localizedMessage)
                        .toJSONString()
                        .toByteArray()
                ).toMono()
            )
        }
    }
}