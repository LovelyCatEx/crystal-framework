package com.lovelycatv.crystalframework.user.controller.manager.user.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
