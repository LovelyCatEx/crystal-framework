package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user")
class ManagerUserController(
    managerService: UserManagerService
) : StandardManagerController<
        UserManagerService,
        UserRepository,
        UserEntity,
        ManagerCreateUserDTO,
        ManagerReadUserDTO,
        ManagerUpdateUserDTO,
        ManagerDeleteUserDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_USER_CREATE,
        superRead = SystemPermission.ACTION_USER_READ,
        superUpdate = SystemPermission.ACTION_USER_UPDATE,
        superDelete = SystemPermission.ACTION_USER_DELETE,
    ),
)
