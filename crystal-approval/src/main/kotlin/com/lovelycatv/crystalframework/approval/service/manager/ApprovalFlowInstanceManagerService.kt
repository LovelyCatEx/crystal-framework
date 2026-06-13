package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface ApprovalFlowInstanceManagerService : CachedBaseManagerService<
        ApprovalFlowInstanceRepository,
        ApprovalFlowInstanceEntity,
        ManagerCreateApprovalFlowInstanceDTO,
        BaseManagerReadDTO,
        ManagerUpdateApprovalFlowInstanceDTO,
        BaseManagerDeleteDTO
>
