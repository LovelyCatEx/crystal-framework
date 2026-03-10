package com.lovelycatv.crystalframework.tenant.service.manager.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.manager.TenantRoleManagerService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantRoleManagerServiceImpl(
    private val tenantRoleRepository: TenantRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantRoleManagerService {
    override val cacheStore: ExpiringKVStore<String, TenantRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantRoleEntity> = TenantRoleEntity::class

    override fun getRepository(): TenantRoleRepository {
        return tenantRoleRepository
    }

    override suspend fun create(dto: ManagerCreateTenantRoleDTO): TenantRoleEntity {
        val entity = TenantRoleEntity(
            id = snowIdGenerator.nextId(),
            tenantId = dto.tenantId,
            name = dto.name,
            description = dto.description,
            parentId = dto.parentId
        ).apply { newEntity() }
        return tenantRoleRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create tenant role")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateTenantRoleDTO, original: TenantRoleEntity): TenantRoleEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.parentId?.let { parentId = it }
        }
    }
}
