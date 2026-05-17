package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteMailTemplateDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
