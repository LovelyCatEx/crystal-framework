package com.lovelycatv.crystalframework.rbac.constants

import com.lovelycatv.crystalframework.rbac.types.PermissionType

object SystemPermission {
    const val MENU_PERMISSION_MANAGER = "permission:/manager/user-permissions"
    const val MENU_ROLE_MANAGER = "role:/manager/user-roles"
    const val MENU_USER_MANAGER = "user:/manager/users"
    const val MENU_ROLE_PERMISSION_MANAGER = "role.permission:/manager/role-permissions"
    const val MENU_USER_ROLE_MANAGER = "user.role:/manager/user-roles-relation"
    const val MENU_SYSTEM_SETTINGS = "settings:/manager/settings"
    const val MENU_OAUTH_ACCOUNT_MANAGER = "oauth.account:/manager/oauth-accounts"

    const val ACTION_PERMISSION_CREATE = "permission.create"
    const val ACTION_PERMISSION_READ = "permission.read"
    const val ACTION_PERMISSION_UPDATE = "permission.update"
    const val ACTION_PERMISSION_DELETE = "permission.delete"

    const val ACTION_ROLE_CREATE = "role.create"
    const val ACTION_ROLE_READ = "role.read"
    const val ACTION_ROLE_UPDATE = "role.update"
    const val ACTION_ROLE_DELETE = "role.delete"

    const val ACTION_USER_CREATE = "user.create"
    const val ACTION_USER_READ = "user.read"
    const val ACTION_USER_UPDATE = "user.update"
    const val ACTION_USER_DELETE = "user.delete"

    const val ACTION_ROLE_PERMISSION_READ = "role.permission.read"
    const val ACTION_ROLE_PERMISSION_UPDATE = "role.permission.update"

    const val ACTION_USER_ROLE_READ = "user.role.read"
    const val ACTION_USER_ROLE_UPDATE = "user.role.update"

    const val ACTION_SYSTEM_SETTINGS_READ = "settings.read"
    const val ACTION_SYSTEM_SETTINGS_UPDATE = "settings.update"

    const val ACTION_OAUTH_ACCOUNT_CREATE = "oauth.account.create"
    const val ACTION_OAUTH_ACCOUNT_READ = "oauth.account.read"
    const val ACTION_OAUTH_ACCOUNT_UPDATE = "oauth.account.update"
    const val ACTION_OAUTH_ACCOUNT_DELETE = "oauth.account.delete"

    const val MENU_FILE_RESOURCE_MANAGER = "file.resource:/manager/file-resources"

    const val ACTION_FILE_RESOURCE_CREATE = "file.resource.create"
    const val ACTION_FILE_RESOURCE_READ = "file.resource.read"
    const val ACTION_FILE_RESOURCE_UPDATE = "file.resource.update"
    const val ACTION_FILE_RESOURCE_DELETE = "file.resource.delete"

    const val MENU_STORAGE_PROVIDER_MANAGER = "storage.provider:/manager/storage-providers"

    const val ACTION_STORAGE_PROVIDER_CREATE = "storage.provider.create"
    const val ACTION_STORAGE_PROVIDER_READ = "storage.provider.read"
    const val ACTION_STORAGE_PROVIDER_UPDATE = "storage.provider.update"
    const val ACTION_STORAGE_PROVIDER_DELETE = "storage.provider.delete"

    const val MENU_MAIL_TEMPLATE_CATEGORY_MANAGER = "mail.template.category:/manager/mail-template-categories"

    const val ACTION_MAIL_TEMPLATE_CATEGORY_CREATE = "mail.template.category.create"
    const val ACTION_MAIL_TEMPLATE_CATEGORY_READ = "mail.template.category.read"
    const val ACTION_MAIL_TEMPLATE_CATEGORY_UPDATE = "mail.template.category.update"
    const val ACTION_MAIL_TEMPLATE_CATEGORY_DELETE = "mail.template.category.delete"

    const val MENU_MAIL_TEMPLATE_TYPE_MANAGER = "mail.template.type:/manager/mail-template-types"

    const val ACTION_MAIL_TEMPLATE_TYPE_CREATE = "mail.template.type.create"
    const val ACTION_MAIL_TEMPLATE_TYPE_READ = "mail.template.type.read"
    const val ACTION_MAIL_TEMPLATE_TYPE_UPDATE = "mail.template.type.update"
    const val ACTION_MAIL_TEMPLATE_TYPE_DELETE = "mail.template.type.delete"

    const val MENU_MAIL_TEMPLATE_MANAGER = "mail.template:/manager/mail-templates"

    const val ACTION_MAIL_TEMPLATE_CREATE = "mail.template.create"
    const val ACTION_MAIL_TEMPLATE_READ = "mail.template.read"
    const val ACTION_MAIL_TEMPLATE_UPDATE = "mail.template.update"
    const val ACTION_MAIL_TEMPLATE_DELETE = "mail.template.delete"

    const val MENU_TENANT_MANAGER = "tenant:/manager/tenants"

    const val ACTION_TENANT_CREATE = "tenant.create"
    const val ACTION_TENANT_READ = "tenant.read"
    const val ACTION_TENANT_UPDATE = "tenant.update"
    const val ACTION_TENANT_DELETE = "tenant.delete"

    const val MENU_TENANT_TIRE_TYPE_MANAGER = "tenant.tire.type:/manager/tenant-tire-types"

    const val ACTION_TENANT_TIRE_TYPE_CREATE = "tenant.tire.type.create"
    const val ACTION_TENANT_TIRE_TYPE_READ = "tenant.tire.type.read"
    const val ACTION_TENANT_TIRE_TYPE_UPDATE = "tenant.tire.type.update"
    const val ACTION_TENANT_TIRE_TYPE_DELETE = "tenant.tire.type.delete"

    const val MENU_TENANT_DEPARTMENT_MANAGER = "tenant.department:/manager/tenant-departments"

    const val ACTION_TENANT_DEPARTMENT_CREATE = "tenant.department.create"
    const val ACTION_TENANT_DEPARTMENT_READ = "tenant.department.read"
    const val ACTION_TENANT_DEPARTMENT_UPDATE = "tenant.department.update"
    const val ACTION_TENANT_DEPARTMENT_DELETE = "tenant.department.delete"

    const val MENU_TENANT_ROLE_MANAGER = "tenant.role:/manager/tenant-roles"

    const val ACTION_TENANT_ROLE_CREATE = "tenant.role.create"
    const val ACTION_TENANT_ROLE_READ = "tenant.role.read"
    const val ACTION_TENANT_ROLE_UPDATE = "tenant.role.update"
    const val ACTION_TENANT_ROLE_DELETE = "tenant.role.delete"

    const val MENU_TENANT_PERMISSION_MANAGER = "tenant.permission:/manager/tenant-permissions"

    const val ACTION_TENANT_PERMISSION_CREATE = "tenant.permission.create"
    const val ACTION_TENANT_PERMISSION_READ = "tenant.permission.read"
    const val ACTION_TENANT_PERMISSION_UPDATE = "tenant.permission.update"
    const val ACTION_TENANT_PERMISSION_DELETE = "tenant.permission.delete"

    const val MENU_TENANT_MEMBER_MANAGER = "tenant.member:/manager/tenant-members"

    const val ACTION_TENANT_MEMBER_CREATE = "tenant.member.create"
    const val ACTION_TENANT_MEMBER_READ = "tenant.member.read"
    const val ACTION_TENANT_MEMBER_UPDATE = "tenant.member.update"
    const val ACTION_TENANT_MEMBER_DELETE = "tenant.member.delete"


    const val MENU_TENANT_DEPARTMENT_MEMBER_RELATION_MANAGER = "tenant.department.member:/manager/tenant-department-members"

    const val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE = "tenant.department.member.create"
    const val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ = "tenant.department.member.read"
    const val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE = "tenant.department.member.update"
    const val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE = "tenant.department.member.delete"

    const val MENU_TENANT_MEMBER_ROLE_RELATION_MANAGER = "tenant.member.role:/manager/tenant-member-roles"

    const val ACTION_TENANT_MEMBER_ROLE_RELATION_READ = "tenant.member.role.read"
    const val ACTION_TENANT_MEMBER_ROLE_RELATION_UPDATE = "tenant.member.role.update"

    const val MENU_TENANT_ROLE_PERMISSION_RELATION_MANAGER = "tenant.role.permission:/manager/tenant-role-permissions"

    const val ACTION_TENANT_ROLE_PERMISSION_RELATION_READ = "tenant.role.permission.read"
    const val ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE = "tenant.role.permission.update"

    const val MENU_TENANT_INVITATION_MANAGER = "tenant.invitation:/manager/tenant-invitations"

    const val ACTION_TENANT_INVITATION_CREATE = "tenant.invitation.create"
    const val ACTION_TENANT_INVITATION_READ = "tenant.invitation.read"
    const val ACTION_TENANT_INVITATION_UPDATE = "tenant.invitation.update"
    const val ACTION_TENANT_INVITATION_DELETE = "tenant.invitation.delete"

    const val COMPONENT_DASHBOARD_BUSINESS_STATISTICS = "dashboard.business.statistics@dashboard.business.statistics"
    const val COMPONENT_DASHBOARD_SYSTEM_METRICS = "dashboard.system.metrics@dashboard.system.metrics"

    /**
     * resolve permission from string declaration
     *
     * @param str declaration, eg: "user:/manager/users"
     * @return (name, description, path)
     */
    fun resolvePermissionDeclaration(str: String): Triple<String, String, String?> {
        val type = if (str.contains(":")) {
            PermissionType.MENU
        } else if (str.contains("@")) {
            PermissionType.COMPONENT
        } else {
            PermissionType.ACTION
        }

        return when (type) {
            PermissionType.ACTION -> {
                Triple(str, str, null)
            }

            PermissionType.MENU -> {
                val (readPermissionKey, path) = str.split(":")
                Triple(readPermissionKey, readPermissionKey, path)
            }

            PermissionType.COMPONENT -> {
                val (readPermissionKey, path) = str.split("@")
                Triple(readPermissionKey, readPermissionKey, path)
            }
        }
    }
}