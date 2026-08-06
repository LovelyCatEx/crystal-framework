package com.lovelycatv.crystalframework.shared.config.database

/**
 * Thrown when a statement targeting a sharded table cannot be routed to exactly one actual table
 * under the single-table precise-sharding model: no sharding-column equality predicate, a range /
 * IN / OR predicate, a batch that spans multiple actual tables, or an algorithm returning a table
 * outside the declared actual-table set.
 */
class ShardingException(message: String) : RuntimeException(message)
