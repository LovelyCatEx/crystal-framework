package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.user.converters.types.ClientRegistrationIdOAuthPlatformConverter
import com.lovelycatv.crystalframework.auth.event.LoginMethod
import com.lovelycatv.crystalframework.auth.event.UserLoginEvent
import com.lovelycatv.crystalframework.auth.filter.CustomAuthFilter
import com.lovelycatv.crystalframework.auth.filter.CustomLoginFilter
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.shared.types.auth.OAuthPlatform
import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.vertex.log.logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono


@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val unauthorizedPathScanner: UnauthorizedPathScanner,
    private val reactiveAuthenticationManager: ReactiveAuthenticationManager,
    private val eventPublisher: ApplicationEventPublisher,
    private val clientRegistrationIdOAuthPlatformConverter: ClientRegistrationIdOAuthPlatformConverter,
) {
    private val logger = logger()
    private val pathPatternParser = PathPatternParser()

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        userAuthorizationService: UserAuthorizationService,
        jwtSignKeyStore: JWTSignKeyStore,
        userRbacQueryService: UserRbacQueryService
    ): SecurityWebFilterChain {
        http.exceptionHandling { exceptionHandlingSpec ->
            exceptionHandlingSpec.authenticationEntryPoint { exchange, exception ->
                logger.warn("Authentication failure: ${exception.localizedMessage}", exception)

                exchange.response.statusCode = HttpStatus.OK
                exchange.response.headers.set(CONTENT_TYPE, APPLICATION_JSON)
                exchange.response.writeWith(
                    Mono.just(
                        exchange.response.bufferFactory().wrap(
                            ApiResponse
                                .unauthorized<Nothing>("invalid token")
                                .toJSONString()
                                .toByteArray()
                        )
                    )
                )
            }

            exceptionHandlingSpec.accessDeniedHandler { exchange, exception ->
                logger.warn("Access denied: ${exchange.request.path}", exception)

                exchange.response.statusCode = HttpStatus.OK
                exchange.response.headers.set(CONTENT_TYPE, APPLICATION_JSON)
                exchange.response.writeWith(
                    exchange.response.bufferFactory().wrap(
                        ApiResponse
                            .forbidden<Nothing>("access denied")
                            .toJSONString()
                            .toByteArray()
                    ).toMono()
                )
            }
        }

        http.oauth2Login {
            it.authenticationSuccessHandler { exchange, authentication ->
                exchange.exchange.response.statusCode = HttpStatus.OK
                exchange.exchange.response.headers.set(CONTENT_TYPE, APPLICATION_JSON)

                val result = userAuthorizationService.processOAuth2AuthenticationSuccess(authentication)

                result.subscribe { responseData ->
                    val remoteIp = exchange.exchange.request.remoteAddress?.address?.hostAddress
                    val userAgent = exchange.exchange.request.headers.getFirst("User-Agent")

                    val oauth2Token = authentication as OAuth2AuthenticationToken
                    val clientRegistrationId = oauth2Token.authorizedClientRegistrationId
                    val oauthPlatform = clientRegistrationIdOAuthPlatformConverter.convert(clientRegistrationId)
                    val oauth2Username = responseData.oauth2Account?.nickname
                    val oauth2AccountId = responseData.oauth2Account?.id

                    eventPublisher.publishEvent(
                        UserLoginEvent(
                            source = this,
                            userId = responseData.user?.id,
                            username = responseData.user?.username,
                            tenantId = null,
                            loginMethod = LoginMethod.OAUTH2.code,
                            oauth2Type = oauthPlatform?.typeId,
                            oauth2Username = oauth2Username,
                            oauth2AccountId = oauth2AccountId,
                            success = true,
                            errorMessage = null,
                            remoteIp = remoteIp,
                            userAgent = userAgent
                        )
                    )
                }

                exchange.exchange.response.writeWith(
                    result.map {
                        exchange.exchange.response.bufferFactory().wrap(
                            it.response.toJSONString().toByteArray()
                        )
                    }
                )
            }

            it.authenticationFailureHandler { exchange, exception ->
                logger.warn("OAuth2 authorization failed", exception)

                val remoteIp = exchange.exchange.request.remoteAddress?.address?.hostAddress
                val userAgent = exchange.exchange.request.headers.getFirst("User-Agent")

                eventPublisher.publishEvent(
                    UserLoginEvent(
                        source = this,
                        userId = null,
                        username = null,
                        tenantId = null,
                        loginMethod = LoginMethod.OAUTH2.code,
                        oauth2Type = null,
                        oauth2Username = null,
                        oauth2AccountId = null,
                        success = false,
                        errorMessage = exception.localizedMessage ?: exception.message ?: "OAuth2 authorization failed",
                        remoteIp = remoteIp,
                        userAgent = userAgent
                    )
                )

                exchange.exchange.response.statusCode = HttpStatus.OK
                exchange.exchange.response.headers.set(CONTENT_TYPE, APPLICATION_JSON)
                exchange.exchange.response.writeWith(
                    exchange.exchange.response.bufferFactory().wrap(
                        ApiResponse
                            .unauthorized<Nothing>(
                                exception.localizedMessage
                                    ?: exception.message
                                    ?: "could not fetch user information"
                            )
                            .toJSONString()
                            .toByteArray()
                    ).toMono()
                )
            }
        }

        // using auth module
        http.formLogin { it.disable() }

        // no basic auth
        http.httpBasic { it.disable() }

        // no cors
        http.cors { it.disable() }

        // no csrf
        http.csrf { it.disable() }

        // Build path patterns for matching
        val unauthorizedPathPatterns = unauthorizedPathScanner.getUnauthorizedPathPatterns() +
                OAuthPlatform.entries.flatMap { platform ->
                    val registrationId = platform.name.lowercase()
                    listOf(
                        pathPatternParser.parse("/login/oauth2/code/${registrationId}"),
                        pathPatternParser.parse("/oauth2/authorization/${registrationId}")
                    )
                } +
                listOf(
                    pathPatternParser.parse("/login"),
                    pathPatternParser.parse("/api/*/actuator/**")
                )

        // custom authentications
        http.authorizeExchange { authorizeExchangeSpec ->
            if (unauthorizedPathPatterns.isNotEmpty()) {
                logger.info("================================================================")
                unauthorizedPathPatterns.map { it.patternString }.forEach {
                    logger.info("+ Unauthorized endpoint: $it")
                }
                logger.info("================================================================")
            }

            authorizeExchangeSpec
                .pathMatchers(*(listOf("/login/oauth2/code/**") + unauthorizedPathPatterns.map { it.patternString }).toTypedArray())
                .permitAll()
                .anyExchange()
                .authenticated()
        }

        // custom login filter
        http.addFilterAfter(
            CustomLoginFilter(
                "/api/v1/user/login",
                reactiveAuthenticationManager,
                userAuthorizationService,
                eventPublisher
            ),
            SecurityWebFiltersOrder.AUTHENTICATION
        )

        // custom auth filter
        http.addFilterAfter(
            CustomAuthFilter(
                unauthorizedPathPatterns = unauthorizedPathPatterns,
                getUserAuthorities = { userId, tenantId, tenantMemberId ->
                    userRbacQueryService.getUserAuthorities(userId, tenantId, tenantMemberId,false)
                },
                getJWTSignKey = {
                    jwtSignKeyStore.getSignKey()
                }
            ),
            SecurityWebFiltersOrder.AUTHENTICATION
        )


        return http.build()
    }

    companion object {
        const val CONTENT_TYPE = "Content-Type"
        const val APPLICATION_JSON = "application/json"
    }
}