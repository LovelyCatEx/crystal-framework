package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.entity.UserRolePermissionRelationEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRolePermissionRelationRepository
import com.lovelycatv.template.springboot.shared.service.BaseService

interface UserRolePermissionRelationService : BaseService<UserRolePermissionRelationRepository, UserRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)
}