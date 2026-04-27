package com.lovelycatv.crystalframework.rbac.controller.manager.role

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.service.UserRoleManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = SystemPermission.ACTION_ROLE_READ,
    readAll = SystemPermission.ACTION_ROLE_READ,
    create = SystemPermission.ACTION_ROLE_CREATE,
    update = SystemPermission.ACTION_ROLE_UPDATE,
    delete = SystemPermission.ACTION_ROLE_DELETE,
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-role")
class ManagerUserRoleController(
    managerService: UserRoleManagerService
) : StandardManagerController<
        UserRoleManagerService,
        UserRoleRepository,
        UserRoleEntity,
        ManagerCreateRoleDTO,
        ManagerReadRoleDTO,
        ManagerUpdateRoleDTO,
        ManagerDeleteRoleDTO
>(managerService)
