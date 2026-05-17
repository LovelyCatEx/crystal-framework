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

    // Tenant Roles
    val MENU_TENANT_ROLE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.role",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/roles"
    )

    const val ACTION_TENANT_ROLE_CREATE_PEM = "i.tenant.role.create"
    val ACTION_TENANT_ROLE_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_CREATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_READ_PEM = "i.tenant.role.read"
    val ACTION_TENANT_ROLE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_UPDATE_PEM = "i.tenant.role.update"
    val ACTION_TENANT_ROLE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_DELETE_PEM = "i.tenant.role.delete"
    val ACTION_TENANT_ROLE_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_DELETE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // Tenant Member Roles
    val MENU_TENANT_MEMBER_ROLE_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.member.role",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/member-roles"
    )

    const val ACTION_TENANT_MEMBER_ROLE_READ_PEM = "i.tenant.member.role.read"
    val ACTION_TENANT_MEMBER_ROLE_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_ROLE_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_MEMBER_ROLE_UPDATE_PEM = "i.tenant.member.role.update"
    val ACTION_TENANT_MEMBER_ROLE_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_ROLE_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // Tenant Role Permissions
    const val ACTION_TENANT_ROLE_PERMISSION_READ_PEM = "i.tenant.role.permission.read"
    val ACTION_TENANT_ROLE_PERMISSION_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_PERMISSION_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_ROLE_PERMISSION_UPDATE_PEM = "i.tenant.role.permission.update"
    val ACTION_TENANT_ROLE_PERMISSION_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_ROLE_PERMISSION_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // Tenant Department
    val MENU_TENANT_DEPARTMENT_MANAGER = TenantPermissionDeclaration(
        name = "i.tenant.department",
        type = TenantPermissionType.MENU,
        path = "/manager/tenant/departments"
    )

    const val ACTION_TENANT_DEPARTMENT_CREATE_PEM = "i.tenant.department.create"
    val ACTION_TENANT_DEPARTMENT_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_CREATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_READ_PEM = "i.tenant.department.read"
    val ACTION_TENANT_DEPARTMENT_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_UPDATE_PEM = "i.tenant.department.update"
    val ACTION_TENANT_DEPARTMENT_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_DELETE_PEM = "i.tenant.department.delete"
    val ACTION_TENANT_DEPARTMENT_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_DELETE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // Tenant Department Member
    const val ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM = "i.tenant.department.member.create"
    val ACTION_TENANT_DEPARTMENT_MEMBER_CREATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM = "i.tenant.department.member.read"
    val ACTION_TENANT_DEPARTMENT_MEMBER_READ = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM = "i.tenant.department.member.update"
    val ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM,
        type = TenantPermissionType.ACTION,
    )

    const val ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM = "i.tenant.department.member.delete"
    val ACTION_TENANT_DEPARTMENT_MEMBER_DELETE = TenantPermissionDeclaration(
        name = ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM,
        type = TenantPermissionType.ACTION,
    )

    // Tenant Mail
    const val ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM = "i.tenant.mail.member.join"
    val ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL = TenantPermissionDeclaration(
        name = ACTION_TENANT_MEMBER_JOIN_REVIEW_EMAIL_PEM,
        type = TenantPermissionType.ACTION,
    )

    fun allPermissions(): List<TenantPermissionDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantPermission, false)
    }
}
