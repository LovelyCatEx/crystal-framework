package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowInstanceDetailsVO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowRecordVO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalNodeStateVO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowRecordRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowInstanceManagerService
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowTaskStatus
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class ApprovalFlowInstanceManagerServiceImpl(
    private val approvalFlowInstanceRepository: ApprovalFlowInstanceRepository,
    private val approvalFlowNodeRepository: ApprovalFlowNodeRepository,
    private val approvalFlowEdgeRepository: ApprovalFlowEdgeRepository,
    private val approvalFlowTaskRepository: ApprovalFlowTaskRepository,
    private val approvalFlowRecordRepository: ApprovalFlowRecordRepository,
    private val approvalFlowDefinitionManagerService: ApprovalFlowDefinitionManagerService,
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
                latestNodeId = dto.latestNodeId,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow instance")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowInstanceDTO,
        original: ApprovalFlowInstanceEntity
    ): ApprovalFlowInstanceEntity {
        return original.apply {
            if (dto.status != null) this.status = dto.status!!
            if (dto.latestNodeId != null) this.latestNodeId = dto.latestNodeId!!
            if (dto.formData != null) this.formData = dto.formData
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<ApprovalFlowInstanceEntity> {
        return approvalFlowInstanceRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }

    override suspend fun getInstanceDetails(instance: ApprovalFlowInstanceEntity): ApprovalFlowInstanceDetailsVO {
        val definition = approvalFlowDefinitionManagerService.getByIdOrNull(instance.definitionId)
            ?: throw BusinessException("Definition ${instance.definitionId} not found")

        val nodes = approvalFlowNodeRepository
            .findByDefinitionIdAndDefinitionVersion(instance.definitionId, instance.definitionVersion)
            .awaitListWithTimeout()
        val edges = approvalFlowEdgeRepository
            .findByDefinitionIdAndDefinitionVersion(instance.definitionId, instance.definitionVersion)
            .awaitListWithTimeout()

        val tasks = approvalFlowTaskRepository.findByInstanceId(instance.id).toList()
        val nodeStates = tasks.groupBy { it.nodeId }.mapValues { (_, group) ->
            ApprovalNodeStateVO(
                status = aggregateNodeStatus(group.map { it.status }),
                taskIds = group.map { it.id.toString() },
            )
        }.mapKeys { it.key.toString() }

        val records = approvalFlowRecordRepository.findByInstanceId(instance.id).toList()
            .map(ApprovalFlowRecordVO::from)

        return ApprovalFlowInstanceDetailsVO(
            instance = instance,
            definition = definition,
            nodes = nodes,
            edges = edges,
            nodeStates = nodeStates,
            records = records,
        )
    }

    override suspend fun isAssigneeOfInstance(instanceId: Long, assigneeId: Long): Boolean {
        return approvalFlowTaskRepository.existsByInstanceIdAndAssigneeId(instanceId, assigneeId)
    }

    private fun aggregateNodeStatus(taskStatuses: List<Int>): Int {
        if (taskStatuses.any { it == ApprovalFlowTaskStatus.REJECTED.typeId }) {
            return ApprovalFlowTaskStatus.REJECTED.typeId
        }
        if (taskStatuses.isNotEmpty() && taskStatuses.all { it == ApprovalFlowTaskStatus.APPROVED.typeId }) {
            return ApprovalFlowTaskStatus.APPROVED.typeId
        }
        return ApprovalFlowTaskStatus.PENDING.typeId
    }
}
