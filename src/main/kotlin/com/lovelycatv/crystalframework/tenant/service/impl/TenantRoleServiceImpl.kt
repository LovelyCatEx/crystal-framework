package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRoleService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantRoleServiceImpl(
    private val tenantRoleRepository: TenantRoleRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantRoleService {
    override val cacheStore: ExpiringKVStore<String, TenantRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantRoleEntity> = TenantRoleEntity::class

    override fun getRepository(): TenantRoleRepository {
        return this.tenantRoleRepository
    }
}
