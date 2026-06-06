package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserPermissionService : CachedBaseService<UserPermissionRepository, UserPermissionEntity> {
    suspend fun getAllPermissions(): List<UserPermissionEntity>
}