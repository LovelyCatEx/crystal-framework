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
