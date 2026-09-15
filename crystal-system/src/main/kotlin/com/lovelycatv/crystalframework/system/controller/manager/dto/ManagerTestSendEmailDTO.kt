/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.controller.manager.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ManagerTestSendEmailDTO(
    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "email format is invalid")
    val email: String?,
)
