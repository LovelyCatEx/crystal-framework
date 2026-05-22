package com.lovelycatv.crystalframework.sdk.rbac.system.types

data class SystemRoleDeclaration(
    val name: String,
    val description: String = name,
    val module: String? = null,
)
