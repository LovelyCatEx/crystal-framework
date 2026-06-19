package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowNodeService : CachedBaseService<ApprovalFlowNodeRepository, ApprovalFlowNodeEntity> {

    fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowNodeEntity>
}
