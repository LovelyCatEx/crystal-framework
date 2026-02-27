package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRolePermissionRelationRepository
import com.lovelycatv.crystalframework.shared.service.BaseService

interface UserRolePermissionRelationService : BaseService<UserRolePermissionRelationRepository, UserRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)
}