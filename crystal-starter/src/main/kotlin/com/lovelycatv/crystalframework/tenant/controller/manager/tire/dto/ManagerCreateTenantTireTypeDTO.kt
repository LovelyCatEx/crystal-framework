package com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

 data class ManagerCreateTenantTireTypeDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 32, message = "Name length cannot exceed 32 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null
)
