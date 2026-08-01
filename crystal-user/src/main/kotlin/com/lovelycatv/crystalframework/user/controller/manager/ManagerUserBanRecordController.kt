package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnlyReadonly
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerCreateUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerDeleteUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerReadUserBanRecordDTO
import com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto.ManagerUpdateUserBanRecordDTO
import com.lovelycatv.crystalframework.user.entity.UserBanRecordEntity
import com.lovelycatv.crystalframework.user.repository.UserBanRecordRepository
import com.lovelycatv.crystalframework.user.service.manager.UserBanRecordManagerService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/user-ban-record")
class ManagerUserBanRecordController(
    managerService: UserBanRecordManagerService
) : ReadonlyManagerController<
        UserBanRecordManagerService,
        UserBanRecordRepository,
        UserBanRecordEntity,
        ManagerCreateUserBanRecordDTO,
        ManagerReadUserBanRecordDTO,
        ManagerUpdateUserBanRecordDTO,
        ManagerDeleteUserBanRecordDTO
        >(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = SystemPermission.ACTION_SYSTEM_USER_BAN_RECORD_READ.name,
    ),
)
