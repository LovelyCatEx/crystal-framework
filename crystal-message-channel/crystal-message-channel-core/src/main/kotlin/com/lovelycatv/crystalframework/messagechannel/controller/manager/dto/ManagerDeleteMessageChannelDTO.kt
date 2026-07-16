package com.lovelycatv.crystalframework.messagechannel.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteMessageChannelDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
