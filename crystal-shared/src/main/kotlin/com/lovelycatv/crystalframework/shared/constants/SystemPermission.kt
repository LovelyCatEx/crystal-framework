package com.lovelycatv.crystalframework.shared.constants

import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemRbacPermissionDeclaration
import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object SystemPermission {
    // ============================================================
    //   Permission  (system)
    // ============================================================
    val ACTION_SYSTEM_PERMISSION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.permission.create",
        description = "Create user permissions"
    )
    val ACTION_SYSTEM_PERMISSION_READ = SystemRbacPermissionDeclaration.action(
        name = "system.permission.read",
        description = "Read user permissions"
    )
    val ACTION_SYSTEM_PERMISSION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.permission.update",
        description = "Update user permissions"
    )
    val ACTION_SYSTEM_PERMISSION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.permission.delete",
        description = "Delete user permissions"
    )
    val MENU_SYSTEM_PERMISSION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.permission",
        path = "/manager/user-permissions",
        description = "Manage user permissions menu"
    )

    // ============================================================
    //   Role  (system)
    // ============================================================
    val ACTION_SYSTEM_ROLE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.role.create",
        description = "Create user roles"
    )
    val ACTION_SYSTEM_ROLE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.role.read",
        description = "Read user roles"
    )
    val ACTION_SYSTEM_ROLE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.role.update",
        description = "Update user roles"
    )
    val ACTION_SYSTEM_ROLE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.role.delete",
        description = "Delete user roles"
    )
    val MENU_SYSTEM_ROLE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.role",
        path = "/manager/user-roles",
        description = "Manage user roles menu"
    )

    // ============================================================
    //   User  (system)
    // ============================================================
    val ACTION_SYSTEM_USER_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.user.create",
        description = "Create users"
    )
    val ACTION_SYSTEM_USER_READ = SystemRbacPermissionDeclaration.action(
        name = "system.user.read",
        description = "Read users"
    )
    val ACTION_SYSTEM_USER_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.user.update",
        description = "Update users"
    )
    val ACTION_SYSTEM_USER_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.user.delete",
        description = "Delete users"
    )
    val ACTION_SYSTEM_USER_REFRESH_AUTHORITY = SystemRbacPermissionDeclaration.action(
        name = "system.user.refreshAuthority",
        description = "Refresh (invalidate) users' authority cache"
    )
    val ACTION_SYSTEM_USER_FORCE_LOGOUT = SystemRbacPermissionDeclaration.action(
        name = "system.user.forceLogout",
        description = "Force users to log out (invalidate existing tokens)"
    )
    val ACTION_SYSTEM_USER_BAN = SystemRbacPermissionDeclaration.action(
        name = "system.user.ban",
        description = "Ban a user for a period"
    )
    val ACTION_SYSTEM_USER_UNBAN = SystemRbacPermissionDeclaration.action(
        name = "system.user.unban",
        description = "Lift a user's ban"
    )
    val ACTION_SYSTEM_USER_SET_ENABLED = SystemRbacPermissionDeclaration.action(
        name = "system.user.setEnabled",
        description = "Enable or disable a user account"
    )
    val ACTION_SYSTEM_USER_BAN_RECORD_READ = SystemRbacPermissionDeclaration.action(
        name = "system.user.banRecord.read",
        description = "Read user ban records"
    )
    val MENU_SYSTEM_USER_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.user",
        path = "/manager/users",
        description = "Manage users menu"
    )
    val MENU_SYSTEM_USER_BAN_RECORD = SystemRbacPermissionDeclaration.menu(
        name = "system.user.banRecord",
        path = "/manager/user-ban-record",
        description = "User ban record menu"
    )

    // ============================================================
    //   Role-Permission Relation  (system)
    // ============================================================
    val ACTION_SYSTEM_ROLE_PERMISSION_READ = SystemRbacPermissionDeclaration.action(
        name = "system.role.permission.read",
        description = "Read role permission assignments"
    )
    val ACTION_SYSTEM_ROLE_PERMISSION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.role.permission.update",
        description = "Update role permission assignments"
    )

    // ============================================================
    //   User-Role Relation  (system)
    // ============================================================
    val ACTION_SYSTEM_USER_ROLE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.user.role.read",
        description = "Read user role assignments"
    )
    val ACTION_SYSTEM_USER_ROLE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.user.role.update",
        description = "Update user role assignments"
    )
    val MENU_SYSTEM_USER_ROLE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.user.role",
        path = "/manager/user-roles-relation",
        description = "Manage user-role assignments menu"
    )

    // ============================================================
    //   System Settings  (system)
    // ============================================================
    val ACTION_SYSTEM_SETTINGS_READ = SystemRbacPermissionDeclaration.action(
        name = "system.settings.read",
        description = "Read system settings"
    )
    val ACTION_SYSTEM_SETTINGS_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.settings.update",
        description = "Update system settings"
    )
    val ACTION_SYSTEM_SETTINGS_TEST_SEND_EMAIL = SystemRbacPermissionDeclaration.action(
        name = "system.settings.test.sendEmail",
        description = "Send test email via system settings"
    )
    val ACTION_SYSTEM_SETTINGS_TEST_SEND_MESSAGE = SystemRbacPermissionDeclaration.action(
        name = "system.settings.test.sendMessage",
        description = "Send test message via system settings"
    )
    val MENU_SYSTEM_SETTINGS = SystemRbacPermissionDeclaration.menu(
        name = "system.settings",
        path = "/manager/settings",
        description = "Access system settings menu"
    )

    // ============================================================
    //   Maintenance  (system)
    // ============================================================
    val ACTION_SYSTEM_MAINTENANCE_ACCESS = SystemRbacPermissionDeclaration.action(
        name = "system.maintenance.access",
        description = "Access maintenance operations"
    )
    val ACTION_SYSTEM_MAINTENANCE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.maintenance.update",
        description = "Update maintenance operations"
    )

    // ============================================================
    //   OAuth Account  (system)
    // ============================================================
    val ACTION_SYSTEM_OAUTH_ACCOUNT_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.oauth.account.create",
        description = "Create OAuth accounts"
    )
    val ACTION_SYSTEM_OAUTH_ACCOUNT_READ = SystemRbacPermissionDeclaration.action(
        name = "system.oauth.account.read",
        description = "Read OAuth accounts"
    )
    val ACTION_SYSTEM_OAUTH_ACCOUNT_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.oauth.account.update",
        description = "Update OAuth accounts"
    )
    val ACTION_SYSTEM_OAUTH_ACCOUNT_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.oauth.account.delete",
        description = "Delete OAuth accounts"
    )
    val MENU_SYSTEM_OAUTH_ACCOUNT_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.oauth.account",
        path = "/manager/oauth-accounts",
        description = "Manage OAuth accounts menu"
    )

    // ============================================================
    //   File Resource  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_FILE_RESOURCE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.file.resource.create",
        description = "Create file resources in any scope"
    )
    val ACTION_X_FILE_RESOURCE_READ = SystemRbacPermissionDeclaration.action(
        name = "x.file.resource.read",
        description = "Read file resources in any scope"
    )
    val ACTION_X_FILE_RESOURCE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.file.resource.update",
        description = "Update file resources in any scope"
    )
    val ACTION_X_FILE_RESOURCE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.file.resource.delete",
        description = "Delete file resources in any scope"
    )

    val ACTION_SYSTEM_FILE_RESOURCE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.file.resource.create",
        description = "Create system-scope file resources"
    )
    val ACTION_SYSTEM_FILE_RESOURCE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.file.resource.read",
        description = "Read system-scope file resources"
    )
    val ACTION_SYSTEM_FILE_RESOURCE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.file.resource.update",
        description = "Update system-scope file resources"
    )
    val ACTION_SYSTEM_FILE_RESOURCE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.file.resource.delete",
        description = "Delete system-scope file resources"
    )
    val MENU_SYSTEM_FILE_RESOURCE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.file.resource",
        path = "/manager/file-resources",
        description = "Manage file resources menu"
    )

    val ACTION_TENANT_FILE_RESOURCE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.file.resource.create",
        description = "Create tenant-scope file resources across tenants"
    )
    val ACTION_TENANT_FILE_RESOURCE_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.file.resource.read",
        description = "Read tenant-scope file resources across tenants"
    )
    val ACTION_TENANT_FILE_RESOURCE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.file.resource.update",
        description = "Update tenant-scope file resources across tenants"
    )
    val ACTION_TENANT_FILE_RESOURCE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.file.resource.delete",
        description = "Delete tenant-scope file resources across tenants"
    )

    // ============================================================
    //   Storage Provider  (system)
    // ============================================================
    val ACTION_SYSTEM_STORAGE_PROVIDER_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.create",
        description = "Create storage providers"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_READ = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.read",
        description = "Read storage providers"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.update",
        description = "Update storage providers"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.delete",
        description = "Delete storage providers"
    )
    val MENU_SYSTEM_STORAGE_PROVIDER_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.storage.provider",
        path = "/manager/storage-providers",
        description = "Manage storage providers menu"
    )

    // ============================================================
    //   Storage Provider Routing Rule  (system)
    // ============================================================
    val ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.routing.rule.create",
        description = "Create storage provider routing rules"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.routing.rule.read",
        description = "Read storage provider routing rules"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.routing.rule.update",
        description = "Update storage provider routing rules"
    )
    val ACTION_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.storage.provider.routing.rule.delete",
        description = "Delete storage provider routing rules"
    )
    val MENU_SYSTEM_STORAGE_PROVIDER_ROUTING_RULE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.storage.provider.routing.rule",
        path = "/manager/storage-provider-routing-rules",
        description = "Manage storage provider routing rules menu"
    )

    // ============================================================
    //   Mail Template Category  (system)
    // ============================================================
    val ACTION_SYSTEM_MAIL_TEMPLATE_CATEGORY_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.category.create",
        description = "Create mail template categories"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_CATEGORY_READ = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.category.read",
        description = "Read mail template categories"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_CATEGORY_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.category.update",
        description = "Update mail template categories"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_CATEGORY_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.category.delete",
        description = "Delete mail template categories"
    )
    val MENU_SYSTEM_MAIL_TEMPLATE_CATEGORY_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.mail.template.category",
        path = "/manager/mail-template-categories",
        description = "Manage mail template categories menu"
    )

    // ============================================================
    //   Mail Template Type  (system)
    // ============================================================
    val ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.type.create",
        description = "Create mail template types"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.type.read",
        description = "Read mail template types"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.type.update",
        description = "Update mail template types"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.type.delete",
        description = "Delete mail template types"
    )
    val MENU_SYSTEM_MAIL_TEMPLATE_TYPE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.mail.template.type",
        path = "/manager/mail-template-types",
        description = "Manage mail template types menu"
    )

    // ============================================================
    //   Mail Template  (system)
    // ============================================================
    val ACTION_SYSTEM_MAIL_TEMPLATE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.create",
        description = "Create mail templates"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.read",
        description = "Read mail templates"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.update",
        description = "Update mail templates"
    )
    val ACTION_SYSTEM_MAIL_TEMPLATE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.mail.template.delete",
        description = "Delete mail templates"
    )
    val MENU_SYSTEM_MAIL_TEMPLATE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.mail.template",
        path = "/manager/mail-templates",
        description = "Manage mail templates menu"
    )

    // ============================================================
    //   Tenant  (system)
    // ============================================================
    val ACTION_SYSTEM_TENANT_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.create",
        description = "Create tenants"
    )
    val ACTION_SYSTEM_TENANT_READ = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.read",
        description = "Read tenants"
    )
    val ACTION_SYSTEM_TENANT_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.update",
        description = "Update tenants"
    )
    val ACTION_SYSTEM_TENANT_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.delete",
        description = "Delete tenants"
    )
    val ACTION_SYSTEM_TENANT_LIFECYCLE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.lifecycle.update",
        description = "Update tenant ownership, tire, subscription window, status, or settings"
    )
    val MENU_SYSTEM_TENANT_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.tenant",
        path = "/manager/tenants",
        description = "Manage tenants menu"
    )

    // ============================================================
    //   Tenant Tire Type  (system)
    // ============================================================
    val ACTION_SYSTEM_TENANT_TIRE_TYPE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.type.create",
        description = "Create tenant tire types"
    )
    val ACTION_SYSTEM_TENANT_TIRE_TYPE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.type.read",
        description = "Read tenant tire types"
    )
    val ACTION_SYSTEM_TENANT_TIRE_TYPE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.type.update",
        description = "Update tenant tire types"
    )
    val ACTION_SYSTEM_TENANT_TIRE_TYPE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.type.delete",
        description = "Delete tenant tire types"
    )
    val MENU_SYSTEM_TENANT_TIRE_TYPE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.tenant.tire.type",
        path = "/manager/tenant-tire-types",
        description = "Manage tenant tire types menu"
    )

    // ============================================================
    //   Tenant Tire Benefit Feature  (system)
    // ============================================================
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.feature.create",
        description = "Create tenant tire benefit features"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.feature.read",
        description = "Read tenant tire benefit features"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.feature.update",
        description = "Update tenant tire benefit features"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.feature.delete",
        description = "Delete tenant tire benefit features"
    )
    val MENU_SYSTEM_TENANT_TIRE_BENEFIT_FEATURE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.tenant.tire.benefit.feature",
        path = "/manager/tenant-tire-benefit-features",
        description = "Manage tenant tire benefit features menu"
    )

    // ============================================================
    //   Tenant Tire Benefit Value  (system)
    // ============================================================
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.value.create",
        description = "Create tenant tire benefit values"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.value.read",
        description = "Read tenant tire benefit values"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.value.update",
        description = "Update tenant tire benefit values"
    )
    val ACTION_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.tenant.tire.benefit.value.delete",
        description = "Delete tenant tire benefit values"
    )
    val MENU_SYSTEM_TENANT_TIRE_BENEFIT_VALUE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.tenant.tire.benefit.value",
        path = "/manager/tenant-tire-benefit-values",
        description = "Manage tenant tire benefit values menu"
    )

    // ============================================================
    //   Tenant Department  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_DEPARTMENT_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.create",
        description = "Create tenant departments across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.read",
        description = "Read tenant departments across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.update",
        description = "Update tenant departments across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.delete",
        description = "Delete tenant departments across tenants"
    )
    val MENU_TENANT_DEPARTMENT_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.department",
        path = "/manager/tenant-departments",
        description = "Manage tenant departments menu"
    )

    // ============================================================
    //   Tenant Role  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_ROLE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.create",
        description = "Create tenant roles across tenants"
    )
    val ACTION_TENANT_ROLE_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.read",
        description = "Read tenant roles across tenants"
    )
    val ACTION_TENANT_ROLE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.update",
        description = "Update tenant roles across tenants"
    )
    val ACTION_TENANT_ROLE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.delete",
        description = "Delete tenant roles across tenants"
    )
    val MENU_TENANT_ROLE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.role",
        path = "/manager/tenant-roles",
        description = "Manage tenant roles menu"
    )

    // ============================================================
    //   Tenant Permission  (super / tenantAdmin readonly)
    // ============================================================
    val ACTION_X_TENANT_PERMISSION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.tenant.permission.create",
        description = "Create tenant permissions"
    )
    val ACTION_X_TENANT_PERMISSION_READ = SystemRbacPermissionDeclaration.action(
        name = "x.tenant.permission.read",
        description = "Read tenant permissions"
    )
    val ACTION_X_TENANT_PERMISSION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.tenant.permission.update",
        description = "Update tenant permissions"
    )
    val ACTION_X_TENANT_PERMISSION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.tenant.permission.delete",
        description = "Delete tenant permissions"
    )
    val MENU_X_TENANT_PERMISSION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "x.tenant.permission",
        path = "/manager/tenant-permissions",
        description = "Manage tenant permissions menu"
    )
    val ACTION_TENANT_PERMISSION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.permission.read",
        description = "Read tenant permissions across tenants"
    )

    // ============================================================
    //   Tenant Member  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_MEMBER_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.create",
        description = "Create tenant members across tenants"
    )
    val ACTION_TENANT_MEMBER_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.read",
        description = "Read tenant members across tenants"
    )
    val ACTION_TENANT_MEMBER_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.update",
        description = "Update tenant members across tenants"
    )
    val ACTION_TENANT_MEMBER_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.delete",
        description = "Delete tenant members across tenants"
    )
    val MENU_TENANT_MEMBER_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.member",
        path = "/manager/tenant-members",
        description = "Manage tenant members menu"
    )

    // ============================================================
    //   Tenant Department-Member Relation  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.member.create",
        description = "Assign tenant department members across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.member.read",
        description = "Read tenant department members across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.member.update",
        description = "Update tenant department members across tenants"
    )
    val ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.department.member.delete",
        description = "Remove tenant department members across tenants"
    )

    // ============================================================
    //   Tenant Member-Role Relation  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_MEMBER_ROLE_RELATION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.role.read",
        description = "Read tenant member role assignments across tenants"
    )
    val ACTION_TENANT_MEMBER_ROLE_RELATION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.member.role.update",
        description = "Update tenant member role assignments across tenants"
    )
    val MENU_TENANT_MEMBER_ROLE_RELATION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.member.role",
        path = "/manager/tenant-member-roles",
        description = "Manage tenant member roles menu"
    )

    // ============================================================
    //   Tenant Role-Permission Relation  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_ROLE_PERMISSION_RELATION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.permission.read",
        description = "Read tenant role permission assignments across tenants"
    )
    val ACTION_TENANT_ROLE_PERMISSION_RELATION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.role.permission.update",
        description = "Update tenant role permission assignments across tenants"
    )

    // ============================================================
    //   Tenant Invitation  (tenantAdmin)
    // ============================================================
    val ACTION_TENANT_INVITATION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.invitation.create",
        description = "Create tenant invitations across tenants"
    )
    val ACTION_TENANT_INVITATION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.invitation.read",
        description = "Read tenant invitations across tenants"
    )
    val ACTION_TENANT_INVITATION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.invitation.update",
        description = "Update tenant invitations across tenants"
    )
    val ACTION_TENANT_INVITATION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.invitation.delete",
        description = "Delete tenant invitations across tenants"
    )
    val MENU_TENANT_INVITATION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.invitation",
        path = "/manager/tenant-invitations",
        description = "Manage tenant invitations menu"
    )

    // ============================================================
    //   Message Channel  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_MESSAGE_CHANNEL_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.message.channel.create",
        description = "Create message channels in any scope"
    )
    val ACTION_X_MESSAGE_CHANNEL_READ = SystemRbacPermissionDeclaration.action(
        name = "x.message.channel.read",
        description = "Read message channels in any scope"
    )
    val ACTION_X_MESSAGE_CHANNEL_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.message.channel.update",
        description = "Update message channels in any scope"
    )
    val ACTION_X_MESSAGE_CHANNEL_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.message.channel.delete",
        description = "Delete message channels in any scope"
    )

    val ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.message.channel.create",
        description = "Create system-scope message channels"
    )
    val ACTION_SYSTEM_MESSAGE_CHANNEL_READ = SystemRbacPermissionDeclaration.action(
        name = "system.message.channel.read",
        description = "Read system-scope message channels"
    )
    val ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.message.channel.update",
        description = "Update system-scope message channels"
    )
    val ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.message.channel.delete",
        description = "Delete system-scope message channels"
    )
    val MENU_SYSTEM_MESSAGE_CHANNEL_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.message.channel",
        path = "/manager/system-message-channels",
        description = "Manage system message channels menu"
    )

    val ACTION_TENANT_MESSAGE_CHANNEL_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.message.channel.create",
        description = "Create tenant-scope message channels across tenants"
    )
    val ACTION_TENANT_MESSAGE_CHANNEL_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.message.channel.read",
        description = "Read tenant-scope message channels across tenants"
    )
    val ACTION_TENANT_MESSAGE_CHANNEL_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.message.channel.update",
        description = "Update tenant-scope message channels across tenants"
    )
    val ACTION_TENANT_MESSAGE_CHANNEL_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.message.channel.delete",
        description = "Delete tenant-scope message channels across tenants"
    )
    val MENU_TENANT_MESSAGE_CHANNEL_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.message.channel",
        path = "/manager/tenant-message-channels",
        description = "Manage tenant message channels menu"
    )

    // ============================================================
    //   Audit Log  (system)
    // ============================================================
    val ACTION_SYSTEM_AUDIT_LOG_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.audit.log.create",
        description = "Create audit logs"
    )
    val ACTION_SYSTEM_AUDIT_LOG_READ = SystemRbacPermissionDeclaration.action(
        name = "system.audit.log.read",
        description = "Read audit logs"
    )
    val ACTION_SYSTEM_AUDIT_LOG_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.audit.log.update",
        description = "Update audit logs"
    )
    val ACTION_SYSTEM_AUDIT_LOG_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.audit.log.delete",
        description = "Delete audit logs"
    )
    val MENU_SYSTEM_AUDIT_LOG_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.audit.log",
        path = "/manager/audit-logs",
        description = "Manage audit logs menu"
    )

    // ============================================================
    //   Mail Send Log  (system)
    // ============================================================
    val ACTION_SYSTEM_MAIL_SEND_LOG_READ = SystemRbacPermissionDeclaration.action(
        name = "system.mail.send.log.read",
        description = "Read mail send logs"
    )
    val MENU_SYSTEM_MAIL_SEND_LOG_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.mail.send.log",
        path = "/manager/mail-send-logs",
        description = "Manage mail send logs menu"
    )

    // ============================================================
    //   User Login Log  (system)
    // ============================================================
    val ACTION_SYSTEM_USER_LOGIN_LOG_READ = SystemRbacPermissionDeclaration.action(
        name = "system.user.login.log.read",
        description = "Read user login logs"
    )
    val MENU_SYSTEM_USER_LOGIN_LOG_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.user.login.log",
        path = "/manager/user-login-logs",
        description = "Manage user login logs menu"
    )

    // ============================================================
    //   Monitor  (system)
    // ============================================================
    val ACTION_SYSTEM_MONITOR_READ = SystemRbacPermissionDeclaration.action(
        name = "system.monitor.read",
        description = "Read system metrics data"
    )
    val MENU_SYSTEM_MONITOR = SystemRbacPermissionDeclaration.menu(
        name = "system.monitor",
        path = "/manager/monitor/system-metrics",
        description = "Manage system metrics monitor menu"
    )
    val ACTION_SYSTEM_MONITOR_SESSIONS_READ = SystemRbacPermissionDeclaration.action(
        name = "system.monitor.sessions.read",
        description = "Read active session monitor data"
    )
    val MENU_SYSTEM_MONITOR_SESSIONS = SystemRbacPermissionDeclaration.menu(
        name = "system.monitor.sessions",
        path = "/manager/sessions",
        description = "Manage active sessions menu"
    )

    // ============================================================
    //   Announcement  (system)
    // ============================================================
    val ACTION_SYSTEM_ANNOUNCEMENT_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.announcement.create",
        description = "Create announcements"
    )
    val ACTION_SYSTEM_ANNOUNCEMENT_READ = SystemRbacPermissionDeclaration.action(
        name = "system.announcement.read",
        description = "Read announcements"
    )
    val ACTION_SYSTEM_ANNOUNCEMENT_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.announcement.update",
        description = "Update announcements"
    )
    val ACTION_SYSTEM_ANNOUNCEMENT_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.announcement.delete",
        description = "Delete announcements"
    )
    val ACTION_SYSTEM_ANNOUNCEMENT_LIST = SystemRbacPermissionDeclaration.action(
        name = "system.announcement.list",
        description = "List published announcements"
    )
    val MENU_SYSTEM_ANNOUNCEMENT_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.announcement",
        path = "/manager/announcements",
        description = "Manage announcements menu"
    )

    // ============================================================
    //   Dashboard  (system)
    // ============================================================
    val ACTION_SYSTEM_DASHBOARD_BUSINESS_STATISTICS_READ = SystemRbacPermissionDeclaration.action(
        name = "system.dashboard.business.statistics.read",
        description = "Read dashboard business statistics"
    )
    val ACTION_SYSTEM_DASHBOARD_SYSTEM_METRICS_READ = SystemRbacPermissionDeclaration.action(
        name = "system.dashboard.system.metrics.read",
        description = "Read dashboard system metrics"
    )
    val COMPONENT_SYSTEM_DASHBOARD_BUSINESS_STATISTICS = SystemRbacPermissionDeclaration.component(
        name = "system.dashboard.business.statistics",
        path = "dashboard.business.statistics",
        description = "Dashboard business statistics widget"
    )
    val COMPONENT_SYSTEM_DASHBOARD_SYSTEM_METRICS = SystemRbacPermissionDeclaration.component(
        name = "system.dashboard.system.metrics",
        path = "dashboard.system.metrics",
        description = "Dashboard system metrics widget"
    )
    val COMPONENT_SYSTEM_DASHBOARD_MY_TENANTS = SystemRbacPermissionDeclaration.component(
        name = "system.dashboard.tenant.joined",
        path = "dashboard.tenant.joined",
        description = "Dashboard joined tenants widget"
    )
    val COMPONENT_SYSTEM_DASHBOARD_ANNOUNCEMENTS = SystemRbacPermissionDeclaration.component(
        name = "system.dashboard.announcements",
        path = "dashboard.announcements",
        description = "Dashboard announcements widget"
    )

    // ============================================================
    //   Approval Flow Definition  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_APPROVAL_FLOW_DEFINITION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.approval.flow.definition.create",
        description = "Create approval flow definitions in any scope"
    )
    val ACTION_X_APPROVAL_FLOW_DEFINITION_READ = SystemRbacPermissionDeclaration.action(
        name = "x.approval.flow.definition.read",
        description = "Read approval flow definitions in any scope"
    )
    val ACTION_X_APPROVAL_FLOW_DEFINITION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.approval.flow.definition.update",
        description = "Update approval flow definitions in any scope"
    )
    val ACTION_X_APPROVAL_FLOW_DEFINITION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.approval.flow.definition.delete",
        description = "Delete approval flow definitions in any scope"
    )

    val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.approval.flow.definition.create",
        description = "Create system-scope approval flow definitions"
    )
    val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ = SystemRbacPermissionDeclaration.action(
        name = "system.approval.flow.definition.read",
        description = "Read system-scope approval flow definitions"
    )
    val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.approval.flow.definition.update",
        description = "Update system-scope approval flow definitions"
    )
    val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.approval.flow.definition.delete",
        description = "Delete system-scope approval flow definitions"
    )
    val MENU_SYSTEM_APPROVAL_FLOW_DEFINITION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.approval.flow.definition",
        path = "/manager/approval-flow-definitions",
        description = "Manage approval flow definitions menu"
    )

    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.approval.flow.definition.create",
        description = "Create tenant-scope approval flow definitions across tenants"
    )
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.approval.flow.definition.read",
        description = "Read tenant-scope approval flow definitions across tenants"
    )
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.approval.flow.definition.update",
        description = "Update tenant-scope approval flow definitions across tenants"
    )
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.approval.flow.definition.delete",
        description = "Delete tenant-scope approval flow definitions across tenants"
    )
    val MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.approval.flow.definition",
        path = "/manager/tenant-approval-flow-definitions",
        description = "Manage tenant approval flow definitions menu"
    )

    // ============================================================
    //   Approval Flow Instance  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_APPROVAL_FLOW_INSTANCE_READ = SystemRbacPermissionDeclaration.action(
        name = "x.approval.flow.instance.read",
        description = "Read approval flow instances in any scope"
    )
    val MENU_X_APPROVAL_FLOW_INSTANCE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "x.approval.flow.instance",
        path = "/manager/approval-flow-instances",
        description = "Manage approval flow instances menu"
    )

    val ACTION_SYSTEM_APPROVAL_FLOW_INSTANCE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.approval.flow.instance.read",
        description = "Read system-scope approval flow instances"
    )

    val ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.approval.flow.instance.read",
        description = "Read tenant-scope approval flow instances across tenants"
    )
    val MENU_TENANT_APPROVAL_FLOW_INSTANCE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.approval.flow.instance",
        path = "/manager/tenant-approval-flow-instances",
        description = "Manage tenant approval flow instances menu"
    )

    // ============================================================
    //   Dictionary Type  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_DICT_TYPE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.type.create",
        description = "Create dictionary types in any scope"
    )
    val ACTION_X_DICT_TYPE_READ = SystemRbacPermissionDeclaration.action(
        name = "x.dict.type.read",
        description = "Read dictionary types in any scope"
    )
    val ACTION_X_DICT_TYPE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.type.update",
        description = "Update dictionary types in any scope"
    )
    val ACTION_X_DICT_TYPE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.type.delete",
        description = "Delete dictionary types in any scope"
    )

    val ACTION_SYSTEM_DICT_TYPE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.type.create",
        description = "Create system-scope dictionary types"
    )
    val ACTION_SYSTEM_DICT_TYPE_READ = SystemRbacPermissionDeclaration.action(
        name = "system.dict.type.read",
        description = "Read system-scope dictionary types"
    )
    val ACTION_SYSTEM_DICT_TYPE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.type.update",
        description = "Update system-scope dictionary types"
    )
    val ACTION_SYSTEM_DICT_TYPE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.type.delete",
        description = "Delete system-scope dictionary types"
    )
    val MENU_SYSTEM_DICT_TYPE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.dict.type",
        path = "/manager/system-dict-types",
        description = "Manage system dictionary types menu"
    )

    val ACTION_TENANT_DICT_TYPE_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.type.create",
        description = "Create tenant-scope dictionary types across tenants"
    )
    val ACTION_TENANT_DICT_TYPE_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.type.read",
        description = "Read tenant-scope dictionary types across tenants"
    )
    val ACTION_TENANT_DICT_TYPE_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.type.update",
        description = "Update tenant-scope dictionary types across tenants"
    )
    val ACTION_TENANT_DICT_TYPE_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.type.delete",
        description = "Delete tenant-scope dictionary types across tenants"
    )
    val MENU_TENANT_DICT_TYPE_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.dict.type",
        path = "/manager/tenant-dict-types",
        description = "Manage tenant dictionary types menu"
    )

    // ============================================================
    //   Dictionary Item  (x + system + tenantAdmin)
    // ============================================================
    val ACTION_X_DICT_ITEM_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.item.create",
        description = "Create dictionary items in any scope"
    )
    val ACTION_X_DICT_ITEM_READ = SystemRbacPermissionDeclaration.action(
        name = "x.dict.item.read",
        description = "Read dictionary items in any scope"
    )
    val ACTION_X_DICT_ITEM_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.item.update",
        description = "Update dictionary items in any scope"
    )
    val ACTION_X_DICT_ITEM_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.dict.item.delete",
        description = "Delete dictionary items in any scope"
    )

    val ACTION_SYSTEM_DICT_ITEM_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.item.create",
        description = "Create system-scope dictionary items"
    )
    val ACTION_SYSTEM_DICT_ITEM_READ = SystemRbacPermissionDeclaration.action(
        name = "system.dict.item.read",
        description = "Read system-scope dictionary items"
    )
    val ACTION_SYSTEM_DICT_ITEM_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.item.update",
        description = "Update system-scope dictionary items"
    )
    val ACTION_SYSTEM_DICT_ITEM_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.dict.item.delete",
        description = "Delete system-scope dictionary items"
    )
    val MENU_SYSTEM_DICT_ITEM_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "system.dict.item",
        path = "/manager/system-dict-items",
        description = "Manage system dictionary items menu"
    )

    val ACTION_TENANT_DICT_ITEM_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.item.create",
        description = "Create tenant-scope dictionary items across tenants"
    )
    val ACTION_TENANT_DICT_ITEM_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.item.read",
        description = "Read tenant-scope dictionary items across tenants"
    )
    val ACTION_TENANT_DICT_ITEM_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.item.update",
        description = "Update tenant-scope dictionary items across tenants"
    )
    val ACTION_TENANT_DICT_ITEM_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.dict.item.delete",
        description = "Delete tenant-scope dictionary items across tenants"
    )
    val MENU_TENANT_DICT_ITEM_MANAGER = SystemRbacPermissionDeclaration.menu(
        name = "tenant.dict.item",
        path = "/manager/tenant-dict-items",
        description = "Manage tenant dictionary items menu"
    )

    // ============================================================
    //   Broadcast  (super / system / tenantAdmin)
    // ============================================================
    val ACTION_X_BROADCAST_CREATE = SystemRbacPermissionDeclaration.action(
        name = "x.broadcast.create",
        description = "Create broadcasts in any scope"
    )
    val ACTION_X_BROADCAST_READ = SystemRbacPermissionDeclaration.action(
        name = "x.broadcast.read",
        description = "Read broadcasts in any scope"
    )
    val ACTION_X_BROADCAST_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "x.broadcast.update",
        description = "Update broadcasts in any scope"
    )
    val ACTION_X_BROADCAST_DELETE = SystemRbacPermissionDeclaration.action(
        name = "x.broadcast.delete",
        description = "Delete broadcasts in any scope"
    )
    val ACTION_SYSTEM_BROADCAST_CREATE = SystemRbacPermissionDeclaration.action(
        name = "system.broadcast.create",
        description = "Create system-scope broadcasts (system announcements)"
    )
    val ACTION_SYSTEM_BROADCAST_READ = SystemRbacPermissionDeclaration.action(
        name = "system.broadcast.read",
        description = "Read system-scope broadcasts"
    )
    val ACTION_SYSTEM_BROADCAST_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "system.broadcast.update",
        description = "Update system-scope broadcasts"
    )
    val ACTION_SYSTEM_BROADCAST_DELETE = SystemRbacPermissionDeclaration.action(
        name = "system.broadcast.delete",
        description = "Delete system-scope broadcasts"
    )
    val ACTION_TENANT_BROADCAST_CREATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.broadcast.create",
        description = "Create tenant-scope broadcasts across tenants"
    )
    val ACTION_TENANT_BROADCAST_READ = SystemRbacPermissionDeclaration.action(
        name = "tenant.broadcast.read",
        description = "Read tenant-scope broadcasts across tenants"
    )
    val ACTION_TENANT_BROADCAST_UPDATE = SystemRbacPermissionDeclaration.action(
        name = "tenant.broadcast.update",
        description = "Update tenant-scope broadcasts across tenants"
    )
    val ACTION_TENANT_BROADCAST_DELETE = SystemRbacPermissionDeclaration.action(
        name = "tenant.broadcast.delete",
        description = "Delete tenant-scope broadcasts across tenants"
    )
    val MENU_SYSTEM_BROADCAST = SystemRbacPermissionDeclaration.menu(
        name = "system.broadcast",
        path = "/manager/broadcasts",
        description = "Broadcast management menu"
    )

    fun allPermissions(): List<SystemRbacPermissionDeclaration> =
        KotlinObjectClassUtils.extractAllValProperties(SystemPermission, false)
}
