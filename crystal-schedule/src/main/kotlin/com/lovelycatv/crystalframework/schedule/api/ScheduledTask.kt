package com.lovelycatv.crystalframework.schedule.api

import com.lovelycatv.crystalframework.schedule.api.context.TaskExecutionContext

/**
 * Abstract interface for scheduled tasks.
 * All concrete scheduled task logic should implement this interface.
 */
fun interface ScheduledTask {
    /**
     * Core execution logic of the task.
     *
     * @param context Task execution context
     * @return Task execution result
     */
    suspend fun execute(context: TaskExecutionContext): TaskResult
}
