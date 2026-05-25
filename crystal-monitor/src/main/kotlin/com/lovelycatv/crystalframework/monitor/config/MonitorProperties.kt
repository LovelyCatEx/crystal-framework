package com.lovelycatv.crystalframework.monitor.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("crystalframework.monitor")
class MonitorProperties {
    /** Flush interval in milliseconds. Default 5000 (5 seconds). */
    var flushIntervalMs: Long = 5000
}
