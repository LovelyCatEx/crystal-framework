/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.service

import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.rbac.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.rbac.tenant.repository.TenantRoleRepository

interface TenantRoleService : CachedBaseService<TenantRoleRepository, TenantRoleEntity> {
    suspend fun getChildren(roleId: Long): List<TenantRoleEntity>

    suspend fun getParents(roleId: Long): List<TenantRoleEntity>
}
