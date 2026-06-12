package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateTenantDictTypeDTO(
    override val scope: Int = 0,
    override val scopeId: Long = 0,

    @field:NotBlank(message = "Code is required")
    @field:Size(max = 64, message = "Code length cannot exceed 64 characters")
    val code: String,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,

    @field:Size(max = 512, message = "Remark length cannot exceed 512 characters")
    val remark: String? = null,

    val status: Int = 1
) : BaseManagerCreateScopedDTO(scope, scopeId)
