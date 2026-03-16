package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import jakarta.validation.constraints.NotNull

data class ManagerReadInvitationDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,

    val tenantId: Long? = null,
) : BaseManagerReadDTO(page, pageSize)