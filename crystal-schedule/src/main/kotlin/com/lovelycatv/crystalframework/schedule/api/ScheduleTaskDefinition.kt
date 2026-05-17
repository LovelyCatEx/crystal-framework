package com.lovelycatv.crystalframework.schedule.api

import com.lovelycatv.crystalframework.schedule.annotations.ScheduledTaskMetadata

/**
 * Scheduled task definition.
 * Encapsulates task metadata and the task instance.
 */
class ScheduleTaskDefinition(
    /**
     * Task name (unique identifier).
     */
    val name: String,
    /**
     * Task description.
     */
    val description: String = "",
    /**
     * Task group.
     */
    val group: String = "default",
    /**
     * Whether the task is enabled.
     */
    val enabled: Boolean = true,
    /**
     * Task instance.
     */
    val task: ScheduledTask
) {
    /**
     * Fully qualified class name of the task.
     */
    val className: String
        get() = task::class.java.name

    companion object {
        /**
         * Build a definition from a ScheduledTask instance and its annotations.
         */
        fun from(task: ScheduledTask): ScheduleTaskDefinition {
            val metadata = task::class.java.getAnnotation(ScheduledTaskMetadata::class.java)
            return if (metadata != null) {
                ScheduleTaskDefinition(
                    name = metadata.name.takeIf { it.isNotBlank() } ?: task::class.java.name,
                    description = metadata.description,
                    group = metadata.group,
                    enabled = metadata.enabled,
                    task = task
                )
            } else {
                ScheduleTaskDefinition(
                    name = task::class.java.name,
                    task = task
                )
            }
        }
    }
}
