package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertDeniedByUnauthorized
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantRoleController] — Tenant-only Standard controller. Exercises
 * the four-permission authorize chain in [com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController]:
 *
 *  - `super` / `system` layers are [PermissionMatrix.NOT_APPLICABLE]; fixtures on those layers
 *    carry no matrix authority so `hasAuthority(readPermission)` and `hasScopedAuthority(scopedReadPermission)`
 *    both fail → [com.lovelycatv.crystalframework.shared.exception.ForbiddenException].
 *  - `tenantAdmin` holds the `tenant.role.read` (SystemPermission) authority — that is the
 *    `readPermission`; `hasAuthority` short-circuits before `isQueryInScope`, so cross-tenant reads
 *    also succeed.
 *  - `tenantPem` holds `i.tenant.role.read` (TenantPermission) — the `scopedReadPermission`;
 *    `isQueryInScope` compares `dto.tenantId == userAuthentication.tenantId`, so a mismatch throws
 *    [com.lovelycatv.crystalframework.shared.exception.UnauthorizedException].
 */
class ManagerTenantRoleControllerIntegrationTest(
    @Autowired private val managerTenantRoleController: ManagerTenantRoleController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_ROLE_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_ROLE_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_ROLE_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_ROLE_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_ROLE_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_ROLE_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_ROLE_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_ROLE_DELETE_PEM,
    )

    private fun readDto(tenantId: Long) = ManagerReadTenantRoleDTO(page = 1, pageSize = 20, tenantId = tenantId)

    @Test
    fun readEndpointDeniesSuperLayer() {
        withTransactionalRollback("tenant-role-read-layer-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SUPER (tenantOnly matrix has no super slot)")
        }
    }

    @Test
    fun readEndpointDeniesSystemLayer() {
        withTransactionalRollback("tenant-role-read-layer-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM (tenantOnly matrix has no system slot)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdminOnOwnTenant() {
        withTransactionalRollback("tenant-role-read-layer-tenantAdmin-own") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (own tenant)")
        }
    }

    /**
     * `tenantAdmin` bypasses tenant-id equality — the legacy `hasAuthority(readPermission)`
     * short-circuit does not consult the DTO's tenantId. A read for a different tenant must
     * therefore also succeed.
     */
    @Test
    fun readEndpointAllowsTenantAdminOnForeignTenant() {
        withTransactionalRollback("tenant-role-read-layer-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (foreign tenant)")
        }
    }

    @Test
    fun readEndpointAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("tenant-role-read-layer-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "read (own tenant)")
        }
    }

    @Test
    fun readEndpointDeniesTenantPemOnForeignTenant() {
        withTransactionalRollback("tenant-role-read-layer-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByUnauthorized(caught, "TENANT_PEM cross-tenant read")
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("tenant-role-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantRoleController.read(user.authentication, readDto(tenantId = OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "unauthorised fixture")
        }
    }

    companion object {
        // Placeholder tenant ids: the test suite never creates real tenant/member rows because
        // the tenantAdmin authorize path short-circuits via `hasAuthority`, and tenantPem denial
        // is exercised on the pre-service `isQueryInScope` comparison (never reaches the DB).
        private const val OWN_TENANT_ID: Long = 1L
        private const val FOREIGN_TENANT_ID: Long = 2L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
    }
}
