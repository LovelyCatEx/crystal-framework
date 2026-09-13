/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.approval.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = ConditionLeaf::class, name = "condition"),
    JsonSubTypes.Type(value = ConditionGroup::class, name = "group"),
)
sealed class ConditionNode

data class ConditionLeaf(
    val field: String,
    val operator: ConditionOperator,
    val value: Any? = null,
    val values: List<Any>? = null
) : ConditionNode()

data class ConditionGroup(
    val logic: ConditionLogic,
    val children: List<ConditionNode>
) : ConditionNode()
