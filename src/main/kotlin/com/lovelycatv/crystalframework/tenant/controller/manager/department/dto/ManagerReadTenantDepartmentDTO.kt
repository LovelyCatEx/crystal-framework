package com.lovelycatv.crystalframework.tenant.controller.manager.department.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

 data class ManagerReadTenantDepartmentDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val tenantId: Long? = null,
    val parentId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
