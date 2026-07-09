package com.lovelycatv.crystalframework.shared.constants

object SystemPermission {
    const val MENU_PERMISSION_MANAGER = "permission:/manager/user-permissions"
    const val MENU_ROLE_MANAGER = "role:/manager/user-roles"
    const val MENU_USER_MANAGER = "user:/manager/users"
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
    const val ACTION_SYSTEM_SETTINGS_TEST_SEND_EMAIL = "settings.test.sendEmail"
    const val ACTION_SYSTEM_SETTINGS_TEST_SEND_MESSAGE = "settings.test.sendMessage"

    const val ACTION_SYSTEM_MAINTENANCE_ACCESS = "maintenance.access"
    const val ACTION_SYSTEM_MAINTENANCE_UPDATE = "maintenance.update"

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

    // Tenant Tire Benefit
    const val MENU_TENANT_TIRE_BENEFIT_FEATURE_MANAGER = "tenant.tire.benefit.feature:/manager/tenant-tire-benefit-features"

    const val ACTION_TENANT_TIRE_BENEFIT_FEATURE_CREATE = "tenant.tire.benefit.feature.create"
    const val ACTION_TENANT_TIRE_BENEFIT_FEATURE_READ = "tenant.tire.benefit.feature.read"
    const val ACTION_TENANT_TIRE_BENEFIT_FEATURE_UPDATE = "tenant.tire.benefit.feature.update"
    const val ACTION_TENANT_TIRE_BENEFIT_FEATURE_DELETE = "tenant.tire.benefit.feature.delete"

    const val MENU_TENANT_TIRE_BENEFIT_VALUE_MANAGER = "tenant.tire.benefit.value:/manager/tenant-tire-benefit-values"

    const val ACTION_TENANT_TIRE_BENEFIT_VALUE_CREATE = "tenant.tire.benefit.value.create"
    const val ACTION_TENANT_TIRE_BENEFIT_VALUE_READ = "tenant.tire.benefit.value.read"
    const val ACTION_TENANT_TIRE_BENEFIT_VALUE_UPDATE = "tenant.tire.benefit.value.update"
    const val ACTION_TENANT_TIRE_BENEFIT_VALUE_DELETE = "tenant.tire.benefit.value.delete"

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

    // Tenant-level Message Channel (tenant-admin layer: cross-tenant admin, TENANT scope only)
    const val MENU_TENANT_MESSAGE_CHANNEL_MANAGER = "tenant.message.channel:/manager/tenant-message-channels"
    const val ACTION_TENANT_MESSAGE_CHANNEL_CREATE = "tenant.message.channel.create"
    const val ACTION_TENANT_MESSAGE_CHANNEL_READ = "tenant.message.channel.read"
    const val ACTION_TENANT_MESSAGE_CHANNEL_UPDATE = "tenant.message.channel.update"
    const val ACTION_TENANT_MESSAGE_CHANNEL_DELETE = "tenant.message.channel.delete"

    // System-level Message Channel
    const val MENU_SYSTEM_MESSAGE_CHANNEL_MANAGER = "system.message.channel:/manager/system-message-channels"

    const val ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE = "system.message.channel.create"
    const val ACTION_SYSTEM_MESSAGE_CHANNEL_READ = "system.message.channel.read"
    const val ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE = "system.message.channel.update"
    const val ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE = "system.message.channel.delete"

    // Cross-scope (super) Message Channel permissions — holders may operate in any scope.
    // Only granted to root and admin.
    const val ACTION_MESSAGE_CHANNEL_CREATE = "message.channel.create"
    const val ACTION_MESSAGE_CHANNEL_READ = "message.channel.read"
    const val ACTION_MESSAGE_CHANNEL_UPDATE = "message.channel.update"
    const val ACTION_MESSAGE_CHANNEL_DELETE = "message.channel.delete"

    const val MENU_AUDIT_LOG_MANAGER = "audit.log:/manager/audit-logs"

    const val ACTION_AUDIT_LOG_CREATE = "audit.log.create"
    const val ACTION_AUDIT_LOG_READ = "audit.log.read"
    const val ACTION_AUDIT_LOG_UPDATE = "audit.log.update"
    const val ACTION_AUDIT_LOG_DELETE = "audit.log.delete"

    const val MENU_MAIL_SEND_LOG_MANAGER = "mail.send.log:/manager/mail-send-logs"

    const val ACTION_MAIL_SEND_LOG_READ = "mail.send.log.read"

    const val MENU_USER_LOGIN_LOG_MANAGER = "user.login.log:/manager/user-login-logs"

    const val ACTION_USER_LOGIN_LOG_READ = "user.login.log.read"

    const val COMPONENT_DASHBOARD_BUSINESS_STATISTICS = "dashboard.business.statistics@dashboard.business.statistics"
    const val COMPONENT_DASHBOARD_SYSTEM_METRICS = "dashboard.system.metrics@dashboard.system.metrics"
    const val COMPONENT_DASHBOARD_MY_TENANTS = "dashboard.tenant.joined@dashboard.tenant.joined"
    const val COMPONENT_DASHBOARD_ANNOUNCEMENTS = "dashboard.announcements@dashboard.announcements"

    const val ACTION_DASHBOARD_BUSINESS_STATISTICS_READ = "dashboard.business.statistics.read"
    const val ACTION_DASHBOARD_SYSTEM_METRICS_READ = "dashboard.system.metrics.read"

    const val MENU_MONITOR_SESSIONS = "monitor.sessions:/manager/sessions"
    const val ACTION_MONITOR_SESSIONS_READ = "monitor.sessions.read"

    const val MENU_ANNOUNCEMENT_MANAGER = "announcement:/manager/announcements"
    const val ACTION_ANNOUNCEMENT_CREATE = "announcement.create"
    const val ACTION_ANNOUNCEMENT_READ = "announcement.read"
    const val ACTION_ANNOUNCEMENT_UPDATE = "announcement.update"
    const val ACTION_ANNOUNCEMENT_DELETE = "announcement.delete"
    const val ACTION_ANNOUNCEMENT_LIST = "announcement.list"

    // Approval Flow Definition (system scope)
    const val MENU_APPROVAL_FLOW_DEFINITION_MANAGER = "approval.flow.definition:/manager/approval-flow-definitions"

    // Cross-scope (super) Approval Flow Definition permissions — admin/root only.
    const val ACTION_APPROVAL_FLOW_DEFINITION_CREATE = "approval.flow.definition.create"
    const val ACTION_APPROVAL_FLOW_DEFINITION_READ = "approval.flow.definition.read"
    const val ACTION_APPROVAL_FLOW_DEFINITION_UPDATE = "approval.flow.definition.update"
    const val ACTION_APPROVAL_FLOW_DEFINITION_DELETE = "approval.flow.definition.delete"

    // System-scope only Approval Flow Definition permissions.
    const val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE = "system.approval.flow.definition.create"
    const val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ = "system.approval.flow.definition.read"
    const val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE = "system.approval.flow.definition.update"
    const val ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE = "system.approval.flow.definition.delete"

    // Approval Flow Definition (tenant-admin layer: cross-tenant admin, TENANT scope only)
    const val MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER = "tenant.approval.flow.definition:/manager/tenant-approval-flow-definitions"
    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE = "tenant.approval.flow.definition.create"
    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ = "tenant.approval.flow.definition.read"
    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE = "tenant.approval.flow.definition.update"
    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE = "tenant.approval.flow.definition.delete"

    // Approval Flow Instance (system scope) - read-all permission for system admins
    const val MENU_APPROVAL_FLOW_INSTANCE_MANAGER = "approval.flow.instance:/manager/approval-flow-instances"
    const val ACTION_APPROVAL_FLOW_INSTANCE_READ = "approval.flow.instance.read"

    // Approval Flow Instance (tenant-admin layer: cross-tenant admin, TENANT scope only)
    const val MENU_TENANT_APPROVAL_FLOW_INSTANCE_MANAGER = "tenant.approval.flow.instance:/manager/tenant-approval-flow-instances"
    const val ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ = "tenant.approval.flow.instance.read"

    // System-level Dictionary (tenantId = 0)
    const val MENU_SYSTEM_DICT_TYPE_MANAGER = "system.dict.type:/manager/system-dict-types"
    const val ACTION_SYSTEM_DICT_TYPE_CREATE = "system.dict.type.create"
    const val ACTION_SYSTEM_DICT_TYPE_READ = "system.dict.type.read"
    const val ACTION_SYSTEM_DICT_TYPE_UPDATE = "system.dict.type.update"
    const val ACTION_SYSTEM_DICT_TYPE_DELETE = "system.dict.type.delete"

    const val MENU_SYSTEM_DICT_ITEM_MANAGER = "system.dict.item:/manager/system-dict-items"
    const val ACTION_SYSTEM_DICT_ITEM_CREATE = "system.dict.item.create"
    const val ACTION_SYSTEM_DICT_ITEM_READ = "system.dict.item.read"
    const val ACTION_SYSTEM_DICT_ITEM_UPDATE = "system.dict.item.update"
    const val ACTION_SYSTEM_DICT_ITEM_DELETE = "system.dict.item.delete"

    // Tenant-level Dictionary (tenant-admin layer: cross-tenant admin, TENANT scope only)
    const val MENU_TENANT_DICT_TYPE_MANAGER = "tenant.dict.type:/manager/tenant-dict-types"
    const val ACTION_TENANT_DICT_TYPE_CREATE = "tenant.dict.type.create"
    const val ACTION_TENANT_DICT_TYPE_READ = "tenant.dict.type.read"
    const val ACTION_TENANT_DICT_TYPE_UPDATE = "tenant.dict.type.update"
    const val ACTION_TENANT_DICT_TYPE_DELETE = "tenant.dict.type.delete"

    const val MENU_TENANT_DICT_ITEM_MANAGER = "tenant.dict.item:/manager/tenant-dict-items"
    const val ACTION_TENANT_DICT_ITEM_CREATE = "tenant.dict.item.create"
    const val ACTION_TENANT_DICT_ITEM_READ = "tenant.dict.item.read"
    const val ACTION_TENANT_DICT_ITEM_UPDATE = "tenant.dict.item.update"
    const val ACTION_TENANT_DICT_ITEM_DELETE = "tenant.dict.item.delete"

    // Cross-scope (super) Dictionary permissions — holders may operate in any scope.
    // Only granted to root and admin.
    const val ACTION_DICT_TYPE_CREATE = "dict.type.create"
    const val ACTION_DICT_TYPE_READ = "dict.type.read"
    const val ACTION_DICT_TYPE_UPDATE = "dict.type.update"
    const val ACTION_DICT_TYPE_DELETE = "dict.type.delete"

    const val ACTION_DICT_ITEM_CREATE = "dict.item.create"
    const val ACTION_DICT_ITEM_READ = "dict.item.read"
    const val ACTION_DICT_ITEM_UPDATE = "dict.item.update"
    const val ACTION_DICT_ITEM_DELETE = "dict.item.delete"
}