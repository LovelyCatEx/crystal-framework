/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.controller.manager.vo

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity

data class ApprovalFlowDefinitionDetailsVO(
    val definition: ApprovalFlowDefinitionEntity,
    val nodes: List<ApprovalFlowNodeEntity>,
    val edges: List<ApprovalFlowEdgeEntity>,
)