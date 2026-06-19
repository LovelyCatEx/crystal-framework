package com.lovelycatv.crystalframework.approval.controller.manager.vo

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity

data class ApprovalFlowDefinitionDetailsVO(
    val definition: ApprovalFlowDefinitionEntity,
    val nodes: List<ApprovalFlowNodeEntity>,
    val edges: List<ApprovalFlowEdgeEntity>,
)