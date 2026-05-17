package com.lovelycatv.crystalframework.tenant.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TenantMailTemplateAutoConfigure {
    @Bean
    @ConditionalOnMissingBean(TenantMailTemplateConfigure::class)
    fun tenantMailTemplateConfigure(): TenantMailTemplateConfigure {
        return DefaultTenantMailTemplateConfigure()
    }
}