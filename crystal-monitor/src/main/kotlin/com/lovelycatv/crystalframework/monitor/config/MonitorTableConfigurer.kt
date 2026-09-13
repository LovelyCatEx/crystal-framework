/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
