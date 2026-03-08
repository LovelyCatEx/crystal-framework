package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MailTemplateTypeRepository : BaseRepository<MailTemplateTypeEntity> {
    fun findByName(name: String): Mono<MailTemplateTypeEntity>
}