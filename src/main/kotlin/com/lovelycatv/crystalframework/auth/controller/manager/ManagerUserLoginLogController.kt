package com.lovelycatv.crystalframework.auth.controller.manager

import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerCreateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerDeleteUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerReadUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerUpdateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.auth.service.manager.UserLoginLogManagerService
import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_USER_LOGIN_LOG_READ],
    readAll = [SystemPermission.ACTION_USER_LOGIN_LOG_READ],
    create = [SystemPermission.ACTION_USER_LOGIN_LOG_READ],
    update = [SystemPermission.ACTION_USER_LOGIN_LOG_READ],
    delete = [SystemPermission.ACTION_USER_LOGIN_LOG_READ],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-login-logs")
class ManagerUserLoginLogController(
    managerService: UserLoginLogManagerService
) : ReadonlyManagerController<
        UserLoginLogManagerService,
        UserLoginLogRepository,
        UserLoginLogEntity,
        ManagerCreateUserLoginLogDTO,
        ManagerReadUserLoginLogDTO,
        ManagerUpdateUserLoginLogDTO,
        ManagerDeleteUserLoginLogDTO
        >(managerService)