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
