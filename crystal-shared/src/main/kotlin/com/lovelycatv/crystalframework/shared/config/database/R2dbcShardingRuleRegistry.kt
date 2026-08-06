package com.lovelycatv.crystalframework.shared.config.database

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
                if (rulesByTable.putIfAbsent(
                        tableName,
                        RegisteredRule(
                            componentName,
                            rule.copy(
                                tableName = tableName,
                                dataSourceName = dataSourceName,
                                shardingColumn = rule.shardingColumn?.trim()?.takeIf(String::isNotEmpty),
                            ),
                        ),
                    ) != null
                ) {
                    throw IllegalStateException(
                        "R2dbcShardingRuleRegistry: duplicate table '$tableName'",
                    )
                }
            }
        }
    }

    fun find(tableName: String?): RegisteredRule? {
        return tableName?.trim()?.lowercase()?.let(rulesByTable::get)
    }

    fun rules(): List<RegisteredRule> = rulesByTable.values.toList()

    data class RegisteredRule(
        val componentName: String,
        val rule: R2dbcShardingRule,
    )
}
