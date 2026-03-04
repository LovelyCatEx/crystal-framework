package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleManagerService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserRoleManagerServiceImpl(
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRoleManagerService {
    override fun getRepository(): UserRoleRepository {
        return userRoleRepository
    }

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

    override val cacheStore: ExpiringKVStore<String, UserRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserRoleEntity> = UserRoleEntity::class
}
