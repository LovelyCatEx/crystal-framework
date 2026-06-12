package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowEdgeService : CachedBaseService<ApprovalFlowEdgeRepository, ApprovalFlowEdgeEntity> {

    fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowEdgeEntity>

    fun findBySourceNodeId(sourceNodeId: Long): Flow<ApprovalFlowEdgeEntity>
}
