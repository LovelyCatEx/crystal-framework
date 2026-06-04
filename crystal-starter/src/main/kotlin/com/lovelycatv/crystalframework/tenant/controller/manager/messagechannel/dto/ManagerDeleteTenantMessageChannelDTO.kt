package com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantMessageChannelDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
