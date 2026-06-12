package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowEdgeRepository : BaseRepository<ApprovalFlowEdgeEntity> {

    fun findByDefinitionIdAndDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowEdgeEntity>

    fun findBySourceNodeId(sourceNodeId: Long): Flow<ApprovalFlowEdgeEntity>
}
