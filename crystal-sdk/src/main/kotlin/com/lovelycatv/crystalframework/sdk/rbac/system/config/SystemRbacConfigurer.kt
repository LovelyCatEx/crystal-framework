package com.lovelycatv.crystalframework.sdk.rbac.system.config

import com.lovelycatv.crystalframework.sdk.rbac.system.SystemRbacRegistry


fun interface SystemRbacConfigurer {
    fun configure(registry: SystemRbacRegistry)
}