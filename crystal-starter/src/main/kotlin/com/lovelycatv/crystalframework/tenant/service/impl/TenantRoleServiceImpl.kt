package com.lovelycatv.crystalframework.tenant.service.impl

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRoleService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class TenantRoleServiceImpl(
    private val tenantRoleRepository: TenantRoleRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantRoleService {
    override val cacheStore: ExpiringKVStore<String, TenantRoleEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantRoleEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantRoleEntity> = TenantRoleEntity::class

    override fun getRepository(): TenantRoleRepository {
        return this.tenantRoleRepository
    }

    override suspend fun getChildren(roleId: Long): List<TenantRoleEntity> {
        val role = this.getByIdOrNull(roleId) ?: return emptyList()

        val results = mutableListOf<TenantRoleEntity>()

        val childrenRoles = this
            .getRepository()
            .findAllByParentId(role.id)
            .awaitListWithTimeout()

        results.addAll(childrenRoles)
        results.addAll(childrenRoles.flatMap {
            getChildren(it.id)
        })

        return results.distinctBy { it.id }
    }

    override suspend fun getParents(roleId: Long): List<TenantRoleEntity> {
        val role = this.getByIdOrNull(roleId) ?: return emptyList()

        val results = mutableListOf<TenantRoleEntity>()

        var parent = role.parentId
        while (parent != null) {
            val parentRole = this.getByIdOrNull(parent)
            if (parentRole != null) {
                results.add(parentRole)
                parent = parentRole.parentId
            }
        }

        return results.distinctBy { it.id }
    }
}
