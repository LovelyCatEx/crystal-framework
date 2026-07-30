package com.lovelycatv.crystalframework.approval.types

/**
 * Definition-level form schema stored in `ApprovalFlowDefinitionEntity.formSchema` as JSON.
 * Kept in strict shape parity with the frontend `ApprovalFormSchema` (see
 * `web/src/types/approval/approval-form-schema.types.ts`).
 */
data class ApprovalFormSchema(
    val version: Int = FORM_SCHEMA_VERSION,
    val fields: List<ApprovalFieldSchema> = emptyList(),
    val groups: List<ApprovalFieldGroup>? = null,
) {
    companion object {
        const val FORM_SCHEMA_VERSION: Int = 1
        /** camelCase key requirement, matches the frontend `APPROVAL_FIELD_KEY_PATTERN`. */
        val FIELD_KEY_PATTERN: Regex = Regex("^[a-z][a-zA-Z0-9]*$")
    }
}
