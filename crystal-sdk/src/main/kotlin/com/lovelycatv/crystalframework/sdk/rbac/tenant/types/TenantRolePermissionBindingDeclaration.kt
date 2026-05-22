package com.lovelycatv.crystalframework.sdk.rbac.tenant.types

data class TenantRolePermissionBindingDeclaration(
    val roleName: String,
    val permissionNames: Set<String>,
)
