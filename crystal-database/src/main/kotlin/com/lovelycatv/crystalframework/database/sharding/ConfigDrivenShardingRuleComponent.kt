package com.lovelycatv.crystalframework.database.sharding

import com.lovelycatv.crystalframework.database.constants.ShardingConstants
import com.lovelycatv.crystalframework.database.exception.ShardingException
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import org.springframework.context.ApplicationContext

/**
 * [R2dbcShardingRuleComponent] backed by `crystalframework.sharding.rules` in application.yaml.
 *
 * Each declared rule is a plain data mapping (table -> data source, optional sharding column /
 * actual tables); the behavioral part — the [ShardingAlgorithm] — cannot live in YAML and is
 * supplied one of two mutually exclusive ways: `algorithm-ref` references a Spring bean by name,
 * resolved from the context at rule-build time, while `inline-expression` carries a SpEL "value ->
 * table name" expression evaluated by [SpelInlineShardingAlgorithm]. Declaring both is rejected.
 * Normalization, the all-or-nothing table-sharding contract and duplicate-table detection are all
 * enforced by [R2dbcShardingRuleRegistry]; this component only maps config to [R2dbcShardingRule].
 */
class ConfigDrivenShardingRuleComponent(
    private val configuration: CrystalFrameworkConfiguration,
    private val applicationContext: ApplicationContext,
) : R2dbcShardingRuleComponent {
    override val name: String = ShardingConstants.CONFIG_DRIVEN_RULE_COMPONENT_NAME

    override fun rules(): Collection<R2dbcShardingRule> {
        // Empty when no ShardingAlgorithm bean is defined; never throws.
        val algorithms = applicationContext.getBeansOfType(ShardingAlgorithm::class.java)
        return configuration.sharding.rules.map { rule ->
            val algorithmRef = rule.algorithmRef.trim().takeIf(String::isNotEmpty)
            val inlineExpression = rule.inlineExpression.trim().takeIf(String::isNotEmpty)
            if (algorithmRef != null && inlineExpression != null) {
                throw ShardingException(
                    "Config-driven sharding rule for table '${rule.table}' declares both algorithm-ref " +
                        "and inline-expression; exactly one may be set",
                )
            }
            val algorithm = when {
                algorithmRef != null -> algorithms[algorithmRef] ?: throw ShardingException(
                    "Config-driven sharding rule for table '${rule.table}' references algorithm bean " +
                        "'$algorithmRef' which does not exist",
                )
                inlineExpression != null -> SpelInlineShardingAlgorithm(rule.dataSource, inlineExpression)
                else -> null
            }
            R2dbcShardingRule(
                tableName = rule.table,
                dataSourceName = rule.dataSource,
                shardingColumn = rule.shardingColumn,
                actualTables = rule.actualTables.toList(),
                algorithm = algorithm,
            )
        }
    }
}
