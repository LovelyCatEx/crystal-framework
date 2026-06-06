package com.lovelycatv.crystalframework.rbac.user.service.result

import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity

data class UserTenantRbacQueryResult(
    val memberId: Long,
    val tenantId: Long,
    val roles: Set<TenantRoleEntity>,
    val permissions: Set<TenantPermissionEntity>,
)