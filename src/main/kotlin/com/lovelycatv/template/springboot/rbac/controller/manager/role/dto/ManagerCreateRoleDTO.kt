package com.lovelycatv.template.springboot.rbac.controller.manager.role.dto

data class ManagerCreateRoleDTO(
    val name: String,
    val description: String? = null
)
