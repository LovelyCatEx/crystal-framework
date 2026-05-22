package com.lovelycatv.crystalframework.sdk.tenant.rbac.types

data class TenantRoleDeclaration(
    val name: String,
    val description: String = name,
    val parentRoleName: String? = null,
)
