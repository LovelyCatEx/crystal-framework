/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.controller.manager.currency.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ManagerCreateCurrencyDTO(
    @field:NotBlank(message = "Code is required")
    @field:Size(max = 32, message = "Code length cannot exceed 32 characters")
    val code: String,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,
    @field:NotBlank(message = "Symbol is required")
    @field:Size(max = 16, message = "Symbol length cannot exceed 16 characters")
    val symbol: String,
    @field:Min(0, message = "Precision must be non-negative")
    @field:Max(8, message = "Precision cannot exceed 8")
    val precision: Int = 2,
    @field:Min(0, message = "Symbol position must be non-negative")
    @field:Max(2, message = "Symbol position cannot exceed 2")
    val symbolPosition: Int = 0,
    @field:Size(max = 8, message = "Decimal separator length cannot exceed 8 characters")
    val decimalSeparator: String = ".",
    @field:Size(max = 8, message = "Thousands separator length cannot exceed 8 characters")
    val thousandsSeparator: String = ",",
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    val enabled: Boolean = true,
    val sort: Int = 0,
)
