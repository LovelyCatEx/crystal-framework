package com.lovelycatv.crystalframework.resource.interfaces

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.runBlocking

class RandomStorageProviderRouter(
    private val storageProviderService: StorageProviderService
) : StorageProviderRouter {
    companion object {
        const val CACHE_KEY_IDENTIFIER = "active-storage-providers"
    }

    override suspend fun get(
        userId: Long,
        fileType: ResourceFileType,
        fileName: String
    ): StorageProviderEntity {
        val providers = storageProviderService.getListCache(CACHE_KEY_IDENTIFIER)
            ?: refreshCache()

        return providers.randomOrNull()
            ?: throw BusinessException("no storage provider found in database")
    }

    /**
     * Invoked from a non-suspend, @Async event listener, so the reactive cache removal is
     * bridged with [runBlocking] (runs off the reactor event-loop).
     */
    override fun invalidateCache() = runBlocking {
        storageProviderService.removeListCache(CACHE_KEY_IDENTIFIER)
    }

    suspend fun refreshCache(): List<StorageProviderEntity> {
        storageProviderService.removeListCache(CACHE_KEY_IDENTIFIER)

        return storageProviderService
            .getRepository()
            .findAllByActive(true)
            .awaitListWithTimeout()
            .also { storageProviderService.updateListCache(CACHE_KEY_IDENTIFIER, it) }
    }
}