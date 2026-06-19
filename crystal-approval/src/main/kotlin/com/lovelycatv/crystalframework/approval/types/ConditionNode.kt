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
