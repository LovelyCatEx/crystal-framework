package com.lovelycatv.crystalframework.mail.controller.manager.template

import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerDeleteMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerReadMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateManagerService
import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_MAIL_TEMPLATE_READ],
    readAll = [SystemPermission.ACTION_MAIL_TEMPLATE_READ],
    create = [SystemPermission.ACTION_MAIL_TEMPLATE_CREATE],
    update = [SystemPermission.ACTION_MAIL_TEMPLATE_UPDATE],
    delete = [SystemPermission.ACTION_MAIL_TEMPLATE_DELETE],
)
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
>(managerService)
