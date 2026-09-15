/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowInstanceRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowInstanceService : CachedBaseService<ApprovalFlowInstanceRepository, ApprovalFlowInstanceEntity> {

    fun findByScopeAndScopeId(scope: Int, scopeId: Long): Flow<ApprovalFlowInstanceEntity>

    fun findByInitiatorId(initiatorId: Long): Flow<ApprovalFlowInstanceEntity>
}
