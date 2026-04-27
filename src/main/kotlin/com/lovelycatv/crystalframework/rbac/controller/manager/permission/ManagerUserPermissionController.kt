package com.lovelycatv.crystalframework.rbac.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerCreatePermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerDeletePermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerReadPermissionDTO
import com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto.ManagerUpdatePermissionDTO
import com.lovelycatv.crystalframework.rbac.entity.UserPermissionEntity
import com.lovelycatv.crystalframework.rbac.repository.UserPermissionRepository
import com.lovelycatv.crystalframework.rbac.service.UserPermissionManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_PERMISSION_READ],
    readAll = [SystemPermission.ACTION_PERMISSION_READ],
    create = [SystemPermission.ACTION_PERMISSION_CREATE],
    update = [SystemPermission.ACTION_PERMISSION_UPDATE],
    delete = [SystemPermission.ACTION_PERMISSION_DELETE],
)
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
>(managerService)
