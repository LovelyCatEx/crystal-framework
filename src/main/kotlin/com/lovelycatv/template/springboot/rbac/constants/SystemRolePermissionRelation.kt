package com.lovelycatv.template.springboot.rbac.constants

import kotlin.reflect.full.memberProperties

object SystemRolePermissionRelation {
    val mapping = mapOf(
        SystemRole.ROLE_ROOT to SystemPermission::class.memberProperties.map {
            it.getter.call() as? String?
                ?: throw IllegalStateException("could not call member property ${it.name} in ${SystemPermission::class.qualifiedName}")
        },
        SystemRole.ROLE_ADMIN to listOf(
            SystemPermission.MENU_USER_MANAGER,
            SystemPermission.ACTION_USER_READ,
            SystemPermission.MENU_SYSTEM_SETTINGS,
            SystemPermission.ACTION_SYSTEM_SETTINGS_READ
        ),
    )
}