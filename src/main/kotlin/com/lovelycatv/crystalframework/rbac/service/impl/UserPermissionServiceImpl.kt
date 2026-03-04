package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.service.UserPermissionService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import kotlin.reflect.KClass

@Service
class UserPermissionServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserPermissionService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override val cacheStore: ExpiringKVStore<String, UserPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserPermissionEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserPermissionEntity> = UserPermissionEntity::class

    override suspend fun getAllPermissions(): List<UserPermissionEntity> {
        return userPermissionRepository.findAll().awaitListWithTimeout()
    }
}