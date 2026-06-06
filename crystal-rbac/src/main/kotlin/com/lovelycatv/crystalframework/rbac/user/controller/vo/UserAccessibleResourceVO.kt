package com.lovelycatv.crystalframework.rbac.user.controller.vo

data class UserAccessibleResourceVO(
    val menus: List<String>,
    val components: List<String>,
)