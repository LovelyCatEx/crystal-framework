package com.lovelycatv.crystalframework.user.controller.manager.user

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerCreateUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerDeleteUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerReadUserDTO
import com.lovelycatv.crystalframework.user.controller.manager.user.dto.ManagerUpdateUserDTO
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.repository.UserRepository
import com.lovelycatv.crystalframework.user.service.UserManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_USER_READ],
    readAll = [SystemPermission.ACTION_USER_READ],
    create = [SystemPermission.ACTION_USER_CREATE],
    update = [SystemPermission.ACTION_USER_UPDATE],
    delete = [SystemPermission.ACTION_USER_DELETE],
)
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
>(managerService)