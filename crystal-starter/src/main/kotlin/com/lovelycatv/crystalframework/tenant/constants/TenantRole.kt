package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantRole {
    val MEMBER = TenantRoleDeclaration(
        name = "member"
    )

    val ADMIN = TenantRoleDeclaration(
        name = "admin",
        parentRole = MEMBER
    )

    val SUPER_ADMIN = TenantRoleDeclaration(
        name = "super_admin",
        parentRole = ADMIN
    )

    val ROOT = TenantRoleDeclaration(
        name = "root",
        parentRole = SUPER_ADMIN
    )

    fun allRoles(): List<TenantRoleDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantRole, false)
    }
}