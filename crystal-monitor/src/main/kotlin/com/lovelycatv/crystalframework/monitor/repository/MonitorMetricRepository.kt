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

    fun insert(type: MetricType, value: Double): Mono<*> {
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

    fun batchInsert(type: MetricType, points: List<MetricPoint>): Mono<*> {
        if (points.isEmpty()) return Mono.empty<Unit>()

        val values = points.indices.joinToString(", ") {
            "(:value_${it}, :created_time_${it})"
        }
        val sql = """
            INSERT INTO ${type.tableName} (value, created_time)
            VALUES $values
        """.trimIndent()

        var spec = databaseClient.sql(sql)
        points.forEachIndexed { i, point ->
            spec = spec.bind("value_${i}", point.value)
                .bind("created_time_${i}", point.timestamp)
        }
        return spec.then()
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

    fun deleteBeforeTime(type: MetricType, beforeTime: Long): Mono<Long> {
        return databaseClient.sql(
            "DELETE FROM ${type.tableName} WHERE created_time < :beforeTime"
        )
            .bind("beforeTime", beforeTime)
            .fetch()
            .rowsUpdated()
    }

}
