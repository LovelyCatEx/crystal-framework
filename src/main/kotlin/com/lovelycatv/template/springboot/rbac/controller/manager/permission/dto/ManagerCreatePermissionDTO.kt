package com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto

data class ManagerCreatePermissionDTO(
    val name: String,
    val description: String? = null,
    val type: Int,
    val path: String? = null
)
