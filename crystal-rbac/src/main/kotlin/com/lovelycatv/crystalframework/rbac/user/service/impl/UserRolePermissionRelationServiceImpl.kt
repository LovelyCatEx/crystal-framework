package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.user.repository.UserRolePermissionRelationRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.types.rbac.SystemRoleAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
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
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRolePermissionRelationService {
    override fun getRepository(): UserRolePermissionRelationRepository {
        return userRolePermissionRelationRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserRolePermissionRelationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserRolePermissionRelationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserRolePermissionRelationEntity> = UserRolePermissionRelationEntity::class

    override suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity> {
        val relationIds = this.getRepository()
            .findByRoleId(roleId)
            .awaitListWithTimeout()

        return userPermissionRepository
            .findAllById(relationIds.map { it.permissionId })
            .awaitListWithTimeout()
    }

    @Transactional
    override suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>) {
        // Guard (H4): a caller may only attach permissions they themselves currently hold.
        val targetPermissionNames = userPermissionRepository
            .findAllById(permissionIds)
            .awaitListWithTimeout()
            .map { it.name }
        if (!RbacUtils.isSystemContext() &&
            !RbacUtils.isRoot() &&
            !RbacUtils.hasAllAuthorities(targetPermissionNames)
        ) {
            throw ForbiddenException("Cannot grant permissions you do not hold")
        }

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

        eventPublisher.publishEvent(SystemRoleAuthoritiesInvalidationEvent(roleId))
    }

    override suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>) {
        val affectedRoleIds = this.getRepository()
            .findByPermissionIdIn(permissionIds)
            .awaitListWithTimeout()
            .map { it.roleId }
            .toSet()

        this.getRepository()
            .deleteByPermissionIdIn(permissionIds)
            .awaitFirstOrNull()

        affectedRoleIds.forEach { eventPublisher.publishEvent(SystemRoleAuthoritiesInvalidationEvent(it)) }
    }

    override suspend fun deleteByRoleIdIn(roleIds: Collection<Long>) {
        this.getRepository()
            .deleteByRoleIdIn(roleIds)
            .awaitFirstOrNull()

        roleIds.toSet().forEach { eventPublisher.publishEvent(SystemRoleAuthoritiesInvalidationEvent(it)) }
    }
}
