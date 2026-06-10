package com.lovelycatv.crystalframework.sdk.gateway

import kotlin.reflect.KClass

class GatewayRegistry {

    private val gateways = linkedMapOf<KClass<out Gateway>, Gateway>()

    fun <T : Gateway> register(type: KClass<T>, implementation: T) {
        val existing = gateways.putIfAbsent(type, implementation)
        if (existing != null) {
            throw IllegalStateException("Gateway [${type.qualifiedName}] is already registered")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Gateway> get(type: KClass<T>): T? {
        return gateways[type] as? T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Gateway> require(type: KClass<T>): T {
        return gateways[type] as? T
            ?: throw IllegalStateException("Gateway [${type.qualifiedName}] is not registered. Is the providing module on the classpath?")
    }

    fun getRegisteredTypes(): Set<KClass<out Gateway>> {
        return gateways.keys.toSet()
    }
}
