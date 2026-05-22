package com.lovelycatv.crystalframework.sdk.tenant.rbac.types

data class TenantRolePermissionBindingDeclaration(
    val roleName: String,
    val permissionNames: Set<String>,
)
