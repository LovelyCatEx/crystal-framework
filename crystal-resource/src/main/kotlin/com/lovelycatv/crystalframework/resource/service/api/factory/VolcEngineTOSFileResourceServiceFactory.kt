package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.VolcEngineTOSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.resource.types.VolcEngineTOSFileResourceServiceProperties
import org.springframework.stereotype.Component

@Component
class VolcEngineTOSFileResourceServiceFactory(
    private val fileResourceService: FileResourceService,
) : FileResourceServiceFactory<VolcEngineTOSFileResourceServiceImpl> {
    override fun getStorageProviderType(): StorageProviderType {
        return StorageProviderType.VOLCENGINE_TOS
    }

    override fun build(storageProvider: StorageProviderEntity): VolcEngineTOSFileResourceServiceImpl {
        val properties: VolcEngineTOSFileResourceServiceProperties = storageProvider.getPropertiesObject()

        return VolcEngineTOSFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            accessKey = properties.accessKey,
            secretKey = properties.secretKey,
            region = properties.region,
            endpoint = properties.endpoint,
            bucketName = properties.bucketName,
            basePath = properties.basePath,
        )
    }
}
