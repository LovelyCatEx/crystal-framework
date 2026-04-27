package com.lovelycatv.crystalframework.shared.annotations

/**
 * Permissions required for the standardized manager controller endpoints.
 *
 * Each action accepts an array of permission identifiers. Authorization succeeds when
 * the current user holds **any** of the listed permissions (OR semantics).
 * An empty array means no permission is configured for that action;
 * for [readAll], the aspect falls back to [read] when [readAll] is empty.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ManagerPermissions(
    val read: Array<String> = [],
    val readAll: Array<String> = [],
    val create: Array<String> = [],
    val update: Array<String> = [],
    val delete: Array<String> = []
)
