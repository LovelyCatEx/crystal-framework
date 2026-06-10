package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
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
        val entity = UserRoleEntity(
            id = snowIdGenerator.nextId(),
            name = dto.name,
            description = dto.description
        ).apply { newEntity() }
        return userRoleRepository.save(entity).awaitFirstOrNull()
            ?: throw RuntimeException("Could not create role")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateRoleDTO, original: UserRoleEntity): UserRoleEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        super.batchDelete(ids)

        userRoleRelationService.deleteByRoleIdIn(ids)

        userRolePermissionRelationService.deleteByRoleIdIn(ids)
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserRoleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserRoleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserRoleEntity> = UserRoleEntity::class
}
