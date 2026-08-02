package com.lovelycatv.crystalframework.approval.service

import com.lovelycatv.crystalframework.approval.types.ApprovalFieldOverride
import com.lovelycatv.crystalframework.approval.types.ApprovalFieldSchema
import com.lovelycatv.crystalframework.approval.types.ApprovalFieldType
import com.lovelycatv.crystalframework.approval.types.ApprovalFormSchema
import com.lovelycatv.crystalframework.approval.types.NodeFormOverlay
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.parseObject

/**
 * Backend-side authoritative validator for approval form artifacts (decision 5: the frontend
 * validation is a UX shortcut, the backend must re-check everything at each write boundary).
 *
 * Three responsibilities, each callable independently so callers only pay for what they need:
 *
 *   * [validateSchema]   — called from `ApprovalFlowDefinitionManagerService.update` before persisting
 *                          `definition.formSchema`. Guards structural integrity of the schema itself.
 *   * [validateFormData] — called from `ApprovalFlowEngine.startFlow` before persisting `instance.formData`.
 *                          Ensures the initiator's payload conforms to the definition schema.
 *   * [validateTaskDiff] — called from `ApprovalFlowEngine.handleTask` before merging the task diff
 *                          onto `instance.formData`. Enforces that operators can only mutate keys
 *                          they're actually allowed to write at the current node (readonly=false in
 *                          the merged schema — mirrors [mergeMergedFieldReadonly]).
 *
 * Returned lists are the concrete errors; an empty list means "valid". Callers translate the
 * lists to `BusinessException`s themselves so the messages localize consistently at the boundary.
 */
object ApprovalFormSchemaValidator {

    /**
     * Validate the definition-level schema JSON. `null` / blank / unparseable input is not
     * considered an error — a definition may legitimately have no form. Only surface issues if
     * the payload *claims* to be a schema but is malformed.
     */
    fun validateSchema(schemaJson: String?): List<String> {
        if (schemaJson.isNullOrBlank()) return emptyList()
        val schema = runCatching { schemaJson.parseObject<ApprovalFormSchema>() }.getOrNull()
            ?: return listOf("Form schema is not a valid ApprovalFormSchema JSON")
        return validateSchema(schema)
    }

    /** Validate an already-parsed schema. Extracted so [ApprovalFlowGraphValidator] can reuse it. */
    fun validateSchema(schema: ApprovalFormSchema): List<String> {
        val errors = mutableListOf<String>()

        val seenKeys = mutableSetOf<String>()
        val groupKeys = schema.groups?.map { it.key }?.toSet() ?: emptySet()

        for ((index, field) in schema.fields.withIndex()) {
            if (field.key.isBlank()) {
                errors += "Field at index $index has blank key"
                continue
            }
            if (!ApprovalFormSchema.FIELD_KEY_PATTERN.matches(field.key)) {
                errors += "Field '${field.key}' does not match camelCase pattern"
            }
            if (!seenKeys.add(field.key)) {
                errors += "Duplicate field key: '${field.key}'"
            }
            if (field.label.isBlank()) {
                errors += "Field '${field.key}' has blank label"
            }
            errors += validateFieldOptions(field)
            errors += validateDictBinding(field)
            errors += validateFieldValidation(field)
            if (field.groupKey != null && field.groupKey !in groupKeys) {
                errors += "Field '${field.key}' references unknown group '${field.groupKey}'"
            }
        }

        return errors
    }

    /**
     * Validate the initiator's formData payload against the definition schema. `null` / blank
     * formData is treated as `{}` — an entirely-empty payload is legal iff every required
     * visible field is either optional or has a default.
     */
    fun validateFormData(
        schemaJson: String?,
        formDataJson: String?,
        dictAllowedCodes: Map<String, Set<String>> = emptyMap(),
    ): List<String> {
        val schema = parseSchemaOrNull(schemaJson) ?: return emptyList()
        val data = parseDataOrEmpty(formDataJson)

        val errors = mutableListOf<String>()
        val declaredKeys = schema.fields.map { it.key }.toSet()

        for (key in data.keys) {
            if (key !in declaredKeys) {
                errors += "Unknown field key in formData: '$key'"
            }
        }

        for (field in schema.fields) {
            if (!field.visible) continue
            val value = data[field.key]
            if (field.required && isEmpty(value)) {
                errors += "Field '${field.key}' is required"
                continue
            }
            if (value != null) {
                errors += validateValueAgainstField(field, value, dictAllowedCodes[field.key].orEmpty())
            }
        }

        return errors
    }

    /**
     * Validate a task-level diff against the definition schema PLUS the node overlay. This is
     * the write-boundary check for [com.lovelycatv.crystalframework.approval.service.engine.ApprovalFlowEngine.handleTask]:
     *
     *   * the diff must only reference declared keys,
     *   * the diff may only touch fields that are visible AND writable at this node,
     *   * value-level type / range constraints still apply.
     *
     * `readonly` collapses using the same rules as the frontend `mergeFieldOverrides` (decision 2):
     * approval nodes default readonly=true, CC nodes force readonly=true regardless of overlay.
     */
    fun validateTaskDiff(
        schemaJson: String?,
        nodeOverlayJson: String?,
        diffJson: String?,
        isCcNode: Boolean,
        isApprovalNode: Boolean,
        dictAllowedCodes: Map<String, Set<String>> = emptyMap(),
    ): List<String> {
        if (diffJson.isNullOrBlank()) return emptyList()

        val schema = parseSchemaOrNull(schemaJson)
        val diff = parseDataOrEmpty(diffJson)

        if (schema == null) {
            // No schema means no field is declared — accepting a non-empty diff would be an escape hatch.
            return if (diff.isEmpty()) emptyList() else listOf("Definition has no form schema; diff must be empty")
        }

        val overlay = parseOverlayOrNull(nodeOverlayJson)
        val errors = mutableListOf<String>()
        val fieldByKey = schema.fields.associateBy { it.key }

        for ((key, value) in diff) {
            val field = fieldByKey[key]
            if (field == null) {
                errors += "Diff references unknown field: '$key'"
                continue
            }
            val effectiveVisible = mergedVisible(field, overlay?.fieldOverrides?.get(key))
            if (!effectiveVisible) {
                errors += "Field '$key' is not visible at this node"
                continue
            }
            val effectiveReadonly = mergeMergedFieldReadonly(
                field, overlay?.fieldOverrides?.get(key), isCcNode, isApprovalNode,
            )
            if (effectiveReadonly) {
                errors += "Field '$key' is readonly at this node"
                continue
            }
            if (value != null) {
                errors += validateValueAgainstField(field, value, dictAllowedCodes[key].orEmpty())
            }
        }

        return errors
    }

    // --- helpers ---

    private fun mergedVisible(field: ApprovalFieldSchema, override: ApprovalFieldOverride?): Boolean =
        override?.visible ?: field.visible

    /**
     * Merge readonly per decision 2. Mirror of the frontend `mergeFieldOverrides` logic. Kept
     * private and tight so schema/formData/diff share the exact same interpretation.
     */
    private fun mergeMergedFieldReadonly(
        field: ApprovalFieldSchema,
        override: ApprovalFieldOverride?,
        isCcNode: Boolean,
        isApprovalNode: Boolean,
    ): Boolean {
        if (isCcNode) return true
        if (isApprovalNode) return override?.readonly ?: true
        return override?.readonly ?: field.readonly
    }

    private fun parseSchemaOrNull(schemaJson: String?): ApprovalFormSchema? {
        if (schemaJson.isNullOrBlank()) return null
        return runCatching { schemaJson.parseObject<ApprovalFormSchema>() }.getOrNull()
    }

    private fun parseOverlayOrNull(overlayJson: String?): NodeFormOverlay? {
        if (overlayJson.isNullOrBlank()) return null
        return runCatching { overlayJson.parseObject<NodeFormOverlay>() }.getOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseDataOrEmpty(json: String?): Map<String, Any?> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching { json.parseObject<Map<String, Any?>>() }.getOrElse { emptyMap() }
    }

    private fun isEmpty(v: Any?): Boolean = when (v) {
        null -> true
        is String -> v.isBlank()
        is Collection<*> -> v.isEmpty()
        is Map<*, *> -> v.isEmpty()
        else -> false
    }

    private fun validateFieldOptions(field: ApprovalFieldSchema): List<String> {
        val optionTypes = setOf(ApprovalFieldType.SELECT, ApprovalFieldType.RADIO, ApprovalFieldType.CHECKBOX)
        if (field.type !in optionTypes) return emptyList()
        val opts = field.options
        if (opts.isNullOrEmpty()) return listOf("Field '${field.key}' (${field.type.value}) must declare non-empty options")
        val errors = mutableListOf<String>()
        val seen = mutableSetOf<String>()
        for (opt in opts) {
            if (opt.value.isBlank()) errors += "Field '${field.key}' has an option with blank value"
            if (opt.label.isBlank()) errors += "Field '${field.key}' has an option with blank label"
            if (!seen.add(opt.value)) errors += "Field '${field.key}' has duplicate option value: '${opt.value}'"
        }
        return errors
    }

    /**
     * Structural check for DICT fields (the cross-scope legality + dict-existence check that needs
     * the database lives in [com.lovelycatv.crystalframework.approval.service.ApprovalDictResolver]).
     * A DICT field must declare a `dictCode` and must NOT carry static `options` — its candidate
     * values come from the dictionary, not the schema.
     */
    private fun validateDictBinding(field: ApprovalFieldSchema): List<String> {
        if (field.type != ApprovalFieldType.DICT) return emptyList()
        val errors = mutableListOf<String>()
        if (field.dictCode.isNullOrBlank()) {
            errors += "Field '${field.key}' (dict) must declare a non-blank dictCode"
        }
        if (!field.options.isNullOrEmpty()) {
            errors += "Field '${field.key}' (dict) must not declare static options; candidates come from the dictionary"
        }
        if (field.dictScope != null && ResourceScope.getById(field.dictScope) == null) {
            errors += "Field '${field.key}' (dict) has invalid dictScope: ${field.dictScope}"
        }
        return errors
    }

    private fun validateFieldValidation(field: ApprovalFieldSchema): List<String> {
        val v = field.validation ?: return emptyList()
        val errors = mutableListOf<String>()
        if (v.min != null && v.max != null && v.min > v.max) {
            errors += "Field '${field.key}' has min > max"
        }
        if (v.minCount != null && v.maxCount != null && v.minCount > v.maxCount) {
            errors += "Field '${field.key}' has minCount > maxCount"
        }
        if (v.precision != null && v.precision < 0) {
            errors += "Field '${field.key}' has negative precision"
        }
        if (v.maxLength != null && v.maxLength < 0) {
            errors += "Field '${field.key}' has negative maxLength"
        }
        if (v.pattern != null) {
            runCatching { Regex(v.pattern) }.onFailure {
                errors += "Field '${field.key}' has invalid regex pattern"
            }
        }
        return errors
    }

    /**
     * Value-level type check. Deliberately lenient about numeric widening (JSON `Long` may
     * arrive as `Int` after deserialization, string dates may come as `String`, etc). The
     * strict guarantee is: no wildly-typed value slips through to the engine or downstream nodes.
     */
    private fun validateValueAgainstField(
        field: ApprovalFieldSchema,
        value: Any,
        dictAllowedCodes: Set<String> = emptySet(),
    ): List<String> {
        val errors = mutableListOf<String>()
        when (field.type) {
            ApprovalFieldType.TEXT, ApprovalFieldType.TEXTAREA -> {
                if (value !is String) {
                    errors += "Field '${field.key}' expects text but got ${value::class.simpleName}"
                    return errors
                }
                val v = field.validation
                if (v?.maxLength != null && value.length > v.maxLength) {
                    errors += "Field '${field.key}' exceeds maxLength ${v.maxLength}"
                }
                if (v?.pattern != null) {
                    val ok = runCatching { Regex(v.pattern).matches(value) }.getOrDefault(true)
                    if (!ok) errors += "Field '${field.key}' does not match required pattern"
                }
            }
            ApprovalFieldType.NUMBER -> {
                val n = (value as? Number)?.toDouble()
                if (n == null) {
                    errors += "Field '${field.key}' expects number but got ${value::class.simpleName}"
                    return errors
                }
                val v = field.validation
                if (v?.min != null && n < v.min) errors += "Field '${field.key}' below min ${v.min}"
                if (v?.max != null && n > v.max) errors += "Field '${field.key}' above max ${v.max}"
            }
            ApprovalFieldType.BOOLEAN -> {
                if (value !is Boolean) errors += "Field '${field.key}' expects boolean but got ${value::class.simpleName}"
            }
            ApprovalFieldType.SELECT, ApprovalFieldType.RADIO -> {
                val validValues = field.options?.map { it.value }?.toSet() ?: emptySet()
                val multiple = field.type == ApprovalFieldType.SELECT && (field.validation?.multiple == true)
                if (multiple) {
                    val list = value as? List<*>
                    if (list == null) {
                        errors += "Field '${field.key}' (multiple) expects array"
                    } else {
                        for (item in list) {
                            if (item !is String || item !in validValues) {
                                errors += "Field '${field.key}' has invalid selection: '$item'"
                            }
                        }
                    }
                } else {
                    if (value !is String || value !in validValues) {
                        errors += "Field '${field.key}' has invalid selection: '$value'"
                    }
                }
            }
            ApprovalFieldType.CHECKBOX -> {
                val list = value as? List<*>
                if (list == null) {
                    errors += "Field '${field.key}' expects array"
                    return errors
                }
                val validValues = field.options?.map { it.value }?.toSet() ?: emptySet()
                for (item in list) {
                    if (item !is String || item !in validValues) {
                        errors += "Field '${field.key}' has invalid selection: '$item'"
                    }
                }
                val v = field.validation
                if (v?.minCount != null && list.size < v.minCount) errors += "Field '${field.key}' has too few selections"
                if (v?.maxCount != null && list.size > v.maxCount) errors += "Field '${field.key}' has too many selections"
            }
            ApprovalFieldType.DATE, ApprovalFieldType.DATETIME -> {
                if (value !is String) errors += "Field '${field.key}' expects ISO date string but got ${value::class.simpleName}"
            }
            ApprovalFieldType.DICT -> {
                // Candidate codes are the caller-injected live dictionary items (kept out of the
                // pure validator). validation.multiple switches single value vs array, mirroring
                // SELECT / CHECKBOX; minCount / maxCount reused for the multiple case.
                val multiple = field.validation?.multiple == true
                if (multiple) {
                    val list = value as? List<*>
                    if (list == null) {
                        errors += "Field '${field.key}' (dict, multiple) expects array"
                    } else {
                        for (item in list) {
                            if (item !is String || item !in dictAllowedCodes) {
                                errors += "Field '${field.key}' has invalid dict selection: '$item'"
                            }
                        }
                        val v = field.validation
                        if (v?.minCount != null && list.size < v.minCount) errors += "Field '${field.key}' has too few selections"
                        if (v?.maxCount != null && list.size > v.maxCount) errors += "Field '${field.key}' has too many selections"
                    }
                } else {
                    if (value !is String || value !in dictAllowedCodes) {
                        errors += "Field '${field.key}' has invalid dict selection: '$value'"
                    }
                }
            }
        }
        return errors
    }
}
