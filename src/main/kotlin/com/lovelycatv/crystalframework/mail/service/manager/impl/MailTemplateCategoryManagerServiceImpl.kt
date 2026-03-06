package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateCategoryManagerService
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerCreateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.mail.controller.manager.category.dto.ManagerUpdateMailTemplateCategoryDTO
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateCategoryManagerServiceImpl(
    private val mailTemplateCategoryRepository: MailTemplateCategoryRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateCategoryManagerService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateCategoryEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateCategoryEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateCategoryEntity> = MailTemplateCategoryEntity::class

    override fun getRepository(): MailTemplateCategoryRepository {
        return this.mailTemplateCategoryRepository
    }

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
