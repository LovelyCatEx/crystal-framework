/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTokenEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowTokenRepository : BaseRepository<ApprovalFlowTokenEntity> {

    fun findByInstanceId(instanceId: Long): Flow<ApprovalFlowTokenEntity>

    fun findByInstanceIdAndStatus(instanceId: Long, status: Int): Flow<ApprovalFlowTokenEntity>

    fun findByForkNodeIdAndCurrentNodeIdAndStatus(
        forkNodeId: Long,
        currentNodeId: Long,
        status: Int
    ): Flow<ApprovalFlowTokenEntity>
}
