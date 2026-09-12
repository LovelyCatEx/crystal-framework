package com.lovelycatv.crystalframework.shared.constants


object SystemRolePermissionRelation {
    val mapping = mapOf(
        SystemRole.ROLE_ROOT to SystemPermission.allPermissions(),
        SystemRole.ROLE_ADMIN to listOf(
            // System
            SystemPermission.ACTION_SYSTEM_MAINTENANCE_ACCESS,
            // Dashboard
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_BUSINESS_STATISTICS,
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_SYSTEM_METRICS,
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_MY_TENANTS,
            SystemPermission.ACTION_SYSTEM_DASHBOARD_BUSINESS_STATISTICS_READ,
            SystemPermission.ACTION_SYSTEM_DASHBOARD_SYSTEM_METRICS_READ,
            // User
            SystemPermission.MENU_SYSTEM_USER_MANAGER,
            SystemPermission.ACTION_SYSTEM_USER_READ,
            SystemPermission.ACTION_SYSTEM_USER_BAN,
            SystemPermission.ACTION_SYSTEM_USER_UNBAN,
            SystemPermission.ACTION_SYSTEM_USER_SET_ENABLED,
            SystemPermission.ACTION_SYSTEM_USER_BAN_RECORD_READ,
            SystemPermission.MENU_SYSTEM_USER_BAN_RECORD,
            SystemPermission.MENU_SYSTEM_OAUTH_ACCOUNT_MANAGER,
            SystemPermission.ACTION_SYSTEM_OAUTH_ACCOUNT_READ,
            // Storage
            SystemPermission.MENU_SYSTEM_STORAGE_PROVIDER_MANAGER,
            SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_READ,
            SystemPermission.MENU_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_MANAGER,
            SystemPermission.ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_READ,
            // File Resource — menu
            SystemPermission.MENU_SYSTEM_FILE_RESOURCE_MANAGER,
            // File Resource — system scope
            SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_CREATE,
            SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_READ,
            SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_UPDATE,
            SystemPermission.ACTION_SYSTEM_FILE_RESOURCE_DELETE,
            // File Resource — tenant-admin (cross-tenant, TENANT scope only)
            SystemPermission.ACTION_TENANT_FILE_RESOURCE_CREATE,
            SystemPermission.ACTION_TENANT_FILE_RESOURCE_READ,
            SystemPermission.ACTION_TENANT_FILE_RESOURCE_UPDATE,
            SystemPermission.ACTION_TENANT_FILE_RESOURCE_DELETE,
            // File Resource — super (cross-scope, admin-only)
            SystemPermission.ACTION_X_FILE_RESOURCE_CREATE,
            SystemPermission.ACTION_X_FILE_RESOURCE_READ,
            SystemPermission.ACTION_X_FILE_RESOURCE_UPDATE,
            SystemPermission.ACTION_X_FILE_RESOURCE_DELETE,
            // Mail template
            SystemPermission.MENU_SYSTEM_MAIL_TEMPLATE_MANAGER,
            SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_READ,
            SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_UPDATE,
            SystemPermission.MENU_SYSTEM_MAIL_TEMPLATE_TYPE_MANAGER,
            SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_READ,
            SystemPermission.MENU_SYSTEM_MAIL_TEMPLATE_CATEGORY_MANAGER,
            SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_CATEGORY_READ,
            // Audit log
            SystemPermission.MENU_SYSTEM_AUDIT_LOG_MANAGER,
            SystemPermission.ACTION_SYSTEM_AUDIT_LOG_READ,
            // Mail log
            SystemPermission.MENU_SYSTEM_MAIL_SEND_LOG_MANAGER,
            SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ,
            // System settings
            SystemPermission.MENU_SYSTEM_SETTINGS,
            SystemPermission.ACTION_SYSTEM_SETTINGS_READ,
            SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_EMAIL,
            SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_MESSAGE,
            // Monitor
            SystemPermission.MENU_SYSTEM_MONITOR,
            SystemPermission.ACTION_SYSTEM_MONITOR_READ,
            SystemPermission.MENU_SYSTEM_MONITOR_SESSIONS,
            SystemPermission.ACTION_SYSTEM_MONITOR_SESSIONS_READ,
            // Announcement
            SystemPermission.MENU_SYSTEM_ANNOUNCEMENT_MANAGER,
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_READ,
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_CREATE,
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_UPDATE,
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_DELETE,
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_LIST,
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_ANNOUNCEMENTS,
            // Tenant Tire Benefit
            SystemPermission.MENU_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_MANAGER,
            SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_READ,
            SystemPermission.MENU_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_MANAGER,
            SystemPermission.ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_READ,
            // Message Channel — menus
            SystemPermission.MENU_SYSTEM_MESSAGE_CHANNEL_MANAGER,
            SystemPermission.MENU_TENANT_MESSAGE_CHANNEL_MANAGER,
            // Message Channel — system scope
            SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE,
            SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_READ,
            SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE,
            SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE,
            // Message Channel — tenant-admin (cross-tenant, TENANT scope only)
            SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE,
            SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ,
            SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE,
            SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE,
            // Message Channel — super (cross-scope, admin-only)
            SystemPermission.ACTION_X_MESSAGE_CHANNEL_CREATE,
            SystemPermission.ACTION_X_MESSAGE_CHANNEL_READ,
            SystemPermission.ACTION_X_MESSAGE_CHANNEL_UPDATE,
            SystemPermission.ACTION_X_MESSAGE_CHANNEL_DELETE,
            // Approval Flow Instance (read-all)
            SystemPermission.MENU_X_APPROVAL_FLOW_INSTANCE_MANAGER,
            SystemPermission.MENU_TENANT_APPROVAL_FLOW_INSTANCE_MANAGER,
            SystemPermission.ACTION_X_APPROVAL_FLOW_INSTANCE_READ,
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
            // Approval Flow Definition — menus
            SystemPermission.MENU_SYSTEM_APPROVAL_FLOW_DEFINITION_MANAGER,
            SystemPermission.MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER,
            // Approval Flow Definition — system scope
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE,
            // Approval Flow Definition — tenant-admin (cross-tenant, TENANT scope only)
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE,
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ,
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE,
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE,
            // Approval Flow Definition — super (cross-scope, admin-only)
            SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_CREATE,
            SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_READ,
            SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_UPDATE,
            SystemPermission.ACTION_X_APPROVAL_FLOW_DEFINITION_DELETE,
            // Dictionary — menus
            SystemPermission.MENU_SYSTEM_DICT_TYPE_MANAGER,
            SystemPermission.MENU_SYSTEM_DICT_ITEM_MANAGER,
            SystemPermission.MENU_TENANT_DICT_TYPE_MANAGER,
            SystemPermission.MENU_TENANT_DICT_ITEM_MANAGER,
            // Dictionary — system scope
            SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE,
            SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ,
            SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE,
            SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE,
            SystemPermission.ACTION_SYSTEM_DICT_ITEM_CREATE,
            SystemPermission.ACTION_SYSTEM_DICT_ITEM_READ,
            SystemPermission.ACTION_SYSTEM_DICT_ITEM_UPDATE,
            SystemPermission.ACTION_SYSTEM_DICT_ITEM_DELETE,
            // Dictionary — tenant-admin (cross-tenant, TENANT scope only)
            SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE,
            SystemPermission.ACTION_TENANT_DICT_TYPE_READ,
            SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE,
            SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE,
            SystemPermission.ACTION_TENANT_DICT_ITEM_CREATE,
            SystemPermission.ACTION_TENANT_DICT_ITEM_READ,
            SystemPermission.ACTION_TENANT_DICT_ITEM_UPDATE,
            SystemPermission.ACTION_TENANT_DICT_ITEM_DELETE,
            // Dictionary — super (cross-scope, admin-only)
            SystemPermission.ACTION_X_DICT_TYPE_CREATE,
            SystemPermission.ACTION_X_DICT_TYPE_READ,
            SystemPermission.ACTION_X_DICT_TYPE_UPDATE,
            SystemPermission.ACTION_X_DICT_TYPE_DELETE,
            SystemPermission.ACTION_X_DICT_ITEM_CREATE,
            SystemPermission.ACTION_X_DICT_ITEM_READ,
            SystemPermission.ACTION_X_DICT_ITEM_UPDATE,
            SystemPermission.ACTION_X_DICT_ITEM_DELETE,
        ),
        SystemRole.ROLE_USER to listOf(
            // Dashboard
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_MY_TENANTS,
            SystemPermission.COMPONENT_SYSTEM_DASHBOARD_ANNOUNCEMENTS,
            // Announcement
            SystemPermission.ACTION_SYSTEM_ANNOUNCEMENT_LIST,
        )
    )
}
