package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.vo.ApprovalFlowInstanceDetailsVO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService

interface ApprovalFlowInstanceManagerService : BaseScopedManagerService<
        ApprovalFlowInstanceRepository,
        ApprovalFlowInstanceEntity,
        ManagerCreateApprovalFlowInstanceDTO,
        ManagerReadApprovalFlowInstanceDTO,
        ManagerUpdateApprovalFlowInstanceDTO,
        BaseManagerDeleteDTO
> {

    /**
     * Assemble the full details view of an instance: the definition version the instance is
     * running against, its nodes and edges, per-node status aggregated from tasks, and every
     * record filed against the instance. The endpoint that calls this is expected to enforce
     * caller-side access control (initiator / participant / read-all admin) before invoking.
     */
    suspend fun getInstanceDetails(instance: ApprovalFlowInstanceEntity): ApprovalFlowInstanceDetailsVO

    /**
     * Check whether the given user has at least one task assigned to them on the given instance.
     * Used by /detailsById to grant participants read access even without read-all authority.
     */
    suspend fun isAssigneeOfInstance(instanceId: Long, assigneeId: Long): Boolean
}
