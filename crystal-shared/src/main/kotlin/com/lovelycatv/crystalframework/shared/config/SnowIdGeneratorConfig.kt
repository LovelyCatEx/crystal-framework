package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode

@Configuration(proxyBeanMethods = false)
class SnowIdGeneratorConfig {
    @Bean
    @Scope(proxyMode = ScopedProxyMode.NO)
    @ConditionalOnMissingBean(SnowIdGenerator::class)
    fun snowIdGenerator(shardingConfiguration: ShardingConfiguration): SnowIdGenerator {
        val s = shardingConfiguration.snowflake
        return SnowIdGenerator(
            s.startPoint,
            s.timestampLength,
            s.dataCenterIdLength,
            s.workerIdLength,
            s.sequenceIdLength,
            s.geneIdLength,
            s.dataCenterId,
            s.workerId,
            s.actualGeneLength,
        )
    }
}
