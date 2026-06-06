package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.tenant.service.TenantBenefitService
import com.lovelycatv.crystalframework.tenant.service.TenantService
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDepartmentManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDepartmentManagerServiceImpl(
    private val tenantDepartmentRepository: TenantDepartmentRepository,
    private val tenantBenefitService: TenantBenefitService,
    private val tenantService: TenantService,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantDepartmentManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantDepartmentEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantDepartmentEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantDepartmentEntity> = TenantDepartmentEntity::class

    override fun getRepository(): TenantDepartmentRepository {
        return tenantDepartmentRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantDepartmentDTO): TenantDepartmentEntity {
        val tireTypeId = tenantService.getByIdOrThrow(dto.tenantId).tireTypeId
        val departmentLimit = tenantBenefitService.getBenefitLimit(tireTypeId, TenantBenefit.DEPARTMENT_MAX_COUNT.featureKey)
        val departmentCount = tenantDepartmentRepository.countByTenantId(dto.tenantId).awaitFirstOrNull() ?: 0
        if (departmentCount >= departmentLimit) {
            throw BusinessException("Department limit reached ($departmentLimit)")
        }

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
            description = dto.description
            parentId = dto.parentId
        }
    }

    override suspend fun findAllByTenantId(tenantId: Long): List<TenantDepartmentEntity> {
        return this.getRepository().findAllByTenantId(tenantId).awaitListWithTimeout()
    }
}
