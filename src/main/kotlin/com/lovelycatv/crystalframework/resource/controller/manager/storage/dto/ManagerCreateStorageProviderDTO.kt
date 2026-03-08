package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateStorageProviderDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    @field:NotNull(message = "Type is required")
    val type: Int,

    @field:NotBlank(message = "Base URL is required")
    @field:Size(max = 256, message = "Base URL length cannot exceed 256 characters")
    val baseUrl: String,

    @field:NotBlank(message = "Properties is required")
    val properties: String
)
