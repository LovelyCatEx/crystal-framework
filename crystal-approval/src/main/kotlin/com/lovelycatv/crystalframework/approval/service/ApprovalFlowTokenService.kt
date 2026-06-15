package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTokenRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowTokenService : CachedBaseService<ApprovalFlowTokenRepository, ApprovalFlowTokenEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTokenEntity>

    fun findByInstanceIdAndStatus(instanceId: Long, status: Int): Flow<ApprovalFlowTokenEntity>

    fun findWaitingAtJoin(forkNodeId: Long, joinNodeId: Long): Flow<ApprovalFlowTokenEntity>
}
