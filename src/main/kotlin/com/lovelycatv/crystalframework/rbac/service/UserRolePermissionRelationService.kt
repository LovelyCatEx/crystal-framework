package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRolePermissionRelationRepository

interface UserRolePermissionRelationService : CachedBaseService<UserRolePermissionRelationRepository, UserRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)
}