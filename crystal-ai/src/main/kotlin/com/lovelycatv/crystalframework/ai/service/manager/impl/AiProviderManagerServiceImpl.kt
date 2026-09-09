package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerCreateAiProviderDTO
import com.lovelycatv.crystalframework.ai.controller.manager.provider.dto.ManagerUpdateAiProviderDTO
import com.lovelycatv.crystalframework.ai.entity.AiProviderEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelRepository
import com.lovelycatv.crystalframework.ai.repository.AiProviderRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiProviderManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AiProviderManagerServiceImpl(
    private val repository: AiProviderRepository,
    private val modelRepository: AiModelRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiProviderManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiProviderEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiProviderEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiProviderEntity> = AiProviderEntity::class

    override fun getRepository(): AiProviderRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiProviderDTO): AiProviderEntity {
        repository.findByKey(dto.key).awaitFirstOrNull()?.let {
            throw BusinessException("AI provider key '${dto.key}' is already taken")
        }
        val entity = AiProviderEntity(
            id = snowIdGenerator.nextId(),
            name = dto.name,
            key = dto.key,
            description = dto.description,
            protocolType = dto.protocolType,
            baseUrl = dto.baseUrl,
            apiKey = dto.apiKey,
            chatCompletionsPath = dto.chatCompletionsPath,
            embeddingPath = dto.embeddingPath,
            requestConfig = dto.requestConfig,
            responseConfig = dto.responseConfig,
            enabled = dto.enabled,
            sort = dto.sort,
        )
        entity.getRealProtocolType()
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create AI provider")
        }
    }

    override suspend fun batchDelete(ids: List<Long>) {
        if (ids.any { modelRepository.findAllByProviderId(it).hasElements().awaitFirstOrNull() == true }) {
            throw BusinessException("Cannot delete AI provider referenced by a model")
        }
        super.batchDelete(ids)
    }
    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAiProviderDTO,
        original: AiProviderEntity,
    ): AiProviderEntity {
        if (dto.key != null && dto.key != original.key) {
            repository.findByKey(dto.key).awaitFirstOrNull()?.let {
                throw BusinessException("AI provider key '${dto.key}' is already taken")
            }
        }
        return original.apply {
            dto.name?.let { name = it }
            dto.key?.let { key = it }
            dto.description?.let { description = it }
            dto.protocolType?.let {
                AiProviderEntity(protocolType = it).getRealProtocolType()
                protocolType = it
            }
            dto.baseUrl?.let { baseUrl = it }
            dto.apiKey?.let { apiKey = it }
            dto.chatCompletionsPath?.let { chatCompletionsPath = it }
            dto.embeddingPath?.let { embeddingPath = it }
            dto.requestConfig?.let { requestConfig = it }
            dto.responseConfig?.let { responseConfig = it }
            dto.enabled?.let { enabled = it }
            dto.sort?.let { sort = it }
        }
    }
}
