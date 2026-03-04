package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserPermissionManagerServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserPermissionManagerService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

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

    override val cacheStore: ExpiringKVStore<String, UserPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserPermissionEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserPermissionEntity> = UserPermissionEntity::class
}