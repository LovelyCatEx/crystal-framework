package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateMailTemplateTypeDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
    val variables: String? = null,
    val categoryId: Long? = null,
    val allowMultiple: Boolean? = null
) : BaseManagerUpdateDTO(id)
