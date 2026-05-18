package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserRoleRelationService : CachedBaseService<UserRoleRelationRepository, UserRoleRelationEntity> {
    suspend fun getUserRoles(userId: Long): List<UserRoleEntity>

    suspend fun setUserRoles(userId: Long, roleIds: List<Long>)

    suspend fun setUserRolesByNames(userId: Long, roleNames: List<String>)

    suspend fun deleteByUserIdIn(userIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}