package com.lovelycatv.crystalframework.rbac.service.impl

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.service.UserPermissionService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.stereotype.Service

@Service
class UserPermissionServiceImpl(
    private val userPermissionRepository: UserPermissionRepository,
    private val redisService: RedisService
) : UserPermissionService {
    override fun getRepository(): UserPermissionRepository {
        return this.userPermissionRepository
    }

    override val cacheStore: ExpiringKVStore<String, UserPermissionEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<UserPermissionEntity>>
        get() = redisService.asKVStore()

    override suspend fun getAllPermissions(): List<UserPermissionEntity> {
        return userPermissionRepository.findAll().awaitListWithTimeout()
    }
}