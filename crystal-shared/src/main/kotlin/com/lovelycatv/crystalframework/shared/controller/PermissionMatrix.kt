package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.vertex.log.logger

/**
 * Four-layer permission matrix for scope-aware manager resources.
 *
 * The single, unified way to declare authorisation for every Manager Controller main line
 * (Standard / Scoped / Tenant / their Readonly variants).
 *
 * Each row is one authority layer; each column is one CRUD operation. Combined with the request's
 * [ResourceScope] and [ScopedOperation], [layersFor] returns the authorities that satisfy the
 * request via an OR-check:
 *
 *  - **super**       — cross-scope admin. Authority string carries no scope prefix.
 *  - **system**      — SYSTEM scope. Authority string prefix `system.`.
 *  - **tenantAdmin** — TENANT scope, cross-tenant. Authority string prefix `tenant.`.
 *  - **tenantPem**   — TENANT scope, own tenant only. Authority string prefix `i.tenant.`.
 *
 * Two sentinel values mark slots that must not compare-equal to any real authority:
 *
 *  - [NOT_APPLICABLE] — this layer has no meaning for the resource (e.g. SYSTEM-only resource
 *                       leaves `tenantAdmin` / `tenantPem` [NOT_APPLICABLE]). [layersFor] and
 *                       [crossTenantLayersFor] filter this value out — the layer does not count.
 *  - [NEVER_GRANTED]  — this layer exists but the operation is forbidden (Readonly variants fill
 *                       mutating slots with this). [layersFor] keeps this value so the OR-check
 *                       sees it and fails deterministically (no role ever holds the sentinel).
 *
 * Prefix validation runs at construction and emits a `logger.warn` for each mismatched slot. The
 * warning is intended to become a hard error in a future release once historical violations are
 * migrated; use [collectPrefixViolations] for opt-in strict checking (tests, gating).
 */
data class PermissionMatrix(
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
    init {
        collectPrefixViolations(this).forEach { logger.warn("PermissionMatrix: $it. This will become a hard error in a future release.") }
    }

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
     * Authorities to OR-check for the given scope + operation. SYSTEM consults super + system;
     * TENANT consults super + tenantAdmin + tenantPem. [NOT_APPLICABLE] slots are filtered out;
     * [NEVER_GRANTED] slots are kept so the OR-check fails deterministically.
     */
    fun layersFor(scope: ResourceScope, operation: ScopedOperation): Array<String> = when (scope) {
        ResourceScope.SYSTEM -> arrayOf(superFor(operation), systemFor(operation))
        ResourceScope.TENANT -> arrayOf(
            superFor(operation),
            tenantAdminFor(operation),
            tenantPemFor(operation),
        )
    }.filter { it != NOT_APPLICABLE }.toTypedArray()

    /**
     * Authorities that let the caller act across tenants (used by the ownership check to skip
     * root-tenant equality). Only super and tenantAdmin qualify; [NOT_APPLICABLE] slots filtered.
     */
    fun crossTenantLayersFor(operation: ScopedOperation): Array<String> = arrayOf(
        superFor(operation),
        tenantAdminFor(operation),
    ).filter { it != NOT_APPLICABLE }.toTypedArray()

    companion object {
        private val logger = logger()

        private val SUPER_FORBIDDEN_PREFIXES = listOf("system.", "tenant.", "i.tenant.")
        private val SYSTEM_REQUIRED_PREFIXES = listOf("system.")
        private val TENANT_ADMIN_REQUIRED_PREFIXES = listOf("tenant.")
        private val TENANT_PEM_REQUIRED_PREFIXES = listOf("i.tenant.")

        /**
         * Sentinel: this layer does not apply to the resource at all (e.g. SYSTEM-only resource
         * has no tenantAdmin/tenantPem layers). Filtered out by [layersFor] and
         * [crossTenantLayersFor] during authority OR-checks — the layer is absent from decision.
         *
         * The leading/trailing `!!` markers deliberately break the permission naming convention
         * so an accidental grant of this string carries no effect.
         */
        const val NOT_APPLICABLE: String = "!!not_applicable!!"

        /**
         * Sentinel: this layer exists but the operation is explicitly forbidden. No role can
         * hold this authority, so any OR-check denies. Kept by [layersFor] as a placeholder so
         * the layer participates but never matches.
         */
        const val NEVER_GRANTED: String = "!!never_granted!!"

        /**
         * Convenience factory for read-only resources. Only the read authority of each layer is
         * meaningful; every CUD slot is [NEVER_GRANTED] so an accidental mutation lookup denies.
         */
        fun readonly(
            superRead: String,
            systemRead: String,
            tenantAdminRead: String,
            tenantPemRead: String,
        ): PermissionMatrix = PermissionMatrix(
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

        /**
         * Opt-in strict validator. Returns a list of human-readable violation messages; an empty
         * list means the matrix conforms to prefix conventions. Used by [PermissionMatrix.init]
         * (soft `logger.warn`) and by tests/gating that want to promote warnings to hard failure.
         */
        fun collectPrefixViolations(matrix: PermissionMatrix): List<String> {
            val violations = mutableListOf<String>()
            fun check(fieldGroup: String, prefixes: List<String>, requireOne: Boolean, vararg values: String) {
                values.forEach { v ->
                    if (v == NOT_APPLICABLE || v == NEVER_GRANTED) return@forEach
                    val ok = if (requireOne) prefixes.any { v.startsWith(it) }
                    else prefixes.none { v.startsWith(it) }
                    if (!ok) {
                        val reason = if (requireOne) "expected one of $prefixes" else "expected none of $prefixes"
                        violations += "$fieldGroup permission '$v' violates prefix convention ($reason)"
                    }
                }
            }
            with(matrix) {
                check("super*", SUPER_FORBIDDEN_PREFIXES, requireOne = false,
                    superCreate, superRead, superUpdate, superDelete)
                check("system*", SYSTEM_REQUIRED_PREFIXES, requireOne = true,
                    systemCreate, systemRead, systemUpdate, systemDelete)
                check("tenantAdmin*", TENANT_ADMIN_REQUIRED_PREFIXES, requireOne = true,
                    tenantAdminCreate, tenantAdminRead, tenantAdminUpdate, tenantAdminDelete)
                check("tenantPem*", TENANT_PEM_REQUIRED_PREFIXES, requireOne = true,
                    tenantPemCreate, tenantPemRead, tenantPemUpdate, tenantPemDelete)
            }
            return violations
        }
    }
}
