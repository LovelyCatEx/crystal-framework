package com.lovelycatv.crystalframework.approval.types

/**
 * A partial per-field override applied by a node's [NodeFormOverlay]. Nulls mean "inherit
 * from the definition"; the merged view is computed by the frontend (single source of truth
 * for merge logic — see decision 7).
 */
data class ApprovalFieldOverride(
    val visible: Boolean? = null,
    val readonly: Boolean? = null,
    val required: Boolean? = null,
)
