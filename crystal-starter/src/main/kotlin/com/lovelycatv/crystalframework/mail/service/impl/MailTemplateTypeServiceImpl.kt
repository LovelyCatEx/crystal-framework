package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateTypeService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateTypeServiceImpl(
    private val mailTemplateTypeRepository: MailTemplateTypeRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateTypeService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateTypeEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateTypeEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateTypeEntity> = MailTemplateTypeEntity::class

    override fun getRepository(): MailTemplateTypeRepository {
        return this.mailTemplateTypeRepository
    }
}
