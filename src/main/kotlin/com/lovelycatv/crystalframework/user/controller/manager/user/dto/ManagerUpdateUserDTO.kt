package com.lovelycatv.crystalframework.user.controller.manager.user.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class ManagerUpdateUserDTO(
    override val id: Long,

    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String? = null,

    val nickname: String? = null
) : BaseManagerUpdateDTO(id)
