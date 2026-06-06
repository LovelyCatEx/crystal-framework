package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserRoleService : CachedBaseService<UserRoleRepository, UserRoleEntity> {
    suspend fun getAllRoles(): List<UserRoleEntity>

    suspend fun getAllRolesAssociatedById(): Map<Long, UserRoleEntity> {
        return this.getAllRoles().associateBy { it.id }
    }
}