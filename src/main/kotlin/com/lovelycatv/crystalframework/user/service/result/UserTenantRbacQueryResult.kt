package com.lovelycatv.crystalframework.user.service.result

import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity

data class UserTenantRbacQueryResult(
    val userId: Long,
    val tenants: List<Tenant>
) {
    data class Tenant(
        val tenantId: Long,
        val roles: Set<TenantRoleEntity>,
        val permissions: Set<TenantPermissionEntity>,
    )
}