/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowEdgeRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowEdgeService : CachedBaseService<ApprovalFlowEdgeRepository, ApprovalFlowEdgeEntity> {

    fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowEdgeEntity>

    fun findBySourceNodeId(sourceNodeId: Long): Flow<ApprovalFlowEdgeEntity>
}
