package com.lovelycatv.crystalframework.tenant.controller.manager.invitation

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.tenantOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerReadInvitationDTO
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantInvitationController] — Tenant-only Standard.
 */
class ManagerTenantInvitationControllerIntegrationTest(
    @Autowired private val managerTenantInvitationController: ManagerTenantInvitationController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.tenantOnly(
        tenantAdminCreate = SystemPermission.ACTION_TENANT_INVITATION_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_INVITATION_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_INVITATION_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_INVITATION_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_INVITATION_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_INVITATION_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_INVITATION_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_INVITATION_DELETE_PEM,
    )

    private fun readDto(tenantId: Long) = ManagerReadInvitationDTO(page = 1, pageSize = 20, tenantId = tenantId)

    @Test
    fun readEndpointDeniesSuperLayer() {
        withTransactionalRollback("tenant-invitation-read-layer-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SUPER (tenantOnly has no super slot)")
        }
    }

    @Test
    fun readEndpointDeniesSystemLayer() {
        withTransactionalRollback("tenant-invitation-read-layer-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM (tenantOnly has no system slot)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdminOnOwnTenant() {
        withTransactionalRollback("tenant-invitation-read-layer-tenantAdmin-own") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (own tenant)")
        }
    }

    @Test
    fun readEndpointAllowsTenantAdminOnForeignTenant() {
        withTransactionalRollback("tenant-invitation-read-layer-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "read (foreign tenant)")
        }
    }

    @Test
    fun readEndpointAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("tenant-invitation-read-layer-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "read (own tenant)")
        }
    }

    @Test
    fun readEndpointDeniesTenantPemOnForeignTenant() {
        withTransactionalRollback("tenant-invitation-read-layer-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM cross-tenant read")
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("tenant-invitation-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantInvitationController.read(user.authentication, readDto(OWN_TENANT_ID)) }.exceptionOrNull()
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
