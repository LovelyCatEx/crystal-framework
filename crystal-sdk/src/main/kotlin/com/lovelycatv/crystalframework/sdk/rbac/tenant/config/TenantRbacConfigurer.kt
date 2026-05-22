package com.lovelycatv.crystalframework.sdk.rbac.tenant.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.TenantRbacRegistry

fun interface TenantRbacConfigurer {
    fun configure(registry: TenantRbacRegistry)
}
