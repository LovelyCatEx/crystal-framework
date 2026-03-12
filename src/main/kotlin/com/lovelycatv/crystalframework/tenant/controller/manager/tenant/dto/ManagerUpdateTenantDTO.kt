package com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

 data class ManagerUpdateTenantDTO(
    override val id: Long,

    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,

    val description: String? = null,

    val ownerUserId: Long? = null,

    val status: Int? = null,

    val tireTypeId: Long? = null,

    @field:Positive(message = "Subscribed time must be a positive timestamp")
    val subscribedTime: Long? = null,

    @field:Positive(message = "Expires time must be a positive timestamp")
    val expiresTime: Long? = null,

    @field:Size(max = 64, message = "Contact name length cannot exceed 64 characters")
    val contactName: String? = null,

    @field:Size(max = 256, message = "Contact email length cannot exceed 256 characters")
    val contactEmail: String? = null,

    @field:Size(max = 32, message = "Contact phone length cannot exceed 32 characters")
    val contactPhone: String? = null,

    val address: String? = null,

    val settings: String? = null
) : BaseManagerUpdateDTO(id)
