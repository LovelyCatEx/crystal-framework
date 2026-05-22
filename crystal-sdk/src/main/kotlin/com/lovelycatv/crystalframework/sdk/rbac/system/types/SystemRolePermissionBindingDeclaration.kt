package com.lovelycatv.crystalframework.sdk.rbac.system.types

data class SystemRolePermissionBindingDeclaration(
    val roleName: String,
    val permissionNames: Set<String>,
)
