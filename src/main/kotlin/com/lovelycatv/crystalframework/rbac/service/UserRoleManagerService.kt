package com.lovelycatv.crystalframework.rbac.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository

interface UserRoleManagerService : CachedBaseManagerService<
        UserRoleRepository,
        UserRoleEntity,
        ManagerCreateRoleDTO,
        ManagerReadRoleDTO,
        ManagerUpdateRoleDTO,
        ManagerDeleteRoleDTO
>
