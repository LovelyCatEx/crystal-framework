package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateService
import com.lovelycatv.crystalframework.mail.service.MailTemplateTypeService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateServiceImpl(
    private val mailTemplateRepository: MailTemplateRepository,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val mailTemplateTypeService: MailTemplateTypeService,
) : MailTemplateService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateEntity> = MailTemplateEntity::class

    override fun getRepository(): MailTemplateRepository {
        return this.mailTemplateRepository
    }

    override suspend fun getAvailableTemplateByTypeName(templateTypeName: String): MailTemplateEntity {
        val type = mailTemplateTypeService
            .getRepository()
            .findByName(templateTypeName)
            .awaitFirstOrNull()
            ?: throw BusinessException("Mail template type $templateTypeName not found")

        return this.getRepository()
            .findAllByTypeIdAndActive(type.id, true)
            .awaitListWithTimeout()
            .randomOrNull()
            ?: throw BusinessException("No available mail template found in type $templateTypeName")
    }
}
