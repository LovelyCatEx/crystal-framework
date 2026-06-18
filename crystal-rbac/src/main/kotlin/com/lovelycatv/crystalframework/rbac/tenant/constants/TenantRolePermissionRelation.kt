package com.lovelycatv.crystalframework.rbac.tenant.constants

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
            // Tenant Settings
            TenantPermission.ACTION_TENANT_SETTINGS_READ,
            TenantPermission.ACTION_TENANT_SETTINGS_UPDATE,
            // Approval Flow Instance (read-all within own tenant)
            TenantPermission.MENU_TENANT_APPROVAL_FLOW_INSTANCE_MANAGER,
            TenantPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
            // Approval Flow Definition (within own tenant)
            TenantPermission.MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER,
            TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE,
            TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ,
            TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE,
            TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE,
            // Dictionary (within own tenant)
            TenantPermission.MENU_TENANT_DICT_TYPE_MANAGER,
            TenantPermission.MENU_TENANT_DICT_ITEM_MANAGER,
            TenantPermission.ACTION_TENANT_DICT_TYPE_CREATE,
            TenantPermission.ACTION_TENANT_DICT_TYPE_READ,
            TenantPermission.ACTION_TENANT_DICT_TYPE_UPDATE,
            TenantPermission.ACTION_TENANT_DICT_TYPE_DELETE,
            TenantPermission.ACTION_TENANT_DICT_ITEM_CREATE,
            TenantPermission.ACTION_TENANT_DICT_ITEM_READ,
            TenantPermission.ACTION_TENANT_DICT_ITEM_UPDATE,
            TenantPermission.ACTION_TENANT_DICT_ITEM_DELETE,
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
            TenantPermission.ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL,
            // Message Channels
            TenantPermission.MENU_TENANT_MESSAGE_CHANNEL_MANAGER,
            TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE,
            TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ,
            TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE,
            TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE,
        ),
        TenantRole.MEMBER to listOf(
            // Dashboard
            TenantPermission.MENU_MY_TENANT_DASHBOARD,
            TenantPermission.ACTION_TENANT_PROFILE_READ_BASIC,
            // Personal Profile (own tenant-scoped OAuth bindings)
            TenantPermission.MENU_TENANT_PERSONAL_PROFILE_MANAGER,
            TenantPermission.ACTION_TENANT_OAUTH_READ,
            TenantPermission.ACTION_TENANT_OAUTH_BIND,
            TenantPermission.ACTION_TENANT_OAUTH_UNBIND,
        ),
    )
}