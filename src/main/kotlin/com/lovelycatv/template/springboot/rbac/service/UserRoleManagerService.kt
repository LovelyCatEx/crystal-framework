package com.lovelycatv.template.springboot.rbac.service

import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.template.springboot.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.template.springboot.rbac.entity.UserRoleEntity
import com.lovelycatv.template.springboot.rbac.repository.UserRoleRepository
import com.lovelycatv.template.springboot.shared.service.BaseManagerService

interface UserRoleManagerService : BaseManagerService<
        UserRoleRepository,
        UserRoleEntity,
        ManagerCreateRoleDTO,
        ManagerReadRoleDTO,
        ManagerUpdateRoleDTO,
        ManagerDeleteRoleDTO
>
