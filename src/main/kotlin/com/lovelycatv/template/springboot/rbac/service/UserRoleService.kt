package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRepository
import com.lovelycatv.template.springboot.shared.service.BaseService

interface UserRoleService : BaseService<UserRoleRepository, UserRoleEntity> {
    suspend fun getAllRoles(): List<UserRoleEntity>

    suspend fun getAllRolesAssociatedById(): Map<Long, UserRoleEntity> {
        return this.getAllRoles().associateBy { it.id }
    }
}