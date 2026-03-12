package com.lovelycatv.crystalframework.tenant.constants

data class TenantRoleDeclaration(
    val name: String,
    val description: String = name,
    val parentRole: TenantRoleDeclaration? = null,
)
