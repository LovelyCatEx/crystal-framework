package com.lovelycatv.crystalframework.approval.controller.manager.vo

/**
 * Raw form-view payload for the approval task handle page.
 *
 * The frontend owns schema merging: it consumes [formSchemaSnapshot] and [nodeFormSchema] with
 * the shared `mergeFieldOverrides` util and produces `MergedFieldSchema[]` for the renderer. This
 * keeps the merge logic single-sourced on the client and avoids two divergent implementations.
 *
 * [formSchemaSnapshot] is sourced from `ApprovalFlowInstanceEntity.formSchemaSnapshot` (option C
 * in the design record), NOT from the current definition — so an operator handling an old
 * instance sees the form the initiator filled in, regardless of any later edits to the definition.
 *
 * All schema fields stay as raw JSON strings (opaque to the backend); [instanceFormData] is the
 * baseline the frontend snapshots to compute the diff on submit.
 */
data class ApprovalFlowTaskFormViewVO(
    val taskId: Long,
    val instanceId: Long,
    val nodeId: Long,
    val nodeType: Int,
    val definitionId: Long,
    val formSchemaSnapshot: String?,
    val nodeFormSchema: String?,
    val instanceFormData: String?,
)
