package com.lovelycatv.crystalframework.sdk.resource.storage.config

import com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry

/**
 * SPI: contribute storage-provider type declarations into [StorageProviderTypeRegistry]
 * during application startup. Implement as a Spring `@Component` — every bean of this
 * type is collected and invoked once by the registry's `@Bean` factory.
 */
fun interface StorageProviderTypeConfigurer {
    fun configure(registry: StorageProviderTypeRegistry)
}
