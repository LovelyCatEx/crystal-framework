package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserPermissionService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class UserPermissionServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : UserPermissionService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override val cacheStore: ReactiveExpiringKVStore<String, UserPermissionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<UserPermissionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<UserPermissionEntity> = UserPermissionEntity::class

    override suspend fun getAllPermissions(): List<UserPermissionEntity> {
        return userPermissionRepository.findAll().awaitListWithTimeout()
    }
}