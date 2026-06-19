package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface ApprovalFlowNodeRepository : BaseRepository<ApprovalFlowNodeEntity> {

    fun findByDefinitionIdAndDefinitionVersion(definitionId: Long, definitionVersion: Int): Flux<ApprovalFlowNodeEntity>
}
