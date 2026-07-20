package com.lovelycatv.crystalframework.shared.annotations

/**
 * Marks an entity field as excluded from QueryNode-based filtering.
 *
 * Used on sensitive columns (password, tokens, secrets) whose values must never be
 * reachable via `POST /manager/xxx/query` `ConditionNode.field = "password"` style
 * boolean-probe attacks.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotQueryable