package com.lovelycatv.crystalframework.resource.controller.manager.file

import com.lovelycatv.crystalframework.resource.constants.FileResourcePermissions
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerFileResourceController] — Standard Scoped with a full 16-slot
 * [PermissionMatrix]. The read DTO carries `scope + scopeId`, so the base class default
 * `resolveScopeFromReadDTO` reads the fields directly (no DB dependency). The authorize chain
 * runs `checkPermission` (RBAC OR-check across `layersFor(scope, op)`) then `checkOwnership`
 * (TENANT-scope only: cross-tenant layer or `scopeId == tenantId`).
 */
class ManagerFileResourceControllerIntegrationTest(
    @Autowired private val managerFileResourceController: ManagerFileResourceController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = FileResourcePermissions.MATRIX

    private fun systemReadDto() = ManagerReadFileResourceDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private fun tenantReadDto(scopeId: Long) = ManagerReadFileResourceDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.TENANT.typeId,
        scopeId = scopeId,
    )

    // ── SYSTEM scope reads ──

    @Test
    fun systemScopeReadAllowsSuperLayer() {
        withTransactionalRollback("file-resource-system-read-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadAllowsSystemLayer() {
        withTransactionalRollback("file-resource-system-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SYSTEM, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantAdminLayer() {
        withTransactionalRollback("file-resource-system-read-tenantAdmin") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_ADMIN reading SYSTEM scope")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantPemLayer() {
        withTransactionalRollback("file-resource-system-read-tenantPem") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM reading SYSTEM scope")
        }
    }

    // ── TENANT scope reads ──

    @Test
    fun tenantScopeReadAllowsSuperLayerCrossTenant() {
        withTransactionalRollback("file-resource-tenant-read-super-foreign") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesSystemLayer() {
        withTransactionalRollback("file-resource-tenant-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM reading TENANT scope")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantAdminCrossTenant() {
        withTransactionalRollback("file-resource-tenant-read-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("file-resource-tenant-read-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "TENANT-scope read (own tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesTenantPemCrossTenant() {
        withTransactionalRollback("file-resource-tenant-read-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM cross-tenant TENANT-scope read")
        }
    }

    @Test
    fun readDeniesUnauthorizedUser() {
        withTransactionalRollback("file-resource-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerFileResourceController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "unauthorised fixture")
        }
    }

    companion object {
        private const val OWN_TENANT_ID: Long = 1L
        private const val FOREIGN_TENANT_ID: Long = 2L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
        private const val SYSTEM_SCOPE_ID: Long = 0L
    }
}
