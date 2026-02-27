package com.lovelycatv.crystalframework.user.controller.manager.user.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateUserDTO(
    override val id: Long,
    val email: String? = null,
    val nickname: String? = null
) : BaseManagerUpdateDTO(id)
