package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.entity.StorageProviderRoutingRuleEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRoutingRuleRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderRoutingRuleService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class StorageProviderRoutingRuleServiceImpl(
    private val storageProviderRoutingRuleRepository: StorageProviderRoutingRuleRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : StorageProviderRoutingRuleService {
    companion object {
        const val CACHE_KEY_IDENTIFIER = "active-storage-provider-routing-rules"
    }

    override fun getRepository(): StorageProviderRoutingRuleRepository {
        return this.storageProviderRoutingRuleRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, StorageProviderRoutingRuleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<StorageProviderRoutingRuleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<StorageProviderRoutingRuleEntity> = StorageProviderRoutingRuleEntity::class

    override suspend fun getActiveRulesSortedByPriority(): List<StorageProviderRoutingRuleEntity> {
        return getListCache(CACHE_KEY_IDENTIFIER) ?: refreshCache()
    }

    suspend fun refreshCache(): List<StorageProviderRoutingRuleEntity> {
        removeListCache(CACHE_KEY_IDENTIFIER)

        return storageProviderRoutingRuleRepository
            .findAllByEnabledOrderByPriorityAsc(true)
            .awaitListWithTimeout()
            .also { updateListCache(CACHE_KEY_IDENTIFIER, it) }
    }
}
