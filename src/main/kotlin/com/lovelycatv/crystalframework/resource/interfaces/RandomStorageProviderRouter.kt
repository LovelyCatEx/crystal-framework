package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout

class RandomStorageProviderRouter(
    private val storageProviderService: StorageProviderService
) : StorageProviderRouter {
    override suspend fun get(
        userId: Long,
        fileType: ResourceFileType,
        fileName: String
    ): StorageProviderEntity {
        val providers = storageProviderService.getListCache("active-storage-providers")
            ?: refreshCache()

        return providers.randomOrNull()
            ?: throw BusinessException("no storage provider found in database")
    }

    override fun invalidateCache() {
        storageProviderService.removeListCache("active-storage-providers")

    }

    suspend fun refreshCache(): List<StorageProviderEntity?> {
        this.invalidateCache()

        return storageProviderService
            .getRepository()
            .findAllByActive(true)
            .awaitListWithTimeout()
            .also { storageProviderService.updateListCache("active-storage-providers", it) }
    }
}