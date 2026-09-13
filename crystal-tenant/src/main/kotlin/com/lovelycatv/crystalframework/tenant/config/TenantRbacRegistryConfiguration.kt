/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
