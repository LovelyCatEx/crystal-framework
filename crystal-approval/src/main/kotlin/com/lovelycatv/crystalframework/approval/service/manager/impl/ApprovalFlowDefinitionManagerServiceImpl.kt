package com.lovelycatv.crystalframework.approval.service.manager.impl

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.constants.ApprovalNodeConfigConstants
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.approval.service.ApprovalDictResolver
import com.lovelycatv.crystalframework.approval.service.ApprovalFlowGraphValidator
import com.lovelycatv.crystalframework.approval.service.ApprovalFormSchemaValidator
import com.lovelycatv.crystalframework.approval.service.manager.ApprovalFlowDefinitionManagerService
import com.lovelycatv.crystalframework.approval.types.ApprovalFormSchema
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowApproverStrategy
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowNodeType
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.approval.types.ApprovalNodeConfig
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.service.TenantMemberService
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.parseObject
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
    private val approvalDictResolver: ApprovalDictResolver,
    private val tenantMemberService: TenantMemberService,
) : ApprovalFlowDefinitionManagerService {
    override val cacheStore: ReactiveExpiringKVStore<String, ApprovalFlowDefinitionEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<ApprovalFlowDefinitionEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<ApprovalFlowDefinitionEntity> = ApprovalFlowDefinitionEntity::class

    override fun getRepository(): ApprovalFlowDefinitionRepository = approvalFlowDefinitionRepository

    override fun getEntityTemplate(): R2dbcEntityTemplate = r2dbcEntityTemplate

    override suspend fun create(dto: ManagerCreateApprovalFlowDefinitionDTO): ApprovalFlowDefinitionEntity {
        validateFormSchemaOrThrow(dto.formSchema, resolveScopeOrThrow(dto.scope), dto.scopeId)
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
        // Empty string is treated as "clear schema" — normalize before the validator sees it so
        // "clear" and "leave alone" don't both surface as validation-worthy inputs.
        if (dto.formSchema != null) {
            val normalized = if (dto.formSchema!!.isBlank()) null else dto.formSchema
            // DICT scope legality binds to the definition's own scope — read from the persisted
            // original, since update DTOs do not resend scope (scope is immutable post-create).
            validateFormSchemaOrThrow(normalized, resolveScopeOrThrow(original.scope), original.scopeId)
        }
        return original.apply {
            if (dto.name != null) this.name = dto.name!!
            if (dto.description != null) this.description = dto.description
            if (dto.status != null) this.status = dto.status!!
            if (dto.formSchema != null) this.formSchema = if (dto.formSchema!!.isBlank()) null else dto.formSchema
        }
    }

    private suspend fun validateFormSchemaOrThrow(schemaJson: String?, scope: ResourceScope, scopeId: Long) {
        val errors = ApprovalFormSchemaValidator.validateSchema(schemaJson).toMutableList()
        // DICT cross-scope legality + dict-type existence needs DB access, so it lives in the
        // resolver rather than the pure validator. Only run it once the schema is structurally sane
        // and actually parseable.
        if (errors.isEmpty() && !schemaJson.isNullOrBlank()) {
            val schema = runCatching { schemaJson.parseObject<ApprovalFormSchema>() }.getOrNull()
            if (schema != null) {
                errors += approvalDictResolver.validateDictBindings(schema, scope, scopeId)
            }
        }
        if (errors.isNotEmpty()) {
            throw BusinessException("Invalid form schema: ${errors.joinToString("; ")}")
        }
    }

    private fun resolveScopeOrThrow(scopeTypeId: Int): ResourceScope =
        ResourceScope.getById(scopeTypeId)
            ?: throw BusinessException("Unknown scope type: $scopeTypeId")

    /**
     * For TENANT-scoped flows, every SPECIFIED_USER approver referenced by an APPROVAL node must be a
     * tenant_member of this flow's own tenant. Without this check a tenant admin could inject another
     * tenant's members as approvers (H-4). SYSTEM flows carry raw user ids that are not tenant-bound,
     * so they are left to the engine's own resolution.
     */
    private suspend fun assertSpecifiedApproversBelongToScope(
        dto: ManagerUpdateApprovalFlowGraphDTO,
        definition: ApprovalFlowDefinitionEntity
    ) {
        if (definition.getRealScope() != ApprovalFlowScope.TENANT) {
            return
        }
        val tenantId = definition.scopeId
        for (nodeDTO in dto.nodes) {
            if (nodeDTO.type != ApprovalFlowNodeType.APPROVAL.typeId) {
                continue
            }
            val config = runCatching { nodeDTO.config?.parseObject<ApprovalNodeConfig>() }.getOrNull()
                ?: continue
            if (ApprovalFlowApproverStrategy.getById(config.strategy) != ApprovalFlowApproverStrategy.SPECIFIED_USER) {
                continue
            }
            @Suppress("UNCHECKED_CAST")
            val memberIds = config.strategyParams[ApprovalNodeConfigConstants.STRATEGY_PARAM_MEMBER_IDS] as? List<String>
                ?: emptyList()
            for (rawMemberId in memberIds) {
                val memberId = rawMemberId.toLongOrNull()
                    ?: throw BusinessException("APPROVAL node '${nodeDTO.nodeKey}' has an invalid member id: $rawMemberId")
                val member = tenantMemberService.getByIdOrNull(memberId)
                if (member == null || member.tenantId != tenantId) {
                    throw BusinessException("APPROVAL node '${nodeDTO.nodeKey}' references a member not belonging to this tenant: $memberId")
                }
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun updateGraph(dto: ManagerUpdateApprovalFlowGraphDTO): List<String> {
        // Fetch definition first so we can feed its formSchema into rules 37-40. If it doesn't
        // exist we surface that as a definitional error rather than mixing it with graph rules.
        val definition = getByIdOrNull(dto.definitionId)
            ?: throw BusinessException("Definition not found")

        val validationErrors = ApprovalFlowGraphValidator.validate(dto, definition.formSchema)
        if (validationErrors.isNotEmpty()) {
            return validationErrors
        }

        // Approver tenant-ownership check needs DB access, so it lives here rather than in the pure
        // ApprovalFlowGraphValidator — mirroring how DICT cross-scope legality lives in the resolver.
        // Guards against injecting other tenants' members as approvers (see H-4).
        assertSpecifiedApproversBelongToScope(dto, definition)

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

        return emptyList()
    }

    override suspend fun findAllByScopeId(scopeId: Long): List<ApprovalFlowDefinitionEntity> {
        return approvalFlowDefinitionRepository.findAllByScopeId(scopeId).awaitListWithTimeout()
    }
}
