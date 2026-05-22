package com.lovelycatv.crystalframework.system.config

import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.system.settings.config.SystemSettingsConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SystemSettingsRegistryConfiguration {
    @Bean
    fun systemSettingsRegistry(
        systemSettingsConfigurers: ObjectProvider<SystemSettingsConfigurer>
    ): SystemSettingsRegistry {
        return SystemSettingsRegistry().apply {
            systemSettingsConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
