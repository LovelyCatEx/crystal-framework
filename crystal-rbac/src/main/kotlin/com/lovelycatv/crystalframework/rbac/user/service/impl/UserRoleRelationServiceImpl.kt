package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleRelationService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleService
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.rbac.UserAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userRoleService: UserRoleService,
) : UserRoleRelationService {
    override fun getRepository(): UserRoleRelationRepository {
        return userRoleRelationRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserRoleRelationEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserRoleRelationEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserRoleRelationEntity> = UserRoleRelationEntity::class

    override suspend fun getUserRoles(userId: Long): List<UserRoleEntity> {
        return userRoleRepository.findAllById(
            userRoleRelationRepository
                .findByUserId(userId)
                .map { it.roleId }
        ).awaitListWithTimeout()
    }

    @Transactional
    override suspend fun setUserRoles(userId: Long, roleIds: List<Long>) {
        // Guard (H3): prevent granting protected system roles from a non-root user context.
        val targetRoleNames = userRoleService.getAllRoles()
            .filter { it.id in roleIds }
            .map { it.name }
            .toSet()
        val touchesProtected = (targetRoleNames intersect SystemRole.PROTECTED_ROLE_NAMES).isNotEmpty()
        if (touchesProtected && !RbacUtils.isSystemContext() && !RbacUtils.isRoot()) {
            throw ForbiddenException("Cannot assign protected system roles")
        }

        // Delete existing relations
        val existing = userRoleRelationRepository
            .findByUserId(userId)
            .awaitListWithTimeout()

        existing.forEach {
            userRoleRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        roleIds.forEach { roleId ->
            val entity = UserRoleRelationEntity(
                id = snowIdGenerator.nextId(),
                userId = userId,
                roleId = roleId
            ).apply { newEntity() }
            userRoleRelationRepository.save(entity).awaitFirstOrNull()
        }

        eventPublisher.publishEvent(UserAuthoritiesInvalidationEvent(userId))
    }

    @Transactional
    override suspend fun setUserRolesByNames(userId: Long, roleNames: List<String>) {
        // Guard (H3): prevent granting protected system roles from a non-root user context.
        val touchesProtected = (roleNames.toSet() intersect SystemRole.PROTECTED_ROLE_NAMES).isNotEmpty()
        if (touchesProtected && !RbacUtils.isSystemContext() && !RbacUtils.isRoot()) {
            throw ForbiddenException("Cannot assign protected system roles")
        }

        val roleIds = userRoleService
            .getAllRoles()
            .filter { it.name in roleNames }
            .map { it.id }

        // Delete existing relations
        val existing = userRoleRelationRepository
            .findByUserId(userId)
            .awaitListWithTimeout()

        existing.forEach {
            userRoleRelationRepository.delete(it).awaitFirstOrNull()
        }

        // Create new relations
        roleIds.forEach { roleId ->
            val entity = UserRoleRelationEntity(
                id = snowIdGenerator.nextId(),
                userId = userId,
                roleId = roleId
            ).apply { newEntity() }
            userRoleRelationRepository.save(entity).awaitFirstOrNull()
        }

        eventPublisher.publishEvent(UserAuthoritiesInvalidationEvent(userId))
    }

    override suspend fun deleteByUserIdIn(userIds: Collection<Long>) {
        this.getRepository().deleteByUserIdIn(userIds).awaitFirstOrNull()

        userIds.toSet().forEach { eventPublisher.publishEvent(UserAuthoritiesInvalidationEvent(it)) }
    }

    override suspend fun deleteByRoleIdIn(roleIds: Collection<Long>) {
        val affectedUserIds = roleIds.toSet().flatMap { roleId ->
            this.getRepository()
                .findByRoleId(roleId)
                .awaitListWithTimeout()
                .map { it.userId }
        }.toSet()

        this.getRepository().deleteByRoleIdIn(roleIds).awaitFirstOrNull()

        // The relation rows are already gone, so a SystemRoleAuthoritiesInvalidationEvent
        // would not match anything anymore. Fan out to the captured users directly.
        affectedUserIds.forEach { eventPublisher.publishEvent(UserAuthoritiesInvalidationEvent(it)) }
    }
}