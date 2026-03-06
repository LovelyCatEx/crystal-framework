package com.lovelycatv.crystalframework.mail.controller.manager.type.dto

data class ManagerCreateMailTemplateTypeDTO(
    val name: String,
    val description: String?,
    val variables: String,
    val categoryId: Long,
    val allowMultiple: Boolean
)
