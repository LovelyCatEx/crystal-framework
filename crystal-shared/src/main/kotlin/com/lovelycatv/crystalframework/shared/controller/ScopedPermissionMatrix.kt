package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation

/**
 * Four-layer permission matrix for scope-aware manager resources.
 *
 * Each row is one authority layer; each column is one CRUD operation. Combined with the request's
 * [ResourceScope] and [ScopedOperation], the framework decides which authorities are consulted:
 *
 *  - **super**       — cross-scope admin. Holders may act in any scope. Reserved for platform
 *                      root/admin roles.
 *  - **system**      — SYSTEM scope. Holders may act on any system-scoped resource. Because there
 *                      is no "primary id" concept inside SYSTEM today, this layer behaves like a
 *                      "SYSTEM-wide admin". A future finer split into system-admin vs system-pem
 *                      can slot in without breaking the matrix shape.
 *  - **tenantAdmin** — TENANT scope, cross-tenant. Holders may act on any tenant's data, but the
 *                      permission is bounded to TENANT-scoped resources only (does not leak into
 *                      SYSTEM). This is the layer to grant to "cross-tenant operator" roles.
 *  - **tenantPem**   — TENANT scope, own tenant only. Holders may act on data whose root tenant
 *                      matches [com.lovelycatv.crystalframework.shared.types.UserAuthentication.tenantId].
 *
 * Authorization pipeline consumed by [StandardScopedManagerController] and
 * [StandardDerivedScopedManagerController]:
 *
 *  1. [checkPermission] runs `hasAnyAuthority(layersFor(scope, op))` — the layers eligible for
 *     the current (scope, op).
 *  2. [checkOwnership] additionally requires — for TENANT scope — one of the cross-tenant
 *     layers (super / tenantAdmin), OR the request's resolved root tenantId to equal the caller's.
 *     [crossTenantLayersFor] provides the union.
 *
 * @see ScopedPermissionMatrix.readonly for the read-only convenience factory (all mutating
 *      slots filled with [NEVER_GRANTED]).
 * @see ScopedRelationshipResolvers for helpers that walk a child entity up to its root scope,
 *      used by derived controllers.
 */
data class ScopedPermissionMatrix(
    val superCreate: String,
    val superRead: String,
    val superUpdate: String,
    val superDelete: String,
    val systemCreate: String,
    val systemRead: String,
    val systemUpdate: String,
    val systemDelete: String,
    val tenantAdminCreate: String,
    val tenantAdminRead: String,
    val tenantAdminUpdate: String,
    val tenantAdminDelete: String,
    val tenantPemCreate: String,
    val tenantPemRead: String,
    val tenantPemUpdate: String,
    val tenantPemDelete: String,
) {
    enum class Layer { SUPER, SYSTEM, TENANT_ADMIN, TENANT_PEM }

    fun of(layer: Layer, operation: ScopedOperation): String = when (layer) {
        Layer.SUPER -> superFor(operation)
        Layer.SYSTEM -> systemFor(operation)
        Layer.TENANT_ADMIN -> tenantAdminFor(operation)
        Layer.TENANT_PEM -> tenantPemFor(operation)
    }

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

    fun tenantAdminFor(operation: ScopedOperation): String = when (operation) {
        ScopedOperation.CREATE -> tenantAdminCreate
        ScopedOperation.READ -> tenantAdminRead
        ScopedOperation.UPDATE -> tenantAdminUpdate
        ScopedOperation.DELETE -> tenantAdminDelete
    }

    fun tenantPemFor(operation: ScopedOperation): String = when (operation) {
        ScopedOperation.CREATE -> tenantPemCreate
        ScopedOperation.READ -> tenantPemRead
        ScopedOperation.UPDATE -> tenantPemUpdate
        ScopedOperation.DELETE -> tenantPemDelete
    }

    /**
     * Authorities to OR-check in [StandardScopedManagerController.checkPermission] for the given
     * scope + operation. SYSTEM consults [Layer.SUPER] + [Layer.SYSTEM]; TENANT consults
     * [Layer.SUPER] + [Layer.TENANT_ADMIN] + [Layer.TENANT_PEM].
     */
    fun layersFor(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(operation), systemFor(operation))
        ResourceScope.TENANT -> arrayOf(
            superFor(operation),
            tenantAdminFor(operation),
            tenantPemFor(operation),
        )
    }

    /**
     * Authorities that let the caller act **across tenants** (used by [checkOwnership] on TENANT
     * scope to decide whether the root-tenant equality check can be skipped). Only [Layer.SUPER]
     * and [Layer.TENANT_ADMIN] qualify.
     */
    fun crossTenantLayersFor(operation: ScopedOperation): Array<String> = arrayOf(
        superFor(operation),
        tenantAdminFor(operation),
    )

    companion object {
        /**
         * Sentinel authority that is intentionally not declared anywhere in the system — no role
         * can hold it. Fill unused slots with this to make any accidental lookup deny by default,
         * rather than accidentally re-purposing another layer's authority string.
         *
         * The leading/trailing `!!` markers are deliberately incompatible with the project's
         * `<module>.<resource>.<op>` permission naming convention.
         */
        const val NEVER_GRANTED: String = "!!never_granted!!"

        /**
         * Convenience factory for read-only resources. Only the read authority of each layer is
         * meaningful; every CRUD slot is [NEVER_GRANTED] so an accidental mutation lookup denies
         * by default.
         */
        fun readonly(
            superRead: String,
            systemRead: String,
            tenantAdminRead: String,
            tenantPemRead: String,
        ): ScopedPermissionMatrix = ScopedPermissionMatrix(
            superCreate = NEVER_GRANTED,
            superRead = superRead,
            superUpdate = NEVER_GRANTED,
            superDelete = NEVER_GRANTED,
            systemCreate = NEVER_GRANTED,
            systemRead = systemRead,
            systemUpdate = NEVER_GRANTED,
            systemDelete = NEVER_GRANTED,
            tenantAdminCreate = NEVER_GRANTED,
            tenantAdminRead = tenantAdminRead,
            tenantAdminUpdate = NEVER_GRANTED,
            tenantAdminDelete = NEVER_GRANTED,
            tenantPemCreate = NEVER_GRANTED,
            tenantPemRead = tenantPemRead,
            tenantPemUpdate = NEVER_GRANTED,
            tenantPemDelete = NEVER_GRANTED,
        )
    }
}
