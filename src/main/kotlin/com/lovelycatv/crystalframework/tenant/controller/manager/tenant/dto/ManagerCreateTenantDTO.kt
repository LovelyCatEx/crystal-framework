package com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

 data class ManagerCreateTenantDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,

    val description: String? = null,

    @field:NotNull(message = "status is required")
    val status: Int,

    @field:NotNull(message = "Owner user ID is required")
    val ownerUserId: Long,

    @field:NotNull(message = "Tire type ID is required")
    val tireTypeId: Long,

    @field:NotNull(message = "Subscribed time is required")
    @field:Positive(message = "Subscribed time must be a positive timestamp")
    val subscribedTime: Long,

    @field:NotNull(message = "Expires time is required")
    @field:Positive(message = "Expires time must be a positive timestamp")
    val expiresTime: Long,

    @field:NotBlank(message = "Contact name is required")
    @field:Size(max = 64, message = "Contact name length cannot exceed 64 characters")
    val contactName: String,

    @field:NotBlank(message = "Contact email is required")
    @field:Size(max = 256, message = "Contact email length cannot exceed 256 characters")
    val contactEmail: String,

    @field:NotBlank(message = "Contact phone is required")
    @field:Size(max = 32, message = "Contact phone length cannot exceed 32 characters")
    val contactPhone: String,

    @field:NotBlank(message = "Address is required")
    val address: String,

    val settings: String? = null
)
