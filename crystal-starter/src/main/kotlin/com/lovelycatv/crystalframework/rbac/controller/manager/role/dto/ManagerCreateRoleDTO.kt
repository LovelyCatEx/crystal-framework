package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateRoleDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null
)
