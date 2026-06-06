package com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadTenantDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val status: Int? = null
) : BaseManagerReadDTO(page, pageSize)
