package com.lovelycatv.crystalframework.tenant.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import jakarta.validation.constraints.NotNull

data class ManagerReadTenantRoleDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,

    @field:NotNull(message = "Tenant ID is required")
    val tenantId: Long,
    val parentId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
