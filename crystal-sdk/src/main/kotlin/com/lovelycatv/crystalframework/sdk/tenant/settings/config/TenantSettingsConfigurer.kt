package com.lovelycatv.crystalframework.sdk.tenant.settings.config

import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry

fun interface TenantSettingsConfigurer {
    fun configure(registry: TenantSettingsRegistry)
}
