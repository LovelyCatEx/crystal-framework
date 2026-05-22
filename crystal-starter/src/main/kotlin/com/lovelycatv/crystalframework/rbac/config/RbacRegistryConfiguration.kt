package com.lovelycatv.crystalframework.rbac.config

import com.lovelycatv.crystalframework.sdk.rbac.RbacRegistry
import com.lovelycatv.crystalframework.sdk.rbac.config.RbacConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RbacRegistryConfiguration {
    @Bean
    fun rbacRegistry(
        rbacConfigurers: ObjectProvider<RbacConfigurer>
    ): RbacRegistry {
        return RbacRegistry().apply {
            rbacConfigurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
