package com.lovelycatv.crystalframework.mail.service.manager.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.service.manager.MailTemplateTypeManagerService
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerCreateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.mail.controller.manager.type.dto.ManagerUpdateMailTemplateTypeDTO
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class MailTemplateTypeManagerServiceImpl(
    private val mailTemplateTypeRepository: MailTemplateTypeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
) : MailTemplateTypeManagerService {
    override val cacheStore: ExpiringKVStore<String, MailTemplateTypeEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<MailTemplateTypeEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<MailTemplateTypeEntity> = MailTemplateTypeEntity::class

    override fun getRepository(): MailTemplateTypeRepository {
        return this.mailTemplateTypeRepository
    }

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
