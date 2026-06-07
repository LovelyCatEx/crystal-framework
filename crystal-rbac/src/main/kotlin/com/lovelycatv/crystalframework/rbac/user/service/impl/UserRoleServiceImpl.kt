package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserRoleServiceImpl(
    private val userRoleRepository: UserRoleRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserRoleService {
    override fun getRepository(): UserRoleRepository {
        return this.userRoleRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserRoleEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserRoleEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserRoleEntity> = UserRoleEntity::class

    override suspend fun getAllRoles(): List<UserRoleEntity> {
        return this.getRepository().findAll().awaitListWithTimeout()
    }
}