package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermissionMatrixTest {

    private fun compliantMatrix(): PermissionMatrix = PermissionMatrix(
        superCreate = "x.role.create",
        superRead = "x.role.read",
        superUpdate = "x.role.update",
        superDelete = "x.role.delete",
        systemCreate = "system.role.create",
        systemRead = "system.role.read",
        systemUpdate = "system.role.update",
        systemDelete = "system.role.delete",
        tenantAdminCreate = "tenant.role.create",
        tenantAdminRead = "tenant.role.read",
        tenantAdminUpdate = "tenant.role.update",
        tenantAdminDelete = "tenant.role.delete",
        tenantPemCreate = "i.tenant.role.create",
        tenantPemRead = "i.tenant.role.read",
        tenantPemUpdate = "i.tenant.role.update",
        tenantPemDelete = "i.tenant.role.delete",
    )

    // region layersFor: SYSTEM scope consults super + system

    @Test
    fun `layersFor SYSTEM CREATE returns super and system authorities`() {
        val result = compliantMatrix().layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf("x.role.create", "system.role.create"), result.toList())
    }

    @Test
    fun `layersFor SYSTEM CREATE filters NOT_APPLICABLE super slot`() {
        val matrix = compliantMatrix().copy(superCreate = PermissionMatrix.NOT_APPLICABLE)
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf("system.role.create"), result.toList())
    }

    @Test
    fun `layersFor SYSTEM CREATE keeps NEVER_GRANTED super slot as placeholder`() {
        val matrix = compliantMatrix().copy(superCreate = PermissionMatrix.NEVER_GRANTED)
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf(PermissionMatrix.NEVER_GRANTED, "system.role.create"), result.toList())
    }

    // endregion

    // region layersFor: TENANT scope consults super + tenantAdmin + tenantPem

    @Test
    fun `layersFor TENANT CREATE returns super tenantAdmin and tenantPem authorities`() {
        val result = compliantMatrix().layersFor(ResourceScope.TENANT, ScopedOperation.CREATE)
        assertEquals(listOf("x.role.create", "tenant.role.create", "i.tenant.role.create"), result.toList())
    }

    @Test
    fun `layersFor TENANT filters all NOT_APPLICABLE slots`() {
        val matrix = compliantMatrix().copy(
            superCreate = PermissionMatrix.NOT_APPLICABLE,
            tenantPemCreate = PermissionMatrix.NOT_APPLICABLE,
        )
        val result = matrix.layersFor(ResourceScope.TENANT, ScopedOperation.CREATE)
        assertEquals(listOf("tenant.role.create"), result.toList())
    }

    // endregion

    // region crossTenantLayersFor

    @Test
    fun `crossTenantLayersFor returns super and tenantAdmin authorities`() {
        val result = compliantMatrix().crossTenantLayersFor(ScopedOperation.CREATE)
        assertEquals(listOf("x.role.create", "tenant.role.create"), result.toList())
    }

    @Test
    fun `crossTenantLayersFor filters NOT_APPLICABLE slots`() {
        val matrix = compliantMatrix().copy(superCreate = PermissionMatrix.NOT_APPLICABLE)
        val result = matrix.crossTenantLayersFor(ScopedOperation.CREATE)
        assertEquals(listOf("tenant.role.create"), result.toList())
    }

    // endregion

    // region readonly factory

    @Test
    fun `readonly factory fills all CUD slots with NEVER_GRANTED`() {
        val matrix = PermissionMatrix.readonly(
            superRead = "x.audit.log.read",
            systemRead = "system.audit.log.read",
            tenantAdminRead = "tenant.audit.log.read",
            tenantPemRead = "i.tenant.audit.log.read",
        )
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superCreate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superUpdate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.superDelete)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.systemCreate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.tenantAdminUpdate)
        assertEquals(PermissionMatrix.NEVER_GRANTED, matrix.tenantPemDelete)
        assertEquals("x.audit.log.read", matrix.superRead)
        assertEquals("i.tenant.audit.log.read", matrix.tenantPemRead)
    }

    @Test
    fun `readonly factory layersFor CREATE returns only NEVER_GRANTED placeholders`() {
        val matrix = PermissionMatrix.readonly(
            superRead = "x.audit.log.read",
            systemRead = "system.audit.log.read",
            tenantAdminRead = "tenant.audit.log.read",
            tenantPemRead = "i.tenant.audit.log.read",
        )
        val result = matrix.layersFor(ResourceScope.SYSTEM, ScopedOperation.CREATE)
        assertEquals(listOf(PermissionMatrix.NEVER_GRANTED, PermissionMatrix.NEVER_GRANTED), result.toList())
    }

    // endregion

    // region collectPrefixViolations

    @Test
    fun `collectPrefixViolations returns empty for compliant matrix`() {
        val violations = PermissionMatrix.collectPrefixViolations(compliantMatrix())
        assertTrue(violations.isEmpty(), "expected no violations but got: $violations")
    }

    @Test
    fun `collectPrefixViolations flags super slot with system prefix`() {
        val ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
            compliantMatrix().copy(superCreate = "system.role.create")
        }
        assertTrue(ex.message!!.contains("super*"), "expected super* violation but got: ${ex.message}")
    }

    @Test
    fun `collectPrefixViolations flags system slot without system prefix`() {
        val ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
            compliantMatrix().copy(systemCreate = "role.create")
        }
        assertTrue(ex.message!!.contains("system*"))
    }

    @Test
    fun `collectPrefixViolations flags tenantAdmin slot without tenant prefix`() {
        val ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
            compliantMatrix().copy(tenantAdminCreate = "system.role.create")
        }
        assertTrue(ex.message!!.contains("tenantAdmin*"))
    }

    @Test
    fun `collectPrefixViolations flags tenantPem slot without i_tenant prefix`() {
        val ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
            compliantMatrix().copy(tenantPemCreate = "tenant.role.create")
        }
        assertTrue(ex.message!!.contains("tenantPem*"))
    }

    @Test
    fun `collectPrefixViolations ignores NOT_APPLICABLE`() {
        val matrix = compliantMatrix().copy(
            superCreate = PermissionMatrix.NOT_APPLICABLE,
            systemCreate = PermissionMatrix.NOT_APPLICABLE,
            tenantAdminCreate = PermissionMatrix.NOT_APPLICABLE,
            tenantPemCreate = PermissionMatrix.NOT_APPLICABLE,
        )
        assertTrue(PermissionMatrix.collectPrefixViolations(matrix).isEmpty())
    }

    @Test
    fun `collectPrefixViolations ignores NEVER_GRANTED`() {
        val matrix = compliantMatrix().copy(
            superCreate = PermissionMatrix.NEVER_GRANTED,
            systemCreate = PermissionMatrix.NEVER_GRANTED,
        )
        assertTrue(PermissionMatrix.collectPrefixViolations(matrix).isEmpty())
    }

    // endregion

    // region of(Layer, ScopedOperation)

    @Test
    fun `of Layer routes to correct slot`() {
        val matrix = compliantMatrix()
        assertEquals("x.role.create", matrix.of(PermissionMatrix.Layer.SUPER, ScopedOperation.CREATE))
        assertEquals("system.role.read", matrix.of(PermissionMatrix.Layer.SYSTEM, ScopedOperation.READ))
        assertEquals("tenant.role.update", matrix.of(PermissionMatrix.Layer.TENANT_ADMIN, ScopedOperation.UPDATE))
        assertEquals("i.tenant.role.delete", matrix.of(PermissionMatrix.Layer.TENANT_PEM, ScopedOperation.DELETE))
    }

    // endregion
}
