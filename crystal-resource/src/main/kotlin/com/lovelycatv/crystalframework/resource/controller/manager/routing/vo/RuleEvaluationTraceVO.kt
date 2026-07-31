package com.lovelycatv.crystalframework.resource.controller.manager.routing.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class RuleEvaluationTraceVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val ruleId: Long,
    val ruleName: String,
    val priority: Int,
    val matched: Boolean,
    val conditionTree: EvaluationNodeTraceVO?,
    val selectedProviderIds: List<Long>? = null,
)
