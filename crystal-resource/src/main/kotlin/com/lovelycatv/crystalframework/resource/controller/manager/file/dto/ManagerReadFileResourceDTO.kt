package com.lovelycatv.crystalframework.resource.controller.manager.file.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadFileResourceDTO(
    override val page: Int,
    override val pageSize: Int,
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val type: Int? = null
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
