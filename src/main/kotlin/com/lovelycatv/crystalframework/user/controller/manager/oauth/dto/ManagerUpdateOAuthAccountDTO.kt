package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateOAuthAccountDTO(
    override val id: Long,
    val userId: Long? = null,
    val platform: Int? = null,

    @field:Size(max = 256, message = "Identifier length cannot exceed 256 characters")
    val identifier: String? = null,

    @field:Size(max = 128, message = "Nickname length cannot exceed 128 characters")
    val nickname: String? = null,

    @field:Size(max = 256, message = "Avatar length cannot exceed 256 characters")
    val avatar: String? = null
) : BaseManagerUpdateDTO(id)
