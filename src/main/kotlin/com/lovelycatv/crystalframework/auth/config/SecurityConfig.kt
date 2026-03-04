package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.filter.CustomAuthFilter
import com.lovelycatv.crystalframework.auth.filter.CustomLoginFilter
import com.lovelycatv.crystalframework.auth.service.UserAuthorizationService
import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.crystalframework.user.converter.OAuth2AuthenticationTokenAccountConverterManager
import com.lovelycatv.crystalframework.user.service.OAuthAccountService
import com.lovelycatv.crystalframework.user.service.UserService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.util.StringUtils
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.time.Duration


@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val unauthorizedPathScanner: UnauthorizedPathScanner,
    private val reactiveAuthenticationManager: ReactiveAuthenticationManager,
    private val userService: UserService,
    private val oauth2ClientProperties: OAuth2ClientProperties,
    private val reactiveClientRegistrationRepository: ReactiveClientRegistrationRepository,
) {
    private val logger = logger()
    private val pathPatternParser = PathPatternParser()

    @Bean
    fun securityWebFilterChain(
        http: ServerHttpSecurity,
        redisService: RedisService,
        userAuthorizationService: UserAuthorizationService,
        jwtSignKeyStore: JWTSignKeyStore
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
                getUserAuthorities = {
                    val redisKey = "userAuthorities:$it"
                    runBlocking(Dispatchers.IO) {
                        val cache = redisService
                            .get<String>(redisKey)
                            .awaitFirstOrNull()
                            ?.split(",")

                        cache?.map { GrantedAuthority { it } }
                            ?: userService
                                .getUserRbacAccessInfo(it)
                                .actions
                                .also {
                                    redisService.set(
                                        redisKey,
                                        it.joinToString(
                                            separator = ",",
                                            prefix = "",
                                            postfix = ""
                                        ) { it.name },
                                        Duration.ofDays(3)
                                    ).awaitFirstOrNull()
                                }
                                .map { GrantedAuthority { it.name } }

                    }
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