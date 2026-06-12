package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowTaskService : CachedBaseService<ApprovalFlowTaskRepository, ApprovalFlowTaskEntity> {

    fun findByInstanceIdAndNodeId(instanceId: Long, nodeId: Long): Flow<ApprovalFlowTaskEntity>

    fun findPendingByAssigneeId(assigneeId: Long): Flow<ApprovalFlowTaskEntity>
}
