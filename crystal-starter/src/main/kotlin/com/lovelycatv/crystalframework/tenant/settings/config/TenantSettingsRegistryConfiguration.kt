package com.lovelycatv.crystalframework.tenant.settings.config

import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.sdk.tenant.settings.config.TenantSettingsConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TenantSettingsRegistryConfiguration {
    @Bean
    fun tenantSettingsRegistry(
        tenantSettingsConfigurers: ObjectProvider<TenantSettingsConfigurer>,
    ): TenantSettingsRegistry {
        return TenantSettingsRegistry().apply {
            tenantSettingsConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
