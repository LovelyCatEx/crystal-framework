package com.lovelycatv.template.springboot.rbac.constants

import com.lovelycatv.template.springboot.rbac.types.PermissionType

object SystemPermission {
    const val MENU_PERMISSION_MANAGER = "permission:/manager/user-permissions"
    const val MENU_ROLE_MANAGER = "role:/manager/user-roles"
    const val MENU_USER_MANAGER = "user:/manager/users"
    const val MENU_ROLE_PERMISSION_MANAGER = "role.permission:/manager/role-permissions"
    const val MENU_USER_ROLE_MANAGER = "user.role:/manager/user-roles-relation"
    const val MENU_SYSTEM_SETTINGS = "settings:/manager/settings"

    const val ACTION_PERMISSION_CREATE = "permission.create"
    const val ACTION_PERMISSION_READ = "permission.read"
    const val ACTION_PERMISSION_UPDATE = "permission.update"
    const val ACTION_PERMISSION_DELETE = "permission.delete"

    const val ACTION_ROLE_CREATE = "role.create"
    const val ACTION_ROLE_READ = "role.read"
    const val ACTION_ROLE_UPDATE = "role.update"
    const val ACTION_ROLE_DELETE = "role.delete"

    const val ACTION_USER_CREATE = "user.create"
    const val ACTION_USER_READ = "user.read"
    const val ACTION_USER_UPDATE = "user.update"
    const val ACTION_USER_DELETE = "user.delete"

    const val ACTION_ROLE_PERMISSION_READ = "role.permission.read"
    const val ACTION_ROLE_PERMISSION_UPDATE = "role.permission.update"

    const val ACTION_USER_ROLE_READ = "user.role.read"
    const val ACTION_USER_ROLE_UPDATE = "user.role.update"

    const val ACTION_SYSTEM_SETTINGS_READ = "settings.read"
    const val ACTION_SYSTEM_SETTINGS_UPDATE = "settings.update"

    /**
     * resolve permission from string declaration
     *
     * @param str declaration, eg: "user:/manager/users"
     * @return (name, description, path)
     */
    fun resolvePermissionDeclaration(str: String): Triple<String, String, String?> {
        val type = if (str.contains(":")) {
            PermissionType.MENU
        } else {
            PermissionType.ACTION
        }

        return when (type) {
            PermissionType.ACTION -> {
                Triple(str, str, null)
            }

            PermissionType.MENU -> {
                val (readPermissionKey, path) = str.split(":")
                Triple(readPermissionKey, readPermissionKey, path)
            }
        }
    }
}