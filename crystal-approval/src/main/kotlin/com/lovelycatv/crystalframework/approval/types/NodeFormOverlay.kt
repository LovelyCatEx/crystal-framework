package com.lovelycatv.crystalframework.approval.types

/**
 * Node-level overlay stored in `ApprovalFlowNodeEntity.formSchema` as JSON. Only carries
 * per-field visibility diffs — the definition-level schema is the single source of truth
 * for the field structure (decision 7 in the design record).
 */
data class NodeFormOverlay(
    val version: Int = ApprovalFormSchema.FORM_SCHEMA_VERSION,
    val fieldOverrides: Map<String, ApprovalFieldOverride> = emptyMap(),
)
