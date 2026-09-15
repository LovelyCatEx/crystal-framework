/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.config

import com.lovelycatv.crystalframework.sdk.gateway.GatewayRegistry
import com.lovelycatv.crystalframework.sdk.gateway.config.GatewayConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayRegistryConfiguration {
    @Bean
    fun gatewayRegistry(
        configurers: ObjectProvider<GatewayConfigurer>,
    ): GatewayRegistry {
        return GatewayRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
