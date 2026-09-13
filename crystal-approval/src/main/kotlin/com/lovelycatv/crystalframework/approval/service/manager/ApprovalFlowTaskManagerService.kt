/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTaskRepository
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.service.BaseScopedManagerService

interface ApprovalFlowTaskManagerService : BaseScopedManagerService<
        ApprovalFlowTaskRepository,
        ApprovalFlowTaskEntity,
        ManagerCreateApprovalFlowTaskDTO,
        ManagerReadApprovalFlowTaskDTO,
        ManagerUpdateApprovalFlowTaskDTO,
        BaseManagerDeleteDTO
>
