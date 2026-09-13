/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.repository

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import reactor.core.publisher.Flux

interface ApprovalFlowDefinitionRepository : BaseRepository<ApprovalFlowDefinitionEntity> {
    fun findAllByScopeId(scopeId: Long): Flux<ApprovalFlowDefinitionEntity>
}
