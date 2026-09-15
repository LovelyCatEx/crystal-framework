/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
