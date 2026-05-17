package com.lovelycatv.crystalframework.tenant.controller.manager.role.dto

import jakarta.validation.constraints.NotNull

data class SetMemberRolesDTO(
    @field:NotNull(message = "Member ID is required")
    val memberId: Long,

    val roleIds: List<Long>
)