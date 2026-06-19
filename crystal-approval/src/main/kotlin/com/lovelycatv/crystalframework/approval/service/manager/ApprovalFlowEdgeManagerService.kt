package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowEdgeDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowEdgeDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface ApprovalFlowEdgeManagerService : CachedBaseManagerService<
        ApprovalFlowEdgeRepository,
        ApprovalFlowEdgeEntity,
        ManagerCreateApprovalFlowEdgeDTO,
        BaseManagerReadDTO,
        ManagerUpdateApprovalFlowEdgeDTO,
        BaseManagerDeleteDTO
> {
    suspend fun getEdgesByDefinitionsIdAndVersion(id: Long, currentVersion: Int): List<ApprovalFlowEdgeEntity>
}
