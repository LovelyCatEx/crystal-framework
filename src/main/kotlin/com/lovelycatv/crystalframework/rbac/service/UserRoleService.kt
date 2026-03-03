package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository

interface UserRoleService : CachedBaseService<UserRoleRepository, UserRoleEntity> {
    suspend fun getAllRoles(): List<UserRoleEntity>

    suspend fun getAllRolesAssociatedById(): Map<Long, UserRoleEntity> {
        return this.getAllRoles().associateBy { it.id }
    }
}