package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class UserPermissionManagerServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userRolePermissionRelationService: UserRolePermissionRelationService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : UserPermissionManagerService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreatePermissionDTO): UserPermissionEntity {
        return this.getRepository().save(
            UserPermissionEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description,
                type = dto.type,
                path = dto.path
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create user permission")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdatePermissionDTO,
        original: UserPermissionEntity
    ): UserPermissionEntity {
        return original.apply {
            if (dto.name != null) {
                this.name = dto.name
            }

            this.description = dto.description

            if (dto.type != null) {
                this.type = dto.type
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun batchDelete(ids: List<Long>) {
        super.batchDelete(ids)

        // Delete related role-permission relations
        userRolePermissionRelationService.deleteByPermissionIdIn(ids)
    }

    override val cacheStore: ExpiringKVStore<String, UserPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserPermissionEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserPermissionEntity> = UserPermissionEntity::class
}