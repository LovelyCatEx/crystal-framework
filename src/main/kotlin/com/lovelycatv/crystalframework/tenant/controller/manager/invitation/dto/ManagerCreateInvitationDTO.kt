package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ManagerCreateInvitationDTO(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: Long,

    @field:NotNull(message = "Creator member ID is required")
    var creatorMemberId: Long,

    val departmentId: Long? = null,

    @field:NotNull(message = "Invitation count is required")
    @field:Min(value = 1)
    @field:Max(value = 9999)
    val invitationCount: Int,

    val expiresTime: Long? = null,

    val requiresReviewing: Boolean = false
)