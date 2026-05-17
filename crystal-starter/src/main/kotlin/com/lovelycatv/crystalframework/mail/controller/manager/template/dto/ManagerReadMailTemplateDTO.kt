package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadMailTemplateDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val typeId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
