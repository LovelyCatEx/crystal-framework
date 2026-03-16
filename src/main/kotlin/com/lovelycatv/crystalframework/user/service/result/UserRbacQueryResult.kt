package com.lovelycatv.crystalframework.user.service.result

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.types.PermissionType

data class UserRbacQueryResult(
    val userId: Long,
    val rolesWithPermissions: List<UserRoleWithPermissions>
) {
    val roles: Set<UserRoleEntity> = this.rolesWithPermissions
        .map { it.role }
        .distinctBy { it.id }
        .toSet()

    val rawPermissions: Set<UserPermissionEntity> = this.rolesWithPermissions
        .flatMap { it.permissions }
        .distinctBy { it.id }
        .toSet()

    val actions: Set<UserPermissionEntity> = this.rawPermissions
        .filter { it.getRealPermissionType() == PermissionType.ACTION }
        .distinctBy { it.id }
        .toSet()

    val paths: Set<UserPermissionEntity> = this.rawPermissions
        .mapNotNull {
            if (it.getRealPermissionType() == PermissionType.MENU && it.path != null) {
                it
            } else {
                null
            }
        }
        .toSet()

    val components: Set<UserPermissionEntity> = this.rawPermissions
        .filter { it.getRealPermissionType() == PermissionType.COMPONENT }
        .distinctBy { it.id }
        .toSet()


    data class UserRoleWithPermissions(
        val role: UserRoleEntity,
        val permissions: List<UserPermissionEntity>
    )
}