/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.entity.UserRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRolePermissionRelationRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserRolePermissionRelationService : CachedBaseService<UserRolePermissionRelationRepository, UserRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<UserPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)

    suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}