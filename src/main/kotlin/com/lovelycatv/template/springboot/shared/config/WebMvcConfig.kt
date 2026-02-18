package com.lovelycatv.template.springboot.shared.config

import com.lovelycatv.template.springboot.shared.types.UserAuthentication
import com.lovelycatv.template.springboot.shared.utils.JwtUtil
import com.lovelycatv.template.springboot.user.service.UserService
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.ApiVersionConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
@EnableWebFlux
class WebMvcConfig(
    private val userService: UserService
) : WebFluxConfigurer {
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.setDefaultVersion("1")
        configurer.usePathSegment(1)
        super.configureApiVersioning(configurer)
    }

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(object : HandlerMethodArgumentResolver {
            override fun supportsParameter(parameter: MethodParameter): Boolean {
                return parameter.parameterType == UserAuthentication::class.java
            }

            override fun resolveArgument(
                parameter: MethodParameter,
                bindingContext: BindingContext,
                exchange: ServerWebExchange,
            ): Mono<Any> {
                val webRequest = exchange.request
                    ?: return Mono.empty()
                val token: String = webRequest.headers?.get("Authorization")?.firstOrNull()
                    ?: return Mono.empty()

                val claims = try {
                    JwtUtil.parseToken("SpringBootTemplate", token)
                } catch (_: Exception) {
                    null
                } ?: return Mono.empty()

                return UserAuthentication(
                    userId = claims.get("userId", String::class.java).toLong(),
                    username = claims.subject
                ).toMono()
            }

        })

        super.configureArgumentResolvers(configurer)
    }
}