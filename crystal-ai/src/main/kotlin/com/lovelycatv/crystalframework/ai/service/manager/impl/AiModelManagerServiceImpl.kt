package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerCreateAiModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.model.dto.ManagerUpdateAiModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiModelRepository
import com.lovelycatv.crystalframework.ai.repository.AiProviderRepository
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiModelManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class AiModelManagerServiceImpl(
    private val repository: AiModelRepository,
    private val providerRepository: AiProviderRepository,
    private val userGroupModelRepository: AiUserGroupModelRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiModelManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiModelEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiModelEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiModelEntity> = AiModelEntity::class

    override fun getRepository(): AiModelRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiModelDTO): AiModelEntity {
        providerRepository.findById(dto.providerId).awaitFirstOrNull()
            ?: throw BusinessException("AI provider ${dto.providerId} not found")
        repository.findByProviderIdAndKey(dto.providerId, dto.key).awaitFirstOrNull()?.let {
            throw BusinessException("AI model key '${dto.key}' is already taken for provider ${dto.providerId}")
        }
        val entity = AiModelEntity(
            id = snowIdGenerator.nextId(),
            providerId = dto.providerId,
            key = dto.key,
            modelName = dto.modelName,
            displayName = dto.displayName,
            description = dto.description,
            capabilities = dto.capabilities,
            contextWindowTokens = dto.contextWindowTokens,
            maxOutputTokens = dto.maxOutputTokens,
            inputPricePerMillion = dto.inputPricePerMillion,
            outputPricePerMillion = dto.outputPricePerMillion,
            cacheReadPricePerMillion = dto.cacheReadPricePerMillion,
            cacheWritePricePerMillion = dto.cacheWritePricePerMillion,
            currency = dto.currency,
            requestConfig = dto.requestConfig,
            enabled = dto.enabled,
            sort = dto.sort,
        )
        entity.getRealCapabilities()
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create AI model")
        }
    }

    override suspend fun batchDelete(ids: List<Long>) {
        if (ids.any { userGroupModelRepository.findAllByModelId(it).hasElements().awaitSingle() }) {
            throw BusinessException("Cannot delete AI model referenced by a user group")
        }
        super.batchDelete(ids)
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAiModelDTO,
        original: AiModelEntity,
    ): AiModelEntity {
        val providerId = dto.providerId ?: original.providerId
        providerRepository.findById(providerId).awaitFirstOrNull()
            ?: throw BusinessException("AI provider $providerId not found")
        val key = dto.key ?: original.key
        if (providerId != original.providerId || key != original.key) {
            repository.findByProviderIdAndKey(providerId, key).awaitFirstOrNull()?.let {
                if (it.id != original.id) throw BusinessException("AI model key '$key' is already taken for provider $providerId")
            }
        }
        dto.capabilities?.let {
            AiModelEntity(capabilities = it).getRealCapabilities()
        }
        return original.apply {
            this.providerId = providerId
            this.key = key
            dto.modelName?.let { modelName = it }
            dto.displayName?.let { displayName = it }
            dto.description?.let { description = it }
            dto.capabilities?.let { capabilities = it }
            dto.contextWindowTokens?.let { contextWindowTokens = it }
            dto.maxOutputTokens?.let { maxOutputTokens = it }
            dto.inputPricePerMillion?.let { inputPricePerMillion = it }
            dto.outputPricePerMillion?.let { outputPricePerMillion = it }
            dto.cacheReadPricePerMillion?.let { cacheReadPricePerMillion = it }
            dto.cacheWritePricePerMillion?.let { cacheWritePricePerMillion = it }
            dto.currency?.let { currency = it }
            dto.requestConfig?.let { requestConfig = it }
            dto.enabled?.let { enabled = it }
            dto.sort?.let { sort = it }
        }
    }
}
