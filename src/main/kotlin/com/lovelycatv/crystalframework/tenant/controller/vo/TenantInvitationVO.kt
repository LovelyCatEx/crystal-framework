package com.lovelycatv.crystalframework.tenant.controller.vo

data class TenantInvitationVO(
    val tenantId: Long,
    val expiresAt: Long?,
    val departmentName: String?,
)
