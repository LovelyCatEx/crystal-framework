package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateMailTemplateDTO(
    override val id: Long,
    val typeId: Long? = null,

    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String? = null,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,

    @field:Size(max = 512, message = "Title length cannot exceed 512 characters")
    val title: String? = null,

    val content: String? = null,
    val active: Boolean? = null
) : BaseManagerUpdateDTO(id)
