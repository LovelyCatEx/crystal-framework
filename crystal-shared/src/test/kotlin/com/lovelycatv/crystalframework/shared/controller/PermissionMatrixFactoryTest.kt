package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermissionMatrixFactoryTest {

    // region systemOnly

    @Test
    fun `systemOnly defaults super to NOT_APPLICABLE and fills tenant with NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.systemOnly(
            systemCreate = "system.oauth.account.create",
            systemRead = "system.oauth.account.read",
            systemUpdate = "system.oauth.account.update",
            systemDelete = "system.oauth.account.delete",
        )
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superRead)
        assertEquals("system.oauth.account.create", matrix.systemCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantAdminCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantPemRead)
    }

    @Test
    fun `systemOnly accepts explicit super layer values`() {
        val matrix = PermissionMatrix.systemOnly(
            systemCreate = "system.oauth.account.create",
            systemRead = "system.oauth.account.read",
            systemUpdate = "system.oauth.account.update",
            systemDelete = "system.oauth.account.delete",
            superCreate = "oauth.account.create",
            superRead = "oauth.account.read",
            superUpdate = "oauth.account.update",
            superDelete = "oauth.account.delete",
        )
        assertEquals("oauth.account.create", matrix.superCreate)
        assertEquals("system.oauth.account.create", matrix.systemCreate)
    }

    @Test
    fun `systemOnly layersFor SYSTEM CREATE without super returns only system authority`() {
        val matrix = PermissionMatrix.systemOnly(
            systemCreate = "system.oauth.account.create",
            systemRead = "system.oauth.account.read",
            systemUpdate = "system.oauth.account.update",
            systemDelete = "system.oauth.account.delete",
        )
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf("system.oauth.account.create"), result.toList())
    }

    @Test
    fun `systemOnly layersFor TENANT CREATE returns empty because tenant layers are NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.systemOnly(
            systemCreate = "system.oauth.account.create",
            systemRead = "system.oauth.account.read",
            systemUpdate = "system.oauth.account.update",
            systemDelete = "system.oauth.account.delete",
        )
        val result = matrix.layersFor(ResourceScope.TENANT, ScopedOperation.CREATE)
        assertTrue(result.isEmpty(), "expected empty but got ${result.toList()}")
    }

    @Test
    fun `systemOnly compliance validated for compliant inputs`() {
        val matrix = PermissionMatrix.systemOnly(
            systemCreate = "system.oauth.account.create",
            systemRead = "system.oauth.account.read",
            systemUpdate = "system.oauth.account.update",
            systemDelete = "system.oauth.account.delete",
        )
        assertTrue(PermissionMatrix.collectPrefixViolations(matrix).isEmpty())
    }

    // endregion

    // region tenantOnly

    @Test
    fun `tenantOnly defaults super and system to NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.tenantOnly(
            tenantAdminCreate = "tenant.role.create",
            tenantAdminRead = "tenant.role.read",
            tenantAdminUpdate = "tenant.role.update",
            tenantAdminDelete = "tenant.role.delete",
            tenantPemCreate = "i.tenant.role.create",
            tenantPemRead = "i.tenant.role.read",
            tenantPemUpdate = "i.tenant.role.update",
            tenantPemDelete = "i.tenant.role.delete",
        )
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.systemCreate)
        assertEquals("tenant.role.create", matrix.tenantAdminCreate)
        assertEquals("i.tenant.role.create", matrix.tenantPemCreate)
    }

    @Test
    fun `tenantOnly layersFor TENANT CREATE returns only tenant layers`() {
        val matrix = PermissionMatrix.tenantOnly(
            tenantAdminCreate = "tenant.role.create",
            tenantAdminRead = "tenant.role.read",
            tenantAdminUpdate = "tenant.role.update",
            tenantAdminDelete = "tenant.role.delete",
            tenantPemCreate = "i.tenant.role.create",
            tenantPemRead = "i.tenant.role.read",
            tenantPemUpdate = "i.tenant.role.update",
            tenantPemDelete = "i.tenant.role.delete",
        )
        val result = matrix.layersFor(ResourceScope.TENANT, ScopedOperation.CREATE)
        assertEquals(listOf("tenant.role.create", "i.tenant.role.create"), result.toList())
    }

    @Test
    fun `tenantOnly layersFor SYSTEM CREATE returns empty because super and system are NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.tenantOnly(
            tenantAdminCreate = "tenant.role.create",
            tenantAdminRead = "tenant.role.read",
            tenantAdminUpdate = "tenant.role.update",
            tenantAdminDelete = "tenant.role.delete",
            tenantPemCreate = "i.tenant.role.create",
            tenantPemRead = "i.tenant.role.read",
            tenantPemUpdate = "i.tenant.role.update",
            tenantPemDelete = "i.tenant.role.delete",
        )
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertTrue(result.isEmpty())
    }

    // endregion

    // region systemOnlyReadonly

    @Test
    fun `systemOnlyReadonly fills super and system CUD with NEVER_GRANTED and tenants with NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.systemOnlyReadonly(
            systemRead = "system.audit.log.read",
        )
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superCreate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superUpdate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superDelete)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.systemCreate)
        assertEquals("system.audit.log.read", matrix.systemRead)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.systemUpdate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantAdminRead)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantPemRead)
    }

    @Test
    fun `systemOnlyReadonly layersFor SYSTEM READ returns superRead NOT_APPLICABLE filtered and systemRead`() {
        val matrix = PermissionMatrix.systemOnlyReadonly(
            systemRead = "system.audit.log.read",
        )
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.READ)
        assertEquals(listOf("system.audit.log.read"), result.toList())
    }

    @Test
    fun `systemOnlyReadonly layersFor SYSTEM CREATE returns NEVER_GRANTED placeholders`() {
        val matrix = PermissionMatrix.systemOnlyReadonly(
            systemRead = "system.audit.log.read",
        )
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf(PermissionMatrix.NEVER_GRANTED, PermissionMatrix.NEVER_GRANTED), result.toList())
    }

    // endregion
}
