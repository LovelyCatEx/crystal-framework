package com.lovelycatv.crystalframework.approval.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class ConditionLogic(@JsonValue val value: String) {
    AND("and"),
    OR("or");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): ConditionLogic {
            return entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown ConditionLogic: $value")
        }
    }
}
