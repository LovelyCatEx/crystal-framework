package com.lovelycatv.crystalframework.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("crystalframework.sharding")
class ShardingConfiguration {
    var snowflake: Snowflake = Snowflake()

    class Snowflake {
        var startPoint: Long = 0
        var timestampLength: Int = 41
        var dataCenterIdLength: Int = 5
        var workerIdLength: Int = 5
        var sequenceIdLength: Int = 12
        var geneIdLength: Int = 0
        var dataCenterId: Long = 0
        var workerId: Long = 0
        var actualGeneLength: Int = 0
    }
}
