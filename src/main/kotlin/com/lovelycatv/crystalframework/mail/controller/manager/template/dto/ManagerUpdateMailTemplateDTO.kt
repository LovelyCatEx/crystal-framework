package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateMailTemplateDTO(
    override val id: Long,
    val typeId: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val title: String? = null,
    val content: String? = null,
    val active: Boolean? = null
) : BaseManagerUpdateDTO(id)
