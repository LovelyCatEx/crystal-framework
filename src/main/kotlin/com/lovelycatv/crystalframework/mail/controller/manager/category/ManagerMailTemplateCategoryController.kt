package com.lovelycatv.crystalframework.mail.controller.manager.category

import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerDeleteMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerReadMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateCategoryManagerService
import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_READ],
    readAll = [SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_READ],
    create = [SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_CREATE],
    update = [SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_UPDATE],
    delete = [SystemPermission.ACTION_MAIL_TEMPLATE_CATEGORY_DELETE],
)
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
>(managerService)
