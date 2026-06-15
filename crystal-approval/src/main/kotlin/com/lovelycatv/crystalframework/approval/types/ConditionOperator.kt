package com.lovelycatv.crystalframework.approval.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ConditionOperator(@JsonValue val value: String) {
    EQ("eq"),
    NE("ne"),
    GT("gt"),
    GTE("gte"),
    LT("lt"),
    LTE("lte"),
    CONTAINS("contains"),
    IN("in");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): ConditionOperator {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ConditionOperator: $value")
        }
    }
}
