package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateTenantDictItemDTO(
    val typeId: Long,

    @field:NotBlank(message = "Item code is required")
    @field:Size(max = 64, message = "Item code length cannot exceed 64 characters")
    val itemCode: String,

    @field:NotBlank(message = "Item value is required")
    @field:Size(max = 256, message = "Item value length cannot exceed 256 characters")
    val itemValue: String,

    val parentId: Long? = null,

    val sortOrder: Int? = null,

    val isDefault: Boolean? = null,

    val status: Int? = null
)
