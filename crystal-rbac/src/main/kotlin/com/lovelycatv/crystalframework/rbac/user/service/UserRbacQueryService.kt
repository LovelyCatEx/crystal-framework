package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.service.result.UserRbacQueryResult
import com.lovelycatv.crystalframework.rbac.user.service.result.UserTenantRbacQueryResult
import org.springframework.security.core.GrantedAuthority

interface UserRbacQueryService {
    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult

    suspend fun getTenantMemberRbacAccessInfo(tenantMemberId: Long, tenantId: Long): UserTenantRbacQueryResult

    suspend fun getUserAuthorities(
        userId: Long,
        tenantId: Long?,
        tenantMemberId: Long?,
        refreshCache: Boolean = false
    ): Set<GrantedAuthority>

    suspend fun clearUserAuthoritiesCache(userId: Long)
}