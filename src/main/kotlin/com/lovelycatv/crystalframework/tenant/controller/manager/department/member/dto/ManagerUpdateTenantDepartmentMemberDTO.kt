package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull

data class ManagerUpdateTenantDepartmentMemberDTO(
    override val id: Long,

    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    @field:NotNull(message = "Member ID is required")
    val memberId: Long,

    @field:NotNull(message = "Role type is required")
    val roleType: Int
) : BaseManagerUpdateDTO(id)
