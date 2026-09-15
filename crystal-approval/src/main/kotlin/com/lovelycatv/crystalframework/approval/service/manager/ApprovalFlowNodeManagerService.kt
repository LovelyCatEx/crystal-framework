/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowNodeDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowNodeDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface ApprovalFlowNodeManagerService : CachedBaseManagerService<
        ApprovalFlowNodeRepository,
        ApprovalFlowNodeEntity,
        ManagerCreateApprovalFlowNodeDTO,
        BaseManagerReadDTO,
        ManagerUpdateApprovalFlowNodeDTO,
        BaseManagerDeleteDTO
> {
    suspend fun getNodesByDefinitionsIdAndVersion(id: Long, currentVersion: Int): List<ApprovalFlowNodeEntity>
}
