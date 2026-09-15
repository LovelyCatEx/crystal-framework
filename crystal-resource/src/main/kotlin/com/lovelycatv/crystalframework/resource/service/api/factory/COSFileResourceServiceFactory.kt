/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
    override fun getStorageProviderTypeId(): Int {
        return StorageProviderType.TENCENT_COS.typeId
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
            basePath = properties.basePath,
        )
    }
}