package com.lovelycatv.template.springboot.user.service.result

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity

data class UserRbacQueryResult(
    val userId: Long,
    val rolesWithPermissions: List<UserRoleWithPermissions>
) {
    val roles: Set<UserRoleEntity> = this.rolesWithPermissions
        .map { it.role }
        .distinctBy { it.id }
        .toSet()

    val permissions: Set<UserPermissionEntity> = rolesWithPermissions
        .flatMap { it.permissions }
        .distinctBy { it.id }
        .toSet()

    data class UserRoleWithPermissions(
        val role: UserRoleEntity,
        val permissions: List<UserPermissionEntity>
    )
}