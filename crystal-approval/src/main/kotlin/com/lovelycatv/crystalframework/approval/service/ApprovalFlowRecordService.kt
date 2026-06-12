package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowRecordRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowRecordService : CachedBaseService<ApprovalFlowRecordRepository, ApprovalFlowRecordEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowRecordEntity>
}
