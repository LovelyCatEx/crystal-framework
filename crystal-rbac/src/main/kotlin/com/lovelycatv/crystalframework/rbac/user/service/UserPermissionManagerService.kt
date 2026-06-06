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
