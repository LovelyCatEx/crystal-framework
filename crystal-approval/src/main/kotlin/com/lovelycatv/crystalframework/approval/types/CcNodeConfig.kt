package com.lovelycatv.crystalframework.approval.types

data class CcNodeConfig(
    val userIds: List<Long> = emptyList(),
    val roleIds: List<Long> = emptyList()
) : ApprovalFlowNodeConfig()
