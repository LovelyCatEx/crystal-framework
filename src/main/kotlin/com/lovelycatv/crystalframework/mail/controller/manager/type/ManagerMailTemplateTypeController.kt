package com.lovelycatv.crystalframework.mail.controller.manager.type

import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerDeleteMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerReadMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateTypeManagerService
import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_READ],
    readAll = [SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_READ],
    create = [SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_CREATE],
    update = [SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_UPDATE],
    delete = [SystemPermission.ACTION_MAIL_TEMPLATE_TYPE_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-template-type")
class ManagerMailTemplateTypeController(
    managerService: MailTemplateTypeManagerService
) : StandardManagerController<
        MailTemplateTypeManagerService,
        MailTemplateTypeRepository,
        MailTemplateTypeEntity,
        ManagerCreateMailTemplateTypeDTO,
        ManagerReadMailTemplateTypeDTO,
        ManagerUpdateMailTemplateTypeDTO,
        ManagerDeleteMailTemplateTypeDTO
>(managerService)
