/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.database

/**
 * Trace of a [GroupNode] evaluation. [logic] is the group's operator name (`"AND"` or `"OR"`),
 * [children] carries the per-child sub-traces, and [matched] is `true` when the child boolean
 * combination is true.
 */
data class EvaluationGroupTrace(
    val logic: String,
    val children: List<EvaluationNodeTrace>,
    override val matched: Boolean,
) : EvaluationNodeTrace()
