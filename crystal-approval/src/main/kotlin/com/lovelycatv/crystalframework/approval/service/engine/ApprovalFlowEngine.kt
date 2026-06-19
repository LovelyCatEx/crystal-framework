package com.lovelycatv.crystalframework.approval.service.engine

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.approval.types.ApprovalFlowScope

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

    suspend fun advanceToken(token: ApprovalFlowTokenEntity, instance: ApprovalFlowInstanceEntity)

    suspend fun resolveApprovers(
        node: ApprovalFlowNodeEntity,
        instance: ApprovalFlowInstanceEntity
    ): List<Long>
}
