package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteMailTemplateTypeDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
