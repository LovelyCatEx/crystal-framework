package com.lovelycatv.crystalframework.tenant.controller.dto

import jakarta.validation.constraints.Size

data class UpdateTenantProfileDTO(
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,

    val description: String? = null,

    @field:Size(max = 64, message = "Contact name length cannot exceed 64 characters")
    val contactName: String? = null,

    @field:Size(max = 256, message = "Contact email length cannot exceed 256 characters")
    val contactEmail: String? = null,

    @field:Size(max = 32, message = "Contact phone length cannot exceed 32 characters")
    val contactPhone: String? = null,

    val address: String? = null
)
