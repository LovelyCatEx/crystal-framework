package com.lovelycatv.crystalframework.mail.controller.manager.category.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateMailTemplateCategoryDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String?
)
