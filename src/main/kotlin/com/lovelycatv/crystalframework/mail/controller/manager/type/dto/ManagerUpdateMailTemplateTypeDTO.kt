package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateMailTemplateTypeDTO(
    override val id: Long,

    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    val variables: String? = null,
    val categoryId: Long? = null,
    val allowMultiple: Boolean? = null
) : BaseManagerUpdateDTO(id)
