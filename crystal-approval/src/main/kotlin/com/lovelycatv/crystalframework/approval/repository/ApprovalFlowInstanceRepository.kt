package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowInstanceRepository : BaseRepository<ApprovalFlowInstanceEntity> {

    fun findByScopeAndScopeId(scope: Int, scopeId: Long): Flow<ApprovalFlowInstanceEntity>

    fun findByInitiatorId(initiatorId: Long): Flow<ApprovalFlowInstanceEntity>
}
