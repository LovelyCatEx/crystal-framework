package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadMailTemplateTypeDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null
) : BaseManagerReadDTO(page, pageSize)
