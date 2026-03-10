package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository
import com.lovelycatv.crystalframework.tenant.service.TenantTireTypeService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantTireTypeServiceImpl(
    private val tenantTireTypeRepository: TenantTireTypeRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantTireTypeService {
    override val cacheStore: ExpiringKVStore<String, TenantTireTypeEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantTireTypeEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantTireTypeEntity> = TenantTireTypeEntity::class

    override fun getRepository(): TenantTireTypeRepository {
        return this.tenantTireTypeRepository
    }
}
