package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import jakarta.validation.constraints.NotNull

data class ManagerCreateTenantMemberDTO(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: Long,

    @field:NotNull(message = "Member user ID is required")
    val memberUserId: Long,

    val status: Int? = null
)
