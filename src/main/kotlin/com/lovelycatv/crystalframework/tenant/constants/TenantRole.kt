package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.shared.utils.KotlinObjectClassUtils

object TenantRole {
    val ROOT = TenantRoleDeclaration(
        name = "root"
    )
    val SUPER_ADMIN = TenantRoleDeclaration(
        name = "super_admin"
    )
    val ADMIN = TenantRoleDeclaration(
        name = "admin"
    )
    val MEMBER = TenantRoleDeclaration(
        name = "member"
    )

    fun allRoles(): List<TenantRoleDeclaration> {
        return KotlinObjectClassUtils.extractAllValProperties(TenantRole, false)
    }
}