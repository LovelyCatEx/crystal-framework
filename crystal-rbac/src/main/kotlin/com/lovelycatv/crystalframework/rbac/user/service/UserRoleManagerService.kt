package com.lovelycatv.crystalframework.rbac.user.service

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface UserRoleManagerService : CachedBaseManagerService<
        UserRoleRepository,
        UserRoleEntity,
        ManagerCreateRoleDTO,
        ManagerReadRoleDTO,
        ManagerUpdateRoleDTO,
        ManagerDeleteRoleDTO
>
