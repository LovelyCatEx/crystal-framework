package com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.TenantBenefitRegistry

fun interface TenantBenefitConfigurer {
    fun configure(registry: TenantBenefitRegistry)
}
