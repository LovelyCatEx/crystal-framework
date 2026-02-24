package com.lovelycatv.template.springboot.user.controller.manager.user.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteUserDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
