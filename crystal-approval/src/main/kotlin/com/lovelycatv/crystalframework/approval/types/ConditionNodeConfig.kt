package com.lovelycatv.crystalframework.approval.types

data class ConditionNodeConfig(
    val conditions: List<ConditionRoute>
) : ApprovalFlowNodeConfig()
