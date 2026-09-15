/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
