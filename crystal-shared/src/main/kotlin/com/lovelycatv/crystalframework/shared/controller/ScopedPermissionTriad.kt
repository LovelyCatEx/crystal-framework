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

    companion object {
        /**
         * Sentinel authority that is intentionally not declared anywhere in the system —
         * no role can hold it, including `root` (which auto-grants every entry on
         * [com.lovelycatv.crystalframework.shared.constants.SystemPermission], a class
         * that does NOT contain this constant). Any RBAC check against this string
         * therefore returns false unconditionally.
         *
         * Used by [readonly] to fill the unused CRUD slots so that, even if a future
         * caller bypasses [com.lovelycatv.crystalframework.shared.controller.ReadonlyScopedManagerController]
         * and looks up `superFor(CREATE)` etc., the answer is "deny" rather than the
         * resource's read authority. Carrying real read strings in those slots would
         * silently grant CREATE/UPDATE/DELETE if the read-only wrapper were ever
         * removed — this sentinel makes the failure mode safe instead.
         *
         * The leading/trailing `!!` markers are deliberately syntactically incompatible
         * with the project's `<module>.<resource>.<op>` permission naming convention,
         * so collision with a real permission is impossible.
         */
        const val NEVER_GRANTED: String = "!!never_granted!!"

        /**
         * Convenience factory for read-only scoped resources. Only the read authority
         * for each layer is meaningful; the CRUD slots are filled with [NEVER_GRANTED]
         * so that any accidental CRUD permission lookup denies by default rather than
         * leaking the read authority.
         */
        fun readonly(
            superRead: String,
            systemRead: String,
            tenantPemRead: String,
        ): ScopedPermissionTriad = ScopedPermissionTriad(
            superCreate = NEVER_GRANTED,
            superRead = superRead,
            superUpdate = NEVER_GRANTED,
            superDelete = NEVER_GRANTED,
            systemCreate = NEVER_GRANTED,
            systemRead = systemRead,
            systemUpdate = NEVER_GRANTED,
            systemDelete = NEVER_GRANTED,
            tenantPemCreate = NEVER_GRANTED,
            tenantPemRead = tenantPemRead,
            tenantPemUpdate = NEVER_GRANTED,
            tenantPemDelete = NEVER_GRANTED,
        )
    }
}
