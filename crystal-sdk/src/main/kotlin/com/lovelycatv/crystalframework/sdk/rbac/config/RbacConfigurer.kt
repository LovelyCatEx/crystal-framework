package com.lovelycatv.crystalframework.sdk.rbac.config

import com.lovelycatv.crystalframework.sdk.rbac.RbacRegistry

fun interface RbacConfigurer {
    fun configure(registry: RbacRegistry)
}