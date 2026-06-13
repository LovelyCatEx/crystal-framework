package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowRecordDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowRecordDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowRecordEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowRecordRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface ApprovalFlowRecordManagerService : CachedBaseManagerService<
        ApprovalFlowRecordRepository,
        ApprovalFlowRecordEntity,
        ManagerCreateApprovalFlowRecordDTO,
        BaseManagerReadDTO,
        ManagerUpdateApprovalFlowRecordDTO,
        BaseManagerDeleteDTO
>
