package com.lovelycatv.crystalframework.tenant.controller.manager.dict

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto.ManagerReadTenantDictTypeDTO
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantDictTypeController] — Standard Scoped with a full 16-slot
 * [PermissionMatrix]. Read DTO carries `scope + scopeId`, so `resolveScopeFromReadDTO` uses the
 * base class default (no DB dependency). The authorize chain runs
 * `checkPermission` (RBAC OR-check across `layersFor(scope, op)`) then `checkOwnership`
 * (TENANT-scope only: cross-tenant layer or `scopeId == tenantId`).
 *
 * SYSTEM-scope reads consult super + system; TENANT-scope reads consult super + tenantAdmin +
 * tenantPem. This test exercises the interesting slices:
 *
 *  - SYSTEM-scope read: SUPER and SYSTEM layers allowed; TENANT_ADMIN/TENANT_PEM denied
 *    (their authorities are not in `layersFor(SYSTEM, READ)`).
 *  - TENANT-scope read: tenantPem must additionally match the tenant id (`checkOwnership`).
 */
class ManagerTenantDictTypeControllerIntegrationTest(
    @Autowired private val managerTenantDictTypeController: ManagerTenantDictTypeController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix(
        superCreate = SystemPermission.ACTION_X_DICT_TYPE_CREATE.name,
        superRead = SystemPermission.ACTION_X_DICT_TYPE_READ.name,
        superUpdate = SystemPermission.ACTION_X_DICT_TYPE_UPDATE.name,
        superDelete = SystemPermission.ACTION_X_DICT_TYPE_DELETE.name,
        systemCreate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_DICT_TYPE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_DICT_TYPE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_DICT_TYPE_DELETE.name,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DICT_TYPE_CREATE.name,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DICT_TYPE_READ.name,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DICT_TYPE_UPDATE.name,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DICT_TYPE_DELETE.name,
        tenantPemCreate = TenantPermission.ACTION_DICT_TYPE_CREATE.name,
        tenantPemRead = TenantPermission.ACTION_DICT_TYPE_READ.name,
        tenantPemUpdate = TenantPermission.ACTION_DICT_TYPE_UPDATE.name,
        tenantPemDelete = TenantPermission.ACTION_DICT_TYPE_DELETE.name,
    )

    private fun systemReadDto() = ManagerReadTenantDictTypeDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private fun tenantReadDto(scopeId: Long) = ManagerReadTenantDictTypeDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.TENANT.typeId,
        scopeId = scopeId,
    )

    // ── SYSTEM scope reads ──

    @Test
    fun systemScopeReadAllowsSuperLayer() {
        withTransactionalRollback("dict-type-system-read-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadAllowsSystemLayer() {
        withTransactionalRollback("dict-type-system-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SYSTEM, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantAdminLayer() {
        withTransactionalRollback("dict-type-system-read-tenantAdmin") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_ADMIN reading SYSTEM scope")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantPemLayer() {
        withTransactionalRollback("dict-type-system-read-tenantPem") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM reading SYSTEM scope")
        }
    }

    // ── TENANT scope reads ──

    @Test
    fun tenantScopeReadAllowsSuperLayerCrossTenant() {
        withTransactionalRollback("dict-type-tenant-read-super-foreign") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesSystemLayer() {
        withTransactionalRollback("dict-type-tenant-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM reading TENANT scope")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantAdminCrossTenant() {
        withTransactionalRollback("dict-type-tenant-read-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("dict-type-tenant-read-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "TENANT-scope read (own tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesTenantPemCrossTenant() {
        withTransactionalRollback("dict-type-tenant-read-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM cross-tenant TENANT-scope read")
        }
    }

    @Test
    fun readDeniesUnauthorizedUser() {
        withTransactionalRollback("dict-type-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDictTypeController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
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
