/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.economy.config

import com.lovelycatv.crystalframework.sdk.economy.EconomyTransactionTypeRegistry
import com.lovelycatv.crystalframework.sdk.economy.config.EconomyTransactionTypeConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EconomyTransactionTypeRegistryConfiguration {
    @Bean
    fun economyTransactionTypeRegistry(
        configurers: ObjectProvider<EconomyTransactionTypeConfigurer>,
    ): EconomyTransactionTypeRegistry {
        return EconomyTransactionTypeRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
