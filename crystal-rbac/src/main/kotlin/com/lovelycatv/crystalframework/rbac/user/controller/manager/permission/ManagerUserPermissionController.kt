package com.lovelycatv.crystalframework.rbac.user.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.user.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.user.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.user.service.UserPermissionManagerService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-permission")
class ManagerUserPermissionController(
    managerService: UserPermissionManagerService
) : StandardManagerController<
        UserPermissionManagerService,
        UserPermissionRepository,
        UserPermissionEntity,
        ManagerCreatePermissionDTO,
        ManagerReadPermissionDTO,
        ManagerUpdatePermissionDTO,
        ManagerDeletePermissionDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_PERMISSION_CREATE,
        superRead = SystemPermission.ACTION_PERMISSION_READ,
        superUpdate = SystemPermission.ACTION_PERMISSION_UPDATE,
        superDelete = SystemPermission.ACTION_PERMISSION_DELETE,
    ),
)
