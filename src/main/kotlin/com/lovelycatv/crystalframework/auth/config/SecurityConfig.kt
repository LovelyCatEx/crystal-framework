package com.lovelycatv.crystalframework.auth.config

import com.google.protobuf.Api
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
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.relational.core.sql.Not
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.server.ServerAuthorizationRequestRepository
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.util.StringUtils
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.util.UriComponentsBuilder
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

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity, redisService: RedisService,
                               oAuth2AuthenticationTokenAccountConverterManager: OAuth2AuthenticationTokenAccountConverterManager,
                               oAuthAccountService: OAuthAccountService,
                               userAuthorizationService: UserAuthorizationService, jwtSignKeyStore: JWTSignKeyStore
    ): SecurityWebFilterChain {
        http.exceptionHandling { exceptionHandlingSpec ->
            exceptionHandlingSpec.authenticationEntryPoint { exchange, exception ->
                logger.warn("Authentication failure: ${exception.localizedMessage}", exception)

                exchange.response.statusCode = HttpStatus.OK
                exchange.response.headers.set("Content-Type", "application/json")
                exchange.response.writeWith(
                    Mono.just(exchange.response.bufferFactory().wrap(
                        ApiResponse
                            .unauthorized<Nothing>("invalid token")
                            .toJSONString()
                            .toByteArray()
                    ))
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
            it.authorizationRequestResolver(
                noStateServerOAuth2AuthorizationRequestResolver()
            )

            it.authorizationRequestRepository(
                noStateAuthorizationRequestRepository()
            )

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

        // Unauthorized endpoints
        val unauthorizedEndpoints = unauthorizedPathScanner
            .getUnauthorizedEndpointsSimple() + oauth2ClientProperties.registration.keys.flatMap {
                listOf(
                    "/login/oauth2/code/${it}",
                    "/login/oauth2/authorization/${it}"
                )
            } + listOf("/login")

        // custom authentications
        http.authorizeExchange { authorizeExchangeSpec ->
            if (unauthorizedEndpoints.isNotEmpty()) {
                logger.info("================================================================")
                unauthorizedEndpoints.forEach {
                    logger.info("+ Unauthorized endpoint: $it")
                }
                logger.info("================================================================")
            }

            authorizeExchangeSpec
                .pathMatchers(*(listOf("/login/oauth2/code/**") + unauthorizedEndpoints).toTypedArray())
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
                unauthorizedEndpoints = unauthorizedEndpoints.map {
                    it.replace("{version}", "v1")
                },
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

    @Bean
    fun noStateServerOAuth2AuthorizationRequestResolver(): ServerOAuth2AuthorizationRequestResolver {
        return object : DefaultServerOAuth2AuthorizationRequestResolver(reactiveClientRegistrationRepository) {
            override fun resolve(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest> {
                return super.resolve(exchange)
                    .map { request ->
                        OAuth2AuthorizationRequest.Builder()
                            .authorizationUri(request.authorizationUri)
                            .clientId(request.clientId)
                            .redirectUri(request.redirectUri)
                            .scopes(request.scopes)
                            .state("")
                            .parameters({ params ->
                                params.putAll(request.additionalParameters)
                                // Remove state
                                params.remove("state")
                            })
                            .build()
                    }
            }
        }
    }

    @Bean
    fun noStateAuthorizationRequestRepository(): ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
        return object : ServerAuthorizationRequestRepository<OAuth2AuthorizationRequest> {
            override fun loadAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest> {
                return Mono.empty()
            }

            override fun saveAuthorizationRequest(
                authorizationRequest: OAuth2AuthorizationRequest,
                exchange: ServerWebExchange
            ): Mono<Void> {
                return Mono.empty()
            }

            override fun removeAuthorizationRequest(exchange: ServerWebExchange): Mono<OAuth2AuthorizationRequest> {
                val client = reactiveClientRegistrationRepository.findByRegistrationId(
                    extractRegistrationIdFromPath(
                        exchange.request.path.value()
                    )
                )

                return client
                    .map { client ->
                        val redirectUrl = expandRedirectUri(exchange.request, client)

                        OAuth2AuthorizationRequest.Builder()
                            .authorizationUri(client.providerDetails.authorizationUri)
                            .clientId(client.clientId)
                            .redirectUri(redirectUrl)
                            .scopes(client.scopes)
                            .attributes(mapOf(
                                OAuth2ParameterNames.REGISTRATION_ID to client.registrationId
                            ))
                            .state("")
                            .build()
                    }
                    .switchIfEmpty {
                        Mono.error(BusinessException("OAuth2 client registration not found"))
                    }
            }
        }
    }

    private fun extractRegistrationIdFromPath(path: String): String {
        val pattern = "/login/oauth2/code/([^/]+)".toRegex()
        return pattern.find(path)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Could not extract registrationId from path: $path")
    }

    private fun expandRedirectUri(request: ServerHttpRequest, clientRegistration: ClientRegistration): String {
        val uriVariables: MutableMap<String, String> = mutableMapOf()
        uriVariables["registrationId"] = clientRegistration.registrationId

        val uriComponents = UriComponentsBuilder.fromUri(request.uri)
            .replacePath(request.path.contextPath().value())
            .replaceQuery(null)
            .fragment(null)
            .build()

        val scheme = uriComponents.scheme
        uriVariables["baseScheme"] = scheme ?: ""
        val host = uriComponents.host
        uriVariables["baseHost"] = host ?: ""

        // following logic is based on HierarchicalUriComponents#toUriString()
        val port = uriComponents.port
        uriVariables["basePort"] = if (port == -1) "" else ":$port"
        var path = uriComponents.path
        if (StringUtils.hasLength(path)) {
            if (path!![0] != '/') {
                path = "/$path"
            }
        }
        uriVariables["basePath"] = path ?: ""
        uriVariables["baseUrl"] = uriComponents.toUriString()
        var action = ""
        if (AuthorizationGrantType.AUTHORIZATION_CODE == clientRegistration.authorizationGrantType) {
            action = "login"
        }
        uriVariables["action"] = action
        return UriComponentsBuilder.fromUriString(clientRegistration.redirectUri)
            .buildAndExpand(uriVariables)
            .toUriString()
    }

}