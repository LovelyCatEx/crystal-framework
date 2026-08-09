package com.lovelycatv.crystalframework.rbac.tenant.constants

import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionDeclaration
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType
import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantPermission {
    // ============================================================
    //   Dashboard
    // ============================================================
    val MENU_DASHBOARD = TenantPermissionDeclaration(
        name = "i.tenant.dashboard",
        description = "My tenant dashboard menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dashboard"
    )

    // ============================================================
    //   Profile
    // ============================================================
    val MENU_PROFILE = TenantPermissionDeclaration(
        name = "i.tenant.profile",
        description = "My tenant profile menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/profile"
    )

    val ACTION_PROFILE_READ_BASIC = TenantPermissionDeclaration(
        name = "i.tenant.profile.read.basic",
        description = "Read basic tenant profile",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_PROFILE_READ = TenantPermissionDeclaration(
        name = "i.tenant.profile.read",
        description = "Read tenant profile",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_PROFILE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.profile.update",
        description = "Update tenant profile",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Personal Profile
    // ============================================================
    val MENU_PERSONAL_PROFILE = TenantPermissionDeclaration(
        name = "i.tenant.personal.profile",
        description = "My personal tenant profile menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/personal-profile"
    )

    val ACTION_PERSONAL_OAUTH_READ = TenantPermissionDeclaration(
        name = "i.tenant.personal.profile.oauth.read",
        description = "Read own OAuth bindings",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_PERSONAL_OAUTH_BIND = TenantPermissionDeclaration(
        name = "i.tenant.personal.profile.oauth.bind",
        description = "Bind an OAuth account",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_PERSONAL_OAUTH_UNBIND = TenantPermissionDeclaration(
        name = "i.tenant.personal.profile.oauth.unbind",
        description = "Unbind an OAuth account",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Member
    // ============================================================
    val MENU_MEMBER = TenantPermissionDeclaration(
        name = "i.tenant.member",
        description = "My tenant members menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/members"
    )

    val ACTION_MEMBER_READ = TenantPermissionDeclaration(
        name = "i.tenant.member.read",
        description = "Read members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.member.update",
        description = "Update members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MEMBER_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.member.delete",
        description = "Remove members from own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Invitation
    // ============================================================
    val MENU_INVITATION = TenantPermissionDeclaration(
        name = "i.tenant.invitation",
        description = "My tenant invitations menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/invitations"
    )

    val ACTION_INVITATION_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.invitation.create",
        description = "Create invitations for own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_INVITATION_READ = TenantPermissionDeclaration(
        name = "i.tenant.invitation.read",
        description = "Read invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_INVITATION_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.invitation.update",
        description = "Update invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_INVITATION_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.invitation.delete",
        description = "Delete invitations of own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Role
    // ============================================================
    val MENU_ROLE = TenantPermissionDeclaration(
        name = "i.tenant.role",
        description = "My tenant roles menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/roles"
    )

    val ACTION_ROLE_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.role.create",
        description = "Create roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_ROLE_READ = TenantPermissionDeclaration(
        name = "i.tenant.role.read",
        description = "Read roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_ROLE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.role.update",
        description = "Update roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_ROLE_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.role.delete",
        description = "Delete roles within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Member Role
    // ============================================================
    val MENU_MEMBER_ROLE = TenantPermissionDeclaration(
        name = "i.tenant.member.role",
        description = "My tenant member roles menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/member-roles"
    )

    val ACTION_MEMBER_ROLE_READ = TenantPermissionDeclaration(
        name = "i.tenant.member.role.read",
        description = "Read member role assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MEMBER_ROLE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.member.role.update",
        description = "Update member role assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Role Permission
    // ============================================================
    val ACTION_ROLE_PERMISSION_READ = TenantPermissionDeclaration(
        name = "i.tenant.role.permission.read",
        description = "Read role permission assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_ROLE_PERMISSION_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.role.permission.update",
        description = "Update role permission assignments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Department
    // ============================================================
    val MENU_DEPARTMENT = TenantPermissionDeclaration(
        name = "i.tenant.department",
        description = "My tenant departments menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/departments"
    )

    val ACTION_DEPARTMENT_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.department.create",
        description = "Create departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_READ = TenantPermissionDeclaration(
        name = "i.tenant.department.read",
        description = "Read departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.department.update",
        description = "Update departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.department.delete",
        description = "Delete departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Department Member
    // ============================================================
    val ACTION_DEPARTMENT_MEMBER_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.department.member.create",
        description = "Assign members to departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_MEMBER_READ = TenantPermissionDeclaration(
        name = "i.tenant.department.member.read",
        description = "Read department members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.department.member.update",
        description = "Update department members within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DEPARTMENT_MEMBER_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.department.member.delete",
        description = "Remove members from departments within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Mail
    // ============================================================
    val ACTION_MEMBER_JOIN_REVIEW_EMAIL = TenantPermissionDeclaration(
        name = "i.tenant.mail.member.join",
        description = "Receive tenant member-join review emails",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Settings
    // ============================================================
    val ACTION_SETTINGS_READ = TenantPermissionDeclaration(
        name = "i.tenant.settings.read",
        description = "Read own tenant settings",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_SETTINGS_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.settings.update",
        description = "Update own tenant settings",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Message Channel
    // ============================================================
    val MENU_MESSAGE_CHANNEL = TenantPermissionDeclaration(
        name = "i.tenant.message.channel",
        description = "My tenant message channels menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/message-channels"
    )

    val ACTION_MESSAGE_CHANNEL_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.message.channel.create",
        description = "Create message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MESSAGE_CHANNEL_READ = TenantPermissionDeclaration(
        name = "i.tenant.message.channel.read",
        description = "Read message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MESSAGE_CHANNEL_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.message.channel.update",
        description = "Update message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_MESSAGE_CHANNEL_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.message.channel.delete",
        description = "Delete message channels within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Message Reception (customer-service inbox)
    // ============================================================
    val ACTION_MESSAGE_RECEPTION_HANDLE = TenantPermissionDeclaration(
        name = "i.tenant.message.reception.handle",
        description = "See and reply to customer-service conversations initiated by users toward own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Dictionary Type
    // ============================================================
    val MENU_DICT_TYPE = TenantPermissionDeclaration(
        name = "i.tenant.dict.type",
        description = "My tenant dictionary types menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dict-types"
    )

    val MENU_DICT_ITEM = TenantPermissionDeclaration(
        name = "i.tenant.dict.item",
        description = "My tenant dictionary items menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dict-items"
    )

    val ACTION_DICT_TYPE_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.dict.type.create",
        description = "Create dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_TYPE_READ = TenantPermissionDeclaration(
        name = "i.tenant.dict.type.read",
        description = "Read dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_TYPE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.dict.type.update",
        description = "Update dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_TYPE_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.dict.type.delete",
        description = "Delete dictionary types within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Dictionary Item
    // ============================================================
    val ACTION_DICT_ITEM_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.dict.item.create",
        description = "Create dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_ITEM_READ = TenantPermissionDeclaration(
        name = "i.tenant.dict.item.read",
        description = "Read dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_ITEM_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.dict.item.update",
        description = "Update dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_DICT_ITEM_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.dict.item.delete",
        description = "Delete dictionary items within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Approval Flow Definition
    // ============================================================
    val MENU_APPROVAL_FLOW_DEFINITION = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition",
        description = "My tenant approval flow definitions menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/approval-flow-definitions"
    )

    val ACTION_APPROVAL_FLOW_DEFINITION_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition.create",
        description = "Create approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_APPROVAL_FLOW_DEFINITION_READ = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition.read",
        description = "Read approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_APPROVAL_FLOW_DEFINITION_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition.update",
        description = "Update approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_APPROVAL_FLOW_DEFINITION_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.definition.delete",
        description = "Delete approval flow definitions within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Approval Flow Instance
    // ============================================================
    val MENU_APPROVAL_FLOW_INSTANCE = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.instance",
        description = "My tenant approval flow instances menu",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/approval-flow-instances"
    )

    val ACTION_APPROVAL_FLOW_INSTANCE_READ = TenantPermissionDeclaration(
        name = "i.tenant.approval.flow.instance.read",
        description = "Read approval flow instances within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   File Resource
    // ============================================================
    val ACTION_FILE_RESOURCE_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.file.resource.create",
        description = "Create file resources within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_FILE_RESOURCE_READ = TenantPermissionDeclaration(
        name = "i.tenant.file.resource.read",
        description = "Read file resources within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_FILE_RESOURCE_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.file.resource.update",
        description = "Update file resources within own tenant",
        type = TenantPermissionType.ACTION,
    )

    val ACTION_FILE_RESOURCE_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.file.resource.delete",
        description = "Delete file resources within own tenant",
        type = TenantPermissionType.ACTION,
    )

    // ============================================================
    //   Broadcast (within own tenant)
    // ============================================================
    val ACTION_BROADCAST_CREATE = TenantPermissionDeclaration(
        name = "i.tenant.broadcast.create",
        description = "Create broadcasts within own tenant",
        type = TenantPermissionType.ACTION,
    )
    val ACTION_BROADCAST_READ = TenantPermissionDeclaration(
        name = "i.tenant.broadcast.read",
        description = "Read broadcasts within own tenant",
        type = TenantPermissionType.ACTION,
    )
    val ACTION_BROADCAST_UPDATE = TenantPermissionDeclaration(
        name = "i.tenant.broadcast.update",
        description = "Update broadcasts within own tenant",
        type = TenantPermissionType.ACTION,
    )
    val ACTION_BROADCAST_DELETE = TenantPermissionDeclaration(
        name = "i.tenant.broadcast.delete",
        description = "Delete broadcasts within own tenant",
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }

    fun allPermissionNames(): Set<String> = ALL_PERMISSION_NAMES
}

// Kept at file top-level (not as a TenantPermission member) so it is invisible to
// KotlinObjectClassUtils.extractAllValProperties, which reflects over
// TenantPermission::class.memberProperties and cannot access private members.
private val ALL_PERMISSION_NAMES: Set<String> by lazy {
    TenantPermission.allPermissions().map { it.name }.toSet()
}
