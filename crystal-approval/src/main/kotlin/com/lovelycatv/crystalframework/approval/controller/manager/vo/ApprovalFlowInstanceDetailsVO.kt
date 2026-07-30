package com.lovelycatv.crystalframework.approval.controller.manager.vo

import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowDefinitionEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowEdgeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowInstanceEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowNodeEntity
import com.lovelycatv.crystalframework.approval.entity.ApprovalFlowTaskEntity

/**
 * One-shot payload for the instance viewer: the instance itself, the definition graph pinned to
 * the instance's own [ApprovalFlowInstanceEntity.definitionVersion] (so viewers see the exact
 * shape the instance is executing, not whatever the definition has been edited to since), the
 * per-node aggregated task status, every audit record, and the raw task list.
 *
 * `tasks` carries each task's `formData` diff (see decision 6 — approvers submit differential
 * diffs). The viewer's form panel replays those diffs onto a timeline so viewers can see "who
 * changed which fields at which step" without having to reconstruct the initial snapshot.
 * Keyed by nodeId (as String because Long → String is the project-wide serialization rule).
 */
data class ApprovalFlowInstanceDetailsVO(
    val instance: ApprovalFlowInstanceEntity,
    val definition: ApprovalFlowDefinitionEntity,
    val nodes: List<ApprovalFlowNodeEntity>,
    val edges: List<ApprovalFlowEdgeEntity>,
    val nodeStates: Map<String, ApprovalNodeStateVO>,
    val records: List<ApprovalFlowRecordVO>,
    val tasks: List<ApprovalFlowTaskEntity>,
)
