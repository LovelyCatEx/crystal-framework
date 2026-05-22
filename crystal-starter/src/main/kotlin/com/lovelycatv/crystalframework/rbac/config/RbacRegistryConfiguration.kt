package com.lovelycatv.crystalframework.rbac.config

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.system.config.SystemRbacConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RbacRegistryConfiguration {
    @Bean
    fun rbacRegistry(
        systemRbacConfigurers: ObjectProvider<SystemRbacConfigurer>
    ): SystemRbacRegistry {
        return SystemRbacRegistry().apply {
            systemRbacConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
