package com.lovelycatv.template.springboot.rbac.constants

object SystemPermission {
    const val MENU_PERMISSION_MANAGER = "permission:/manager/user-permissions"
    const val MENU_ROLE_MANAGER = "permission:/manager/user-roles"
    const val MENU_USER_MANAGER = "permission:/manager/users"

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
}