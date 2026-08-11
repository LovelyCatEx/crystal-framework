package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.constants.SystemRole
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.rbac.SystemRoleAuthoritiesInvalidationEvent
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class UserRoleManagerServiceImpl(
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userRoleRelationService: UserRoleRelationService,
    private val userRolePermissionRelationService: UserRolePermissionRelationService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : UserRoleManagerService {
    override fun getRepository(): UserRoleRepository {
        return userRoleRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateRoleDTO): UserRoleEntity {
        ensureRoleNameIsMutable(dto.name)

        val entity = UserRoleEntity(
            id = snowIdGenerator.nextId(),
            name = dto.name,
            description = dto.description
        ).apply { newEntity() }
        return withInvalidateEntityCacheContext(entity) {
            userRoleRepository.save(entity).awaitFirstOrNull()
                ?: throw RuntimeException("Could not create role")
        }
    }

    override suspend fun update(dto: ManagerUpdateRoleDTO): UserRoleEntity? {
        return super.update(dto)?.also {
            eventPublisher.publishEvent(SystemRoleAuthoritiesInvalidationEvent(it.id))
        }
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateRoleDTO, original: UserRoleEntity): UserRoleEntity {
        dto.name?.takeIf { it != original.name }?.let { ensureRoleNameIsMutable(original.name, it) }
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        val roles = userRoleRepository.findAllById(ids).awaitListWithTimeout()
        if (roles.any { it.name in SystemRole.PROTECTED_ROLE_NAMES }) {
            throwProtectedRoleException()
        }

        super.batchDelete(ids)

        userRoleRelationService.deleteByRoleIdIn(ids)

        userRolePermissionRelationService.deleteByRoleIdIn(ids)
    }

    private suspend fun ensureRoleNameIsMutable(name: String) {
        if (name in SystemRole.PROTECTED_ROLE_NAMES && !RbacUtils.isSystemContext()) {
            throwProtectedRoleException()
        }
    }

    private fun ensureRoleNameIsMutable(originalName: String, newName: String) {
        if (originalName in SystemRole.PROTECTED_ROLE_NAMES || newName in SystemRole.PROTECTED_ROLE_NAMES) {
            throwProtectedRoleException()
        }
    }

    private fun throwProtectedRoleException(): Nothing {
        throw ForbiddenException(
            "Cannot create, rename, or delete protected system roles",
            context = ForbiddenContext(reason = ForbiddenReason.ROLE_PROTECTED, scope = ResourceScope.SYSTEM)
        )
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserRoleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserRoleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserRoleEntity> = UserRoleEntity::class
}
