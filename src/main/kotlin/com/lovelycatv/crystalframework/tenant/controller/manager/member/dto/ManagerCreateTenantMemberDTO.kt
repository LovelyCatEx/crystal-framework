package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.NotNull

data class ManagerCreateTenantMemberDTO(
    override val tenantId: Long,

    @field:NotNull(message = "Member user ID is required")
    val memberUserId: Long,

    val status: Int? = null
) : BaseManagerCreateTenantResourceDTO(tenantId)
