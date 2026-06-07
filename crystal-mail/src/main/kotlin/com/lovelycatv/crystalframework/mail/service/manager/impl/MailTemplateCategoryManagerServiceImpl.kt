package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateCategoryManagerService
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
class MailTemplateCategoryManagerServiceImpl(
    private val mailTemplateCategoryRepository: MailTemplateCategoryRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : MailTemplateCategoryManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, MailTemplateCategoryEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<MailTemplateCategoryEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<MailTemplateCategoryEntity> = MailTemplateCategoryEntity::class

    override fun getRepository(): MailTemplateCategoryRepository {
        return this.mailTemplateCategoryRepository
    }

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateMailTemplateCategoryDTO): MailTemplateCategoryEntity {
        return this.getRepository().save(
            MailTemplateCategoryEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create mail template category")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateMailTemplateCategoryDTO,
        original: MailTemplateCategoryEntity
    ): MailTemplateCategoryEntity {
        return original.apply {
            dto.name?.let { name = it }
            dto.description?.let { description = it }
        }
    }
}
