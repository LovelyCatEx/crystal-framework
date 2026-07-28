package com.lovelycatv.crystalframework.tenant.controller.manager.department.member

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbiddenOrUnauthorized
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantDepartmentMemberController] — Tenant-only Standard, derived
 * from a parent department (DTO carries `departmentId`, not `tenantId`). The controller overrides
 * `isQueryInScope` / `isCreateInScope` / `isUpdateInScope` to walk the parent chain via
 * `checkIsRelatedToRootParent`, so the tenantPem own-tenant path requires real department rows to
 * succeed. This test avoids that DB dependency by only exercising the pre-service branches:
 *
 *  - SUPER / SYSTEM → [com.lovelycatv.crystalframework.shared.exception.ForbiddenException] from
 *    the missing authority (authorize never reaches isQueryInScope).
 *  - tenantAdmin → allowed (short-circuit via `hasAuthority(readPermission)`; isQueryInScope is
 *    skipped, so no DB row is required — the follow-on `managerService.query` on an empty DB
 *    returns an empty page).
 *  - tenantPem → denied on any read because `isQueryInScope` calls
 *    `tenantDepartmentManagerService.checkIsRelatedToRootParent(departmentId, tenantId)` which
 *    returns false on an empty DB → the branch throws
 *    [com.lovelycatv.crystalframework.shared.exception.UnauthorizedException]. Either
 *    `ForbiddenException` (from a missing authority path) or `UnauthorizedException` (from the
 *    scope check) is a valid deny outcome; the [assertDeniedByForbiddenOrUnauthorized] helper
 *    tolerates both without over-specifying the enforcement path.
 */
class ManagerTenantDepartmentMemberControllerIntegrationTest(
    @Autowired private val managerTenantDepartmentMemberController: ManagerTenantDepartmentMemberController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_DEPARTMENT_MEMBER_RELATION_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_DEPARTMENT_MEMBER_DELETE_PEM,
    )

    private fun readDto() = ManagerReadTenantDepartmentMemberDTO(
        page = 1,
        pageSize = 20,
        departmentId = DEPARTMENT_ID,
    )

    @Test
    fun readEndpointDeniesSuperLayer() {
        withTransactionalRollback("tenant-dept-member-read-layer-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDepartmentMemberController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SUPER (tenantOnly has no super slot)")
        }
    }

    @Test
    fun readEndpointDeniesSystemLayer() {
        withTransactionalRollback("tenant-dept-member-read-layer-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDepartmentMemberController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM (tenantOnly has no system slot)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdmin() {
        withTransactionalRollback("tenant-dept-member-read-layer-tenantAdmin") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDepartmentMemberController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read")
        }
    }

    @Test
    fun readEndpointDeniesTenantPemWithoutRealDepartmentRow() {
        withTransactionalRollback("tenant-dept-member-read-layer-tenantPem-no-row") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDepartmentMemberController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertDeniedByForbiddenOrUnauthorized(caught, "TENANT_PEM read requires a real department row to link back to the tenant; empty DB")
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("tenant-dept-member-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantDepartmentMemberController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "unauthorised fixture")
        }
    }

    companion object {
        private const val OWN_TENANT_ID: Long = 1L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
        private const val DEPARTMENT_ID: Long = 999_999L
    }
}
