package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.sdk.gateway.GatewayRegistry
import com.lovelycatv.crystalframework.sdk.gateway.config.GatewayConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRegistryConfiguration {
    @Bean
    fun gatewayRegistry(
        configurers: ObjectProvider<GatewayConfigurer>,
    ): GatewayRegistry {
        return GatewayRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
