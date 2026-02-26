package com.lovelycatv.template.springboot.auth.config

import com.lovelycatv.template.springboot.auth.filter.CustomAuthFilter
import com.lovelycatv.template.springboot.auth.filter.CustomLoginFilter
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.service.redis.RedisService
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import com.lovelycatv.template.springboot.user.service.UserService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Mono
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
) {
    private val logger = logger()

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity, redisService: RedisService): SecurityWebFilterChain {
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
            .getUnauthorizedEndpointsSimple()

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
                .pathMatchers(*(listOf("/health") + unauthorizedEndpoints).toTypedArray())
                .permitAll()
                .anyExchange()
                .authenticated()
        }

        // custom login filter
        http.addFilterAt(
            CustomLoginFilter("/api/v1/user/login", reactiveAuthenticationManager),
            SecurityWebFiltersOrder.AUTHENTICATION
        )

        // custom auth filter
        http.addFilterAfter(
            CustomAuthFilter(
                unauthorizedEndpoints = unauthorizedEndpoints.map {
                    it.replace("{version}", "v1")
                }
            ) {
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
            SecurityWebFiltersOrder.AUTHENTICATION
        )


        return http.build()
    }
}