package com.lovelycatv.crystalframework.shared.constants

import kotlin.reflect.full.memberProperties

object SystemRolePermissionRelation {
    val mapping = mapOf(
        SystemRole.ROLE_ROOT to SystemPermission::class.memberProperties.map {
            it.getter.call() as? String?
                ?: throw IllegalStateException("could not call member property ${it.name} in ${SystemPermission::class.qualifiedName}")
        },
        SystemRole.ROLE_ADMIN to listOf(
            // System
            SystemPermission.ACTION_SYSTEM_MAINTENANCE_ACCESS,
            // Dashboard
            SystemPermission.COMPONENT_DASHBOARD_BUSINESS_STATISTICS,
            SystemPermission.COMPONENT_DASHBOARD_SYSTEM_METRICS,
            SystemPermission.COMPONENT_DASHBOARD_MY_TENANTS,
            SystemPermission.ACTION_DASHBOARD_BUSINESS_STATISTICS_READ,
            SystemPermission.ACTION_DASHBOARD_SYSTEM_METRICS_READ,
            // User
            SystemPermission.MENU_USER_MANAGER,
            SystemPermission.ACTION_USER_READ,
            SystemPermission.MENU_OAUTH_ACCOUNT_MANAGER,
            SystemPermission.ACTION_OAUTH_ACCOUNT_READ,
            // Storage
            SystemPermission.MENU_STORAGE_PROVIDER_MANAGER,
            SystemPermission.ACTION_STORAGE_PROVIDER_READ,
            SystemPermission.MENU_FILE_RESOURCE_MANAGER,
            SystemPermission.ACTION_FILE_RESOURCE_READ,
            // Mail template
            SystemPermission.MENU_MAIL_TEMPLATE_MANAGER,
            SystemPermission.ACTION_MAIL_TEMPLATE_READ,
            SystemPermission.ACTION_MAIL_TEMPLATE_UPDATE,
            SystemPermission.MENU_MAIL_TEMPLATE_TYPE_MANAGER,
            SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_READ,
            SystemPermission.MENU_MAIL_TEMPLATE_CATEGORY_MANAGER,
            SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_READ,
            // Audit log
            SystemPermission.MENU_AUDIT_LOG_MANAGER,
            SystemPermission.ACTION_AUDIT_LOG_READ,
            // Mail log
            SystemPermission.MENU_MAIL_SEND_LOG_MANAGER,
            SystemPermission.ACTION_MAIL_SEND_LOG_READ,
            // System settings
            SystemPermission.MENU_SYSTEM_SETTINGS,
            SystemPermission.ACTION_SYSTEM_SETTINGS_READ,
            SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_EMAIL,
            SystemPermission.ACTION_SYSTEM_SETTINGS_TEST_SEND_MESSAGE,
            // Monitor
            SystemPermission.MENU_MONITOR_SESSIONS,
            SystemPermission.ACTION_MONITOR_SESSIONS_READ,
            // Announcement
            SystemPermission.MENU_ANNOUNCEMENT_MANAGER,
            SystemPermission.ACTION_ANNOUNCEMENT_READ,
            SystemPermission.ACTION_ANNOUNCEMENT_CREATE,
            SystemPermission.ACTION_ANNOUNCEMENT_UPDATE,
            SystemPermission.ACTION_ANNOUNCEMENT_DELETE,
            SystemPermission.ACTION_ANNOUNCEMENT_LIST,
            SystemPermission.COMPONENT_DASHBOARD_ANNOUNCEMENTS,
            // Tenant Tire Benefit
            SystemPermission.MENU_TENANT_TIRE_BENEFIT_FEATURE_MANAGER,
            SystemPermission.ACTION_TENANT_TIRE_BENEFIT_FEATURE_READ,
            SystemPermission.MENU_TENANT_TIRE_BENEFIT_VALUE_MANAGER,
            SystemPermission.ACTION_TENANT_TIRE_BENEFIT_VALUE_READ,
            // Tenant Message Channel
            SystemPermission.MENU_TENANT_MESSAGE_CHANNEL_MANAGER,
            SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ,
            // Approval Flow Instance (read-all)
            SystemPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ,
            SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ,
            // Approval Flow Definition — menus
            SystemPermission.MENU_APPROVAL_FLOW_DEFINITION_MANAGER,
            SystemPermission.MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER,
            // Approval Flow Definition — system scope
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE,
            SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE,
            // Approval Flow Definition — super (cross-scope, admin-only)
            SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE,
            SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ,
            SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE,
            SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE,
            // Dictionary — menus
            SystemPermission.MENU_SYSTEM_DICT_TYPE_MANAGER,
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
            // Dictionary — super (cross-scope, admin-only)
            SystemPermission.ACTION_DICT_TYPE_CREATE,
            SystemPermission.ACTION_DICT_TYPE_READ,
            SystemPermission.ACTION_DICT_TYPE_UPDATE,
            SystemPermission.ACTION_DICT_TYPE_DELETE,
            SystemPermission.ACTION_DICT_ITEM_CREATE,
            SystemPermission.ACTION_DICT_ITEM_READ,
            SystemPermission.ACTION_DICT_ITEM_UPDATE,
            SystemPermission.ACTION_DICT_ITEM_DELETE,
        ),
        SystemRole.ROLE_USER to listOf(
            // Dashboard
            SystemPermission.COMPONENT_DASHBOARD_MY_TENANTS,
            SystemPermission.COMPONENT_DASHBOARD_ANNOUNCEMENTS,
            // Announcement
            SystemPermission.ACTION_ANNOUNCEMENT_LIST,
        )
    )
}