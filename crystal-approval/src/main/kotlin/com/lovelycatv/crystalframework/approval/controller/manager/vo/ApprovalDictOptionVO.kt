package com.lovelycatv.crystalframework.approval.controller.manager.vo

/**
 * A single selectable option of a DICT form field, resolved live from the dictionary module.
 * `value` is the dict item code (what gets stored in formData), `label` is its display text.
 */
data class ApprovalDictOptionVO(
    val value: String,
    val label: String,
)
