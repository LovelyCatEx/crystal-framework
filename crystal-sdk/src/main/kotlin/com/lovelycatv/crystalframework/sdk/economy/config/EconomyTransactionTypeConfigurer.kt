/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.economy.config

import com.lovelycatv.crystalframework.sdk.economy.EconomyTransactionTypeRegistry

/**
 * SPI: contribute economy transaction type declarations into [EconomyTransactionTypeRegistry]
 * during application startup. Implement as a Spring `@Component` — every bean of this type is
 * collected and invoked once by the registry's `@Bean` factory.
 */
fun interface EconomyTransactionTypeConfigurer {
    fun configure(registry: EconomyTransactionTypeRegistry)
}
