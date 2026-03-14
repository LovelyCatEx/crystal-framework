package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteInvitationDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)