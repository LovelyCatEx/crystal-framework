package com.lovelycatv.crystalframework.tenant.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

 data class ManagerUpdateTenantRoleDTO(
    override val id: Long,

    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val parentId: Long? = null
) : BaseManagerUpdateDTO(id)
