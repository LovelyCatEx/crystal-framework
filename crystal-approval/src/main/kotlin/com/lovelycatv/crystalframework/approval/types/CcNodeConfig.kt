package com.lovelycatv.crystalframework.approval.types

data class CcNodeConfig(
    val userIds: List<String> = emptyList(),
    val roleIds: List<String> = emptyList(),
    val channelIds: List<String> = emptyList(),
) : ApprovalFlowNodeConfig()
