package com.lovelycatv.crystalframework.schedule.api.context

import java.util.*

/**
 * Default implementation of task execution context.
 */
open class DefaultTaskExecutionContext(
    private val args: Map<String, Any?> = emptyMap(),
    override val triggerTime: Long = System.currentTimeMillis(),
    override val executionId: String = UUID.randomUUID().toString()
) : TaskExecutionContext {
    override fun getArgs(): Map<String, Any?> = args
}
