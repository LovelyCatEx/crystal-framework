package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerCreateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto.ManagerUpdateAiUserGroupDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupMemberRepository
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupManagerService
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
class AiUserGroupManagerServiceImpl(
    private val repository: AiUserGroupRepository,
    private val memberRepository: AiUserGroupMemberRepository,
    private val modelRepository: AiUserGroupModelRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiUserGroupManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiUserGroupEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiUserGroupEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiUserGroupEntity> = AiUserGroupEntity::class

    override fun getRepository(): AiUserGroupRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiUserGroupDTO): AiUserGroupEntity {
        repository.findByKey(dto.key).awaitFirstOrNull()?.let {
            throw BusinessException("AI user group key '${dto.key}' is already taken")
        }
        val entity = AiUserGroupEntity(
            id = snowIdGenerator.nextId(),
            name = dto.name,
            key = dto.key,
            description = dto.description,
            billingMultiplier = dto.billingMultiplier,
            enabled = dto.enabled,
            isDefault = dto.isDefault,
            sort = dto.sort,
        )
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create AI user group")
        }
    }

    override suspend fun batchDelete(ids: List<Long>) {
        if (ids.any { id ->
                memberRepository.findAllByUserGroupId(id).hasElements().awaitFirstOrNull() == true ||
                    modelRepository.findAllByUserGroupId(id).hasElements().awaitFirstOrNull() == true
            }) {
            throw BusinessException("Cannot delete AI user group with members or models")
        }
        super.batchDelete(ids)
    }
    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAiUserGroupDTO,
        original: AiUserGroupEntity,
    ): AiUserGroupEntity {
        if (dto.key != null && dto.key != original.key) {
            repository.findByKey(dto.key).awaitFirstOrNull()?.let {
                throw BusinessException("AI user group key '${dto.key}' is already taken")
            }
        }
        return original.apply {
            dto.name?.let { name = it }
            dto.key?.let { key = it }
            dto.description?.let { description = it }
            dto.billingMultiplier?.let { billingMultiplier = it }
            dto.enabled?.let { enabled = it }
            dto.isDefault?.let { isDefault = it }
            dto.sort?.let { sort = it }
        }
    }
}
