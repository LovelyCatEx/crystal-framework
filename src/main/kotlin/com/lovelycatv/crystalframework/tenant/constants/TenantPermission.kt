package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils
import com.lovelycatv.crystalframework.tenant.types.TenantPermissionType

object TenantPermission {
    // My Tenant Dashboard
    val MENU_MY_TENANT_DASHBOARD = TenantPermissionDeclaration(
        name = "i.tenant.dashboard",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/dashboard"
    )

    // My Tenant Profile
    val MENU_TENANT_PROFILE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.profile",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/profile"
    )

    const val ACTION_TENANT_PROFILE_READ_BASIC_PEM = "i.tenant.profile.read.basic"
    val ACTION_TENANT_PROFILE_READ_BASIC = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_READ_BASIC_PEM,
        type = TenantPermissionType.ACTION,
    )


    const val ACTION_TENANT_PROFILE_READ_PEM = "i.tenant.profile.read"
    val ACTION_TENANT_PROFILE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_PROFILE_UPDATE_PEM = "i.tenant.profile.update"
    val ACTION_TENANT_PROFILE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_PROFILE_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // My Tenant Members
    val MENU_TENANT_MEMBER_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.member",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/members"
    )

    const val ACTION_TENANT_MEMBER_READ_PEM = "i.tenant.member.read"
    val ACTION_TENANT_MEMBER_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_UPDATE_PEM = "i.tenant.member.update"
    val ACTION_TENANT_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_DELETE_PEM = "i.tenant.member.delete"
    val ACTION_TENANT_MEMBER_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_DELETE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // My Tenant Invitations
    val MENU_TENANT_INVITATION_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.invitation",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/invitations"
    )

    const val ACTION_TENANT_INVITATION_CREATE_PEM = "i.tenant.invitation.create"
    val ACTION_TENANT_INVITATION_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_CREATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_READ_PEM = "i.tenant.invitation.read"
    val ACTION_TENANT_INVITATION_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_UPDATE_PEM = "i.tenant.invitation.update"
    val ACTION_TENANT_INVITATION_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_INVITATION_DELETE_PEM = "i.tenant.invitation.delete"
    val ACTION_TENANT_INVITATION_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_INVITATION_DELETE_PEM,
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }
}