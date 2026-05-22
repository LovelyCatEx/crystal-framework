package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

data class TenantPermissionDeclaration(
    val name: String,
    val description: String = name,
    val type: TenantPermissionType,
    val path: String? = null,
)
