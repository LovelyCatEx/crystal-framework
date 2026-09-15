/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface ApprovalFlowEdgeRepository : BaseRepository<ApprovalFlowEdgeEntity> {

    fun findByDefinitionIdAndDefinitionVersion(definitionId: Long, definitionVersion: Int): Flux<ApprovalFlowEdgeEntity>

    fun findBySourceNodeId(sourceNodeId: Long): Flux<ApprovalFlowEdgeEntity>
}
