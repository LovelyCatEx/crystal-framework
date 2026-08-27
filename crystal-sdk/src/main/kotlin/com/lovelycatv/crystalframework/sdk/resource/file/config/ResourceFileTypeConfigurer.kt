package com.lovelycatv.crystalframework.sdk.resource.file.config

import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry

/**
 * SPI: contribute file-resource type declarations into [ResourceFileTypeRegistry]
 * during application startup. Implement as a Spring `@Component` — every bean of this
 * type is collected and invoked once by the registry's `@Bean` factory.
 */
fun interface ResourceFileTypeConfigurer {
    fun configure(registry: ResourceFileTypeRegistry)
}
