package com.lovelycatv.crystalframework.database.sharding

/**
 * Describes how a table is associated with a logical data source, and optionally how the
 * logical table is split into physical (actual) tables.
 *
 * Two levels, independent:
 * - Database routing (always): [dataSourceName] decides which data source executes the SQL.
 * - Table sharding (optional): when [shardingColumn], [actualTables] and [algorithm] are all
 *   provided, the logical [tableName] is rewritten to one of [actualTables] resolved by
 *   [algorithm] from the sharding column's value. These three fields are all-or-nothing —
 *   consistency is enforced by [R2dbcShardingRuleRegistry].
 */
data class R2dbcShardingRule(
    val tableName: String,
    val dataSourceName: String,
    val shardingColumn: String? = null,
    val actualTables: List<String> = emptyList(),
    val algorithm: ShardingAlgorithm? = null,
) {
    /** True when this rule carries a complete table-sharding definition. */
    val tableShardingEnabled: Boolean
        get() = shardingColumn != null && algorithm != null && actualTables.isNotEmpty()
}
