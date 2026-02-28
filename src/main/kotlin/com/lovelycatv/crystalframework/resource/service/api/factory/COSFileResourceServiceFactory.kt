package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.COSFileResourceServiceImpl
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
        val properties = storageProvider.getPropertiesMap()

        return COSFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            accessKey = properties["accessKey"] as String,
            secretKey = properties["secretKey"] as String,
            region = properties["region"] as String,
            bucketName = properties["bucketName"] as String,
        )
    }
}