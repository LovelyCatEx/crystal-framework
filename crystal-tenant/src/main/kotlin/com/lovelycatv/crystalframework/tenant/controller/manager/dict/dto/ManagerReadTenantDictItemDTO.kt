package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadTenantDictItemDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val typeId: Long,
) : BaseManagerReadTenantResourceDTO(page, pageSize, null)
