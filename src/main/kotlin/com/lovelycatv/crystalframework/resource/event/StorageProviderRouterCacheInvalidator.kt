package com.lovelycatv.crystalframework.resource.event

import com.lovelycatv.crystalframework.cache.event.SingleEntityCacheEvent
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.vertex.log.logger
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class StorageProviderRouterCacheInvalidator(
    private val applicationContext: ApplicationContext
) {
    private val logger = logger()

    @Async
    @EventListener
    fun handleEntityCacheEvent(event: SingleEntityCacheEvent) {
        if (event.entityClass == StorageProviderEntity::class) {
            applicationContext
                .getBeansOfType<StorageProviderRouter>()
                .values
                .forEach { it.invalidateCache() }

            logger.info("StorageProviderRouter cache invalidated successfully, triggered by event: $event")
        }
    }
}