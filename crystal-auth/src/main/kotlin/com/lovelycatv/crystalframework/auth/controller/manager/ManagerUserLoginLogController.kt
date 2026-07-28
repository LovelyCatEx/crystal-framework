package com.lovelycatv.crystalframework.auth.controller.manager

import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerCreateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerDeleteUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerReadUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerUpdateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.entity.UserLoginLogEntity
import com.lovelycatv.crystalframework.auth.repository.UserLoginLogRepository
import com.lovelycatv.crystalframework.auth.service.manager.UserLoginLogManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnlyReadonly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-login-log")
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
        >(
    managerService,
    // Legacy annotation incorrectly bound CUD to the READ constant; systemOnlyReadonly fills every
    // CUD slot with PermissionMatrix.NEVER_GRANTED which the Mutability.READ_ONLY guard also blocks.
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        superRead = SystemPermission.ACTION_USER_LOGIN_LOG_READ,
    ),
)
