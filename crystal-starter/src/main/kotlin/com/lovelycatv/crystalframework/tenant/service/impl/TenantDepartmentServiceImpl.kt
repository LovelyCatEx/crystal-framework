package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.tenant.service.TenantDepartmentService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentServiceImpl(
    private val tenantDepartmentRepository: TenantDepartmentRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentService {
    override val cacheStore: ExpiringKVStore<String, TenantDepartmentEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantDepartmentEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantDepartmentEntity> = TenantDepartmentEntity::class

    override fun getRepository(): TenantDepartmentRepository {
        return this.tenantDepartmentRepository
    }
}
