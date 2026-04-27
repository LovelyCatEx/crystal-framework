package com.lovelycatv.crystalframework.rbac.constants

import kotlin.reflect.full.memberProperties

object SystemRolePermissionRelation {
    val mapping = mapOf(
        SystemRole.ROLE_ROOT to SystemPermission::class.memberProperties.map {
            it.getter.call() as? String?
                ?: throw IllegalStateException("could not call member property ${it.name} in ${SystemPermission::class.qualifiedName}")
        },
        SystemRole.ROLE_ADMIN to listOf(
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
            // System settings
            SystemPermission.MENU_SYSTEM_SETTINGS,
            SystemPermission.ACTION_SYSTEM_SETTINGS_READ,
        ),
        SystemRole.ROLE_USER to listOf(
            // Dashboard
            SystemPermission.COMPONENT_DASHBOARD_MY_TENANTS,
        )
    )
}