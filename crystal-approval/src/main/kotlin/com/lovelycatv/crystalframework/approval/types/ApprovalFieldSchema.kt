package com.lovelycatv.crystalframework.approval.types

/**
 * A single field in a definition-level form schema. Mirrors the frontend
 * `ApprovalFieldSchema` (see `web/src/types/approval/approval-form-schema.types.ts`).
 *
 * `defaultValue` is deliberately untyped — validation lives in `ApprovalFormSchemaValidator`,
 * not in the data class, so the payload survives round-trips even when new field types are
 * introduced client-side ahead of the backend.
 */
data class ApprovalFieldSchema(
    val key: String,
    val type: ApprovalFieldType,
    val label: String = "",
    val description: String? = null,
    val placeholder: String? = null,
    val defaultValue: Any? = null,
    val required: Boolean = false,
    val readonly: Boolean = false,
    val visible: Boolean = true,
    val validation: ApprovalFieldValidation? = null,
    val options: List<ApprovalFieldOption>? = null,
    val groupKey: String? = null,
)
