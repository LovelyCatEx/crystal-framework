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
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration

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
            roles = emptySet(),
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
        val redisKey = "userAuthorities:$userId"
        val cache = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()
            ?.split(",")
        return if (!refreshCache && cache != null) {
            cache.map { GrantedAuthority { it } }.toSet()
        } else {
            val rbacPermissions = this
                .getUserRbacAccessInfo(userId)
                .actions
                .map { it.name }

            val tenantRbacPermissions = if (tenantId != null && tenantMemberId != null) {
                this
                    .getTenantMemberRbacAccessInfo(tenantMemberId, tenantId)
                    .permissions
                    .filter { it.type == TenantPermissionType.ACTION.typeId }
                    .map { it.name }
            } else {
                emptySet()
            }

            val permissions = rbacPermissions + tenantRbacPermissions

            redisService.set(
                redisKey,
                permissions.joinToString(
                    separator = ",",
                    prefix = "",
                    postfix = ""
                ) { it },
                Duration.ofDays(3)
            ).awaitFirstOrNull()

            permissions.map { GrantedAuthority { it } }.toSet()
        }
    }

    override suspend fun clearUserAuthoritiesCache(userId: Long) {
        val redisKey = "userAuthorities:$userId"
        redisService.removeKey(redisKey).awaitFirstOrNull()
    }
}