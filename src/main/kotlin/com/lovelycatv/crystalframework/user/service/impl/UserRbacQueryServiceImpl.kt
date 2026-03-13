package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.rbac.service.UserRolePermissionRelationService
import com.lovelycatv.crystalframework.rbac.service.UserRoleRelationService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantMemberRoleRelationService
import com.lovelycatv.crystalframework.tenant.service.TenantRolePermissionRelationService
import com.lovelycatv.crystalframework.tenant.service.impl.TenantMemberRelationServiceImpl
import com.lovelycatv.crystalframework.user.service.UserRbacQueryService
import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import com.lovelycatv.crystalframework.user.service.result.UserTenantRbacQueryResult
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserRbacQueryServiceImpl(
    private val userRoleRelationService: UserRoleRelationService,
    private val rolePermissionRelationService: UserRolePermissionRelationService,
    private val tenantMemberRelationService: TenantMemberRelationService,
    private val tenantMemberRoleRelationService: TenantMemberRoleRelationService,
    private val tenantRolePermissionRelationService: TenantRolePermissionRelationService,
    private val redisService: RedisService
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

    override suspend fun getUserTenantRbacAccessInfo(userId: Long): UserTenantRbacQueryResult {
        return UserTenantRbacQueryResult(
            userId = userId,
            tenants = tenantMemberRelationService
                .getUserTenantMembers(userId)
                .map { tenantMemberEntity ->
                    val memberId = tenantMemberEntity.id
                    val roles = tenantMemberRoleRelationService
                        .getMemberRolesRecursive(memberId)

                    UserTenantRbacQueryResult.Tenant(
                        tenantId = tenantMemberEntity.tenantId,
                        roles = roles,
                        permissions = roles
                            .flatMap {
                                tenantRolePermissionRelationService
                                    .getRolePermissions(it.id)
                                    .toSet()
                            }
                            .distinctBy { it.id }
                            .toSet()
                    )
                }
        )
    }

    override suspend fun getUserAuthorities(userId: Long, tenantId: Long?, refreshCache: Boolean): Set<GrantedAuthority> {
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

            val tenantRbacPermissions = this
                .getUserTenantRbacAccessInfo(userId)
                .tenants
                .filter { it.tenantId == tenantId }
                .flatMap { it.permissions }
                .map { it.name }

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