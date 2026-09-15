/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ManagerUpdateAiUserGroupDTO(
    override val id: Long,
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String? = null,
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String? = null,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    @field:DecimalMin(value = "0.0", inclusive = false)
    val billingMultiplier: BigDecimal? = null,
    val enabled: Boolean? = null,
    val isDefault: Boolean? = null,
    val sort: Int? = null,
) : BaseManagerUpdateDTO(id)
