package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.resource.interfaces.RandomStorageProviderRouter
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class StorageProviderRouterConfiguration {
    @Bean
    @ConditionalOnMissingBean(StorageProviderRouter::class)
    fun storageProviderRouter(
        storageProviderService: StorageProviderService
    ): StorageProviderRouter {
        return RandomStorageProviderRouter(storageProviderService)
    }
}