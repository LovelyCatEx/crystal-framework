package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateRoleDTO(
    override val id: Long,

    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null
) : BaseManagerUpdateDTO(id)
