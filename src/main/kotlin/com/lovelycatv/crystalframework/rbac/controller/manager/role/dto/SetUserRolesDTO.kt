package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

data class SetUserRolesDTO(
    val userId: Long,
    val roleIds: List<Long>
)