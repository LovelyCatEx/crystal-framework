package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface ApprovalFlowDefinitionRepository : BaseRepository<ApprovalFlowDefinitionEntity> {
    fun findAllByScopeId(scopeId: Long): Flux<ApprovalFlowDefinitionEntity>
}
