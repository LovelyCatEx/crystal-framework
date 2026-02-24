package com.lovelycatv.template.springboot.rbac.controller.manager.role.dto

data class SetUserRolesDTO(
    val userId: Long,
    val roleIds: List<Long>
)