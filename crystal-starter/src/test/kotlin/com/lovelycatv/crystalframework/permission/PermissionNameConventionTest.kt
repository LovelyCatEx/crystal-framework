package com.lovelycatv.crystalframework.permission

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.sdk.rbac.tenant.types.TenantPermissionType
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemPermissionType
import com.lovelycatv.crystalframework.shared.types.rbac.system.SystemRbacPermissionDeclaration
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

// Enforces the 4-layer permission naming convention documented in
// .claude/research/permission-naming-redesign.md and
// docs/contribute/{system,tenant}-permission.md.
//
// Rules:
//  - SystemPermission names must start with `x.`, `system.`, or `tenant.` (super / system / tenantAdmin layers)
//  - TenantPermission names must start with `i.tenant.` (tenantPem layer)
//  - Kotlin identifier layer segment must agree with the name prefix
//    (ACTION_X_XXX → `x.`, ACTION_SYSTEM_XXX → `system.`, ACTION_TENANT_XXX → `tenant.`)
//  - MENU / COMPONENT declarations must carry a non-blank path; ACTION must not carry a path
//  - Permission names must be globally unique across SystemPermission + TenantPermission
class PermissionNameConventionTest {

    private val systemDeclarations: List<Pair<String, SystemRbacPermissionDeclaration>> by lazy {
        SystemPermission::class.java.declaredFields
            .filter { it.type == SystemRbacPermissionDeclaration::class.java }
            .map { field ->
                field.isAccessible = true
                field.name to (field.get(SystemPermission) as SystemRbacPermissionDeclaration)
            }
    }

    @Test
    fun systemPermissionNamesFollowLayerPrefixConvention() {
        val violators = systemDeclarations
            .filter { (_, decl) ->
                !decl.name.startsWith("x.") &&
                    !decl.name.startsWith("system.") &&
                    !decl.name.startsWith("tenant.")
            }
            .map { it.second.name }
        assertTrue(
            violators.isEmpty(),
            "SystemPermission names must start with one of ['x.', 'system.', 'tenant.']; violators: $violators"
        )
    }

    @Test
    fun systemPermissionMustNotUseTenantPemPrefix() {
        val violators = systemDeclarations
            .filter { (_, decl) -> decl.name.startsWith("i.tenant.") }
            .map { it.second.name }
        assertTrue(
            violators.isEmpty(),
            "SystemPermission must not contain 'i.tenant.' names (that layer belongs to TenantPermission); violators: $violators"
        )
    }

    @Test
    fun tenantPermissionNamesUseTenantPemPrefix() {
        val violators = TenantPermission.allPermissions()
            .map { it.name }
            .filter { !it.startsWith("i.tenant.") }
        assertTrue(
            violators.isEmpty(),
            "TenantPermission names must start with 'i.tenant.'; violators: $violators"
        )
    }

    @Test
    fun systemPermissionIdentifierLayerSegmentMatchesNamePrefix() {
        val violators = systemDeclarations.filter { (identifier, decl) ->
            val expectedSegment = when {
                decl.name.startsWith("x.") -> "_X_"
                decl.name.startsWith("system.") -> "_SYSTEM_"
                decl.name.startsWith("tenant.") -> "_TENANT_"
                else -> return@filter true
            }
            !identifier.contains(expectedSegment)
        }
        assertTrue(
            violators.isEmpty(),
            "SystemPermission identifier layer segment does not match name prefix; violators: ${violators.map { "${it.first} -> ${it.second.name}" }}"
        )
    }

    @Test
    fun systemPermissionTypeMatchesPathPresence() {
        val violators = systemDeclarations.filter { (_, decl) ->
            when (decl.type) {
                SystemPermissionType.ACTION -> decl.path != null
                SystemPermissionType.MENU -> decl.path.isNullOrBlank()
                SystemPermissionType.COMPONENT -> decl.path.isNullOrBlank()
            }
        }
        assertTrue(
            violators.isEmpty(),
            "SystemPermission type/path mismatch (ACTION must have no path; MENU/COMPONENT must have non-blank path); violators: ${violators.map { "${it.second.name} (type=${it.second.type}, path=${it.second.path ?: "null"})" }}"
        )
    }

    @Test
    fun tenantPermissionTypeMatchesPathPresence() {
        val violators = TenantPermission.allPermissions().filter { decl ->
            when (decl.type) {
                TenantPermissionType.ACTION -> decl.path != null
                TenantPermissionType.MENU -> decl.path.isNullOrBlank()
            }
        }
        assertTrue(
            violators.isEmpty(),
            "TenantPermission type/path mismatch (ACTION must have no path; MENU must have non-blank path); violators: ${violators.map { "${it.name} (type=${it.type}, path=${it.path ?: "null"})" }}"
        )
    }

    @Test
    fun allPermissionNamesAreGloballyUnique() {
        val systemNames = systemDeclarations.map { it.second.name }
        val tenantNames = TenantPermission.allPermissions().map { it.name }
        val duplicates = (systemNames + tenantNames).groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(
            duplicates.isEmpty(),
            "Permission names must be globally unique across SystemPermission + TenantPermission; duplicates: $duplicates"
        )
    }
}
