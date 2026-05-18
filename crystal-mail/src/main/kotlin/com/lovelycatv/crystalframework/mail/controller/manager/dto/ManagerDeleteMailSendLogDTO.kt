package com.lovelycatv.crystalframework.mail.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteMailSendLogDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)