package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.rbac.service.UserRoleService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.user.event.UserAuthoritiesInvalidationEvent
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import kotlin.reflect.KClass

@Service
class UserRoleRelationServiceImpl(
    private val userRoleRelationRepository: UserRoleRelationRepository,
    private val userRoleRepository: UserRoleRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val userRoleService: UserRoleService,
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

        eventPublisher.publishEvent(UserAuthoritiesInvalidationEvent(userId))
    }

    @Transactional
    override suspend fun setUserRolesByNames(userId: Long, roleNames: List<String>) {
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
