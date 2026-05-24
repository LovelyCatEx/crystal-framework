package com.lovelycatv.crystalframework.monitor.config

import com.lovelycatv.crystalframework.monitor.types.MetricType
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class MonitorConfig : WebFluxConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(String::class.java, MetricType::class.java) { source ->
            MetricType.valueOf(source.uppercase())
        }
    }
}
