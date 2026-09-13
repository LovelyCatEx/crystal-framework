/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
