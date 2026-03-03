package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.COSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.COSFileResourceServiceProperties
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import org.springframework.stereotype.Component

@Component
class COSFileResourceServiceFactory(
    private val fileResourceService: FileResourceService,
) : FileResourceServiceFactory<COSFileResourceServiceImpl> {
    override fun getStorageProviderType(): StorageProviderType {
        return StorageProviderType.TENCENT_COS
    }

    override fun build(storageProvider: StorageProviderEntity): COSFileResourceServiceImpl {
        val properties: COSFileResourceServiceProperties = storageProvider.getPropertiesObject()

        return COSFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            accessKey = properties.accessKey,
            secretKey = properties.secretKey,
            region = properties.region,
            bucketName = properties.bucketName,
        )
    }
}