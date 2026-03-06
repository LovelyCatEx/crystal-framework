package com.lovelycatv.crystalframework.mail.controller.manager.template.dto

data class ManagerCreateMailTemplateDTO(
    val typeId: Long,
    val name: String,
    val description: String?,
    val title: String,
    val content: String,
    val active: Boolean
)
