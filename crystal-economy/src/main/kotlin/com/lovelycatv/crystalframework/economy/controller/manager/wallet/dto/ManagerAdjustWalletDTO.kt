/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.wallet.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ManagerAdjustWalletDTO(
    @field:NotNull(message = "Scope is required")
    val scope: Int,
    @field:NotNull(message = "Scope id is required")
    val scopeId: Long,
    @field:NotNull(message = "Owner id is required")
    val ownerId: Long,
    @field:NotNull(message = "Currency id is required")
    val currencyId: Long,
    @field:NotNull(message = "Amount is required")
    val amount: BigDecimal,
    @field:NotNull(message = "Type is required")
    val type: Int,
    val referenceId: Long? = null,
    @field:Size(max = 512, message = "Remark length cannot exceed 512 characters")
    val remark: String? = null,
)
