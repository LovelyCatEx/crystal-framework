package com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteBroadcastDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
