package com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

 data class ManagerCreateTenantPermissionDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 256, message = "Name length cannot exceed 256 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val type: Int = 0,

    @field:Size(max = 256, message = "Path length cannot exceed 256 characters")
    val path: String? = null,

    val preserved1: Int? = null,

    val preserved2: Int? = null
)
