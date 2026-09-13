/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface UserPermissionManagerService : CachedBaseManagerService<
        UserPermissionRepository,
        UserPermissionEntity,
        ManagerCreatePermissionDTO,
        ManagerReadPermissionDTO,
        ManagerUpdatePermissionDTO,
        ManagerDeletePermissionDTO
>
