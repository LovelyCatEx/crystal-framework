package com.lovelycatv.crystalframework.approval.types

data class ApprovalNodeConfig(
    val approveMode: Int,
    val strategy: Int,
    val strategyParams: Map<String, Any?> = emptyMap()
) : ApprovalFlowNodeConfig()
