/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.currency.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class ManagerUpdateCurrencyDTO(
    override val id: Long,
    @field:Size(max = 32, message = "Code length cannot exceed 32 characters")
    val code: String? = null,
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,
    @field:Size(max = 16, message = "Symbol length cannot exceed 16 characters")
    val symbol: String? = null,
    @field:Min(0, message = "Precision must be non-negative")
    @field:Max(8, message = "Precision cannot exceed 8")
    val precision: Int? = null,
    @field:Min(0, message = "Symbol position must be non-negative")
    @field:Max(2, message = "Symbol position cannot exceed 2")
    val symbolPosition: Int? = null,
    @field:Size(max = 8, message = "Decimal separator length cannot exceed 8 characters")
    val decimalSeparator: String? = null,
    @field:Size(max = 8, message = "Thousands separator length cannot exceed 8 characters")
    val thousandsSeparator: String? = null,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    val enabled: Boolean? = null,
    val sort: Int? = null,
) : BaseManagerUpdateDTO(id)
