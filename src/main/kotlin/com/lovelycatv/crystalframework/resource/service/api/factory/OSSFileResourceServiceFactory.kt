package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.COSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.service.api.impl.OSSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import org.springframework.stereotype.Component

@Component
class OSSFileResourceServiceFactory(
    private val fileResourceService: FileResourceService,
) : FileResourceServiceFactory<OSSFileResourceServiceImpl> {
    override fun getStorageProviderType(): StorageProviderType {
        return StorageProviderType.ALIYUN_OSS
    }

    override fun build(storageProvider: StorageProviderEntity): OSSFileResourceServiceImpl {
        val properties = storageProvider.getPropertiesMap()

        return OSSFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            accessKeyId = properties["accessKeyId"] as String,
            accessKeySecret = properties["accessKeySecret"] as String,
            securityToken = properties["securityToken"] as String,
            region = properties["region"] as String,
            bucketName = properties["bucketName"] as String,
        )
    }
}