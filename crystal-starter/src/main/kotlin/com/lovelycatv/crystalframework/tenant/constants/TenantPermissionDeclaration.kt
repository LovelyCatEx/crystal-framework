package com.lovelycatv.crystalframework.tenant.constants

import com.lovelycatv.crystalframework.tenant.types.TenantPermissionType

data class TenantPermissionDeclaration(
    val name: String,
    val description: String = name,
    val type: TenantPermissionType,
    val path: String? = null,
)
