package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.service.SnowflakeIdGeneratorRegistry
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class SnowflakeIdGeneratorRegistryConfig {
    @Bean
    fun snowflakeIdGeneratorRegistry(
        config: CrystalFrameworkConfiguration,
        nodeLease: SnowflakeNodeLease,
    ): SnowflakeIdGeneratorRegistry =
        SnowflakeIdGeneratorRegistry(config.sharding.snowflake, nodeLease)

    @Bean
    fun snowIdGenerator(registry: SnowflakeIdGeneratorRegistry): SnowIdGenerator = registry.defaultGenerator()
}
