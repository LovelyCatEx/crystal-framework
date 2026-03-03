package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository

interface UserPermissionService : CachedBaseService<UserPermissionRepository, UserPermissionEntity> {
    suspend fun getAllPermissions(): List<UserPermissionEntity>
}