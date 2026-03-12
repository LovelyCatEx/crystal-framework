package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import jakarta.validation.constraints.NotNull

data class SetTenantMembersDTO(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: Long,

    val userIds: List<Long>
)
