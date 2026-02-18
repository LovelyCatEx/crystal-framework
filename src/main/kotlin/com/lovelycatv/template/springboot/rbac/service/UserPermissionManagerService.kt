package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerCreatePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerDeletePermissionDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.template.springboot.rbac.entity.UserPermissionEntity
import com.lovelycatv.template.springboot.rbac.repository.UserPermissionRepository
import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.template.springboot.shared.service.BaseManagerService

interface UserPermissionManagerService : BaseManagerService<
        UserPermissionRepository,
        UserPermissionEntity,
        ManagerCreatePermissionDTO,
        BaseManagerReadDTO,
        ManagerUpdatePermissionDTO,
        ManagerDeletePermissionDTO
>