package com.lovelycatv.crystalframework.shared.config.database

/**
 * User-supplied SPI that maps a single sharding-column value to a physical (actual) table name.
 *
 * Modeled after ShardingSphere's `StandardShardingAlgorithm` precise-sharding path: given the
 * logical table and the concrete value of the sharding column extracted from a statement, return
 * the one actual table the statement must be rewritten to target.
 *
 * Contract:
 * - Only precise single-value sharding is supported. The engine never calls this for range / IN /
 *   missing-key statements — those are rejected before reaching the algorithm.
 * - The returned name MUST be one of the rule's declared actual tables; the engine validates this
 *   and throws [ShardingException] otherwise.
 * - Implementations must be stateless and thread-safe: a single instance is shared across all
 *   reactive executions.
 */
interface ShardingAlgorithm {
    /**
     * @param logicalTable the logical table name (already normalized to lower-case)
     * @param shardingValue the concrete value bound to the sharding column, or null when the bound
     *   value is SQL NULL
     * @return the actual table name to rewrite the statement to
     */
    fun doSharding(logicalTable: String, shardingValue: Any?): String
}
