package com.lovelycatv.crystalframework.mail.controller.manager.category.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadMailTemplateCategoryDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null
) : BaseManagerReadDTO(page, pageSize)
