package com.lovelycatv.crystalframework.shared.annotations

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

/**
 * Authorisation contract for endpoints that need a small, machine-readable permission requirement
 * without introducing SpEL. Every deny path funnels through the aspect handler and raises a
 * structured `ForbiddenException`, so the frontend `ForbiddenModal` can render the actual required
 * permissions and scope.
 *
 * Semantics:
 *  - Exactly one of [anyOf] / [allOf] must be non-empty. If both are populated, the aspect
 *    prefers [anyOf] and ignores [allOf].
 *  - `hasAnyAuthority(...)` semantics for [anyOf]; `hasAllAuthorities(...)` semantics for [allOf].
 *  - [scope] is required. It travels into `ForbiddenContext.scope` when the check fails, so the
 *    UI can label the resource correctly (SYSTEM vs TENANT).
 *
 * The Spring Security-native annotations (`@PreAuthorize`, `@PostAuthorize`, `@PreFilter`,
 * `@PostFilter`, `@Secured`, `@RolesAllowed`) are banned across the codebase — a bean factory
 * post-processor aborts context refresh if any of them appear. Use [RequiresAuthority] instead.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class RequiresAuthority(
    val anyOf: Array<String> = [],
    val allOf: Array<String> = [],
    val scope: ResourceScope,
)
