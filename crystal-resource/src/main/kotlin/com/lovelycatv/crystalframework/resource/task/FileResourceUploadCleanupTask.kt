package com.lovelycatv.crystalframework.resource.task

import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext
import org.springframework.stereotype.Component

@Component
@ScheduledTaskMetadata(
    name = "FileResourceUploadCleanupTask",
    description = "Clean up expired or failed file uploads every 10 minutes",
    group = "resource"
)
@CronTaskExecutor(cron = "0 */10 * * * ?")
class FileResourceUploadCleanupTask(
    private val fileResourceService: FileResourceService,
) : ScheduledTask {

    override suspend fun execute(context: TaskExecutionContext): TaskResult {
        val cleaned = fileResourceService.cleanupUploads(System.currentTimeMillis())
        return TaskResult.Success("Cleaned up $cleaned expired/failed file upload records")
    }
}
