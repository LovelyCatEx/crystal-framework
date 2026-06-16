package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerDeleteApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowGraphDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowDefinitionRepository
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService

interface ApprovalFlowDefinitionManagerService : BaseScopedManagerService<
        ApprovalFlowDefinitionRepository,
        ApprovalFlowDefinitionEntity,
        ManagerCreateApprovalFlowDefinitionDTO,
        ManagerReadApprovalFlowDefinitionDTO,
        ManagerUpdateApprovalFlowDefinitionDTO,
        ManagerDeleteApprovalFlowDefinitionDTO
> {
    suspend fun updateGraph(dto: ManagerUpdateApprovalFlowGraphDTO): List<String>
}
