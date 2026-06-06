package com.lovelycatv.crystalframework.user.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
