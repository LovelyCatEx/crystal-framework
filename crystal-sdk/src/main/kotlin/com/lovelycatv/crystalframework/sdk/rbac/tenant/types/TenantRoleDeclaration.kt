package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

data class TenantRoleDeclaration(
    val name: String,
    val description: String = name,
    val parentRoleName: String? = null,
)
