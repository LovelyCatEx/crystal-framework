package com.lovelycatv.crystalframework.shared.controller

/**
 * Top-level builder for [PermissionMatrix.of]. Each of the four `super` / `system` /
 * `tenantAdmin` / `tenantPem` methods opens a [PermissionLayerBuilder] block; layers not opened
 * end up entirely [PermissionMatrix.NOT_APPLICABLE].
 *
 * `super` is a Kotlin keyword so the DSL method name is backticked — call sites must also
 * backtick it: `` `super` { create = ... } ``.
 */
@PermissionMatrixDsl
class PermissionMatrixBuilder {
    private val superLayer = PermissionLayerBuilder()
    private val systemLayer = PermissionLayerBuilder()
    private val tenantAdminLayer = PermissionLayerBuilder()
    private val tenantPemLayer = PermissionLayerBuilder()

    fun `super`(block: PermissionLayerBuilder.() -> Unit) {
        superLayer.apply(block)
    }

    fun system(block: PermissionLayerBuilder.() -> Unit) {
        systemLayer.apply(block)
    }

    fun tenantAdmin(block: PermissionLayerBuilder.() -> Unit) {
        tenantAdminLayer.apply(block)
    }

    fun tenantPem(block: PermissionLayerBuilder.() -> Unit) {
        tenantPemLayer.apply(block)
    }

    internal fun build(): PermissionMatrix = PermissionMatrix(
        superCreate = superLayer.create,
        superRead = superLayer.read,
        superUpdate = superLayer.update,
        superDelete = superLayer.delete,
        systemCreate = systemLayer.create,
        systemRead = systemLayer.read,
        systemUpdate = systemLayer.update,
        systemDelete = systemLayer.delete,
        tenantAdminCreate = tenantAdminLayer.create,
        tenantAdminRead = tenantAdminLayer.read,
        tenantAdminUpdate = tenantAdminLayer.update,
        tenantAdminDelete = tenantAdminLayer.delete,
        tenantPemCreate = tenantPemLayer.create,
        tenantPemRead = tenantPemLayer.read,
        tenantPemUpdate = tenantPemLayer.update,
        tenantPemDelete = tenantPemLayer.delete,
    )
}

/**
 * DSL entry point. Layers not explicitly opened default to entirely [PermissionMatrix.NOT_APPLICABLE].
 *
 * ```
 * permissions = PermissionMatrix.of {
 *     `super` { create = "..."; read = "..."; update = "..."; delete = "..." }
 *     system { create = "system.foo.create"; read = "system.foo.read"; ... }
 *     tenantAdmin { create = "tenant.foo.create"; ... }
 *     tenantPem { create = "i.tenant.foo.create"; ... }
 * }
 * ```
 */
fun PermissionMatrix.Companion.of(block: PermissionMatrixBuilder.() -> Unit): PermissionMatrix =
    PermissionMatrixBuilder().apply(block).build()

/**
 * Convenience factory for SYSTEM-only resources (no tenant scope at all). Fills tenant layers
 * with [PermissionMatrix.NOT_APPLICABLE]. Callers that also want cross-scope super admin can pass
 * `super*` values; leaving them defaults to [PermissionMatrix.NOT_APPLICABLE].
 */
fun PermissionMatrix.Companion.systemOnly(
    systemCreate: String,
    systemRead: String,
    systemUpdate: String,
    systemDelete: String,
    superCreate: String = PermissionMatrix.NOT_APPLICABLE,
    superRead: String = PermissionMatrix.NOT_APPLICABLE,
    superUpdate: String = PermissionMatrix.NOT_APPLICABLE,
    superDelete: String = PermissionMatrix.NOT_APPLICABLE,
): PermissionMatrix = PermissionMatrix(
    superCreate = superCreate,
    superRead = superRead,
    superUpdate = superUpdate,
    superDelete = superDelete,
    systemCreate = systemCreate,
    systemRead = systemRead,
    systemUpdate = systemUpdate,
    systemDelete = systemDelete,
    tenantAdminCreate = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminRead = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminUpdate = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminDelete = PermissionMatrix.NOT_APPLICABLE,
    tenantPemCreate = PermissionMatrix.NOT_APPLICABLE,
    tenantPemRead = PermissionMatrix.NOT_APPLICABLE,
    tenantPemUpdate = PermissionMatrix.NOT_APPLICABLE,
    tenantPemDelete = PermissionMatrix.NOT_APPLICABLE,
)

/**
 * Convenience factory for TENANT-only resources (no SYSTEM scope semantics — e.g. `TenantRole`).
 * Fills super/system layers with [PermissionMatrix.NOT_APPLICABLE] by default. SYSTEM-level
 * cross-tenant admin already works via the `tenantAdmin` layer (holder of `tenant.*` bypasses
 * tenant-id check), so `super*` typically stays defaulted.
 */
fun PermissionMatrix.Companion.tenantOnly(
    tenantAdminCreate: String,
    tenantAdminRead: String,
    tenantAdminUpdate: String,
    tenantAdminDelete: String,
    tenantPemCreate: String,
    tenantPemRead: String,
    tenantPemUpdate: String,
    tenantPemDelete: String,
    superCreate: String = PermissionMatrix.NOT_APPLICABLE,
    superRead: String = PermissionMatrix.NOT_APPLICABLE,
    superUpdate: String = PermissionMatrix.NOT_APPLICABLE,
    superDelete: String = PermissionMatrix.NOT_APPLICABLE,
    systemCreate: String = PermissionMatrix.NOT_APPLICABLE,
    systemRead: String = PermissionMatrix.NOT_APPLICABLE,
    systemUpdate: String = PermissionMatrix.NOT_APPLICABLE,
    systemDelete: String = PermissionMatrix.NOT_APPLICABLE,
): PermissionMatrix = PermissionMatrix(
    superCreate = superCreate,
    superRead = superRead,
    superUpdate = superUpdate,
    superDelete = superDelete,
    systemCreate = systemCreate,
    systemRead = systemRead,
    systemUpdate = systemUpdate,
    systemDelete = systemDelete,
    tenantAdminCreate = tenantAdminCreate,
    tenantAdminRead = tenantAdminRead,
    tenantAdminUpdate = tenantAdminUpdate,
    tenantAdminDelete = tenantAdminDelete,
    tenantPemCreate = tenantPemCreate,
    tenantPemRead = tenantPemRead,
    tenantPemUpdate = tenantPemUpdate,
    tenantPemDelete = tenantPemDelete,
)

/**
 * Convenience factory for SYSTEM-only + read-only resources (audit logs, mail send logs, login
 * logs). Fills super/system CUD slots with [PermissionMatrix.NEVER_GRANTED] and tenant layers
 * with [PermissionMatrix.NOT_APPLICABLE]. Only the two `read` authorities are meaningful.
 */
fun PermissionMatrix.Companion.systemOnlyReadonly(
    systemRead: String,
    superRead: String = PermissionMatrix.NOT_APPLICABLE,
): PermissionMatrix = PermissionMatrix(
    superCreate = PermissionMatrix.NEVER_GRANTED,
    superRead = superRead,
    superUpdate = PermissionMatrix.NEVER_GRANTED,
    superDelete = PermissionMatrix.NEVER_GRANTED,
    systemCreate = PermissionMatrix.NEVER_GRANTED,
    systemRead = systemRead,
    systemUpdate = PermissionMatrix.NEVER_GRANTED,
    systemDelete = PermissionMatrix.NEVER_GRANTED,
    tenantAdminCreate = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminRead = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminUpdate = PermissionMatrix.NOT_APPLICABLE,
    tenantAdminDelete = PermissionMatrix.NOT_APPLICABLE,
    tenantPemCreate = PermissionMatrix.NOT_APPLICABLE,
    tenantPemRead = PermissionMatrix.NOT_APPLICABLE,
    tenantPemUpdate = PermissionMatrix.NOT_APPLICABLE,
    tenantPemDelete = PermissionMatrix.NOT_APPLICABLE,
)
