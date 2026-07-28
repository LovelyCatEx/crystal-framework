package com.lovelycatv.crystalframework.mail.controller.manager.category

import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerDeleteMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerReadMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateCategoryManagerService
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
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/mail-template-category")
class ManagerMailTemplateCategoryController(
    managerService: MailTemplateCategoryManagerService
) : StandardManagerController<
        MailTemplateCategoryManagerService,
        MailTemplateCategoryRepository,
        MailTemplateCategoryEntity,
        ManagerCreateMailTemplateCategoryDTO,
        ManagerReadMailTemplateCategoryDTO,
        ManagerUpdateMailTemplateCategoryDTO,
        ManagerDeleteMailTemplateCategoryDTO
>(
    managerService,
    permissions = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_CREATE,
        superRead = SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_READ,
        superUpdate = SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_UPDATE,
        superDelete = SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_DELETE,
    ),
)
