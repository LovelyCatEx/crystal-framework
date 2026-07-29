package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowInstanceDTO
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixTestUser
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerApprovalFlowInstanceController] — ReadonlyScoped with
 * [PermissionMatrix.readonly]. The controller overrides `checkPermission` to allow every
 * authenticated caller on READ (the endpoint doubles as a personal "my instances" view), and
 * `buildQueryResponse` injects an `initiator_id` filter for callers without the read-all authority.
 *
 * Consequences for this test:
 *
 *  - Read on SYSTEM scope succeeds for every layer including the unauthorised fixture — the
 *    injected initiator filter falls back to `userAuthentication.userId` (always non-null) and the
 *    downstream `managerService.query` on an empty DB returns an empty page.
 *  - CUD is blocked unconditionally by [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY]
 *    before checkPermission runs, matching the sample [com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerAuditLogControllerIntegrationTest]
 *    pattern.
 */
class ManagerApprovalFlowInstanceControllerIntegrationTest(
    @Autowired private val managerApprovalFlowInstanceController: ManagerApprovalFlowInstanceController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.readonly(
        superRead = SystemPermission.ACTION_X_APPROVAL_FLOW_INSTANCE_READ.name,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_APPROVAL_FLOW_INSTANCE_READ.name,
        tenantPemRead = TenantPermission.ACTION_APPROVAL_FLOW_INSTANCE_READ.name,
    )

    private fun systemReadDto() = ManagerReadApprovalFlowInstanceDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private suspend fun setupUserForLayer(
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = when (layer) {
        PermissionMatrix.Layer.SUPER -> setupSuperUser(matrix, operation)
        PermissionMatrix.Layer.SYSTEM -> setupSystemUser(matrix, operation)
        PermissionMatrix.Layer.TENANT_ADMIN -> setupTenantAdminUser(matrix, operation, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
        PermissionMatrix.Layer.TENANT_PEM -> setupTenantPemUser(matrix, operation, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun systemScopeReadAllowsEveryLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("approval-instance-system-read-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowInstanceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, layer, "SYSTEM-scope read (override checkPermission allows READ for any auth)")
        }
    }

    @Test
    fun systemScopeReadAllowsUnauthorizedUser() {
        withTransactionalRollback("approval-instance-system-read-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowInstanceController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read for unauthorised user (initiator-only view)")
        }
    }

    @Test
    fun createEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-instance-create-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.CREATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowInstanceController.create(
                        user.authentication,
                        ManagerCreateApprovalFlowInstanceDTO(),
                    )
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "create on readonly-scoped controller")
        }
    }

    @Test
    fun updateEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-instance-update-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.UPDATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowInstanceController.update(user.authentication, ManagerUpdateApprovalFlowInstanceDTO(id = 0L))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "update on readonly-scoped controller")
        }
    }

    @Test
    fun deleteEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-instance-delete-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.DELETE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowInstanceController.delete(user.authentication, BaseManagerDeleteDTO(ids = listOf(0L)))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "delete on readonly-scoped controller")
        }
    }

    companion object {
        private const val OWN_TENANT_ID: Long = 1L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
        private const val SYSTEM_SCOPE_ID: Long = 0L
    }
}
