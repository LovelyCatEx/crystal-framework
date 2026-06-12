package com.lovelycatv.crystalframework.approval.types

import com.lovelycatv.crystalframework.approval.constants.ConditionLogic

data class ConditionRoute(
    val targetNodeId: Long,
    val logic: String = ConditionLogic.AND,
    val rules: List<ExpressionRule>
)
