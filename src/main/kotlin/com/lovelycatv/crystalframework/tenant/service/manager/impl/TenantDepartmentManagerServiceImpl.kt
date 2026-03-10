package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentManagerServiceImpl(
    private val tenantDepartmentRepository: TenantDepartmentRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantDepartmentManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantDepartmentEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantDepartmentEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantDepartmentEntity> = TenantDepartmentEntity::class

    override fun getRepository(): TenantDepartmentRepository {
        return tenantDepartmentRepository
    }

    override suspend fun create(dto: ManagerCreateTenantDepartmentDTO): TenantDepartmentEntity {
        val entity = TenantDepartmentEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            name = dto.name,
            description = dto.description,
            parentId = dto.parentId
        ).apply { newEntity() }
        return tenantDepartmentRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant department")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantDepartmentDTO, original: TenantDepartmentEntity): TenantDepartmentEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.parentId?.let { parentId = it }
        }
    }
}
