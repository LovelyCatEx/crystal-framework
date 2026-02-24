package com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto

data class SetRolePermissionsDTO(
    val roleId: Long,
    val permissionIds: List<Long>
)