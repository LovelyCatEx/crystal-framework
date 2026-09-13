/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.database

data class R2dbcRouteDecision(
    val tableName: String?,
    val shardingColumn: String?,
    val ruleName: String?,
    val dataSourceName: String,
)