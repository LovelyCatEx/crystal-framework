package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import jakarta.validation.constraints.NotNull

data class SetUserRolesDTO(
    @field:NotNull(message = "User ID is required")
    val userId: Long,

    val roleIds: List<Long>
)