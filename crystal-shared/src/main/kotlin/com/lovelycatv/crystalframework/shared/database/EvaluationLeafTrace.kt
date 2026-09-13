/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.database

/**
 * Trace of a single [ConditionNode] evaluation. [expectedValue] is the operator's operand
 * rendered as a string (comma-joined for IN, null for IS_NULL/IS_NOT_NULL); [actualValue] is the
 * evaluation context's value for [field], stringified (null if the key was missing).
 */
data class EvaluationLeafTrace(
    val field: String,
    val operator: String,
    val expectedValue: String?,
    val actualValue: String?,
    override val matched: Boolean,
) : EvaluationNodeTrace()
