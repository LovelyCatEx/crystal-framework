package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import jakarta.validation.constraints.NotNull

data class ManagerReadTenantDepartmentMemberDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,

    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    val memberId: Long? = null,
    val roleType: Int? = null
) : BaseManagerReadDTO(page, pageSize)
