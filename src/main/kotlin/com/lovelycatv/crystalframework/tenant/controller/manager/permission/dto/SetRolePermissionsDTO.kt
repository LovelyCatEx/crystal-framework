package com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto

import jakarta.validation.constraints.NotNull

data class SetRolePermissionsDTO(
    @field:NotNull(message = "Role ID is required")
    val roleId: Long,

    val permissionIds: List<Long>
)