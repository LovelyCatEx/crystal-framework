package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.sdk.resource.file.ResourceFileTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.file.config.ResourceFileTypeConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResourceFileTypeRegistryConfiguration {
    @Bean
    fun resourceFileTypeRegistry(
        configurers: ObjectProvider<ResourceFileTypeConfigurer>,
    ): ResourceFileTypeRegistry {
        return ResourceFileTypeRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
