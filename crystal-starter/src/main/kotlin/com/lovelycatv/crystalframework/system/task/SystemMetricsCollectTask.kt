package com.lovelycatv.crystalframework.system.task

import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext
import com.lovelycatv.crystalframework.shared.types.system.event.SystemMetricsEvent
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import java.io.File
import java.lang.management.ManagementFactory

@Component
@ScheduledTaskMetadata(name = "SystemMetricsCollectTask", description = "Collect system metrics every second", group = "system")
@CronTaskExecutor(cron = "0/1 * * * * ?")
class SystemMetricsCollectTask(
    private val eventPublisher: ApplicationEventPublisher,
    private val meterRegistry: MeterRegistry,
) : ScheduledTask {

    override suspend fun execute(context: TaskExecutionContext): TaskResult {
        val osBean = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
        val memoryMXBean = ManagementFactory.getMemoryMXBean()
        val gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans()

        val rootFile = File(".").canonicalFile.toPath().root.toFile()
        val diskTotal = rootFile.totalSpace
        val diskUsable = rootFile.usableSpace

        val event = SystemMetricsEvent(
            timestamp = System.currentTimeMillis(),
            cpuUsage = (osBean?.cpuLoad ?: 0.0) * 100.0,
            systemLoadAverage = osBean?.systemLoadAverage ?: 0.0,
            availableProcessors = osBean?.availableProcessors ?: 0,
            memoryTotal = osBean?.totalMemorySize ?: 0,
            memoryUsed = (osBean?.totalMemorySize ?: 0) - (osBean?.freeMemorySize ?: 0),
            jvmHeapMax = memoryMXBean.heapMemoryUsage.max,
            jvmHeapUsed = memoryMXBean.heapMemoryUsage.used,
            jvmNonHeapCommitted = memoryMXBean.nonHeapMemoryUsage.committed,
            jvmNonHeapUsed = memoryMXBean.nonHeapMemoryUsage.used,
            diskTotal = diskTotal,
            diskUsed = diskTotal - diskUsable,
            dbActiveConnections = try {
                meterRegistry.get("r2dbc.pool.acquired").gauge().value().toInt()
            } catch (_: Exception) {
                0
            },
            dbMaxConnections = try {
                meterRegistry.get("r2dbc.pool.max.allocated").gauge().value().toInt()
            } catch (_: Exception) {
                -1
            },
            gcCount = gcMXBeans.sumOf { it.collectionCount },
            gcTotalTime = gcMXBeans.sumOf { it.collectionTime },
        )

        eventPublisher.publishEvent(event)
        return TaskResult.Success("System metrics collected")
    }
}
