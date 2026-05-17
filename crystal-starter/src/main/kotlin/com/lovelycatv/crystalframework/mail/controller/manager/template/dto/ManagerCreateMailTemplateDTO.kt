package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateMailTemplateDTO(
    @field:NotNull(message = "Type ID is required")
    val typeId: Long,

    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,

    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String?,

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 512, message = "Title length cannot exceed 512 characters")
    val title: String,

    @field:NotBlank(message = "Content is required")
    val content: String,

    @field:NotNull(message = "Active is required")
    val active: Boolean
)
