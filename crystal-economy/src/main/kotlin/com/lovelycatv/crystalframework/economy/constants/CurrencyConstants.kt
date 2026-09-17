/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.constants

object CurrencyConstants {
    /**
     * Sentinel for a currency id that is not bound to any currency row. Snowflake ids are always
     * positive, so 0 marks "no currency" — used as the entity default and to skip charging when a
     * model has no valid currency.
     */
    const val NO_CURRENCY_ID: Long = 0L
}
