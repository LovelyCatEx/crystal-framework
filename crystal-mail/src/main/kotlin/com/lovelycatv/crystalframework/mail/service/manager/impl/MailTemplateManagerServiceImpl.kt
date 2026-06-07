package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.mail.service.MailTemplateTypeService
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateManagerServiceImpl(
    private val mailTemplateRepository: MailTemplateRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val mailTemplateTypeService: MailTemplateTypeService,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MailTemplateManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, MailTemplateEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MailTemplateEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MailTemplateEntity> = MailTemplateEntity::class

    override fun getRepository(): MailTemplateRepository {
        return this.mailTemplateRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateMailTemplateDTO): MailTemplateEntity {
        return this.getRepository().save(
            MailTemplateEntity(
                id = snowIdGenerator.nextId(),
                typeId = dto.typeId,
                name = dto.name,
                description = dto.description,
                title = dto.title,
                content = dto.content,
                active = actualTemplateActive(dto.active, dto.typeId)
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
            dto.active?.let { active = actualTemplateActive(it, dto.typeId ?: this.typeId ) }
        }
    }

    private suspend fun actualTemplateActive(requiredActive: Boolean, templateTypeId: Long): Boolean {
        // Check whether the template type allowing multiple templates
        var actualActive = requiredActive

        val templateType = mailTemplateTypeService.getByIdOrThrow(templateTypeId)
        if (!templateType.allowMultiple) {
            val templatesInSameType = mailTemplateRepository
                .findAllByTypeIdAndActive(templateType.id, true)
                .awaitListWithTimeout()

            if (templatesInSameType.isNotEmpty()) {
                actualActive = false
            }
        }

        return actualActive
    }
}
