package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateServiceImpl(
    private val mailTemplateRepository: MailTemplateRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateEntity> = MailTemplateEntity::class

    override fun getRepository(): MailTemplateRepository {
        return this.mailTemplateRepository
    }
}
