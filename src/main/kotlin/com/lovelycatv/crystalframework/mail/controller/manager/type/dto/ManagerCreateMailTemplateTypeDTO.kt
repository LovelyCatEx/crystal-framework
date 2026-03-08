package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateMailTemplateTypeDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String?,

    @field:NotBlank(message = "Variables is required")
    val variables: String,

    @field:NotNull(message = "Category ID is required")
    val categoryId: Long,

    @field:NotNull(message = "Allow multiple is required")
    val allowMultiple: Boolean
)
