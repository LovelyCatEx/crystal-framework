package com.lovelycatv.crystalframework.tenant.controller.dto

import jakarta.validation.constraints.Size

data class UpsertTenantMemberProfileDTO(
    @field:Size(max = 32, message = "Phone length cannot exceed 32 characters")
    val phone: String? = null,

    @field:Size(max = 32, message = "Nickname length cannot exceed 32 characters")
    val nickname: String? = null,

    val avatar: Long? = null,

    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String? = null,

    @field:Size(max = 512, message = "Bio length cannot exceed 512 characters")
    val bio: String? = null,

    val gender: Int? = null,

    val birthday: Long? = null,

    @field:Size(max = 64, message = "Timezone length cannot exceed 64 characters")
    val timezone: String? = null,

    @field:Size(max = 16, message = "Locale length cannot exceed 16 characters")
    val locale: String? = null,
)
