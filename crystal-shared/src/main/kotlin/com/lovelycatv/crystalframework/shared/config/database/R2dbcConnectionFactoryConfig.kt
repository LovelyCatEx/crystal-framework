package com.lovelycatv.crystalframework.shared.config.database

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
