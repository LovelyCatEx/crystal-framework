package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateManagerService
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateManagerServiceImpl(
    private val mailTemplateRepository: MailTemplateRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateManagerService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateEntity> = MailTemplateEntity::class

    override fun getRepository(): MailTemplateRepository {
        return this.mailTemplateRepository
    }

    override suspend fun create(dto: ManagerCreateMailTemplateDTO): MailTemplateEntity {
        return this.getRepository().save(
            MailTemplateEntity(
                id = snowIdGenerator.nextId(),
                typeId = dto.typeId,
                name = dto.name,
                description = dto.description,
                title = dto.title,
                content = dto.content,
                active = dto.active
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create mail template")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateMailTemplateDTO,
        original: MailTemplateEntity
    ): MailTemplateEntity {
        return original.apply {
            dto.typeId?.let { typeId = it }
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.title?.let { title = it }
            dto.content?.let { content = it }
            dto.active?.let { active = it }
        }
    }
}
