package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux

interface ApprovalFlowTaskRepository : BaseRepository<ApprovalFlowTaskEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTaskEntity>

    fun findByInstanceIdAndNodeId(instanceId: Long, nodeId: Long): Flow<ApprovalFlowTaskEntity>

    fun findByAssigneeIdAndStatus(assigneeId: Long, status: Int): Flow<ApprovalFlowTaskEntity>

    fun findAllByScopeId(scopeId: Long): Flux<ApprovalFlowTaskEntity>
}
