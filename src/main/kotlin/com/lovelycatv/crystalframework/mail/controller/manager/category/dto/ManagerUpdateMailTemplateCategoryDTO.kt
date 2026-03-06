package com.lovelycatv.crystalframework.mail.controller.manager.category.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateMailTemplateCategoryDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null
) : BaseManagerUpdateDTO(id)
