package com.lovelycatv.crystalframework.shared.database

/**
 * A leaf node in a [QueryNode] tree representing a single field comparison.
 *
 * @param field    Database column name (e.g. `"username"`, `"created_time"`).
 * @param operator Comparison operator.
 * @param value    Single value used by most operators.
 * @param values   Multi-value list used by [QueryOperator.IN].
 */
data class ConditionNode(
    val field: String,
    val operator: QueryOperator,
    val value: Any? = null,
    val values: List<Any>? = null,
) : QueryNode()
