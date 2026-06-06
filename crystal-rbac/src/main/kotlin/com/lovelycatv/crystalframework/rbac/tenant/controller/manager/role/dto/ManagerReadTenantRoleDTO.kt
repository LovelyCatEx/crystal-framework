package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO

data class ManagerReadTenantRoleDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long? = null,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val parentId: Long? = null
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
