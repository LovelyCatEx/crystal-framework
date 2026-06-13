package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface ApprovalFlowTaskManagerService : CachedBaseManagerService<
        ApprovalFlowTaskRepository,
        ApprovalFlowTaskEntity,
        ManagerCreateApprovalFlowTaskDTO,
        BaseManagerReadDTO,
        ManagerUpdateApprovalFlowTaskDTO,
        BaseManagerDeleteDTO
>
