package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
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
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRoleRelationService {
    override fun getRepository(): UserRoleRelationRepository {
        return userRoleRelationRepository
    }

    override val cacheStore: ExpiringKVStore<String, UserRoleRelationEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserRoleRelationEntity>>
        get() = redisService.asKVStore()
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
    }
}
