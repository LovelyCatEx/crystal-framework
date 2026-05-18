package com.lovelycatv.crystalframework.mail.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateMailSendLogDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)