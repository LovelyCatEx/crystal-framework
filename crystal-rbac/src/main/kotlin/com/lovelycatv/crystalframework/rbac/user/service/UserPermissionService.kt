/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface UserPermissionService : CachedBaseService<UserPermissionRepository, UserPermissionEntity> {
    suspend fun getAllPermissions(): List<UserPermissionEntity>
}