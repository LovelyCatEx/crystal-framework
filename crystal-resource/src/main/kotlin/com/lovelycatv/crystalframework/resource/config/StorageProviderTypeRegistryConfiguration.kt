package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.sdk.resource.storage.StorageProviderTypeRegistry
import com.lovelycatv.crystalframework.sdk.resource.storage.config.StorageProviderTypeConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageProviderTypeRegistryConfiguration {
    @Bean
    fun storageProviderTypeRegistry(
        configurers: ObjectProvider<StorageProviderTypeConfigurer>,
    ): StorageProviderTypeRegistry {
        return StorageProviderTypeRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
