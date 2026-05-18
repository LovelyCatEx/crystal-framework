package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserPermissionService : CachedBaseService<UserPermissionRepository, UserPermissionEntity> {
    suspend fun getAllPermissions(): List<UserPermissionEntity>
}