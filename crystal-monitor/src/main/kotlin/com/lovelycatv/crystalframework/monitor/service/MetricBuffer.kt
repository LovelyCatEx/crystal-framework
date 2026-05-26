package com.lovelycatv.crystalframework.monitor.service

import com.lovelycatv.crystalframework.monitor.types.MetricPoint
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.shared.types.system.event.SystemMetricsEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MetricBuffer {

    private val buffer = ConcurrentHashMap<MetricType, MutableList<MetricPoint>>()

    fun append(event: SystemMetricsEvent) {
        for (type in MetricType.entries) {
            val value = extractValue(type, event)
            val list = buffer.computeIfAbsent(type) { ArrayList() }
            synchronized(list) {
                list.add(MetricPoint(value, event.timestamp))
            }
        }
    }

    fun drain(): Map<MetricType, List<MetricPoint>> {
        val snapshot = HashMap<MetricType, List<MetricPoint>>()
        for ((type, list) in buffer) {
            synchronized(list) {
                if (list.isNotEmpty()) {
                    snapshot[type] = ArrayList(list)
                    list.clear()
                }
            }
        }
        return snapshot
    }

    fun getUnflushedInRange(type: MetricType, startTime: Long, endTime: Long): List<MetricPoint> {
        val list = buffer[type] ?: return emptyList()
        synchronized(list) {
            return list.filter { it.timestamp in startTime..endTime }
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
