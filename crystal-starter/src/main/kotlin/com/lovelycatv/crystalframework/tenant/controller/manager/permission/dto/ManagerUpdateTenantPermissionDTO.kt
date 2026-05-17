package com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

 data class ManagerUpdateTenantPermissionDTO(
    override val id: Long,

    @field:Size(max = 256, message = "Name length cannot exceed 256 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val type: Int? = null,

    @field:Size(max = 256, message = "Path length cannot exceed 256 characters")
    val path: String? = null,

    val preserved1: Int? = null,

    val preserved2: Int? = null
) : BaseManagerUpdateDTO(id)
