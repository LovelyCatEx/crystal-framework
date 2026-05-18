package com.lovelycatv.crystalframework.mail.controller.manager

import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerCreateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerDeleteMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerReadMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerUpdateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailSendLogManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    readAll = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    create = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    update = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
    delete = [SystemPermission.ACTION_MAIL_SEND_LOG_READ],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-send-logs")
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
>(managerService)