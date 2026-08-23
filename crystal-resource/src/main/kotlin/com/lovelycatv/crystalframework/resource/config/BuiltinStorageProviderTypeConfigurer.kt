package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.storage.config.StorageProviderTypeConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Registers the framework's built-in [StorageProviderType]s. `HIGHEST_PRECEDENCE` so third-party
 * configurers observe the built-ins already present and can fail-fast on typeId collisions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class BuiltinStorageProviderTypeConfigurer : StorageProviderTypeConfigurer {
    override fun configure(registry: StorageProviderTypeRegistry) {
        registry.registers(StorageProviderType.entries)
    }
}
