package com.lovelycatv.crystalframework.schedule.api.context

import com.aizuda.snailjob.client.job.core.dto.JobArgs
import java.util.*

class SnailTaskExecutionContext(
    private val args: Map<String, Any?> = emptyMap(),
    override val triggerTime: Long = System.currentTimeMillis(),
    override val executionId: String = UUID.randomUUID().toString(),
    private val originalJobArgs: JobArgs
) : TaskExecutionContext {
    override fun getArgs(): Map<String, Any?> = args
}