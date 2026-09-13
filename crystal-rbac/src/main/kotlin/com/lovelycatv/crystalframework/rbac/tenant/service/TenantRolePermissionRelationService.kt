/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRolePermissionRelationEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRolePermissionRelationRepository

interface TenantRolePermissionRelationService : CachedBaseService<TenantRolePermissionRelationRepository, TenantRolePermissionRelationEntity> {
    suspend fun getRolePermissions(roleId: Long): List<TenantPermissionEntity>

    suspend fun getRolePermissions(roleIds: List<Long>): List<TenantPermissionEntity>

    suspend fun setRolePermissions(roleId: Long, permissionIds: List<Long>)

    suspend fun deleteByPermissionIdIn(permissionIds: Collection<Long>)

    suspend fun deleteByRoleIdIn(roleIds: Collection<Long>)
}
