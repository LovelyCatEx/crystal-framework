package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface ApprovalFlowEdgeRepository : BaseRepository<ApprovalFlowEdgeEntity> {

    fun findByDefinitionIdAndDefinitionVersion(definitionId: Long, definitionVersion: Int): Flux<ApprovalFlowEdgeEntity>

    fun findBySourceNodeId(sourceNodeId: Long): Flux<ApprovalFlowEdgeEntity>
}
