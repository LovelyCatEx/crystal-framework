package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import jakarta.validation.constraints.NotNull

data class SetDepartmentMembersDTO(
    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    val memberIds: List<Long>
)
