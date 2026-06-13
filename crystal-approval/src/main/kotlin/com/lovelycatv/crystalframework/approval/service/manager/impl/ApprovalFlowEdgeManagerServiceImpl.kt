package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowEdgeDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowEdgeDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowEdgeManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowEdgeManagerServiceImpl(
    private val approvalFlowEdgeRepository: ApprovalFlowEdgeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowEdgeManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowEdgeEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowEdgeEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowEdgeEntity> = ApprovalFlowEdgeEntity::class

    override fun getRepository(): ApprovalFlowEdgeRepository = approvalFlowEdgeRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowEdgeDTO): ApprovalFlowEdgeEntity {
        return getRepository().save(
            ApprovalFlowEdgeEntity(
                id = snowIdGenerator.nextId(),
                definitionId = dto.definitionId,
                definitionVersion = dto.definitionVersion,
                sourceNodeId = dto.sourceNodeId,
                targetNodeId = dto.targetNodeId,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow edge")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowEdgeDTO,
        original: ApprovalFlowEdgeEntity
    ): ApprovalFlowEdgeEntity {
        return original.apply {
            if (dto.sourceNodeId != null) this.sourceNodeId = dto.sourceNodeId!!
            if (dto.targetNodeId != null) this.targetNodeId = dto.targetNodeId!!
        }
    }

    override suspend fun getEdgesByDefinitionsIdAndVersion(
        id: Long,
        currentVersion: Int
    ): List<ApprovalFlowEdgeEntity> {
        return getRepository()
            .findByDefinitionIdAndDefinitionVersion(id, currentVersion)
            .awaitListWithTimeout()
    }
}
