/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
