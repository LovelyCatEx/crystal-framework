package com.lovelycatv.crystalframework.sdk.system.settings.config

import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry

fun interface SystemSettingsConfigurer {
    fun configure(registry: SystemSettingsRegistry)
}
