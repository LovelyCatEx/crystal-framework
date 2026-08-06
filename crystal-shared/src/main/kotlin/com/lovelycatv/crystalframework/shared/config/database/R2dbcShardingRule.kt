package com.lovelycatv.crystalframework.shared.config.database

/**
 * Describes how a table is associated with a logical data source.
 */
data class R2dbcShardingRule(
    val tableName: String,
    val dataSourceName: String,
    val shardingColumn: String? = null,
)
