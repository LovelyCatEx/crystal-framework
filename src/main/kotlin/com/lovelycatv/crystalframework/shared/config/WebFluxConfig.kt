package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.JacksonJsonDecoder
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.ApiVersionConfigurer
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import tools.jackson.databind.json.JsonMapper

@Configuration
class WebFluxConfig(
    private val jsonMapper: JsonMapper,
) : WebFluxConfigurer {
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.setDefaultVersion("1")
        configurer.usePathSegment(1)
        super.configureApiVersioning(configurer)
    }

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        configurer.defaultCodecs().jacksonJsonEncoder(
            JacksonJsonEncoder(jsonMapper)
        )
        configurer.defaultCodecs().jacksonJsonDecoder(
            JacksonJsonDecoder(jsonMapper)
        )

        super.configureHttpMessageCodecs(configurer)
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
                return ReactiveSecurityContextHolder
                    .getContext()
                    .mapNotNull { it.authentication }
                    .mapNotNull { it.principal as? UserAuthentication }
            }
        })

        super.configureArgumentResolvers(configurer)
    }
}
