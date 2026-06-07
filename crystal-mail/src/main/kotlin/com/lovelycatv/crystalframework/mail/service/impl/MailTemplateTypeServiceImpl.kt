package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateTypeService
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateTypeServiceImpl(
    private val mailTemplateTypeRepository: MailTemplateTypeRepository,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateTypeService {
    override val cacheStore: ReactiveExpiringKVStore<String, MailTemplateTypeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MailTemplateTypeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MailTemplateTypeEntity> = MailTemplateTypeEntity::class

    override fun getRepository(): MailTemplateTypeRepository {
        return this.mailTemplateTypeRepository
    }
}
