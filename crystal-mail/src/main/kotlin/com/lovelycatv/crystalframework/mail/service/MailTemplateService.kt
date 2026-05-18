package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

interface MailTemplateService : CachedBaseService<MailTemplateRepository, MailTemplateEntity> {
    suspend fun getAvailableTemplateByTypeName(templateTypeName: String): MailTemplateEntity
}
