package com.lovelycatv.crystalframework.monitor.task

import com.lovelycatv.crystalframework.monitor.repository.MonitorMetricRepository
import com.lovelycatv.crystalframework.monitor.types.MetricType
import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ScheduledTaskMetadata(name = "MonitorDataCleanupTask", description = "Clean up monitor metrics older than 14 days at 1:00 AM daily", group = "monitor")
@CronTaskExecutor(cron = "0 0 1 * * ?")
class MonitorDataCleanupTask(
    private val monitorMetricRepository: MonitorMetricRepository,
) : ScheduledTask {

    override suspend fun execute(context: TaskExecutionContext): TaskResult {
        val cutoffTime = System.currentTimeMillis() - Duration.ofDays(14).toMillis()
        var totalDeleted = 0L

        for (type in MetricType.entries) {
            val deleted = monitorMetricRepository.deleteBeforeTime(type, cutoffTime).awaitSingle()
            totalDeleted += deleted
        }

        return TaskResult.Success("Cleaned up $totalDeleted monitor metric records older than 14 days")
    }
}
