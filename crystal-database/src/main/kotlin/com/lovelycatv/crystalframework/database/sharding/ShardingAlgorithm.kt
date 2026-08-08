package com.lovelycatv.crystalframework.database.sharding

/**
 * User-supplied SPI that maps a single sharding-column value to a target data source and table.
 *
 * Modeled after ShardingSphere's `StandardShardingAlgorithm` precise-sharding path: given the
 * logical table and the concrete value of the sharding column extracted from a statement, return
 * the target data source and actual table the statement must be rewritten to target.
 *
 * Contract:
 * - Only precise single-value sharding is supported. The engine never calls this for range / IN /
 *   missing-key statements — those are rejected before reaching the algorithm.
 * - The returned dataSourceName MUST exist in [com.lovelycatv.crystalframework.database.R2dbcConnectionPoolRegistry]; the engine validates
 *   this and throws [com.lovelycatv.crystalframework.database.exception.ShardingException] otherwise.
 * - The returned tableName MUST be one of the rule's declared actual tables (for table sharding)
 *   or the logical table itself (for database-only sharding); the engine validates this.
 * - Implementations must be stateless and thread-safe: a single instance is shared across all
 *   reactive executions.
 *
 * Examples:
 * - Database-only sharding: return ShardingTarget("db_$shard", logicalTable)
 * - Table-only sharding: return ShardingTarget("primary", "${logicalTable}_$shard")
 * - Database + table sharding: return ShardingTarget("db_$dbShard", "${logicalTable}_$tableShard")
 */
interface ShardingAlgorithm {
    /**
     * @param logicalTable the logical table name (already normalized to lower-case)
     * @param shardingValue the concrete value bound to the sharding column, or null when the bound
     *   value is SQL NULL
     * @return target data source and table
     */
    fun doSharding(logicalTable: String, shardingValue: Any?): ShardingTarget
}
