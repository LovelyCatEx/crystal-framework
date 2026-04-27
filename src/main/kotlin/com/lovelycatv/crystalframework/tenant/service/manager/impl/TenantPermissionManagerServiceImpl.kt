package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.manager.TenantPermissionManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantPermissionManagerServiceImpl(
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
) : TenantPermissionManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantPermissionEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantPermissionEntity> = TenantPermissionEntity::class

    override fun getRepository(): TenantPermissionRepository {
        return tenantPermissionRepository
    }

    override suspend fun create(dto: ManagerCreateTenantPermissionDTO): TenantPermissionEntity {
        val name = dto.name.trim()
        if (name.isEmpty()) {
            throw BusinessException("permission name must not be blank")
        }
        if (tenantPermissionRepository.findByName(name).awaitFirstOrNull() != null) {
            throw BusinessException("permission $name already exists")
        }

        val entity = TenantPermissionEntity(
            id = snowIdGenerator.nextId(),
            name = name,
            description = dto.description,
            type = dto.type,
            path = dto.path,
            preserved1 = dto.preserved1,
            preserved2 = dto.preserved2
        ).apply { newEntity() }
        return tenantPermissionRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant permission")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantPermissionDTO, original: TenantPermissionEntity): TenantPermissionEntity {
        dto.name?.let { newName ->
            val trimmed = newName.trim()
            if (trimmed.isEmpty()) {
                throw BusinessException("permission name must not be blank")
            }
            if (trimmed != original.name) {
                tenantPermissionRepository.findByName(trimmed).awaitFirstOrNull()?.let { collision ->
                    if (collision.id != original.id) {
                        throw BusinessException("permission $trimmed already exists")
                    }
                }
                original.name = trimmed
            }
        }
        return original.apply {
            dto.description?.let { description = it }
            dto.type?.let { type = it }
            dto.path?.let { path = it }
            dto.preserved1?.let { preserved1 = it }
            dto.preserved2?.let { preserved2 = it }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        tenantRolePermissionRelationService.deleteByPermissionIdIn(ids)

        super.batchDelete(ids)
    }
}
