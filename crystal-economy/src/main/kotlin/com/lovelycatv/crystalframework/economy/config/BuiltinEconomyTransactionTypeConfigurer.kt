/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.config

import com.lovelycatv.crystalframework.economy.types.EconomyTransactionType
import com.lovelycatv.crystalframework.sdk.economy.EconomyTransactionTypeRegistry
import com.lovelycatv.crystalframework.sdk.economy.config.EconomyTransactionTypeConfigurer
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Registers the framework's built-in [EconomyTransactionType]s. `HIGHEST_PRECEDENCE` so third-party
 * configurers observe the built-ins already present and can fail-fast on typeId / key collisions.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class BuiltinEconomyTransactionTypeConfigurer : EconomyTransactionTypeConfigurer {
    override fun configure(registry: EconomyTransactionTypeRegistry) {
        registry.registers(EconomyTransactionType.entries)
    }
}
