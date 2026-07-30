package com.lovelycatv.crystalframework.shared.database

import org.springframework.data.relational.core.query.Criteria

class CrystalDatabaseExtensions private constructor()

/**
 * DSL entry point for building a [Criteria] using [TypedCriteriaBuilder].
 */
inline fun <reified T : Any> criteria(block: TypedCriteriaBuilder<T>.() -> Unit): Criteria {
    val builder = TypedCriteriaBuilder(T::class)
    builder.block()
    return builder.build()
}

/**
 * Recursively convert a [QueryNode] tree into a Spring Data [Criteria].
 *
 * - [ConditionNode] → a single field predicate.
 * - [GroupNode] with [QueryLogic.AND] → all children ANDed together.
 * - [GroupNode] with [QueryLogic.OR]  → all children ORed together.
 *
 * Nesting is supported to arbitrary depth.
 */
@Suppress("UNCHECKED_CAST")
fun criteriaFromQueryNode(node: QueryNode): Criteria {
    return when (node) {
        is ConditionNode -> buildLeafCriteria(node)
        is GroupNode -> buildGroupCriteria(node)
    }
}

@Suppress("UNCHECKED_CAST")
private fun buildLeafCriteria(node: ConditionNode): Criteria {
    val col = node.field
    return when (node.operator) {
        QueryOperator.EQ -> {
            val v = coerceNumericString(requireNotNull(node.value) { "EQ requires a value for field '${node.field}'" })
            Criteria.where(col).`is`(v)
        }
        QueryOperator.NE -> {
            val v = coerceNumericString(requireNotNull(node.value) { "NE requires a value for field '${node.field}'" })
            Criteria.where(col).not(v)
        }
        QueryOperator.LIKE -> {
            val v = requireNotNull(node.value) { "LIKE requires a value for field '${node.field}'" }
            Criteria.where(col).like(v.toString())
        }
        QueryOperator.CONTAINS -> {
            val v = requireNotNull(node.value) { "CONTAINS requires a value for field '${node.field}'" }
            Criteria.where(col).like("%${v}%")
        }
        QueryOperator.GT -> {
            val v = coerceNumericString(requireNotNull(node.value) { "GT requires a value for field '${node.field}'" }) as Comparable<Any>
            Criteria.where(col).greaterThan(v)
        }
        QueryOperator.GTE -> {
            val v = coerceNumericString(requireNotNull(node.value) { "GTE requires a value for field '${node.field}'" }) as Comparable<Any>
            Criteria.where(col).greaterThanOrEquals(v)
        }
        QueryOperator.LT -> {
            val v = coerceNumericString(requireNotNull(node.value) { "LT requires a value for field '${node.field}'" }) as Comparable<Any>
            Criteria.where(col).lessThan(v)
        }
        QueryOperator.LTE -> {
            val v = coerceNumericString(requireNotNull(node.value) { "LTE requires a value for field '${node.field}'" }) as Comparable<Any>
            Criteria.where(col).lessThanOrEquals(v)
        }
        QueryOperator.IN -> {
            val vs = requireNotNull(node.values) { "IN requires a values list for field '${node.field}'" }
                .map { coerceNumericString(it) }
            Criteria.where(col).`in`(vs)
        }
        QueryOperator.IS_NULL -> Criteria.where(col).isNull()
        QueryOperator.IS_NOT_NULL -> Criteria.where(col).isNotNull()
    }
}

private val NUMERIC_STRING_REGEX = Regex("^-?\\d+$")

/**
 * Coerce a stringified integer to [Long] so that comparison operators bind the parameter
 * as `BIGINT` instead of `VARCHAR`. Necessary because JSON transport carries snowflake ids
 * (and other 64-bit integers) as strings to avoid JavaScript precision loss, but PostgreSQL
 * has no implicit `bigint = varchar` coercion.
 *
 * Only applied to EQ/NE/GT/GTE/LT/LTE/IN, not LIKE/CONTAINS where string semantics are intended.
 */
private fun coerceNumericString(value: Any): Any {
    if (value !is String) return value
    if (!NUMERIC_STRING_REGEX.matches(value)) return value
    return value.toLongOrNull() ?: value
}

private fun buildGroupCriteria(group: GroupNode): Criteria {
    require(group.children.isNotEmpty()) { "GroupNode must have at least one child" }

    val childCriterias = group.children.map { criteriaFromQueryNode(it) }

    return when (group.logic) {
        QueryLogic.AND -> childCriterias.reduce { acc, c -> acc.and(c) }
        QueryLogic.OR  -> childCriterias.reduce { acc, c -> acc.or(c) }
    }
}
