/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database.exception

/**
 * Thrown when a statement targeting a sharded table cannot be routed to exactly one actual table
 * under the single-table precise-sharding model: no sharding-column equality predicate, a range /
 * IN / OR predicate, a batch that spans multiple actual tables, or an algorithm returning a table
 * outside the declared actual-table set.
 */
class ShardingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)