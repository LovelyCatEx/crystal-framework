package com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreatePermissionDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 256, message = "Name length cannot exceed 256 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    @field:NotNull(message = "Type is required")
    val type: Int,

    @field:Size(max = 256, message = "Path length cannot exceed 256 characters")
    val path: String? = null
)
