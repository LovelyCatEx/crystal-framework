/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.R2dbcRouteDecision

class R2dbcShardingRouter(
    private val defaultDataSourceName: String,
    private val rules: R2dbcShardingRuleRegistry,
) {
    fun resolve(tableName: String?): R2dbcRouteDecision {
        val normalizedTableName = tableName?.trim()?.lowercase()?.takeIf(String::isNotEmpty)
        val registeredRule = rules.find(normalizedTableName)
        return if (registeredRule == null) {
            R2dbcRouteDecision(
                tableName = normalizedTableName,
                shardingColumn = null,
                ruleName = null,
                dataSourceName = defaultDataSourceName,
            )
        } else {
            R2dbcRouteDecision(
                tableName = normalizedTableName,
                shardingColumn = registeredRule.rule.shardingColumn,
                ruleName = registeredRule.componentName,
                dataSourceName = registeredRule.rule.dataSourceName,
            )
        }
    }
}
