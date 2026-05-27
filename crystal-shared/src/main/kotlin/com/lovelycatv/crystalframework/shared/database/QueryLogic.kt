package com.lovelycatv.crystalframework.shared.database

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Logical combinator for [GroupNode] children.
 */
enum class QueryLogic(@JsonValue val value: String) {
    AND("and"),
    OR("or");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): QueryLogic {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown QueryLogic: $value")
        }
    }
}
