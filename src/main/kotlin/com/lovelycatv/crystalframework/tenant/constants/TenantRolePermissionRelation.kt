package com.lovelycatv.crystalframework.tenant.constants

object TenantRolePermissionRelation {
    val mapping = mapOf(
        TenantRole.ROOT to TenantPermission.allPermissions(),
        TenantRole.SUPER_ADMIN to listOf(
            // Profile
            TenantPermission.MENU_TENANT_PROFILE_MANAGER,
            TenantPermission.ACTION_TENANT_PROFILE_UPDATE,
            // Roles
            TenantPermission.ACTION_TENANT_ROLE_CREATE,
            TenantPermission.ACTION_TENANT_ROLE_UPDATE,
            TenantPermission.ACTION_TENANT_ROLE_DELETE,
            // Role Permissions
            TenantPermission.ACTION_TENANT_ROLE_PERMISSION_READ,
            TenantPermission.ACTION_TENANT_ROLE_PERMISSION_UPDATE,
            // Member Roles
            TenantPermission.MENU_TENANT_MEMBER_ROLE_MANAGER,
            TenantPermission.ACTION_TENANT_MEMBER_ROLE_READ,
            TenantPermission.ACTION_TENANT_MEMBER_ROLE_UPDATE,
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
            // Roles
            TenantPermission.MENU_TENANT_ROLE_MANAGER,
            TenantPermission.ACTION_TENANT_ROLE_READ,
            // Departments
            TenantPermission.MENU_TENANT_DEPARTMENT_MANAGER,
            TenantPermission.ACTION_TENANT_DEPARTMENT_CREATE,
            TenantPermission.ACTION_TENANT_DEPARTMENT_READ,
            TenantPermission.ACTION_TENANT_DEPARTMENT_UPDATE,
            TenantPermission.ACTION_TENANT_DEPARTMENT_DELETE,
            // Department Members
            TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_CREATE,
            TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_READ,
            TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE,
            TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_DELETE,
            // Mail
            TenantPermission.ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL
        ),
        TenantRole.MEMBER to listOf(
            // Dashboard
            TenantPermission.MENU_MY_TENANT_DASHBOARD,
            TenantPermission.ACTION_TENANT_PROFILE_READ_BASIC
        ),
    )
}