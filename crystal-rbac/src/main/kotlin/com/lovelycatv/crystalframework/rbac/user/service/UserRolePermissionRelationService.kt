package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRolePermissionRelationRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserRolePermissionRelationService : CachedBaseService<UserRolePermissionRelationRepository, UserRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)

    suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}