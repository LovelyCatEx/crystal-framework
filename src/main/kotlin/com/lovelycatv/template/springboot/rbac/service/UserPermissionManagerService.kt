package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.service.BaseManagerService

interface UserPermissionManagerService : BaseManagerService<
        UserPermissionRepository,
        UserPermissionEntity,
        ManagerCreatePermissionDTO,
        ManagerReadPermissionDTO,
        ManagerUpdatePermissionDTO,
        ManagerDeletePermissionDTO
>