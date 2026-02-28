package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.api.factory.FileResourceServiceFactory
import com.lovelycatv.crystalframework.resource.service.api.impl.COSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.core.OrderComparator
import org.springframework.stereotype.Component

@Component
class FileResourceServiceManager(
    private val applicationContext: ApplicationContext
) {
    private val cacheMap = mutableMapOf<Long, AbstractFileResourceService>()

    fun getService(provider: StorageProviderEntity): AbstractFileResourceService {
        return cacheMap.getOrPut(provider.id) {
            val serviceFactories = applicationContext
                .getBeansOfType<FileResourceServiceFactory<*>>()
                .values

            val factory = serviceFactories
                .filter { it.getStorageProviderType() == provider.getRealStorageProviderType() }
                .minWithOrNull(OrderComparator.INSTANCE)
                ?: throw BusinessException("No file resource service factory found for provider ${provider.id}")

            factory.build(provider)
        }
    }
}