package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.shared.service.BaseService

interface UserRoleService : BaseService<UserRoleRepository, UserRoleEntity> {
    suspend fun getAllRoles(): List<UserRoleEntity>

    suspend fun getAllRolesAssociatedById(): Map<Long, UserRoleEntity> {
        return this.getAllRoles().associateBy { it.id }
    }
}