package com.lovelycatv.crystalframework.schedule.api

/**
 * Task execution result.
 */
sealed class TaskResult {
    abstract val message: String?

    data class Success(
        override val message: String? = null,
        val data: Any? = null
    ) : TaskResult()

    data class Failure(
        override val message: String? = null,
        val exception: Throwable? = null
    ) : TaskResult()
}
