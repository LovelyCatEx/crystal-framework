package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import jakarta.validation.constraints.NotNull

data class ManagerCreateTenantDepartmentMemberDTO(
    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    @field:NotNull(message = "Member ID is required")
    val memberId: Long,

    val roleType: Int? = null
)
