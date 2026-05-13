package com.lovelycatv.crystalframework.schedule.adapter.snailjob

import com.aizuda.snailjob.client.job.core.annotation.JobExecutor
import com.aizuda.snailjob.client.job.core.dto.JobArgs
import com.aizuda.snailjob.common.log.SnailJobLog
import com.aizuda.snailjob.model.dto.ExecuteResult
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.SnailTaskExecutionContext
import com.lovelycatv.crystalframework.schedule.registry.TaskRegistry
import com.lovelycatv.crystalframework.shared.utils.parseObject
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

/**
 * SnailJob task adapter.
 * Converts SnailJob scheduling triggers into unified ScheduledTask execution.
 */
@Component
class SnailJobTaskAdapter(
    private val taskRegistry: TaskRegistry
) {

    /**
     * Generic task execution entry point.
     * All SnailJob configured tasks point to this method, dispatching to concrete ScheduledTask via jobBizId.
     */
    @JobExecutor(name = "__crystal_framework_scheduled_task_proxy__")
    fun execute(jobArgs: JobArgs): ExecuteResult {
        return runBlocking {
            val jobName = jobArgs.jobBizId
                ?: return@runBlocking ExecuteResult.failure("Job biz id is missing")

            val task: ScheduledTask = taskRegistry.getTask(jobName)
                ?: return@runBlocking ExecuteResult.failure("Task not found: $jobName")

            @Suppress("UNCHECKED_CAST")
            val args = try {
                when (val params = jobArgs.jobParams) {
                    is Map<*, *> -> params.filterKeys { it is String }.mapKeys { it.key as String }
                    is String -> {
                        params.parseObject<Map<String, Any?>>()
                    }
                    else -> throw IllegalArgumentException("Invalid type of job params")
                }
            } catch (e: Exception) {
                mapOf(
                    "__parameter_type__" to (jobArgs.jobParams::class.qualifiedName ?: ""),
                    "__error_message__" to e.message
                )
            }

            val context = SnailTaskExecutionContext(
                args = args,
                executionId = jobArgs.taskBatchId?.toString() ?: UUID.randomUUID().toString(),
                originalJobArgs = jobArgs
            )

            SnailJobLog.REMOTE.info("[SnailJob] Executing task: $jobName, executionId: ${context.executionId}")

            try {
                when (val result = task.execute(context)) {
                    is TaskResult.Success -> {
                        SnailJobLog.REMOTE.info("[SnailJob] Task $jobName executed successfully: ${result.message}")
                        ExecuteResult.success(result.data)
                    }
                    is TaskResult.Failure -> {
                        SnailJobLog.REMOTE.error("[SnailJob] Task $jobName failed: ${result.message}")
                        ExecuteResult.failure(result.message ?: "Task execution failed")
                    }
                }
            } catch (e: Exception) {
                SnailJobLog.REMOTE.error("[SnailJob] Task $jobName exception: ${e.message}")
                ExecuteResult.failure(e.message ?: "Unknown error")
            }
        }
    }
}
