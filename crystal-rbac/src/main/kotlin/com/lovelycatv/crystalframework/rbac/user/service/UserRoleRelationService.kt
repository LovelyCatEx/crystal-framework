/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleRelationEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRelationRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserRoleRelationService : CachedBaseService<UserRoleRelationRepository, UserRoleRelationEntity> {
    suspend fun getUserRoles(userId: Long): List<UserRoleEntity>

    suspend fun setUserRoles(userId: Long, roleIds: List<Long>)

    suspend fun setUserRolesByNames(userId: Long, roleNames: List<String>)

    suspend fun deleteByUserIdIn(userIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}