package com.lovelycatv.crystalframework.mail.controller.manager.template

import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerDeleteMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerReadMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateManagerService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-template")
class ManagerMailTemplateController(
    managerService: MailTemplateManagerService
) : StandardManagerController<
        MailTemplateManagerService,
        MailTemplateRepository,
        MailTemplateEntity,
        ManagerCreateMailTemplateDTO,
        ManagerReadMailTemplateDTO,
        ManagerUpdateMailTemplateDTO,
        ManagerDeleteMailTemplateDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_DELETE.name,
    ),
)
