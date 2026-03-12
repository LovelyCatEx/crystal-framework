package com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

 data class ManagerUpdateTenantTireTypeDTO(
    override val id: Long,

    @field:Size(max = 32, message = "Name length cannot exceed 32 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null
) : BaseManagerUpdateDTO(id)
