/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.repository.ApprovalFlowNodeRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import kotlinx.coroutines.flow.Flow

interface ApprovalFlowNodeService : CachedBaseService<ApprovalFlowNodeRepository, ApprovalFlowNodeEntity> {

    fun findByDefinitionVersion(definitionId: Long, definitionVersion: Int): Flow<ApprovalFlowNodeEntity>
}
