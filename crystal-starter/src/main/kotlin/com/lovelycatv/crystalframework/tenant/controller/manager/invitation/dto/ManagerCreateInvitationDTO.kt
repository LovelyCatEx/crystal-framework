package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ManagerCreateInvitationDTO(
    override val tenantId: Long,

    var creatorMemberId: Long? = null,

    val departmentId: Long? = null,

    @field:NotNull(message = "Invitation count is required")
    @field:Min(value = 1)
    @field:Max(value = 9999)
    val invitationCount: Int,

    val expiresTime: Long? = null,

    val requiresReviewing: Boolean = false
) : BaseManagerCreateTenantResourceDTO(tenantId)
