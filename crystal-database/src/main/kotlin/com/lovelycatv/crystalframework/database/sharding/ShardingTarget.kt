/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.sharding

/**
 * Result of sharding algorithm execution: target data source and table.
 *
 * Enables both database sharding and table sharding in a single routing decision.
 * - Database-only sharding: all instances return same tableName (logical table)
 * - Table-only sharding: all instances return same dataSourceName (e.g. "primary")
 * - Database + table sharding: both fields vary based on sharding value
 *
 * @param dataSourceName name of target data source, must exist in [com.lovelycatv.crystalframework.database.R2dbcConnectionPoolRegistry]
 * @param tableName name of target table (actual table for table sharding, logical table otherwise)
 */
data class ShardingTarget(
    val dataSourceName: String,
    val tableName: String,
)
