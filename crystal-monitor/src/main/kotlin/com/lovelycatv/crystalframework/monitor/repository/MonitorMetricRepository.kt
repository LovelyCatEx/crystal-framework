package com.lovelycatv.crystalframework.monitor.repository

import com.lovelycatv.crystalframework.monitor.types.MetricPoint
import com.lovelycatv.crystalframework.monitor.types.MetricType
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class MonitorMetricRepository(
    private val databaseClient: DatabaseClient,
) {

    fun insert(type: MetricType, value: Double): Mono<Void> {
        val now = System.currentTimeMillis()
        return databaseClient.sql(
            """
            INSERT INTO ${type.tableName} (value, created_time)
            VALUES (:value, :createdTime)
            """.trimIndent()
        )
            .bind("value", value)
            .bind("createdTime", now)
            .then()
    }

    fun findByTimeRange(
        type: MetricType,
        startTime: Long,
        endTime: Long,
    ): Flux<MetricPoint> {
        return databaseClient.sql(
            """
            SELECT value, created_time AS timestamp
            FROM ${type.tableName}
            WHERE created_time BETWEEN :startTime AND :endTime
            ORDER BY created_time ASC
            """.trimIndent()
        )
            .bind("startTime", startTime)
            .bind("endTime", endTime)
            .map { row, _ ->
                MetricPoint(
                    value = row.get("value", Double::class.java) ?: 0.0,
                    timestamp = row.get("timestamp", Long::class.java) ?: 0L,
                )
            }
            .all()
    }

    fun findAggregation(
        type: MetricType,
        startTime: Long,
        endTime: Long,
    ): Mono<MetricAggregation> {
        return databaseClient.sql(
            """
            SELECT AVG(value) AS avg, MAX(value) AS max, MIN(value) AS min
            FROM ${type.tableName}
            WHERE created_time BETWEEN :startTime AND :endTime
            """.trimIndent()
        )
            .bind("startTime", startTime)
            .bind("endTime", endTime)
            .map { row, _ ->
                MetricAggregation(
                    avg = row.get("avg", Double::class.java),
                    max = row.get("max", Double::class.java),
                    min = row.get("min", Double::class.java),
                )
            }
            .one()
    }
}
