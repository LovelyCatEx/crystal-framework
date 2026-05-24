package com.lovelycatv.crystalframework.monitor.service

import com.lovelycatv.crystalframework.monitor.repository.MetricAggregation
import com.lovelycatv.crystalframework.monitor.repository.MonitorMetricRepository
import com.lovelycatv.crystalframework.monitor.types.MetricAggregationResponse
import com.lovelycatv.crystalframework.monitor.types.MetricPoint
import com.lovelycatv.crystalframework.monitor.types.MetricQueryResponse
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.monitor.utils.TimeUtils
import com.lovelycatv.crystalframework.shared.types.system.event.SystemMetricsEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MonitorMetricService(
    private val repository: MonitorMetricRepository,
) {

    fun record(type: MetricType, value: Double): Mono<Void> {
        return repository.insert(type, value)
    }

    fun recordAll(event: SystemMetricsEvent): Flux<Void> {
        return Flux.fromIterable(MetricType.entries)
            .flatMap { type ->
                val value = extractValue(type, event)
                repository.insert(type, value)
            }
    }

    fun query(type: MetricType, duration: String): Mono<MetricQueryResponse> {
        val now = System.currentTimeMillis()
        val startTime = now - TimeUtils.parseDuration(duration)
        return repository.findByTimeRange(type, startTime, now)
            .collectList()
            .map { MetricQueryResponse(metricType = type, data = it) }
    }

    fun queryCustomRange(type: MetricType, startTime: Long, endTime: Long): Mono<MetricQueryResponse> {
        return repository.findByTimeRange(type, startTime, endTime)
            .collectList()
            .map { MetricQueryResponse(metricType = type, data = it) }
    }

    fun aggregate(type: MetricType, duration: String): Mono<MetricAggregationResponse> {
        val now = System.currentTimeMillis()
        val startTime = now - TimeUtils.parseDuration(duration)
        return repository.findAggregation(type, startTime, now)
            .map {
                MetricAggregationResponse(
                    metricType = type,
                    avg = it.avg,
                    max = it.max,
                    min = it.min,
                    startTime = startTime,
                    endTime = now,
                )
            }
    }

    private fun extractValue(type: MetricType, event: SystemMetricsEvent): Double {
        return when (type) {
            MetricType.CPU_USAGE -> event.cpuUsage
            MetricType.CPU_LOAD_AVERAGE -> event.systemLoadAverage
            MetricType.MEMORY_USED -> event.memoryUsed.toDouble()
            MetricType.JVM_HEAP_USED -> event.jvmHeapUsed.toDouble()
            MetricType.JVM_NONHEAP_COMMITTED -> event.jvmNonHeapCommitted.toDouble()
            MetricType.JVM_NONHEAP_USED -> event.jvmNonHeapUsed.toDouble()
            MetricType.DISK_USED -> event.diskUsed.toDouble()
            MetricType.DB_CONNECTIONS_ACTIVE -> event.dbActiveConnections.toDouble()
            MetricType.GC_COUNT -> event.gcCount.toDouble()
            MetricType.GC_TIME -> event.gcTotalTime.toDouble()
        }
    }
}
