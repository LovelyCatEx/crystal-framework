package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.repository.UserRolePermissionRelationRepository
import com.lovelycatv.crystalframework.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class UserRolePermissionRelationServiceImpl(
    private val userRolePermissionRelationRepository: UserRolePermissionRelationRepository,
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRolePermissionRelationService {
    override fun getRepository(): UserRolePermissionRelationRepository {
        return userRolePermissionRelationRepository
    }

    override val cacheStore: ExpiringKVStore<String, UserRolePermissionRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserRolePermissionRelationEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserRolePermissionRelationEntity> = UserRolePermissionRelationEntity::class

    override suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity> {
        val relationIds = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        return relationIds.map {
            userPermissionRepository.findById(it.permissionId).awaitFirstOrNull()
                ?: throw BusinessException("permission with id $it not found")
        }
    }

    @Transactional
    override suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>) {
        // Delete existing relations
        val existing = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        existing.forEach {
            userRolePermissionRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        permissionIds.forEach { permissionId ->
            val entity = UserRolePermissionRelationEntity(
                id = snowIdGenerator.nextId(),
                roleId = roleId,
                permissionId = permissionId
            ).apply { newEntity() }
            userRolePermissionRelationRepository.save(entity).awaitFirstOrNull()
        }
    }

    override suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>) {
        this.getRepository()
            .deleteByPermissionIdIn(permissionIds)
            .awaitFirstOrNull()
    }

    override suspend fun deleteByRoleIdIn(roleIds: Collection<Long>) {
        this.getRepository()
            .deleteByRoleIdIn(roleIds)
            .awaitFirstOrNull()
    }
}
