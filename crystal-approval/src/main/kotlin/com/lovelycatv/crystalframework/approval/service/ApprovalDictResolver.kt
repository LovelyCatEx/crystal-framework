package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.types.ApprovalFieldOption
import com.lovelycatv.crystalframework.approval.types.ApprovalFormSchema
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

/**
 * Resolves DICT form fields against the dictionary module, honoring the scope model.
 *
 * A DICT field stores only a stable dict `code` (+ optional `dictScope` override). The concrete
 * dict type is resolved at render / validate time against a `(scope, scopeId)` coordinate derived
 * from the owning flow definition or instance — so the exact same schema drives a SYSTEM-scope flow
 * against the system dictionary and a TENANT-scope flow against that tenant's dictionary, with no
 * per-scope branching in the schema itself.
 *
 * Scope resolution rules (see [resolveScopeCoordinate]):
 *   - `dictScope == null` → inherit the flow's own scope + scopeId.
 *   - `dictScope == SYSTEM` → the shared system dictionary (`scopeId = 0`), always legal.
 *   - `dictScope == TENANT` → only legal when the flow itself is TENANT-scoped; a SYSTEM flow may
 *     not reach into a tenant's private dictionary.
 */
interface ApprovalDictResolver {

    /**
     * The selectable options ({ value = itemCode, label = itemValue }) of a single DICT field.
     * Returns an empty list when the referenced dict type does not exist in the resolved scope.
     */
    suspend fun resolveItems(
        flowScope: ResourceScope,
        flowScopeId: Long,
        dictCode: String,
        dictScope: Int?,
    ): List<ApprovalFieldOption>

    /**
     * Convenience for the renderer endpoints: parse [schemaJson], locate the DICT field named
     * [fieldKey], and resolve its selectable items against `(flowScope, flowScopeId)`. Returns an
     * empty list when the schema is blank / unparseable, the field is missing, the field is not a
     * DICT, or it has no dictCode. `scope + scopeId` come from the server-side flow entity, never
     * from the client, so a caller cannot steer the lookup into another tenant's dictionary.
     */
    suspend fun resolveItemsForField(
        schemaJson: String?,
        fieldKey: String,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): List<ApprovalFieldOption>

    /**
     * Allowed item codes keyed by field key, for every DICT field in [schema]. Fed into
     * [ApprovalFormSchemaValidator.validateFormData] / [ApprovalFormSchemaValidator.validateTaskDiff]
     * so value-level validation stays a pure function while the candidate set comes from the live
     * dictionary.
     */
    suspend fun resolveAllowedCodes(
        schema: ApprovalFormSchema,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): Map<String, Set<String>>

    /**
     * Schema-level validation of DICT bindings: cross-scope legality and dict-type existence.
     * Complements the pure structural checks in [ApprovalFormSchemaValidator.validateSchema]
     * (which cannot touch the database). Returns the concrete errors; empty means valid.
     */
    suspend fun validateDictBindings(
        schema: ApprovalFormSchema,
        flowScope: ResourceScope,
        flowScopeId: Long,
    ): List<String>
}
