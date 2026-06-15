package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowTokenRepository : BaseRepository<ApprovalFlowTokenEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTokenEntity>

    fun findByInstanceIdAndStatus(instanceId: Long, status: Int): Flow<ApprovalFlowTokenEntity>

    fun findByForkNodeIdAndCurrentNodeIdAndStatus(
        forkNodeId: Long,
        currentNodeId: Long,
        status: Int
    ): Flow<ApprovalFlowTokenEntity>
}
