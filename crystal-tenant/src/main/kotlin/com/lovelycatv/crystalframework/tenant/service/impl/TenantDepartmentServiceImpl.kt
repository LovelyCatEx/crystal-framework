package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentServiceImpl(
    private val tenantDepartmentRepository: TenantDepartmentRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantDepartmentEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantDepartmentEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantDepartmentEntity> = TenantDepartmentEntity::class

    override fun getRepository(): TenantDepartmentRepository {
        return this.tenantDepartmentRepository
    }
}
