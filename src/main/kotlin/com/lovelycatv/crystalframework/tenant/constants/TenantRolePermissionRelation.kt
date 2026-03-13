package com.lovelycatv.crystalframework.tenant.constants

object TenantRolePermissionRelation {
    val mapping = mapOf(
        TenantRole.ROOT to TenantPermission.allPermissions(),
        TenantRole.SUPER_ADMIN to listOf(
            TenantPermission.MENU_TENANT_PROFILE_MANAGER,
            TenantPermission.ACTION_TENANT_PROFILE_READ,
            TenantPermission.ACTION_TENANT_PROFILE_UPDATE,
        ),
        TenantRole.ADMIN to listOf(
            TenantPermission.ACTION_TENANT_PROFILE_READ,
        ),
        TenantRole.MEMBER to listOf(
            TenantPermission.MENU_MY_TENANT_DASHBOARD,
            TenantPermission.ACTION_TENANT_PROFILE_READ_BASIC
        ),
    )
}