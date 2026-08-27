package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.LocalFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.LocalFileResourceServiceProperties
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.resource.utils.ResourceUrlSigner
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.stereotype.Component

@Component
class LocalFileResourceServiceFactory(
    private val fileResourceService: FileResourceService,
    private val systemModuleClient: SystemModuleClient,
    private val resourceUrlSigner: ResourceUrlSigner,
) : FileResourceServiceFactory<LocalFileResourceServiceImpl> {
    override fun getStorageProviderTypeId(): Int {
        return StorageProviderType.LOCAL_FILE_SYSTEM.typeId
    }

    override fun build(storageProvider: StorageProviderEntity): LocalFileResourceServiceImpl {
        val properties: LocalFileResourceServiceProperties = storageProvider.properties.parseObject()

        return LocalFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            basePath = properties.basePath,
            systemModuleClient = systemModuleClient,
            resourceUrlSigner = resourceUrlSigner,
        )
    }
}
