package com.lovelycatv.crystalframework.rbac.user.service.impl

import com.lovelycatv.crystalframework.rbac.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.rbac.user.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleRelationService
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.rbac.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.tenant.service.manager.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.rbac.user.service.result.UserRbacQueryResult
import com.lovelycatv.crystalframework.rbac.user.service.result.UserTenantRbacQueryResult
import com.lovelycatv.crystalframework.shared.constants.RbacConstants
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service

@Service
class UserRbacQueryServiceImpl(
    private val userRoleRelationService: UserRoleRelationService,
    private val rolePermissionRelationService: UserRolePermissionRelationService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val redisService: ReactiveRedisService
) : UserRbacQueryService {
    override suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult {
        return UserRbacQueryResult(
            userId = userId,
            rolesWithPermissions = userRoleRelationService.getUserRoles(userId).map {
                UserRbacQueryResult.UserRoleWithPermissions(
                    role = it,
                    permissions = rolePermissionRelationService.getRolePermissions(it.id)
                )
            }
        )
    }

    override suspend fun getTenantMemberRbacAccessInfo(tenantMemberId: Long, tenantId: Long): UserTenantRbacQueryResult {
        val roles = tenantMemberRoleRelationService.getMemberRolesRecursive(tenantMemberId)

        return UserTenantRbacQueryResult(
            memberId = tenantMemberId,
            tenantId = tenantId,
            roles = roles,
            permissions = tenantRolePermissionRelationService
                .getRolePermissions(roles.map { it.id })
                .distinctBy { it.id }
                .toSet()
        )
    }

    override suspend fun getUserAuthorities(
        userId: Long,
        tenantId: Long?,
        tenantMemberId: Long?,
        refreshCache: Boolean
    ): Set<GrantedAuthority> {
        val redisKey = RedisConstants.getUserAuthoritiesCacheKey(userId, tenantId)
        val cache = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()
            ?.split(",")
        return if (!refreshCache && cache != null) {
            cache.map { GrantedAuthority { it } }.toSet()
        } else {
            val rbac = this.getUserRbacAccessInfo(userId)

            val rbacPermissions = rbac
                .rawPermissions
                .map { it.name }

            val rbacRoles = rbac
                .roles
                .map { it.name }

            val (tenantRbacPermissions, tenantRbacRoles) = if (tenantId != null && tenantMemberId != null) {
                val tenantRbac = this.getTenantMemberRbacAccessInfo(tenantMemberId, tenantId)

                val tenantRbacPermissions = tenantRbac
                    .permissions
                    .map { it.name }

                val tenantRbacRoles = tenantRbac
                    .roles
                    .map { it.name }

                tenantRbacPermissions to tenantRbacRoles
            } else {
                emptyList<String>() to emptyList<String>()
            }

            val permissions = rbacPermissions +
                    tenantRbacPermissions +
                    rbacRoles.map { RbacConstants.ROLE_PREFIX + it } +
                    tenantRbacRoles.map { RbacConstants.TENANT_ROLE_PREFIX + it }

            redisService.set(
                redisKey,
                permissions.joinToString(separator = ",") { it },
                RedisConstants.USER_AUTHORITIES_CACHE_TTL
            ).awaitFirstOrNull()

            // Register this slice under the user's index Set and keep the index strictly longer-lived
            // than any slice, so clearUserAuthoritiesCache can always enumerate and drop every slice.
            val indexKey = RedisConstants.getUserAuthoritiesIndexKey(userId)
            redisService.opsForSet<String>().add(indexKey, redisKey).awaitFirstOrNull()
            redisService.expire(indexKey, RedisConstants.USER_AUTHORITIES_INDEX_TTL).awaitFirstOrNull()

            permissions.map { GrantedAuthority { it } }.toSet()
        }
    }

    override suspend fun clearUserAuthoritiesCache(userId: Long) {
        val indexKey = RedisConstants.getUserAuthoritiesIndexKey(userId)
        val sliceKeys = redisService.opsForSet<String>()
            .members(indexKey)
            .collectList()
            .awaitFirstOrNull()
            .orEmpty()

        redisService.removeKey(*(sliceKeys + indexKey).toTypedArray()).awaitFirstOrNull()
    }
}