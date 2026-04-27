package com.lovelycatv.crystalframework.tenant.controller.manager.role.dto

import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateTenantRoleDTO(
    override val tenantId: Long,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val parentId: Long? = null
) : BaseManagerCreateTenantResourceDTO(tenantId)
