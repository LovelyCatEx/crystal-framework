package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowRecordRepository : BaseRepository<ApprovalFlowRecordEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowRecordEntity>
}
