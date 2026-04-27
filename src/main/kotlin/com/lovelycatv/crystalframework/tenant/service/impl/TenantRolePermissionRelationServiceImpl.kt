package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.event.TenantRoleAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantRolePermissionRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.tenant.repository.TenantRolePermissionRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class TenantRolePermissionRelationServiceImpl(
    private val tenantRolePermissionRelationRepository: TenantRolePermissionRelationRepository,
    private val tenantPermissionRepository: TenantPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantRolePermissionRelationService {
    override fun getRepository(): TenantRolePermissionRelationRepository {
        return tenantRolePermissionRelationRepository
    }

    override val cacheStore: ExpiringKVStore<String, TenantRolePermissionRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantRolePermissionRelationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantRolePermissionRelationEntity> = TenantRolePermissionRelationEntity::class

    override suspend fun getRolePermissions(roleId: Long): List<TenantPermissionEntity> {
        val relationIds = this.getRepository()
            .findAllByRoleId(roleId)
            .awaitListWithTimeout()

        return relationIds.mapNotNull {
            tenantPermissionRepository.findById(it.permissionId).awaitFirstOrNull()
        }
    }

    @Transactional
    override suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findAllByRoleId(roleId)
            .awaitListWithTimeout()

        existing.forEach {
            tenantRolePermissionRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        permissionIds.forEach { permissionId ->
            val entity = TenantRolePermissionRelationEntity(
                id = snowIdGenerator.nextId(),
                roleId = roleId,
                permissionId = permissionId
            ).apply { newEntity() }
            tenantRolePermissionRelationRepository.save(entity).awaitFirstOrNull()
        }

        eventPublisher.publishEvent(TenantRoleAuthoritiesInvalidationEvent(roleId))
    }

    override suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>) {
        val affectedRoleIds = this.getRepository()
            .findAllByPermissionIdIn(permissionIds)
            .awaitListWithTimeout()
            .map { it.roleId }
            .toSet()

        this.getRepository()
            .deleteByPermissionIdIn(permissionIds)
            .awaitFirstOrNull()

        affectedRoleIds.forEach { eventPublisher.publishEvent(TenantRoleAuthoritiesInvalidationEvent(it)) }
    }

    override suspend fun deleteByRoleIdIn(roleIds: Collection<Long>) {
        roleIds.toSet().forEach { eventPublisher.publishEvent(TenantRoleAuthoritiesInvalidationEvent(it)) }

        this.getRepository()
            .deleteByRoleIdIn(roleIds)
            .awaitFirstOrNull()
    }
}
