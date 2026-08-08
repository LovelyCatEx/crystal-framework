package com.lovelycatv.crystalframework.database

data class R2dbcRouteDecision(
    val tableName: String?,
    val shardingColumn: String?,
    val ruleName: String?,
    val dataSourceName: String,
)