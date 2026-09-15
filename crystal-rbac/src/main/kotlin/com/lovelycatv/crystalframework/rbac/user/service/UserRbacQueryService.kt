/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.service.result.UserRbacQueryResult
import com.lovelycatv.crystalframework.rbac.user.service.result.UserTenantRbacQueryResult
import org.springframework.security.core.GrantedAuthority

interface UserRbacQueryService {
    suspend fun getUserRbacAccessInfo(userId: Long): UserRbacQueryResult

    suspend fun getTenantMemberRbacAccessInfo(tenantMemberId: Long, tenantId: Long): UserTenantRbacQueryResult

    suspend fun getTenantPermissionsByRoleIds(roleIds: Collection<Long>): Set<TenantPermissionEntity>

    suspend fun getUserAuthorities(
        userId: Long,
        tenantId: Long?,
        tenantMemberId: Long?,
        refreshCache: Boolean = false
    ): Set<GrantedAuthority>

    suspend fun clearUserAuthoritiesCache(userId: Long)
}