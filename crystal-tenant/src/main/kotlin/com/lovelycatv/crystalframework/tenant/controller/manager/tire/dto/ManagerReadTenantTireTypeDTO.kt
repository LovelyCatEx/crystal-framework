package com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadTenantTireTypeDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null
) : BaseManagerReadDTO(page, pageSize)
