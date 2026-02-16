package com.lovelycatv.template.springboot.auth.config

import com.lovelycatv.template.springboot.auth.filter.CustomLoginFilter
import com.lovelycatv.template.springboot.shared.response.ApiResponse
import com.lovelycatv.template.springboot.shared.utils.toJSONString
import com.lovelycatv.vertex.log.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
@EnableWebFlux
@EnableWebFluxSecurity
class SecurityConfig(
    private val unauthorizedPathScanner: UnauthorizedPathScanner,
    private val reactiveAuthenticationManager: ReactiveAuthenticationManager
) {
    private val logger = logger()

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
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

        // custom authentications
        http.authorizeExchange { authorizeExchangeSpec ->
            // Unauthorized endpoints
            val unauthorizedEndpoints = unauthorizedPathScanner
                .getUnauthorizedEndpointsSimple()

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

        return http.build()
    }
}