package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerDeleteMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerReadMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO

interface MailTemplateCategoryManagerService : CachedBaseManagerService<
        MailTemplateCategoryRepository,
        MailTemplateCategoryEntity,
        ManagerCreateMailTemplateCategoryDTO,
        ManagerReadMailTemplateCategoryDTO,
        ManagerUpdateMailTemplateCategoryDTO,
        ManagerDeleteMailTemplateCategoryDTO
>
