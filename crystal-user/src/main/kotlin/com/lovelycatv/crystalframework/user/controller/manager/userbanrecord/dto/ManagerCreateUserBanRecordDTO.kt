package com.lovelycatv.crystalframework.user.controller.manager.userbanrecord.dto

/**
 * Ban records are created by the ban business flow, not through the standard manager create endpoint.
 * This DTO exists only to satisfy the generic type constraint of the manager controller family.
 */
data class ManagerCreateUserBanRecordDTO(
    val placeholder: String? = null
)
