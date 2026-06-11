package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateTenantDictItemDTO(
    override val id: Long,

    @field:Size(max = 256, message = "Item value length cannot exceed 256 characters")
    val itemValue: String? = null,

    val parentId: Long? = null,

    val sortOrder: Int? = null,

    val isDefault: Boolean? = null,

    val status: Int? = null
) : BaseManagerUpdateDTO(id)
