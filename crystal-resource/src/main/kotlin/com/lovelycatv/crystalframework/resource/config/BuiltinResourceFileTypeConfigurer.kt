package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.file.config.ResourceFileTypeConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Registers the framework's built-in [ResourceFileType]s. `HIGHEST_PRECEDENCE` so third-party
 * configurers observe the built-ins already present and can fail-fast on typeId / key collisions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class BuiltinResourceFileTypeConfigurer : ResourceFileTypeConfigurer {
    override fun configure(registry: ResourceFileTypeRegistry) {
        registry.registers(ResourceFileType.entries)
    }
}
