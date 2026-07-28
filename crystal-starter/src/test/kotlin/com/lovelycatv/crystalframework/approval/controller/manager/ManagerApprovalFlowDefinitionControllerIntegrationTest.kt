package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowDefinitionDTO
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
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
 * Integration test for [ManagerApprovalFlowDefinitionController] — Scoped with a full 16-slot
 * [PermissionMatrix]. Same authorize pipeline as
 * [com.lovelycatv.crystalframework.tenant.controller.manager.dict.ManagerTenantDictTypeControllerIntegrationTest]
 * (`checkPermission` → `checkOwnership`); its DTO carries `scope + scopeId` directly so no DB
 * bootstrap is needed.
 */
class ManagerApprovalFlowDefinitionControllerIntegrationTest(
    @Autowired private val managerApprovalFlowDefinitionController: ManagerApprovalFlowDefinitionController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix(
        superCreate = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_CREATE,
        superRead = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_READ,
        superUpdate = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_UPDATE,
        superDelete = SystemPermission.ACTION_APPROVAL_FLOW_DEFINITION_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_APPROVAL_FLOW_DEFINITION_DELETE,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_APPROVAL_FLOW_DEFINITION_DELETE_PEM,
    )

    private fun systemReadDto() = ManagerReadApprovalFlowDefinitionDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private fun tenantReadDto(scopeId: Long) = ManagerReadApprovalFlowDefinitionDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.TENANT.typeId,
        scopeId = scopeId,
    )

    // ── SYSTEM scope reads ──

    @Test
    fun systemScopeReadAllowsSuperLayer() {
        withTransactionalRollback("approval-def-system-read-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadAllowsSystemLayer() {
        withTransactionalRollback("approval-def-system-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SYSTEM, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantAdminLayer() {
        withTransactionalRollback("approval-def-system-read-tenantAdmin") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_ADMIN reading SYSTEM scope")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantPemLayer() {
        withTransactionalRollback("approval-def-system-read-tenantPem") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM reading SYSTEM scope")
        }
    }

    // ── TENANT scope reads ──

    @Test
    fun tenantScopeReadAllowsSuperLayerCrossTenant() {
        withTransactionalRollback("approval-def-tenant-read-super-foreign") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesSystemLayer() {
        withTransactionalRollback("approval-def-tenant-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM reading TENANT scope")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantAdminCrossTenant() {
        withTransactionalRollback("approval-def-tenant-read-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("approval-def-tenant-read-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "TENANT-scope read (own tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesTenantPemCrossTenant() {
        withTransactionalRollback("approval-def-tenant-read-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM cross-tenant TENANT-scope read")
        }
    }

    @Test
    fun readDeniesUnauthorizedUser() {
        withTransactionalRollback("approval-def-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowDefinitionController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
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
