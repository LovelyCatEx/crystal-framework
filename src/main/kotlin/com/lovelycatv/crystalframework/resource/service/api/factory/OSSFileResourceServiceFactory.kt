package com.lovelycatv.crystalframework.resource.service.api.factory

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.service.api.impl.COSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.service.api.impl.OSSFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.OSSFileResourceServiceProperties
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
        val properties: OSSFileResourceServiceProperties = storageProvider.getPropertiesObject()

        return OSSFileResourceServiceImpl(
            storageProvider = storageProvider,
            fileResourceService = fileResourceService,
            accessKeyId = properties.accessKeyId,
            accessKeySecret = properties.accessKeySecret,
            securityToken = properties.securityToken,
            region = properties.region,
            bucketName = properties.bucketName,
        )
    }
}