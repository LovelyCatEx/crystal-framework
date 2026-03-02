package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout

class RandomStorageProviderRouter(
    private val storageProviderRepository: StorageProviderRepository
) : StorageProviderRouter {
    private var cache: List<StorageProviderEntity>? = null

    override suspend fun get(
        userId: Long,
        fileType: ResourceFileType,
        fileName: String
    ): StorageProviderEntity {
        val providers = cache ?: refreshCache()

        return providers.randomOrNull()
            ?: throw BusinessException("no storage provider found in database")
    }

    suspend fun refreshCache(): List<StorageProviderEntity?> {
        this.refreshCacheLazy()

        return storageProviderRepository
            .findAll()
            .awaitListWithTimeout()
            .also { this.cache = it }
    }

    fun refreshCacheLazy() {
        this.cache = null
    }
}