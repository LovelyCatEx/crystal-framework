package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerCreateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerUpdateTenantDictTypeDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDictTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDictTypeRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantDictTypeManagerService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantDictTypeManagerServiceImpl(
    private val tenantDictTypeRepository: TenantDictTypeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : TenantDictTypeManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, TenantDictTypeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantDictTypeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<TenantDictTypeEntity> = TenantDictTypeEntity::class

    override fun getRepository(): TenantDictTypeRepository = tenantDictTypeRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateTenantDictTypeDTO): TenantDictTypeEntity {
        val entity: TenantDictTypeEntity = TenantDictTypeEntity(
            id = snowIdGenerator.nextId(),
            scope = dto.scope,
            scopeId = dto.scopeId,
            code = dto.code,
            name = dto.name,
            remark = dto.remark,
            status = dto.status
        ) newEntity true
        return tenantDictTypeRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant dict type")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateTenantDictTypeDTO,
        original: TenantDictTypeEntity
    ): TenantDictTypeEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.remark?.let { remark = it }
            dto.status?.let { status = it }
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<TenantDictTypeEntity> {
        return tenantDictTypeRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }
}
