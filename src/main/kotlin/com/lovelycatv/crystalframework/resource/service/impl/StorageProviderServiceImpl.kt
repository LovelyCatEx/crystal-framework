package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.stereotype.Service

@Service
class StorageProviderServiceImpl(
    private val storageProviderRepository: StorageProviderRepository,
    private val redisService: RedisService
) : StorageProviderService {
    override fun getRepository(): StorageProviderRepository {
        return this.storageProviderRepository
    }

    override val cacheStore: ExpiringKVStore<String, StorageProviderEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<StorageProviderEntity>>
        get() = redisService.asKVStore()
}