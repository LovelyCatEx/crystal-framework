/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

/**
 * Optional per-field validation constraints. Fields that don't apply to the current
 * [ApprovalFieldType] are ignored silently by [com.lovelycatv.crystalframework.approval.service.ApprovalFormSchemaValidator]
 * — matches the frontend renderer's tolerance so schemas stay portable.
 */
data class ApprovalFieldValidation(
    val maxLength: Int? = null,
    val pattern: String? = null,
    val min: Double? = null,
    val max: Double? = null,
    val precision: Int? = null,
    val minCount: Int? = null,
    val maxCount: Int? = null,
    val minDate: String? = null,
    val maxDate: String? = null,
    val multiple: Boolean? = null,
)
