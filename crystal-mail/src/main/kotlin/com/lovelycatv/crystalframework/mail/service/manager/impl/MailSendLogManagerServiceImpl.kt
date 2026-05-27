package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailSendLogManagerService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailSendLogManagerServiceImpl(
    private val mailSendLogRepository: MailSendLogRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MailSendLogManagerService {
    override val cacheStore: ExpiringKVStore<String, MailSendLogEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailSendLogEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailSendLogEntity> = MailSendLogEntity::class

    override fun getRepository(): MailSendLogRepository {
        return this.mailSendLogRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate
}