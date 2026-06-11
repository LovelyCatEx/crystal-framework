package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateTenantDictTypeDTO(
    override val id: Long,

    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Remark length cannot exceed 512 characters")
    val remark: String? = null,

    val status: Int? = null
) : BaseManagerUpdateDTO(id)
