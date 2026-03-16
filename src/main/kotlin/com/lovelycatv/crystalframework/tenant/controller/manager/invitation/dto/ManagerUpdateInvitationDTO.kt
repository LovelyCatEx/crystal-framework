package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull

data class ManagerUpdateInvitationDTO(
    @field:NotNull(message = "ID is required")
    override val id: Long,

    var creatorMemberId: Long? = null,

    val departmentId: Long? = null,

    val invitationCount: Int? = null,

    val expiresTime: Long? = null,

    val requiresReviewing: Boolean? = null
) : BaseManagerUpdateDTO(id)