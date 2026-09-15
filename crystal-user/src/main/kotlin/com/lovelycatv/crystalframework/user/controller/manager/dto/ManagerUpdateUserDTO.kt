/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.dto

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
