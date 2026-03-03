package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRelationRepository

interface UserRoleRelationService : CachedBaseService<UserRoleRelationRepository, UserRoleRelationEntity> {
    suspend fun getUserRoles(userId: Long): List<UserRoleEntity>

    suspend fun setUserRoles(userId: Long, roleIds: List<Long>)
}