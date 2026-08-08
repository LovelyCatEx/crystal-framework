package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.R2dbcDataSourceRegistry

class R2dbcShardingRuleRegistry(
    components: Collection<R2dbcShardingRuleComponent>,
    dataSources: R2dbcDataSourceRegistry,
) {
    private val rulesByTable = linkedMapOf<String, RegisteredRule>()
    private val componentsByName = linkedMapOf<String, R2dbcShardingRuleComponent>()

    init {
        components.forEach { component ->
            val componentName = component.name.trim()
            require(componentName.isNotEmpty()) {
                "R2dbcShardingRuleRegistry: component name must not be blank"
            }
            if (componentsByName.putIfAbsent(componentName, component) != null) {
                throw IllegalStateException(
                    "R2dbcShardingRuleRegistry: duplicate component name '$componentName'",
                )
            }

            val componentTables = mutableSetOf<String>()
            component.rules().forEach { rule ->
                val tableName = rule.tableName.trim().lowercase()
                require(tableName.isNotEmpty()) {
                    "R2dbcShardingRuleRegistry: table name for component '$componentName' must not be blank"
                }
                require(componentTables.add(tableName)) {
                    "R2dbcShardingRuleRegistry: component '$componentName' declares table '$tableName' more than once"
                }
                val dataSourceName = rule.dataSourceName.trim()
                dataSources.require(dataSourceName)
                val normalizedRule = normalizeAndValidate(componentName, tableName, dataSourceName, rule)
                if (rulesByTable.putIfAbsent(
                        tableName,
                        RegisteredRule(componentName, normalizedRule),
                    ) != null
                ) {
                    throw IllegalStateException(
                        "R2dbcShardingRuleRegistry: duplicate table '$tableName'",
                    )
                }
            }
        }
    }

    /**
     * Normalizes the [rule]'s table-level fields and enforces the all-or-nothing table-sharding
     * contract: [R2dbcShardingRule.shardingColumn], [R2dbcShardingRule.algorithm] and
     * [R2dbcShardingRule.actualTables] must either all be present (table sharding) or all be absent
     * (data-source routing only).
     */
    private fun normalizeAndValidate(
        componentName: String,
        tableName: String,
        dataSourceName: String,
        rule: R2dbcShardingRule,
    ): R2dbcShardingRule {
        val shardingColumn = rule.shardingColumn?.trim()?.takeIf(String::isNotEmpty)
        val algorithm = rule.algorithm
        val actualTables = rule.actualTables
            .map { it.trim().lowercase() }
            .filter(String::isNotEmpty)

        // `shardingColumn` alone is data-source routing metadata (consumed by R2dbcRouteDecision) and
        // does NOT imply table sharding. Table sharding is only in play once an algorithm or an actual
        // table list is declared; then all three fields must be present together.
        val tableShardingConfigured = algorithm != null || actualTables.isNotEmpty()
        if (tableShardingConfigured) {
            require(shardingColumn != null) {
                "R2dbcShardingRuleRegistry: component '$componentName' table '$tableName' declares table " +
                    "sharding but shardingColumn is blank"
            }
            require(algorithm != null) {
                "R2dbcShardingRuleRegistry: component '$componentName' table '$tableName' declares table " +
                    "sharding but algorithm is null"
            }
            require(actualTables.isNotEmpty()) {
                "R2dbcShardingRuleRegistry: component '$componentName' table '$tableName' declares table " +
                    "sharding but actualTables is empty"
            }
            require(actualTables.toSet().size == actualTables.size) {
                "R2dbcShardingRuleRegistry: component '$componentName' table '$tableName' declares duplicate " +
                    "actual tables $actualTables"
            }
        }

        return rule.copy(
            tableName = tableName,
            dataSourceName = dataSourceName,
            shardingColumn = shardingColumn,
            actualTables = actualTables,
            algorithm = algorithm,
        )
    }

    fun find(tableName: String?): RegisteredRule? {
        return tableName?.trim()?.lowercase()?.let(rulesByTable::get)
    }

    fun rules(): List<RegisteredRule> = rulesByTable.values.toList()

    /**
     * True when no rule is registered. [com.lovelycatv.crystalframework.database.CrystalShardingConnection] uses this to skip re-parsing every
     * outbound statement in deployments that declare no sharding at all.
     */
    fun isEmpty(): Boolean = rulesByTable.isEmpty()

    data class RegisteredRule(
        val componentName: String,
        val rule: R2dbcShardingRule,
    )
}
