package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class StorageProviderServiceImpl(
    private val storageProviderRepository: StorageProviderRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : StorageProviderService {
    override fun getRepository(): StorageProviderRepository {
        return this.storageProviderRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, StorageProviderEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<StorageProviderEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<StorageProviderEntity> = StorageProviderEntity::class
}