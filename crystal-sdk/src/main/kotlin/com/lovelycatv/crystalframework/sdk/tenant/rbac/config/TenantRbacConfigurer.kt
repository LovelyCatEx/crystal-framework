package com.lovelycatv.crystalframework.sdk.tenant.rbac.config

import com.lovelycatv.crystalframework.sdk.tenant.rbac.TenantRbacRegistry

fun interface TenantRbacConfigurer {
    fun configure(registry: TenantRbacRegistry)
}
