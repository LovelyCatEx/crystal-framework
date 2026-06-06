package com.lovelycatv.crystalframework.tenant.controller.dto

data class AcceptTenantInvitationDTO(
    val invitationCode: String = "",
    val realName: String = "",
    val phoneNumber: String = "",
)
