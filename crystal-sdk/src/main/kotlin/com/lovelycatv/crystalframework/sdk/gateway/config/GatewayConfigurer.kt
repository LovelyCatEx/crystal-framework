package com.lovelycatv.crystalframework.sdk.gateway.config

import com.lovelycatv.crystalframework.sdk.gateway.GatewayRegistry

fun interface GatewayConfigurer {
    fun configure(registry: GatewayRegistry)
}
