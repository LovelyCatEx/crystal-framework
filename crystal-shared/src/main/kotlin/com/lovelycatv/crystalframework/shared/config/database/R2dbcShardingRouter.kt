package com.lovelycatv.crystalframework.shared.config.database

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
