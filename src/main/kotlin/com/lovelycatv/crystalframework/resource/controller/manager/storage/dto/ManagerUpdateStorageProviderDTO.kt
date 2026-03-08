package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateStorageProviderDTO(
    override val id: Long,

    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val type: Int? = null,

    @field:Size(max = 256, message = "Base URL length cannot exceed 256 characters")
    val baseUrl: String? = null,

    val properties: String? = null,
    val active: Boolean? = null,
) : BaseManagerUpdateDTO(id)
