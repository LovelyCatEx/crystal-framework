package com.lovelycatv.crystalframework.monitor.config

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ActuatorConfig {
    @Primary
    @Bean
    fun webEndpointProperties(): WebEndpointProperties {
        val properties = WebEndpointProperties()
        properties.setBasePath("/api/v1/actuator")
        properties.exposure.include = mutableSetOf("health", "prometheus")
        return properties
    }
}