package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Service
class ApprovalFlowDefinitionManagerServiceImpl(
    private val approvalFlowDefinitionRepository: ApprovalFlowDefinitionRepository,
    private val approvalFlowNodeRepository: ApprovalFlowNodeRepository,
    private val approvalFlowEdgeRepository: ApprovalFlowEdgeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : ApprovalFlowDefinitionManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowDefinitionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowDefinitionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowDefinitionEntity> = ApprovalFlowDefinitionEntity::class

    override fun getRepository(): ApprovalFlowDefinitionRepository = approvalFlowDefinitionRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowDefinitionDTO): ApprovalFlowDefinitionEntity {
        return getRepository().save(
            ApprovalFlowDefinitionEntity(
                id = snowIdGenerator.nextId(),
                scope = dto.scope,
                scopeId = dto.scopeId,
                name = dto.name,
                description = dto.description,
                formSchema = dto.formSchema,
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create approval flow definition")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateApprovalFlowDefinitionDTO,
        original: ApprovalFlowDefinitionEntity
    ): ApprovalFlowDefinitionEntity {
        return original.apply {
            if (dto.name != null) this.name = dto.name!!
            if (dto.description != null) this.description = dto.description
            if (dto.status != null) this.status = dto.status!!
            if (dto.formSchema != null) this.formSchema = dto.formSchema
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun updateGraph(dto: ManagerUpdateApprovalFlowGraphDTO) {
        val definition = getByIdOrNull(dto.definitionId)
            ?: throw BusinessException("Definition not found")

        val newVersion = definition.currentVersion + 1

        // Save nodes first, build nodeKey -> id mapping
        val nodeKeyToIdMap = mutableMapOf<String, Long>()
        for (nodeDTO in dto.nodes) {
            val nodeId = snowIdGenerator.nextId()
            approvalFlowNodeRepository.save(
                ApprovalFlowNodeEntity(
                    id = nodeId,
                    definitionId = definition.id,
                    definitionVersion = newVersion,
                    nodeKey = nodeDTO.nodeKey,
                    type = nodeDTO.type,
                    name = nodeDTO.name,
                    config = nodeDTO.config,
                    formSchema = nodeDTO.formSchema,
                    positionX = nodeDTO.positionX,
                    positionY = nodeDTO.positionY,
                ) newEntity true
            ).awaitFirstOrNull() ?: throw BusinessException("Could not save node: ${nodeDTO.nodeKey}")
            nodeKeyToIdMap[nodeDTO.nodeKey] = nodeId
        }

        // Save edges, resolve nodeKey to actual node id
        for (edgeDTO in dto.edges) {
            val sourceNodeId = nodeKeyToIdMap[edgeDTO.sourceNodeKey]
                ?: throw BusinessException("Unknown source node key: ${edgeDTO.sourceNodeKey}")
            val targetNodeId = nodeKeyToIdMap[edgeDTO.targetNodeKey]
                ?: throw BusinessException("Unknown target node key: ${edgeDTO.targetNodeKey}")

            approvalFlowEdgeRepository.save(
                ApprovalFlowEdgeEntity(
                    id = snowIdGenerator.nextId(),
                    definitionId = definition.id,
                    definitionVersion = newVersion,
                    sourceNodeId = sourceNodeId,
                    targetNodeId = targetNodeId,
                ) newEntity true
            ).awaitFirstOrNull() ?: throw BusinessException("Could not save edge")
        }

        // Update definition version
        withUpdateEntityContext(definition.id) {
            getRepository().save(
                definition.apply {
                    this.currentVersion = newVersion
                    this.onUpdate()
                } newEntity false
            ).awaitFirstOrNull() ?: throw BusinessException("Could not update definition version")
        }
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<ApprovalFlowDefinitionEntity> {
        return approvalFlowDefinitionRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }
}
