/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
