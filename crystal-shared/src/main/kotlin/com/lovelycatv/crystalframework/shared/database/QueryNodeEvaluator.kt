package com.lovelycatv.crystalframework.shared.database

import java.math.BigDecimal

/**
 * In-memory evaluator for a [QueryNode] tree against a plain context map, mirroring
 * [criteriaFromQueryNode]'s SQL semantics but without a database round-trip. Used by rule
 * engines (e.g. storage provider routing rules) that match a [QueryNode] condition tree
 * against a runtime context instead of a database row.
 */
object QueryNodeEvaluator {
    fun evaluate(node: QueryNode, context: Map<String, Any?>): Boolean {
        return when (node) {
            is ConditionNode -> evaluateCondition(node, context)
            is GroupNode -> evaluateGroup(node, context)
        }
    }

    /**
     * Mirrors [evaluate] but returns an [EvaluationNodeTrace] tree annotated with each node's
     * outcome. Behavior for individual comparisons matches [evaluate] exactly (delegates to the
     * same private helpers), so trace results are guaranteed consistent with actual routing.
     */
    fun evaluateWithTrace(node: QueryNode, context: Map<String, Any?>): EvaluationNodeTrace {
        return when (node) {
            is ConditionNode -> evaluateConditionWithTrace(node, context)
            is GroupNode -> evaluateGroupWithTrace(node, context)
        }
    }

    private fun evaluateGroup(group: GroupNode, context: Map<String, Any?>): Boolean {
        return when (group.logic) {
            QueryLogic.AND -> group.children.all { evaluate(it, context) }
            QueryLogic.OR -> group.children.any { evaluate(it, context) }
        }
    }

    private fun evaluateGroupWithTrace(group: GroupNode, context: Map<String, Any?>): EvaluationGroupTrace {
        val childTraces = group.children.map { evaluateWithTrace(it, context) }
        val matched = when (group.logic) {
            QueryLogic.AND -> childTraces.all { it.matched }
            QueryLogic.OR -> childTraces.any { it.matched }
        }
        return EvaluationGroupTrace(
            logic = group.logic.name,
            children = childTraces,
            matched = matched,
        )
    }

    private fun evaluateCondition(node: ConditionNode, context: Map<String, Any?>): Boolean {
        val actual = context[node.field]
        return when (node.operator) {
            QueryOperator.EQ -> looseEquals(actual, node.value)
            QueryOperator.NE -> !looseEquals(actual, node.value)
            QueryOperator.LIKE -> matchesLikePattern(actual, node.value)
            QueryOperator.CONTAINS -> actual?.toString()?.contains(node.value?.toString() ?: "") == true
            QueryOperator.GT -> compareNumeric(actual, node.value)?.let { it > 0 } == true
            QueryOperator.GTE -> compareNumeric(actual, node.value)?.let { it >= 0 } == true
            QueryOperator.LT -> compareNumeric(actual, node.value)?.let { it < 0 } == true
            QueryOperator.LTE -> compareNumeric(actual, node.value)?.let { it <= 0 } == true
            QueryOperator.IN -> node.values.orEmpty().any { looseEquals(actual, it) }
            QueryOperator.IS_NULL -> actual == null
            QueryOperator.IS_NOT_NULL -> actual != null
        }
    }

    private fun evaluateConditionWithTrace(node: ConditionNode, context: Map<String, Any?>): EvaluationLeafTrace {
        val actual = context[node.field]
        val matched = evaluateCondition(node, context)
        val expectedRepr = when (node.operator) {
            QueryOperator.IN -> node.values?.joinToString(",") ?: ""
            QueryOperator.IS_NULL, QueryOperator.IS_NOT_NULL -> null
            else -> node.value?.toString()
        }
        return EvaluationLeafTrace(
            field = node.field,
            operator = node.operator.name,
            expectedValue = expectedRepr,
            actualValue = actual?.toString(),
            matched = matched,
        )
    }

    /** Equality that treats numerically-equal values as equal regardless of Int/Long/String representation. */
    private fun looseEquals(actual: Any?, expected: Any?): Boolean {
        if (actual == null || expected == null) return actual == expected
        toBigDecimalOrNull(actual)?.let { a ->
            toBigDecimalOrNull(expected)?.let { e -> return a.compareTo(e) == 0 }
        }
        return actual.toString() == expected.toString()
    }

    private fun compareNumeric(actual: Any?, expected: Any?): Int? {
        val a = toBigDecimalOrNull(actual) ?: return null
        val e = toBigDecimalOrNull(expected) ?: return null
        return a.compareTo(e)
    }

    private fun toBigDecimalOrNull(value: Any?): BigDecimal? {
        return when (value) {
            null -> null
            is BigDecimal -> value
            is Number -> BigDecimal(value.toString())
            is String -> value.toBigDecimalOrNull()
            else -> null
        }
    }

    private fun matchesLikePattern(actual: Any?, pattern: Any?): Boolean {
        if (actual == null || pattern == null) return false
        val regexBody = pattern.toString().map { c ->
            when (c) {
                '%' -> ".*"
                '_' -> "."
                else -> Regex.escape(c.toString())
            }
        }.joinToString("")
        return Regex("^$regexBody$").matches(actual.toString())
    }
}
