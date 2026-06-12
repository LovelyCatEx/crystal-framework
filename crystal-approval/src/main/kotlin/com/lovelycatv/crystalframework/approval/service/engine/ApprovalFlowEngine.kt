package com.lovelycatv.crystalframework.approval.service.engine

import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity

interface ApprovalFlowEngine {

    suspend fun startFlow(
        definitionId: Long,
        initiatorId: Long,
        scope: ApprovalFlowScope,
        scopeId: Long,
        formData: String
    ): ApprovalFlowInstanceEntity

    suspend fun handleTask(
        taskId: Long,
        operatorId: Long,
        approved: Boolean,
        comment: String?,
        formData: String?
    )

    suspend fun advanceToNextNode(instance: ApprovalFlowInstanceEntity)

    suspend fun resolveApprovers(
        node: ApprovalFlowNodeEntity,
        instance: ApprovalFlowInstanceEntity
    ): List<Long>
}
