package com.lovelycatv.crystalframework.user.controller.manager.user.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
