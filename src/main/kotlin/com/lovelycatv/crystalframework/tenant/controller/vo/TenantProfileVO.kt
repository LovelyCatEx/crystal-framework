package com.lovelycatv.crystalframework.tenant.controller.vo

data class TenantProfileVO(
    val tenantId: Long,
    val ownerUserId: Long,
    val name: String,
    val description: String?,
    val icon: String?,
    val status: Int,
    val tireTypeId: Long,
    val subscribedTime: Long,
    val expiresTime: Long,
    val contactName: String,
    val contactEmail: String,
    val contactPhone: String,
    val address: String,
    val createdTime: Long,
    val modifiedTime: Long
)
