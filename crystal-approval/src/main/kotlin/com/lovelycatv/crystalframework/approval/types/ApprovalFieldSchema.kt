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
    /**
     * DICT only — the stable business `code` of the referenced dict type (see
     * `TenantDictTypeEntity.code`). The concrete dict type is resolved at render/validate time
     * against a `(scope, scopeId)` derived from the owning flow definition, so the same schema
     * naturally works for a SYSTEM-scope flow and a TENANT-scope flow. Required for DICT fields,
     * ignored otherwise.
     */
    val dictCode: String? = null,
    /**
     * DICT only — optional scope override ([com.lovelycatv.crystalframework.shared.types.common.ResourceScope]
     * typeId). `null` inherits the flow definition's own scope; a concrete value pins the lookup
     * to that scope (e.g. a TENANT flow referencing a shared SYSTEM dict). Cross-scope validity is
     * enforced by `ApprovalDictResolver` / `ApprovalFormSchemaValidator` — a SYSTEM flow may not
     * reference a TENANT dict.
     */
    val dictScope: Int? = null,
)
