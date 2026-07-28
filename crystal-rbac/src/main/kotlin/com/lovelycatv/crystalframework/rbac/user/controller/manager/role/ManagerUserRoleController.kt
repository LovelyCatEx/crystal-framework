package com.lovelycatv.crystalframework.rbac.user.controller.manager.role

import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerCreateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerDeleteRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerReadRoleDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto.ManagerUpdateRoleDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserRoleEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserRoleRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserRoleManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_ROLE_CREATE,
        superRead = SystemPermission.ACTION_ROLE_READ,
        superUpdate = SystemPermission.ACTION_ROLE_UPDATE,
        superDelete = SystemPermission.ACTION_ROLE_DELETE,
    ),
)
