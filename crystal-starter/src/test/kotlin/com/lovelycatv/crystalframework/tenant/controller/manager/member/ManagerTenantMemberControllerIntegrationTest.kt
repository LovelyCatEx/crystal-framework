package com.lovelycatv.crystalframework.tenant.controller.manager.member

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertDeniedByUnauthorized
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantMemberController] — Tenant-only Standard. `tenantPemCreate`
 * is [PermissionMatrix.NEVER_GRANTED] (members are provisioned via the invitation flow), but read
 * paths follow the same tenant-only shape as [ManagerTenantRoleControllerIntegrationTest].
 */
class ManagerTenantMemberControllerIntegrationTest(
    @Autowired private val managerTenantMemberController: ManagerTenantMemberController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_MEMBER_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_MEMBER_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_MEMBER_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_MEMBER_DELETE,
        tenantPemCreate = PermissionMatrix.NEVER_GRANTED,
        tenantPemRead = TenantPermission.ACTION_TENANT_MEMBER_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_MEMBER_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_MEMBER_DELETE_PEM,
    )

    private fun readDto(tenantId: Long) = ManagerReadTenantMemberDTO(page = 1, pageSize = 20, tenantId = tenantId)

    @Test
    fun readEndpointDeniesSuperLayer() {
        withTransactionalRollback("tenant-member-read-layer-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SUPER (tenantOnly has no super slot)")
        }
    }

    @Test
    fun readEndpointDeniesSystemLayer() {
        withTransactionalRollback("tenant-member-read-layer-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM (tenantOnly has no system slot)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdminOnOwnTenant() {
        withTransactionalRollback("tenant-member-read-layer-tenantAdmin-own") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (own tenant)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdminOnForeignTenant() {
        withTransactionalRollback("tenant-member-read-layer-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (foreign tenant)")
        }
    }

    @Test
    fun readEndpointAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("tenant-member-read-layer-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "read (own tenant)")
        }
    }

    @Test
    fun readEndpointDeniesTenantPemOnForeignTenant() {
        withTransactionalRollback("tenant-member-read-layer-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByUnauthorized(caught, "TENANT_PEM cross-tenant read")
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("tenant-member-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantMemberController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "unauthorised fixture")
        }
    }

    companion object {
        private const val OWN_TENANT_ID: Long = 1L
        private const val FOREIGN_TENANT_ID: Long = 2L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
    }
}
