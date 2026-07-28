package com.lovelycatv.crystalframework.shared.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PermissionMatrixDslTest {

    @Test
    fun `dsl produces matrix equivalent to hand-written constructor`() {
        val fromDsl = PermissionMatrix.of {
            `super` {
                create = "role.create"
                read = "role.read"
                update = "role.update"
                delete = "role.delete"
            }
            system {
                create = "system.role.create"
                read = "system.role.read"
                update = "system.role.update"
                delete = "system.role.delete"
            }
            tenantAdmin {
                create = "tenant.role.create"
                read = "tenant.role.read"
                update = "tenant.role.update"
                delete = "tenant.role.delete"
            }
            tenantPem {
                create = "i.tenant.role.create"
                read = "i.tenant.role.read"
                update = "i.tenant.role.update"
                delete = "i.tenant.role.delete"
            }
        }

        val handWritten = PermissionMatrix(
            superCreate = "role.create",
            superRead = "role.read",
            superUpdate = "role.update",
            superDelete = "role.delete",
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

        assertEquals(handWritten, fromDsl)
    }

    @Test
    fun `dsl layers not opened default to NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.of {
            system {
                create = "system.role.create"
                read = "system.role.read"
                update = "system.role.update"
                delete = "system.role.delete"
            }
        }
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superRead)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantAdminCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.tenantPemDelete)
        assertEquals("system.role.create", matrix.systemCreate)
    }

    @Test
    fun `dsl layer opened without any fields defaults to all NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.of {
            `super` { }
        }
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superRead)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superUpdate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.superDelete)
    }

    @Test
    fun `dsl layer partially opened fills unset fields with NOT_APPLICABLE`() {
        val matrix = PermissionMatrix.of {
            system {
                read = "system.role.read"
            }
        }
        assertEquals("system.role.read", matrix.systemRead)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.systemCreate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.systemUpdate)
        assertEquals(PermissionMatrix.NOT_APPLICABLE, matrix.systemDelete)
    }
}
