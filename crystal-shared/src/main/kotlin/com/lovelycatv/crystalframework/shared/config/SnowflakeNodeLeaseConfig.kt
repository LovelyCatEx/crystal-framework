/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SnowflakeNodeLeaseConfig {
    @Bean
    fun snowflakeNodeLease(
        redisService: ReactiveRedisService,
        config: CrystalFrameworkConfiguration,
    ): SnowflakeNodeLease = SnowflakeNodeLease(redisService, config.sharding.snowflake)
}
