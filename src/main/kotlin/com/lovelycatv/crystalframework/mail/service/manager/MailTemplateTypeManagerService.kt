package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerDeleteMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerReadMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO

interface MailTemplateTypeManagerService : CachedBaseManagerService<
        MailTemplateTypeRepository,
        MailTemplateTypeEntity,
        ManagerCreateMailTemplateTypeDTO,
        ManagerReadMailTemplateTypeDTO,
        ManagerUpdateMailTemplateTypeDTO,
        ManagerDeleteMailTemplateTypeDTO
>
