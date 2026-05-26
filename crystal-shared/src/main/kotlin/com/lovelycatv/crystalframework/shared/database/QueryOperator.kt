package com.lovelycatv.crystalframework.shared.database

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Comparison operators supported by [ConditionNode].
 */
enum class QueryOperator(@JsonValue val value: String) {
    EQ("eq"),
    NE("ne"),
    LIKE("like"),
    CONTAINS("contains"),
    GT("gt"),
    GTE("gte"),
    LT("lt"),
    LTE("lte"),
    IN("in"),
    IS_NULL("isNull"),
    IS_NOT_NULL("isNotNull");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): QueryOperator {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown QueryOperator: $value")
        }
    }
}
