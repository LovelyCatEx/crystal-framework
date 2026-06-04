package com.lovelycatv.crystalframework.monitor.service

import com.lovelycatv.crystalframework.monitor.repository.MonitorMetricRepository
import com.lovelycatv.crystalframework.monitor.types.MetricPoint
import com.lovelycatv.crystalframework.monitor.types.MetricQueryResponse
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.monitor.utils.TimeUtils
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MonitorMetricService(
    private val repository: MonitorMetricRepository,
    private val metricBuffer: MetricBuffer,
) {

    fun record(type: MetricType, value: Double): Mono<*> {
        return repository.insert(type, value)
    }

    @Scheduled(fixedRateString = "#{@crystalFrameworkConfiguration.monitor.flushIntervalMs}")
    fun flushBuffer() {
        val snapshot = metricBuffer.drain()
        if (snapshot.isEmpty()) return

        Flux.fromIterable(snapshot.entries)
            .flatMap { (type, points) ->
                repository.batchInsert(type, points)
            }
            .then()
            .subscribe()
    }

    fun batchQuery(types: List<MetricType>, duration: String): Mono<Map<String, MetricQueryResponse>> {
        return Flux.fromIterable(types)
            .flatMap { type -> query(type, duration).map { type.name to it } }
            .collectMap({ it.first }, { it.second })
    }

    fun query(type: MetricType, duration: String): Mono<MetricQueryResponse> {
        val now = System.currentTimeMillis()
        val startTime = now - TimeUtils.parseDuration(duration)
        return repository.findByTimeRange(type, startTime, now)
            .collectList()
            .map { dbPoints ->
                val buffered = metricBuffer.getUnflushedInRange(type, startTime, now)
                val merged = (dbPoints + buffered)
                    .distinctBy { it.timestamp }
                    .sortedBy { it.timestamp }
                MetricQueryResponse(metricType = type, data = merged)
            }
    }

}
