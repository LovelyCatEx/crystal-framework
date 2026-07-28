package com.lovelycatv.crystalframework.mail.controller.manager

import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerCreateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerDeleteMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerReadMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerUpdateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailSendLogManagerService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-send-log")
class ManagerMailSendLogController(
    managerService: MailSendLogManagerService
) : ReadonlyManagerController<
        MailSendLogManagerService,
        MailSendLogRepository,
        MailSendLogEntity,
        ManagerCreateMailSendLogDTO,
        ManagerReadMailSendLogDTO,
        ManagerUpdateMailSendLogDTO,
        ManagerDeleteMailSendLogDTO
>(
    managerService,
    // Legacy annotation incorrectly bound CUD to the READ constant; systemOnlyReadonly fills every
    // CUD slot with PermissionMatrix.NEVER_GRANTED which the Mutability.READ_ONLY guard also blocks.
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        superRead = SystemPermission.ACTION_MAIL_SEND_LOG_READ,
    ),
)
