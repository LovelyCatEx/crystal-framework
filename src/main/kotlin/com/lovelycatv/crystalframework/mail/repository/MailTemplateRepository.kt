package com.lovelycatv.crystalframework.mail.repository

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface MailTemplateRepository : BaseRepository<MailTemplateEntity> {
    fun findAllByTypeIdAndActive(typeId: Long, active: Boolean): Flux<MailTemplateEntity>
    fun findByName(name: String): Mono<MailTemplateEntity>
}