package com.lovelycatv.crystalframework.tenant.controller.manager.department.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerReadTenantResourceDTO

data class ManagerReadTenantDepartmentDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val parentId: Long? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
