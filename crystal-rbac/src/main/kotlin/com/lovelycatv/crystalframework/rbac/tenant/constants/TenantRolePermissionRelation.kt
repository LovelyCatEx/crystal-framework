package com.lovelycatv.crystalframework.rbac.tenant.constants

object TenantRolePermissionRelation {
    val mapping = mapOf(
        TenantRole.ROOT to TenantPermission.allPermissions(),
        TenantRole.SUPER_ADMIN to listOf(
            // Profile
            TenantPermission.MENU_PROFILE,
            TenantPermission.ACTION_PROFILE_UPDATE,
            // Roles
            TenantPermission.ACTION_ROLE_CREATE,
            TenantPermission.ACTION_ROLE_UPDATE,
            TenantPermission.ACTION_ROLE_DELETE,
            // Role Permissions
            TenantPermission.ACTION_ROLE_PERMISSION_READ,
            TenantPermission.ACTION_ROLE_PERMISSION_UPDATE,
            // Member Roles
            TenantPermission.MENU_MEMBER_ROLE,
            TenantPermission.ACTION_MEMBER_ROLE_READ,
            TenantPermission.ACTION_MEMBER_ROLE_UPDATE,
            // Tenant Settings
            TenantPermission.ACTION_SETTINGS_READ,
            TenantPermission.ACTION_SETTINGS_UPDATE,
            // Approval Flow Instance (read-all within own tenant)
            TenantPermission.MENU_APPROVAL_FLOW_INSTANCE,
            TenantPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
            // Approval Flow Definition (within own tenant)
            TenantPermission.MENU_APPROVAL_FLOW_DEFINITION,
            TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE,
            TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ,
            TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE,
            TenantPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE,
            // Dictionary (within own tenant)
            TenantPermission.MENU_DICT_TYPE,
            TenantPermission.MENU_DICT_ITEM,
            TenantPermission.ACTION_DICT_TYPE_CREATE,
            TenantPermission.ACTION_DICT_TYPE_READ,
            TenantPermission.ACTION_DICT_TYPE_UPDATE,
            TenantPermission.ACTION_DICT_TYPE_DELETE,
            TenantPermission.ACTION_DICT_ITEM_CREATE,
            TenantPermission.ACTION_DICT_ITEM_READ,
            TenantPermission.ACTION_DICT_ITEM_UPDATE,
            TenantPermission.ACTION_DICT_ITEM_DELETE,
            // Broadcast (within own tenant)
            TenantPermission.ACTION_BROADCAST_CREATE,
            TenantPermission.ACTION_BROADCAST_READ,
            TenantPermission.ACTION_BROADCAST_UPDATE,
            TenantPermission.ACTION_BROADCAST_DELETE,
        ),
        TenantRole.ADMIN to listOf(
            // Profile
            TenantPermission.ACTION_PROFILE_READ,
            // Members
            TenantPermission.MENU_MEMBER,
            TenantPermission.ACTION_MEMBER_READ,
            TenantPermission.ACTION_MEMBER_UPDATE,
            TenantPermission.ACTION_MEMBER_DELETE,
            // Invitations
            TenantPermission.MENU_INVITATION,
            TenantPermission.ACTION_INVITATION_CREATE,
            TenantPermission.ACTION_INVITATION_READ,
            TenantPermission.ACTION_INVITATION_UPDATE,
            TenantPermission.ACTION_INVITATION_DELETE,
            // Roles
            TenantPermission.MENU_ROLE,
            TenantPermission.ACTION_ROLE_READ,
            // Departments
            TenantPermission.MENU_DEPARTMENT,
            TenantPermission.ACTION_DEPARTMENT_CREATE,
            TenantPermission.ACTION_DEPARTMENT_READ,
            TenantPermission.ACTION_DEPARTMENT_UPDATE,
            TenantPermission.ACTION_DEPARTMENT_DELETE,
            // Department Members
            TenantPermission.ACTION_DEPARTMENT_MEMBER_CREATE,
            TenantPermission.ACTION_DEPARTMENT_MEMBER_READ,
            TenantPermission.ACTION_DEPARTMENT_MEMBER_UPDATE,
            TenantPermission.ACTION_DEPARTMENT_MEMBER_DELETE,
            // Mail
            TenantPermission.ACTION_MEMBER_JOIN_REVIEW_EMAIL,
            // Message Channels
            TenantPermission.MENU_MESSAGE_CHANNEL,
            TenantPermission.ACTION_MESSAGE_CHANNEL_CREATE,
            TenantPermission.ACTION_MESSAGE_CHANNEL_READ,
            TenantPermission.ACTION_MESSAGE_CHANNEL_UPDATE,
            TenantPermission.ACTION_MESSAGE_CHANNEL_DELETE,
            // File Resources
            TenantPermission.ACTION_FILE_RESOURCE_CREATE,
            TenantPermission.ACTION_FILE_RESOURCE_READ,
            TenantPermission.ACTION_FILE_RESOURCE_UPDATE,
            TenantPermission.ACTION_FILE_RESOURCE_DELETE,
        ),
        TenantRole.MEMBER to listOf(
            // Dashboard
            TenantPermission.MENU_DASHBOARD,
            TenantPermission.ACTION_PROFILE_READ_BASIC,
            // Personal Profile (own tenant-scoped OAuth bindings)
            TenantPermission.MENU_PERSONAL_PROFILE,
            TenantPermission.ACTION_PERSONAL_OAUTH_READ,
            TenantPermission.ACTION_PERSONAL_OAUTH_BIND,
            TenantPermission.ACTION_PERSONAL_OAUTH_UNBIND,
        ),
    )
}
