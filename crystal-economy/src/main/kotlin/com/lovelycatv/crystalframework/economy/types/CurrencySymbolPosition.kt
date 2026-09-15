/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.types

/**
 * Where a currency symbol is rendered relative to a formatted amount.
 *
 * - [PREFIX]: symbol before the amount (`$1000`)
 * - [SUFFIX]: symbol after the amount (`200.24¥`)
 * - [REPLACE_DECIMAL]: symbol replaces the decimal separator (`200$24`)
 */
enum class CurrencySymbolPosition(val typeId: Int) {
    PREFIX(0),
    SUFFIX(1),
    REPLACE_DECIMAL(2);

    companion object {
        fun getById(typeId: Int): CurrencySymbolPosition? = entries.firstOrNull { it.typeId == typeId }
    }
}
