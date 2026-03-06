package com.lovelycatv.crystalframework.mail.controller.manager.category.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteMailTemplateCategoryDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
