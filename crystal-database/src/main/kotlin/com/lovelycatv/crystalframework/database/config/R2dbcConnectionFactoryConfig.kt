/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.config

import com.lovelycatv.crystalframework.database.CrystalShardingConnectionFactory
import com.lovelycatv.crystalframework.database.R2dbcConnectionPoolRegistry
import com.lovelycatv.crystalframework.database.sharding.R2dbcShardingRuleRegistry
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration(proxyBeanMethods = false)
class R2dbcConnectionFactoryConfig {
    @Bean(name = ["connectionFactory"])
    @Primary
    fun connectionFactory(
        poolRegistry: R2dbcConnectionPoolRegistry,
        shardingRuleRegistry: R2dbcShardingRuleRegistry,
    ): ConnectionFactory {
        return CrystalShardingConnectionFactory(
            poolRegistry,
            shardingRuleRegistry,
        )
    }
}
