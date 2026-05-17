package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.tenant.service.TenantPermissionService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantPermissionServiceImpl(
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantPermissionService {
    override val cacheStore: ExpiringKVStore<String, TenantPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantPermissionEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantPermissionEntity> = TenantPermissionEntity::class

    override fun getRepository(): TenantPermissionRepository {
        return this.tenantPermissionRepository
    }
}
