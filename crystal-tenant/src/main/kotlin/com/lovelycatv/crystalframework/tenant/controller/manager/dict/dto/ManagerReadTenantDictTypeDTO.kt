package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadTenantDictTypeDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long,
    override val id: Long? = null,
    override val query: QueryNode? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
