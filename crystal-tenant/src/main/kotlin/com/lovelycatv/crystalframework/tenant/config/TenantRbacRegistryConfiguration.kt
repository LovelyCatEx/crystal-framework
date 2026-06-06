package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.config.TenantRbacConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TenantRbacRegistryConfiguration {
    @Bean
    fun tenantRbacRegistry(
        tenantRbacConfigurers: ObjectProvider<TenantRbacConfigurer>
    ): TenantRbacRegistry {
        return TenantRbacRegistry().apply {
            tenantRbacConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
