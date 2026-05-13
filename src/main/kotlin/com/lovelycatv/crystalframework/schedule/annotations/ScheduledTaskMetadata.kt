package com.lovelycatv.crystalframework.schedule.annotations

/**
 * Scheduled task metadata annotation.
 * Used to describe basic task information for internal system management and frontend display.
 *
 * If this annotation is not present, the task name defaults to the fully qualified class name.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScheduledTaskMetadata(
    /**
     * Task name.
     * - Used for task registration, logging, and frontend display.
     * - If empty, defaults to the fully qualified class name.
     */
    val name: String = "",
    /**
     * Task description.
     * - Used for frontend display and management interface.
     */
    val description: String = "",
    /**
     * Task group.
     * - Used for frontend categorization.
     */
    val group: String = "default",
    /**
     * Whether the task is enabled.
     */
    val enabled: Boolean = true
)