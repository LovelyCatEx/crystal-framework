package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.TenantBenefitRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.config.TenantBenefitConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TenantBenefitRegistryConfiguration {
    @Bean
    fun tenantBenefitRegistry(
        tenantBenefitConfigurers: ObjectProvider<TenantBenefitConfigurer>
    ): TenantBenefitRegistry {
        return TenantBenefitRegistry().apply {
            tenantBenefitConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
