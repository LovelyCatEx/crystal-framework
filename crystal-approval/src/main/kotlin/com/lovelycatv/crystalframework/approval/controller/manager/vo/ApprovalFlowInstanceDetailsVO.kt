package com.lovelycatv.crystalframework.approval.controller.manager.vo

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity

/**
 * One-shot payload for the instance viewer: the instance itself, the definition graph pinned to
 * the instance's own [ApprovalFlowInstanceEntity.definitionVersion] (so viewers see the exact
 * shape the instance is executing, not whatever the definition has been edited to since), the
 * per-node aggregated task status, and every audit record. Keyed by nodeId (as String because
 * Long → String is the project-wide serialization rule for Long-range fields).
 */
data class ApprovalFlowInstanceDetailsVO(
    val instance: ApprovalFlowInstanceEntity,
    val definition: ApprovalFlowDefinitionEntity,
    val nodes: List<ApprovalFlowNodeEntity>,
    val edges: List<ApprovalFlowEdgeEntity>,
    val nodeStates: Map<String, ApprovalNodeStateVO>,
    val records: List<ApprovalFlowRecordVO>,
)
