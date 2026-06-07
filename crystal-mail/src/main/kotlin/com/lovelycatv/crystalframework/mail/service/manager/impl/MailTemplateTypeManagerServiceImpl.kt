package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateTypeManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateTypeManagerServiceImpl(
    private val mailTemplateTypeRepository: MailTemplateTypeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MailTemplateTypeManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, MailTemplateTypeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MailTemplateTypeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MailTemplateTypeEntity> = MailTemplateTypeEntity::class

    override fun getRepository(): MailTemplateTypeRepository {
        return this.mailTemplateTypeRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateMailTemplateTypeDTO): MailTemplateTypeEntity {
        return this.getRepository().save(
            MailTemplateTypeEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description,
                variables = dto.variables,
                categoryId = dto.categoryId,
                allowMultiple = dto.allowMultiple
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create mail template type")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateMailTemplateTypeDTO,
        original: MailTemplateTypeEntity
    ): MailTemplateTypeEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
            dto.variables?.let { variables = it }
            dto.categoryId?.let { categoryId = it }
            dto.allowMultiple?.let { allowMultiple = it }
        }
    }
}
