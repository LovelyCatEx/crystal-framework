package com.lovelycatv.crystalframework.ai.service.manager.impl

import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerCreateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto.ManagerUpdateAiUserGroupModelDTO
import com.lovelycatv.crystalframework.ai.entity.AiUserGroupModelEntity
import com.lovelycatv.crystalframework.ai.repository.AiUserGroupModelRepository
import com.lovelycatv.crystalframework.ai.service.manager.AiUserGroupModelManagerService
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
class AiUserGroupModelManagerServiceImpl(
    private val repository: AiUserGroupModelRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : AiUserGroupModelManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, AiUserGroupModelEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<AiUserGroupModelEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<AiUserGroupModelEntity> = AiUserGroupModelEntity::class

    override fun getRepository(): AiUserGroupModelRepository = repository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateAiUserGroupModelDTO): AiUserGroupModelEntity {
        val entity = AiUserGroupModelEntity(
            id = snowIdGenerator.nextId(),
            userGroupId = dto.userGroupId,
            modelId = dto.modelId,
        )
        return withInvalidateEntityCacheContext(entity.id) {
            repository.save(entity newEntity true).awaitFirstOrNull()
                ?: throw BusinessException("Could not create AI user group model association")
        }
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateAiUserGroupModelDTO,
        original: AiUserGroupModelEntity,
    ): AiUserGroupModelEntity {
        return original.apply {
            dto.userGroupId?.let { userGroupId = it }
            dto.modelId?.let { modelId = it }
        }
    }
}
