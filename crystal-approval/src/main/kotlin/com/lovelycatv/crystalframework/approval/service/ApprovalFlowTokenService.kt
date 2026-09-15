/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowTokenRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowTokenService : CachedBaseService<ApprovalFlowTokenRepository, ApprovalFlowTokenEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTokenEntity>

    fun findByInstanceIdAndStatus(instanceId: Long, status: Int): Flow<ApprovalFlowTokenEntity>

    fun findWaitingAtJoin(forkNodeId: Long, joinNodeId: Long): Flow<ApprovalFlowTokenEntity>
}
