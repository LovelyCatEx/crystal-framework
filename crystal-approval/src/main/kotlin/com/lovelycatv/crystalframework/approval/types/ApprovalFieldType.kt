package com.lovelycatv.crystalframework.approval.types

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

/**
 * Form field type, kept in strict sync with the frontend `ApprovalFieldType` (see
 * `web/src/types/approval/approval-enums.ts`). Persisted inside definition/node formSchema
 * JSON as its lowercase string value — matches [ConditionOperator]'s serialization style
 * so both enums are readable in raw JSON payloads.
 */
enum class ApprovalFieldType(@JsonValue val value: String) {
    TEXT("text"),
    TEXTAREA("textarea"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    SELECT("select"),
    RADIO("radio"),
    CHECKBOX("checkbox"),
    DATE("date"),
    DATETIME("datetime"),
    DICT("dict");

    companion object {
        @JsonCreator
        @JvmStatic
        fun fromValue(value: String): ApprovalFieldType? {
            return entries.find { it.value.equals(value, ignoreCase = true) }
        }

        /**
         * Which [ConditionOperator]s are semantically valid for each field type. Mirrors
         * `APPROVAL_FIELD_OPERATORS` on the frontend so the graph validator (server-side)
         * and the CONDITION inspector (client-side) reject the same operator/type pairs.
         */
        val ALLOWED_OPERATORS: Map<ApprovalFieldType, Set<ConditionOperator>> = mapOf(
            TEXT to setOf(ConditionOperator.EQ, ConditionOperator.NE, ConditionOperator.CONTAINS),
            TEXTAREA to setOf(ConditionOperator.CONTAINS),
            NUMBER to setOf(
                ConditionOperator.EQ, ConditionOperator.NE,
                ConditionOperator.GT, ConditionOperator.GTE,
                ConditionOperator.LT, ConditionOperator.LTE,
            ),
            BOOLEAN to setOf(ConditionOperator.EQ, ConditionOperator.NE),
            SELECT to setOf(ConditionOperator.EQ, ConditionOperator.NE, ConditionOperator.IN),
            RADIO to setOf(ConditionOperator.EQ, ConditionOperator.NE, ConditionOperator.IN),
            CHECKBOX to setOf(ConditionOperator.CONTAINS, ConditionOperator.IN),
            DATE to setOf(
                ConditionOperator.EQ, ConditionOperator.NE,
                ConditionOperator.GT, ConditionOperator.GTE,
                ConditionOperator.LT, ConditionOperator.LTE,
            ),
            DATETIME to setOf(
                ConditionOperator.EQ, ConditionOperator.NE,
                ConditionOperator.GT, ConditionOperator.GTE,
                ConditionOperator.LT, ConditionOperator.LTE,
            ),
            // DICT covers both single (like SELECT/RADIO) and multi (like CHECKBOX) selection,
            // switched by validation.multiple — so its operator set is the union of both.
            DICT to setOf(
                ConditionOperator.EQ, ConditionOperator.NE,
                ConditionOperator.IN, ConditionOperator.CONTAINS,
            ),
        )
    }
}
