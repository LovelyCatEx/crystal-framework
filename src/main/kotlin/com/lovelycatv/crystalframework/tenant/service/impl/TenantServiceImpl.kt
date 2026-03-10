package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantServiceImpl(
    private val tenantRepository: TenantRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantService {
    override val cacheStore: ExpiringKVStore<String, TenantEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantEntity> = TenantEntity::class

    override fun getRepository(): TenantRepository {
        return this.tenantRepository
    }
}
