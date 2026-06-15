package com.lovelycatv.crystalframework.approval.types

data class ConditionNodeConfig(
    val routes: List<ConditionRoute>
) : ApprovalFlowNodeConfig()
