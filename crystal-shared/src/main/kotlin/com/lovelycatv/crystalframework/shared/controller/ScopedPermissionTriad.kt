package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation

/**
 * Three-layer permission declaration for scope-aware manager resources.
 *
 * Each scope-aware resource exposes 12 permissions arranged in three layers:
 *
 * - **super**: cross-scope authority. A user holding `super<op>` can perform `<op>`
 *   in any scope. Reserved for system-wide administrators (root, admin).
 * - **system**: scope=SYSTEM only. Allows operating on system-scoped resources.
 * - **tenantPem**: scope=TENANT only. Allows tenant members to operate on resources
 *   inside their own tenant.
 *
 * Authorization rule (consumed by [StandardScopedManagerController] and
 * [StandardDerivedScopedManagerController]):
 *
 * - SYSTEM scope → `hasAnyAuthority(super<op>, system<op>)`
 * - TENANT scope → `hasAnyAuthority(super<op>, tenantPem<op>)`
 *
 * Use [forScope] to fetch the relevant pair of authorities for a given scope + operation.
 */
data class ScopedPermissionTriad(
    val superCreate: String,
    val superRead: String,
    val superUpdate: String,
    val superDelete: String,
    val systemCreate: String,
    val systemRead: String,
    val systemUpdate: String,
    val systemDelete: String,
    val tenantPemCreate: String,
    val tenantPemRead: String,
    val tenantPemUpdate: String,
    val tenantPemDelete: String,
) {
    fun superFor(operation: ScopedOperation): String = when (operation) {
        ScopedOperation.CREATE -> superCreate
        ScopedOperation.READ -> superRead
        ScopedOperation.UPDATE -> superUpdate
        ScopedOperation.DELETE -> superDelete
    }

    fun systemFor(operation: ScopedOperation): String = when (operation) {
        ScopedOperation.CREATE -> systemCreate
        ScopedOperation.READ -> systemRead
        ScopedOperation.UPDATE -> systemUpdate
        ScopedOperation.DELETE -> systemDelete
    }

    fun tenantPemFor(operation: ScopedOperation): String = when (operation) {
        ScopedOperation.CREATE -> tenantPemCreate
        ScopedOperation.READ -> tenantPemRead
        ScopedOperation.UPDATE -> tenantPemUpdate
        ScopedOperation.DELETE -> tenantPemDelete
    }

    /**
     * Returns the array of authorities to OR-check for the given scope + operation.
     */
    fun forScope(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(operation), systemFor(operation))
        ResourceScope.TENANT -> arrayOf(superFor(operation), tenantPemFor(operation))
    }
}
