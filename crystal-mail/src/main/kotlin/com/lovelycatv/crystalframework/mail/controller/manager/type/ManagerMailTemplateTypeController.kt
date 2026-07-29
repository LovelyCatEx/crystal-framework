package com.lovelycatv.crystalframework.mail.controller.manager.type

import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerDeleteMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerReadMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateTypeManagerService
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
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_TYPE_DELETE.name,
    ),
)
