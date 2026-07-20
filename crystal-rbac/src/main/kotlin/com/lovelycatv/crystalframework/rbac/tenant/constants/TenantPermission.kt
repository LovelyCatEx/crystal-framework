package com.lovelycatv.crystalframework.rbac.tenant.constants

import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType
import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantPermission {
    // My Tenant Dashboard
    val MENU_MY_TENANT_DASHBOARD = TenantPermissionDeclaration(
        name = "i.tenant.dashboard",
        description = "My tenant dashboard menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dashboard"
    )

    // My Tenant Profile
    val MENU_TENANT_PROFILE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.profile",
        description = "My tenant profile menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/profile"
    )

    const val ACTION_TENANT_PROFILE_READ_BASIC_PEM = "i.tenant.profile.read.basic"
    val ACTION_TENANT_PROFILE_READ_BASIC = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_READ_BASIC_PEM,
        description = "Read basic tenant profile",
        type = TenantPermissionType.ACTION,
    )


    const val ACTION_TENANT_PROFILE_READ_PEM = "i.tenant.profile.read"
    val ACTION_TENANT_PROFILE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_READ_PEM,
        description = "Read tenant profile",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_PROFILE_UPDATE_PEM = "i.tenant.profile.update"
    val ACTION_TENANT_PROFILE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_UPDATE_PEM,
        description = "Update tenant profile",
        type = TenantPermissionType.ACTION,
    )

    // My Personal Profile (tenant-scoped, for the current member)
    val MENU_TENANT_PERSONAL_PROFILE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.personal.profile",
        description = "My personal tenant profile menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/personal-profile"
    )

    const val ACTION_TENANT_OAUTH_READ_PEM = "i.tenant.personal.profile.oauth.read"
    val ACTION_TENANT_OAUTH_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_OAUTH_READ_PEM,
        description = "Read own OAuth bindings",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_OAUTH_BIND_PEM = "i.tenant.personal.profile.oauth.bind"
    val ACTION_TENANT_OAUTH_BIND = TenantPermissionDeclaration(
        name = ACTION_TENANT_OAUTH_BIND_PEM,
        description = "Bind an OAuth account",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_OAUTH_UNBIND_PEM = "i.tenant.personal.profile.oauth.unbind"
    val ACTION_TENANT_OAUTH_UNBIND = TenantPermissionDeclaration(
        name = ACTION_TENANT_OAUTH_UNBIND_PEM,
        description = "Unbind an OAuth account",
        type = TenantPermissionType.ACTION,
    )

    // My Tenant Members
    val MENU_TENANT_MEMBER_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.member",
        description = "My tenant members menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/members"
    )

    const val ACTION_TENANT_MEMBER_READ_PEM = "i.tenant.member.read"
    val ACTION_TENANT_MEMBER_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_READ_PEM,
        description = "Read members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_UPDATE_PEM = "i.tenant.member.update"
    val ACTION_TENANT_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_UPDATE_PEM,
        description = "Update members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_DELETE_PEM = "i.tenant.member.delete"
    val ACTION_TENANT_MEMBER_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_DELETE_PEM,
        description = "Remove members from own tenant",
        type = TenantPermissionType.ACTION,
    )

    // My Tenant Invitations
    val MENU_TENANT_INVITATION_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.invitation",
        description = "My tenant invitations menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/invitations"
    )

    const val ACTION_TENANT_INVITATION_CREATE_PEM = "i.tenant.invitation.create"
    val ACTION_TENANT_INVITATION_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_CREATE_PEM,
        description = "Create invitations for own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_READ_PEM = "i.tenant.invitation.read"
    val ACTION_TENANT_INVITATION_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_READ_PEM,
        description = "Read invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_UPDATE_PEM = "i.tenant.invitation.update"
    val ACTION_TENANT_INVITATION_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_UPDATE_PEM,
        description = "Update invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_DELETE_PEM = "i.tenant.invitation.delete"
    val ACTION_TENANT_INVITATION_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_DELETE_PEM,
        description = "Delete invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Roles
    val MENU_TENANT_ROLE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.role",
        description = "My tenant roles menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/roles"
    )

    const val ACTION_TENANT_ROLE_CREATE_PEM = "i.tenant.role.create"
    val ACTION_TENANT_ROLE_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_CREATE_PEM,
        description = "Create roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_READ_PEM = "i.tenant.role.read"
    val ACTION_TENANT_ROLE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_READ_PEM,
        description = "Read roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_UPDATE_PEM = "i.tenant.role.update"
    val ACTION_TENANT_ROLE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_UPDATE_PEM,
        description = "Update roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_DELETE_PEM = "i.tenant.role.delete"
    val ACTION_TENANT_ROLE_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_DELETE_PEM,
        description = "Delete roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Member Roles
    val MENU_TENANT_MEMBER_ROLE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.member.role",
        description = "My tenant member roles menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/member-roles"
    )

    const val ACTION_TENANT_MEMBER_ROLE_READ_PEM = "i.tenant.member.role.read"
    val ACTION_TENANT_MEMBER_ROLE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_ROLE_READ_PEM,
        description = "Read member role assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_ROLE_UPDATE_PEM = "i.tenant.member.role.update"
    val ACTION_TENANT_MEMBER_ROLE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_ROLE_UPDATE_PEM,
        description = "Update member role assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Role Permissions
    const val ACTION_TENANT_ROLE_PERMISSION_READ_PEM = "i.tenant.role.permission.read"
    val ACTION_TENANT_ROLE_PERMISSION_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_PERMISSION_READ_PEM,
        description = "Read role permission assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_PERMISSION_UPDATE_PEM = "i.tenant.role.permission.update"
    val ACTION_TENANT_ROLE_PERMISSION_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_PERMISSION_UPDATE_PEM,
        description = "Update role permission assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Department
    val MENU_TENANT_DEPARTMENT_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.department",
        description = "My tenant departments menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/departments"
    )

    const val ACTION_TENANT_DEPARTMENT_CREATE_PEM = "i.tenant.department.create"
    val ACTION_TENANT_DEPARTMENT_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_CREATE_PEM,
        description = "Create departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_READ_PEM = "i.tenant.department.read"
    val ACTION_TENANT_DEPARTMENT_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_READ_PEM,
        description = "Read departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_UPDATE_PEM = "i.tenant.department.update"
    val ACTION_TENANT_DEPARTMENT_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_UPDATE_PEM,
        description = "Update departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_DELETE_PEM = "i.tenant.department.delete"
    val ACTION_TENANT_DEPARTMENT_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_DELETE_PEM,
        description = "Delete departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Department Member
    const val ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM = "i.tenant.department.member.create"
    val ACTION_TENANT_DEPARTMENT_MEMBER_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM,
        description = "Assign members to departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM = "i.tenant.department.member.read"
    val ACTION_TENANT_DEPARTMENT_MEMBER_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM,
        description = "Read department members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM = "i.tenant.department.member.update"
    val ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM,
        description = "Update department members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM = "i.tenant.department.member.delete"
    val ACTION_TENANT_DEPARTMENT_MEMBER_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM,
        description = "Remove members from departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Mail
    const val ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM = "i.tenant.mail.member.join"
    val ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM,
        description = "Receive tenant member-join review emails",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Settings (embedded as a Segmented section in Tenant Profile page)
    const val ACTION_TENANT_SETTINGS_READ_PEM = "i.tenant.settings.read"
    val ACTION_TENANT_SETTINGS_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_SETTINGS_READ_PEM,
        description = "Read own tenant settings",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_SETTINGS_UPDATE_PEM = "i.tenant.settings.update"
    val ACTION_TENANT_SETTINGS_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_SETTINGS_UPDATE_PEM,
        description = "Update own tenant settings",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Message Channel
    val MENU_TENANT_MESSAGE_CHANNEL_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.message.channel",
        description = "My tenant message channels menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/message-channels"
    )

    const val ACTION_TENANT_MESSAGE_CHANNEL_CREATE_PEM = "i.tenant.message.channel.create"
    val ACTION_TENANT_MESSAGE_CHANNEL_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MESSAGE_CHANNEL_CREATE_PEM,
        description = "Create message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MESSAGE_CHANNEL_READ_PEM = "i.tenant.message.channel.read"
    val ACTION_TENANT_MESSAGE_CHANNEL_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_MESSAGE_CHANNEL_READ_PEM,
        description = "Read message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MESSAGE_CHANNEL_UPDATE_PEM = "i.tenant.message.channel.update"
    val ACTION_TENANT_MESSAGE_CHANNEL_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MESSAGE_CHANNEL_UPDATE_PEM,
        description = "Update message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MESSAGE_CHANNEL_DELETE_PEM = "i.tenant.message.channel.delete"
    val ACTION_TENANT_MESSAGE_CHANNEL_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MESSAGE_CHANNEL_DELETE_PEM,
        description = "Delete message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Dictionary Type
    val MENU_TENANT_DICT_TYPE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.dict.type",
        description = "My tenant dictionary types menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dict-types"
    )

    val MENU_TENANT_DICT_ITEM_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.dict.item",
        description = "My tenant dictionary items menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dict-items"
    )

    const val ACTION_TENANT_DICT_TYPE_CREATE_PEM = "i.tenant.dict.type.create"
    val ACTION_TENANT_DICT_TYPE_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_TYPE_CREATE_PEM,
        description = "Create dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_TYPE_READ_PEM = "i.tenant.dict.type.read"
    val ACTION_TENANT_DICT_TYPE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_TYPE_READ_PEM,
        description = "Read dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_TYPE_UPDATE_PEM = "i.tenant.dict.type.update"
    val ACTION_TENANT_DICT_TYPE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_TYPE_UPDATE_PEM,
        description = "Update dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_TYPE_DELETE_PEM = "i.tenant.dict.type.delete"
    val ACTION_TENANT_DICT_TYPE_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_TYPE_DELETE_PEM,
        description = "Delete dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Dictionary Item
    const val ACTION_TENANT_DICT_ITEM_CREATE_PEM = "i.tenant.dict.item.create"
    val ACTION_TENANT_DICT_ITEM_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_ITEM_CREATE_PEM,
        description = "Create dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_ITEM_READ_PEM = "i.tenant.dict.item.read"
    val ACTION_TENANT_DICT_ITEM_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_ITEM_READ_PEM,
        description = "Read dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_ITEM_UPDATE_PEM = "i.tenant.dict.item.update"
    val ACTION_TENANT_DICT_ITEM_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_ITEM_UPDATE_PEM,
        description = "Update dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DICT_ITEM_DELETE_PEM = "i.tenant.dict.item.delete"
    val ACTION_TENANT_DICT_ITEM_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DICT_ITEM_DELETE_PEM,
        description = "Delete dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Approval Flow Definition
    val MENU_TENANT_APPROVAL_FLOW_DEFINITION_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition",
        description = "My tenant approval flow definitions menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/approval-flow-definitions"
    )

    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE_PEM = "i.tenant.approval.flow.definition.create"
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE_PEM,
        description = "Create approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ_PEM = "i.tenant.approval.flow.definition.read"
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ_PEM,
        description = "Read approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE_PEM = "i.tenant.approval.flow.definition.update"
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE_PEM,
        description = "Update approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE_PEM = "i.tenant.approval.flow.definition.delete"
    val ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE_PEM,
        description = "Delete approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // Tenant Approval Flow Instance (read-all permission for tenant admins)
    val MENU_TENANT_APPROVAL_FLOW_INSTANCE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.instance",
        description = "My tenant approval flow instances menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/approval-flow-instances"
    )

    const val ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM = "i.tenant.approval.flow.instance.read"
    val ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ_PEM,
        description = "Read approval flow instances within own tenant",
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }

    private val ALL_PERMISSION_NAMES: Set<String> by lazy {
        allPermissions().map { it.name }.toSet()
    }

    fun allPermissionNames(): Set<String> = ALL_PERMISSION_NAMES
}
