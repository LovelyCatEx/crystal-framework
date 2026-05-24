package com.lovelycatv.crystalframework.monitor.config

import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.sdk.database.config.TableConfigurer
import com.lovelycatv.crystalframework.sdk.database.TableRegistry
import org.springframework.stereotype.Component

@Component
class MonitorTableConfigurer : TableConfigurer {
    override fun configure(registry: TableRegistry) {
        MetricType.entries.forEach { type ->
            registry.register(type.tableName, isBaseEntity = false)
        }
    }
}
