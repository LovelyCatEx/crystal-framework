/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
