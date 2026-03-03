package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.LocalFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import org.springframework.stereotype.Component

@Component
class LocalFileResourceServiceFactory(
    private val fileResourceService: FileResourceService,
) : FileResourceServiceFactory<LocalFileResourceServiceImpl> {
    override fun getStorageProviderType(): StorageProviderType {
        return StorageProviderType.LOCAL_FILE_SYSTEM
    }

    override fun build(storageProvider: StorageProviderEntity): LocalFileResourceServiceImpl {
        val properties = storageProvider.getPropertiesMap()

        return LocalFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            basePath = properties["basePath"] as String,
        )
    }
}
