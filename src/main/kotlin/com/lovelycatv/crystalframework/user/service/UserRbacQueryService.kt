package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.user.service.result.UserRbacQueryResult
import com.lovelycatv.crystalframework.user.service.result.UserTenantRbacQueryResult
import org.springframework.security.core.GrantedAuthority

interface UserRbacQueryService {
    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult

    suspend fun getUserTenantRbacAccessInfo(userId: Long): UserTenantRbacQueryResult

    suspend fun getUserAuthorities(userId: Long, tenantId: Long?): Set<GrantedAuthority>
}