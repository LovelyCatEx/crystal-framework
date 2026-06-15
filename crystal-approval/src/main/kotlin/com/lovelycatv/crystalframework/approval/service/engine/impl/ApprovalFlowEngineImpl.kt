package com.lovelycatv.crystalframework.approval.service.engine.impl

import com.lovelycatv.crystalframework.approval.types.*
import com.lovelycatv.crystalframework.approval.entity.*
import com.lovelycatv.crystalframework.approval.service.*
import com.lovelycatv.crystalframework.approval.service.engine.ApprovalFlowEngine
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.parseObject
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApprovalFlowEngineImpl(
    private val definitionService: ApprovalFlowDefinitionService,
    private val nodeService: ApprovalFlowNodeService,
    private val edgeService: ApprovalFlowEdgeService,
    private val instanceService: ApprovalFlowInstanceService,
    private val taskService: ApprovalFlowTaskService,
    private val recordService: ApprovalFlowRecordService,
    private val tokenService: ApprovalFlowTokenService,
    private val snowIdGenerator: SnowIdGenerator
) : ApprovalFlowEngine {

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun startFlow(
        definitionId: Long,
        initiatorId: Long,
        scope: ApprovalFlowScope,
        scopeId: Long,
        formData: String
    ): ApprovalFlowInstanceEntity {
        val definition = definitionService.getByIdOrThrow(definitionId)

        if (definition.getRealStatus() != ApprovalFlowDefinitionStatus.PUBLISHED) {
            throw BusinessException("Flow definition is not published")
        }

        val version = definition.currentVersion
        val nodes = nodeService.findByDefinitionVersion(definitionId, version).toList()
        val startNode = nodes.firstOrNull { it.getRealType() == ApprovalFlowNodeType.START }
            ?: throw BusinessException("Flow definition has no START node")

        val edges = edgeService.findBySourceNodeId(startNode.id).toList()
        val firstTargetNodeId = edges.firstOrNull()?.targetNodeId
            ?: throw BusinessException("START node has no outgoing edge")

        val instance = ApprovalFlowInstanceEntity(
            id = snowIdGenerator.nextId(),
            scope = scope.typeId,
            scopeId = scopeId,
            definitionId = definitionId,
            definitionVersion = version,
            initiatorId = initiatorId,
            status = ApprovalFlowInstanceStatus.IN_PROGRESS.typeId,
            formData = formData,
            latestNodeId = firstTargetNodeId
        ).apply { newEntity() }

        instanceService.getRepository().save(instance).awaitFirst()

        val record = ApprovalFlowRecordEntity(
            id = snowIdGenerator.nextId(),
            scope = scope.typeId,
            scopeId = scopeId,
            instanceId = instance.id,
            nodeId = startNode.id,
            operatorId = initiatorId,
            action = ApprovalFlowRecordAction.INITIATE.typeId
        ).apply { newEntity() }
        recordService.getRepository().save(record).awaitFirst()

        val initialToken = ApprovalFlowTokenEntity(
            id = snowIdGenerator.nextId(),
            instanceId = instance.id,
            currentNodeId = firstTargetNodeId,
            status = ApprovalFlowTokenStatus.ACTIVE.typeId,
            forkNodeId = null
        ).apply { newEntity() }
        tokenService.getRepository().save(initialToken).awaitFirst()

        advanceToken(initialToken, instance)

        return instance
    }

    @Transactional(rollbackFor = [Exception::class])
    override suspend fun handleTask(
        taskId: Long,
        operatorId: Long,
        approved: Boolean,
        comment: String?,
        formData: String?
    ) {
        val task = taskService.getByIdOrThrow(taskId)

        if (task.getRealStatus() != ApprovalFlowTaskStatus.PENDING) {
            throw BusinessException("Task is not pending")
        }

        task.status = if (approved) ApprovalFlowTaskStatus.APPROVED.typeId else ApprovalFlowTaskStatus.REJECTED.typeId
        task.comment = comment
        task.formData = formData
        taskService.withUpdateEntityContext(task) {
            task.onUpdate()
            taskService.getRepository().save(task).awaitFirst()
        }

        val instance = instanceService.getByIdOrThrow(task.instanceId)
        val token = tokenService.getByIdOrThrow(task.tokenId)

        val record = ApprovalFlowRecordEntity(
            id = snowIdGenerator.nextId(),
            scope = instance.scope,
            scopeId = instance.scopeId,
            instanceId = instance.id,
            nodeId = task.nodeId,
            operatorId = operatorId,
            action = if (approved) ApprovalFlowRecordAction.APPROVE.typeId else ApprovalFlowRecordAction.REJECT.typeId,
            comment = comment
        ).apply { newEntity() }
        recordService.getRepository().save(record).awaitFirst()

        val node = nodeService.getByIdOrThrow(task.nodeId)
        val config = node.config?.parseObject<ApprovalNodeConfig>()
        val approveMode = config?.approveMode?.let { ApprovalFlowApproveMode.getById(it) } ?: ApprovalFlowApproveMode.AND

        val allTasks = taskService.findByInstanceIdAndNodeId(instance.id, node.id).toList()

        when (approveMode) {
            ApprovalFlowApproveMode.OR -> {
                if (approved) {
                    allTasks.filter { it.id != task.id && it.getRealStatus() == ApprovalFlowTaskStatus.PENDING }.forEach {
                        it.status = ApprovalFlowTaskStatus.SKIPPED.typeId
                        taskService.withUpdateEntityContext(it) {
                            it.onUpdate()
                            taskService.getRepository().save(it).awaitFirst()
                        }
                    }
                    moveToNext(token, instance)
                } else {
                    val hasPending = allTasks.any { it.id != task.id && it.getRealStatus() == ApprovalFlowTaskStatus.PENDING }
                    if (!hasPending) {
                        rejectInstance(instance)
                    }
                }
            }
            ApprovalFlowApproveMode.AND -> {
                if (!approved) {
                    rejectInstance(instance)
                } else {
                    val allApproved = allTasks.all { it.getRealStatus() == ApprovalFlowTaskStatus.APPROVED }
                    if (allApproved) {
                        moveToNext(token, instance)
                    }
                }
            }
        }
    }

    override suspend fun advanceToken(token: ApprovalFlowTokenEntity, instance: ApprovalFlowInstanceEntity) {
        val currentNode = nodeService.getByIdOrThrow(token.currentNodeId)

        when (currentNode.getRealType()) {
            ApprovalFlowNodeType.APPROVAL -> {
                val approvers = resolveApprovers(currentNode, instance)
                approvers.forEach { assigneeId ->
                    val task = ApprovalFlowTaskEntity(
                        id = snowIdGenerator.nextId(),
                        scope = instance.scope,
                        scopeId = instance.scopeId,
                        instanceId = instance.id,
                        nodeId = currentNode.id,
                        tokenId = token.id,
                        assigneeId = assigneeId,
                        status = ApprovalFlowTaskStatus.PENDING.typeId
                    ).apply { newEntity() }
                    taskService.getRepository().save(task).awaitFirst()
                }
            }
            ApprovalFlowNodeType.CONDITION -> {
                val config = currentNode.config?.parseObject<ConditionNodeConfig>()
                    ?: throw BusinessException("Condition node has no config")
                val formData = instance.formData?.parseObject<Map<String, Any?>>() ?: emptyMap()
                val targetNodeId = evaluateCondition(config, formData)
                moveToken(token, targetNodeId)
                updateLatestNodeId(instance, targetNodeId)
                advanceToken(token, instance)
            }
            ApprovalFlowNodeType.FORK -> {
                completeToken(token)
                val forkEdges = edgeService.findBySourceNodeId(currentNode.id).toList()
                forkEdges.forEach { edge ->
                    val childToken = ApprovalFlowTokenEntity(
                        id = snowIdGenerator.nextId(),
                        instanceId = instance.id,
                        currentNodeId = edge.targetNodeId,
                        status = ApprovalFlowTokenStatus.ACTIVE.typeId,
                        forkNodeId = currentNode.id
                    ).apply { newEntity() }
                    tokenService.getRepository().save(childToken).awaitFirst()
                    advanceToken(childToken, instance)
                }
            }
            ApprovalFlowNodeType.JOIN -> {
                token.status = ApprovalFlowTokenStatus.WAITING.typeId
                tokenService.withUpdateEntityContext(token) {
                    token.onUpdate()
                    tokenService.getRepository().save(token).awaitFirst()
                }
                tryMergeAtJoin(token, instance)
            }
            ApprovalFlowNodeType.END -> {
                completeToken(token)
                completeInstance(instance)
            }
            else -> {
                moveToNext(token, instance)
            }
        }
    }

    private suspend fun moveToNext(token: ApprovalFlowTokenEntity, instance: ApprovalFlowInstanceEntity) {
        val edges = edgeService.findBySourceNodeId(token.currentNodeId).toList()
        if (edges.isEmpty()) {
            completeToken(token)
            completeInstance(instance)
            return
        }
        val nextNodeId = edges.first().targetNodeId
        moveToken(token, nextNodeId)
        updateLatestNodeId(instance, nextNodeId)
        advanceToken(token, instance)
    }

    private suspend fun tryMergeAtJoin(arrivedToken: ApprovalFlowTokenEntity, instance: ApprovalFlowInstanceEntity) {
        val forkNodeId = arrivedToken.forkNodeId
            ?: throw BusinessException("Token at JOIN has no forkNodeId")

        val forkOutEdgeCount = edgeService.findBySourceNodeId(forkNodeId).toList().size
        val waitingTokens = tokenService.findWaitingAtJoin(forkNodeId, arrivedToken.currentNodeId).toList()

        if (waitingTokens.size < forkOutEdgeCount) {
            return
        }

        waitingTokens.forEach { t ->
            t.status = ApprovalFlowTokenStatus.COMPLETED.typeId
            tokenService.withUpdateEntityContext(t) {
                t.onUpdate()
                tokenService.getRepository().save(t).awaitFirst()
            }
        }

        val parentDerivedSource = findParentDerivedSource(forkNodeId, instance)

        val mergedToken = ApprovalFlowTokenEntity(
            id = snowIdGenerator.nextId(),
            instanceId = instance.id,
            currentNodeId = arrivedToken.currentNodeId,
            status = ApprovalFlowTokenStatus.ACTIVE.typeId,
            forkNodeId = parentDerivedSource
        ).apply { newEntity() }
        tokenService.getRepository().save(mergedToken).awaitFirst()

        moveToNext(mergedToken, instance)
    }

    private suspend fun findParentDerivedSource(forkNodeId: Long, instance: ApprovalFlowInstanceEntity): Long? {
        val completedTokenAtFork = tokenService.findByInstanceId(instance.id).toList()
            .firstOrNull { it.currentNodeId == forkNodeId && it.getRealStatus() == ApprovalFlowTokenStatus.COMPLETED }
        return completedTokenAtFork?.forkNodeId
    }

    override suspend fun resolveApprovers(
        node: ApprovalFlowNodeEntity,
        instance: ApprovalFlowInstanceEntity
    ): List<Long> {
        val config = node.config?.parseObject<ApprovalNodeConfig>()
            ?: throw BusinessException("Approval node has no config")

        return when (ApprovalFlowApproverStrategy.getById(config.strategy)) {
            ApprovalFlowApproverStrategy.SPECIFIED_USER -> {
                @Suppress("UNCHECKED_CAST")
                val userIds = config.strategyParams["userIds"] as? List<String> ?: emptyList()
                userIds.map { it.toLong() }
            }
            else -> {
                throw BusinessException("Approver strategy not yet implemented: ${config.strategy}")
            }
        }
    }

    private suspend fun moveToken(token: ApprovalFlowTokenEntity, nodeId: Long) {
        token.currentNodeId = nodeId
        tokenService.withUpdateEntityContext(token) {
            token.onUpdate()
            tokenService.getRepository().save(token).awaitFirst()
        }
    }

    private suspend fun completeToken(token: ApprovalFlowTokenEntity) {
        token.status = ApprovalFlowTokenStatus.COMPLETED.typeId
        tokenService.withUpdateEntityContext(token) {
            token.onUpdate()
            tokenService.getRepository().save(token).awaitFirst()
        }
    }

    private suspend fun completeInstance(instance: ApprovalFlowInstanceEntity) {
        instance.status = ApprovalFlowInstanceStatus.APPROVED.typeId
        instanceService.withUpdateEntityContext(instance) {
            instance.onUpdate()
            instanceService.getRepository().save(instance).awaitFirst()
        }
    }

    private suspend fun rejectInstance(instance: ApprovalFlowInstanceEntity) {
        instance.status = ApprovalFlowInstanceStatus.REJECTED.typeId
        instanceService.withUpdateEntityContext(instance) {
            instance.onUpdate()
            instanceService.getRepository().save(instance).awaitFirst()
        }
    }

    private suspend fun updateLatestNodeId(instance: ApprovalFlowInstanceEntity, nodeId: Long) {
        instance.latestNodeId = nodeId
        instanceService.withUpdateEntityContext(instance) {
            instance.onUpdate()
            instanceService.getRepository().save(instance).awaitFirst()
        }
    }

    private fun evaluateCondition(config: ConditionNodeConfig, formData: Map<String, Any?>): Long {
        for (route in config.routes) {
            if (evaluate(route.condition, formData)) {
                return route.targetNodeId
            }
        }
        throw BusinessException("No condition matched and no default route")
    }

    private fun evaluate(node: ConditionNode, formData: Map<String, Any?>): Boolean {
        return when (node) {
            is ConditionGroup -> when (node.logic) {
                ConditionLogic.AND -> node.children.all { evaluate(it, formData) }
                ConditionLogic.OR -> node.children.any { evaluate(it, formData) }
            }
            is ConditionLeaf -> evaluateLeaf(node, formData)
        }
    }

    private fun evaluateLeaf(leaf: ConditionLeaf, formData: Map<String, Any?>): Boolean {
        val fieldValue = formData[leaf.field] ?: return false
        return when (leaf.operator) {
            ConditionOperator.EQ -> fieldValue.toString() == leaf.value.toString()
            ConditionOperator.NE -> fieldValue.toString() != leaf.value.toString()
            ConditionOperator.GT -> compareValues(fieldValue, leaf.value!!) > 0
            ConditionOperator.GTE -> compareValues(fieldValue, leaf.value!!) >= 0
            ConditionOperator.LT -> compareValues(fieldValue, leaf.value!!) < 0
            ConditionOperator.LTE -> compareValues(fieldValue, leaf.value!!) <= 0
            ConditionOperator.CONTAINS -> fieldValue.toString().contains(leaf.value.toString())
            ConditionOperator.IN -> {
                val list = leaf.values ?: return false
                fieldValue.toString() in list.map { it.toString() }
            }
        }
    }

    private fun compareValues(a: Any, b: Any): Int {
        val numA = a.toString().toDoubleOrNull()
        val numB = b.toString().toDoubleOrNull()
        return if (numA != null && numB != null) {
            numA.compareTo(numB)
        } else {
            a.toString().compareTo(b.toString())
        }
    }
}