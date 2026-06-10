package com.lovelycatv.crystalframework.rbac.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.rbac.tenant.service.TenantPermissionService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantPermissionServiceImpl(
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantPermissionService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantPermissionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantPermissionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantPermissionEntity> = TenantPermissionEntity::class

    override fun getRepository(): TenantPermissionRepository {
        return this.tenantPermissionRepository
    }
}
