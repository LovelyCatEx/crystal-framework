package com.lovelycatv.crystalframework.schedule.annotations

/**
 * Spring Cron task trigger annotation.
 * Dedicated for Spring Boot built-in scheduled task scheduling.
 *
 * Only tasks annotated with this and configured with a cron expression will be registered as Spring Cron tasks.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CronTaskExecutor(
    /**
     * Spring Cron expression.
     * Example: "0 0 * * * ?" means execute every hour.
     * If empty, the task will not be registered as a Spring Cron task.
     */
    val cron: String = ""
)