package com.lovelycatv.crystalframework.sdk.rbac.types

data class RbacRoleDeclaration(
    val name: String,
    val description: String = name,
    val module: String? = null,
)
