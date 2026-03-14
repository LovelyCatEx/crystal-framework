package com.lovelycatv.crystalframework.tenant.constants

object TenantRolePermissionRelation {
    val mapping = mapOf(
        TenantRole.ROOT to TenantPermission.allPermissions(),
        TenantRole.SUPER_ADMIN to listOf(
            // Profile
            TenantPermission.MENU_TENANT_PROFILE_MANAGER,
            TenantPermission.ACTION_TENANT_PROFILE_UPDATE,
        ),
        TenantRole.ADMIN to listOf(
            // Profile
            TenantPermission.ACTION_TENANT_PROFILE_READ,
            // Members
            TenantPermission.MENU_TENANT_MEMBER_MANAGER,
            TenantPermission.ACTION_TENANT_MEMBER_READ,
            TenantPermission.ACTION_TENANT_MEMBER_UPDATE,
            TenantPermission.ACTION_TENANT_MEMBER_DELETE,
            // Invitations
            TenantPermission.MENU_TENANT_INVITATION_MANAGER,
            TenantPermission.ACTION_TENANT_INVITATION_CREATE,
            TenantPermission.ACTION_TENANT_INVITATION_READ,
            TenantPermission.ACTION_TENANT_INVITATION_UPDATE,
            TenantPermission.ACTION_TENANT_INVITATION_DELETE,
        ),
        TenantRole.MEMBER to listOf(
            TenantPermission.MENU_MY_TENANT_DASHBOARD,
            TenantPermission.ACTION_TENANT_PROFILE_READ_BASIC
        ),
    )
}