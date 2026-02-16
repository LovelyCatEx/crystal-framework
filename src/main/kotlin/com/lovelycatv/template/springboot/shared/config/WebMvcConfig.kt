package com.lovelycatv.template.springboot.shared.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.ApiVersionConfigurer
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
@EnableWebFlux
class WebMvcConfig : WebFluxConfigurer {
    override fun configureApiVersioning(configurer: ApiVersionConfigurer) {
        configurer.setDefaultVersion("1")
        configurer.usePathSegment(1)
        super.configureApiVersioning(configurer)
    }
}