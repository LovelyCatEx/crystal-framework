package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.types.ResourceFileType

interface StorageProviderRouter {
    suspend fun get(
        userId: Long,
        fileType: ResourceFileType,
        fileName: String,
    ): StorageProviderEntity
}