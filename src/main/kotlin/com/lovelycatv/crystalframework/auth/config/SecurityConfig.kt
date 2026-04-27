package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.filter.CustomAuthFilter
import com.lovelycatv.crystalframework.auth.filter.CustomLoginFilter
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.service.UserRbacQueryService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
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
    private val oauth2ClientProperties: OAuth2ClientProperties,
) {
    private val logger = logger()
    private val pathPatternParser = PathPatternParser()

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        redisService: RedisService,
        userAuthorizationService: UserAuthorizationService,
        jwtSignKeyStore: JWTSignKeyStore, userRbacQueryService: UserRbacQueryService
    ): SecurityWebFilterChain {
        http.exceptionHandling { exceptionHandlingSpec ->
            exceptionHandlingSpec.authenticationEntryPoint { exchange, exception ->
                logger.warn("Authentication failure: ${exception.localizedMessage}", exception)

                exchange.response.statusCode = HttpStatus.OK
                exchange.response.headers.set("Content-Type", "application/json")
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
                exchange.response.headers.set("Content-Type", "application/json")
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
                exchange.exchange.response.headers.set("Content-Type", "application/json")

                val result = userAuthorizationService.processOAuth2AuthenticationSuccess(authentication)

                exchange.exchange.response.writeWith(
                    result.map {
                        exchange.exchange.response.bufferFactory().wrap(
                            it.toJSONString().toByteArray()
                        )
                    }
                )
            }

            it.authenticationFailureHandler { exchange, exception ->
                logger.warn("OAuth2 authorization failed", exception)

                exchange.exchange.response.statusCode = HttpStatus.OK
                exchange.exchange.response.headers.set("Content-Type", "application/json")
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
                oauth2ClientProperties.registration.keys.flatMap {
                    listOf(
                        pathPatternParser.parse("/login/oauth2/code/${it}"),
                        pathPatternParser.parse("/oauth2/authorization/${it}")
                    )
                } + listOf(pathPatternParser.parse("/login"))

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
                userAuthorizationService
            ),
            SecurityWebFiltersOrder.AUTHENTICATION
        )

        // custom auth filter
        http.addFilterAfter(
            CustomAuthFilter(
                unauthorizedPathPatterns = unauthorizedPathPatterns,
                getUserAuthorities = { userId, tenantId ->
                    userRbacQueryService.getUserAuthorities(userId, tenantId)
                },
                getJWTSignKey = {
                    jwtSignKeyStore.getSignKey()
                }
            ),
            SecurityWebFiltersOrder.AUTHENTICATION
        )


        return http.build()
    }
}