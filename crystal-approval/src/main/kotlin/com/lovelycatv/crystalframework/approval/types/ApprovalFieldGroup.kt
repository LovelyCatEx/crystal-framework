package com.lovelycatv.crystalframework.approval.types

/** Optional one-level grouping for form layout. Nested groups are not allowed by design. */
data class ApprovalFieldGroup(
    val key: String,
    val label: String,
)
