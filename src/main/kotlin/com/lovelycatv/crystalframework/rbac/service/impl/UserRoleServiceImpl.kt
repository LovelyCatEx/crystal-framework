package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserRoleServiceImpl(
    private val userRoleRepository: UserRoleRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRoleService {
    override fun getRepository(): UserRoleRepository {
        return this.userRoleRepository
    }

    override val cacheStore: ExpiringKVStore<String, UserRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<UserRoleEntity> = UserRoleEntity::class

    override suspend fun getAllRoles(): List<UserRoleEntity> {
        return this.getRepository().findAll().awaitListWithTimeout()
    }
}