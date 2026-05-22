package com.lovelycatv.crystalframework.sdk.rbac.types

data class RbacRolePermissionBindingDeclaration(
    val roleName: String,
    val permissionNames: Set<String>,
)
