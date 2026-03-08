package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository

interface MailTemplateService : CachedBaseService<MailTemplateRepository, MailTemplateEntity> {
    suspend fun getAvailableTemplateByTypeName(templateTypeName: String): MailTemplateEntity
}
