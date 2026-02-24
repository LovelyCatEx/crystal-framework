package com.lovelycatv.template.springboot.user.controller.manager.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateUserDTO(
    override val id: Long,
    val email: String? = null,
    val nickname: String? = null
) : BaseManagerUpdateDTO(id)
