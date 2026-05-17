package com.lovelycatv.crystalframework.schedule.api.context

/**
 * Task execution context.
 * Encapsulates parameters and environment information during task execution.
 */
interface TaskExecutionContext {
    /**
     * Get all task parameters.
     */
    fun getArgs(): Map<String, Any?>

    /**
     * Get a parameter by name.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getArg(name: String): T? {
        return getArgs()[name] as? T?
    }

    /**
     * Timestamp when the task was triggered.
     */
    val triggerTime: Long

    /**
     * Unique execution ID for each run.
     */
    val executionId: String
}