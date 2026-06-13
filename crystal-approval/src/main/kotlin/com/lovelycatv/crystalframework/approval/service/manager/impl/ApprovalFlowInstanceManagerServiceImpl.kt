package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowInstanceManagerService
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
class ApprovalFlowInstanceManagerServiceImpl(
    private val approvalFlowInstanceRepository: ApprovalFlowInstanceRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowInstanceManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowInstanceEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowInstanceEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowInstanceEntity> = ApprovalFlowInstanceEntity::class

    override fun getRepository(): ApprovalFlowInstanceRepository = approvalFlowInstanceRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowInstanceDTO): ApprovalFlowInstanceEntity {
        return getRepository().save(
            ApprovalFlowInstanceEntity(
                id = snowIdGenerator.nextId(),
                scope = dto.scope,
                scopeId = dto.scopeId,
                definitionId = dto.definitionId,
                definitionVersion = dto.definitionVersion,
                initiatorId = dto.initiatorId,
                formData = dto.formData,
                currentNodeId = dto.currentNodeId,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow instance")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowInstanceDTO,
        original: ApprovalFlowInstanceEntity
    ): ApprovalFlowInstanceEntity {
        return original.apply {
            if (dto.status != null) this.status = dto.status!!
            if (dto.currentNodeId != null) this.currentNodeId = dto.currentNodeId!!
            if (dto.formData != null) this.formData = dto.formData
        }
    }
}
