/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import kotlinx.coroutines.flow.Flow
import reactor.core.publisher.Flux

interface ApprovalFlowInstanceRepository : BaseRepository<ApprovalFlowInstanceEntity> {

    fun findByScopeAndScopeId(scope: Int, scopeId: Long): Flow<ApprovalFlowInstanceEntity>

    fun findByInitiatorId(initiatorId: Long): Flow<ApprovalFlowInstanceEntity>

    fun findAllByScopeId(scopeId: Long): Flux<ApprovalFlowInstanceEntity>
}
