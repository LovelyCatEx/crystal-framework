package com.lovelycatv.crystalframework.approval.controller.manager

import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerCreateApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerReadApprovalFlowTaskDTO
import com.lovelycatv.crystalframework.approval.controller.manager.dto.ManagerUpdateApprovalFlowTaskDTO
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
 * Integration test for [ManagerApprovalFlowTaskController] — ReadonlyScoped with no
 * [PermissionMatrix] (production uses `permissions = null`). The controller overrides
 * `checkPermission` to return `true` iff `operation == READ`; combined with the null-matrix
 * default of `checkOwnership = true`, every authenticated user may READ. Writes are blocked by
 * [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY] before either
 * permission hook runs.
 *
 * Because the matrix is null, the base-class fixture helpers cannot derive authorities from it;
 * we pass a placeholder [PermissionMatrix.readonly] matrix (whose values never enter the
 * production authorize path) purely so the base helpers know which slot to consult. What matters
 * is that no authority in the fixture intersects the controller's decision, which is intentional:
 * this controller is a "personal to-do view" and does not consult authorities on read.
 */
class ManagerApprovalFlowTaskControllerIntegrationTest(
    @Autowired private val managerApprovalFlowTaskController: ManagerApprovalFlowTaskController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    /**
     * Placeholder matrix — production controller has `permissions = null`. The values here never
     * feed into the controller's authorize path (its checkPermission override ignores authorities
     * entirely on READ), but the fixture helpers need *some* matrix to know which authority to
     * install for the layer under test. Using [PermissionMatrix.readonly] with dummy strings keeps
     * every CUD slot at [PermissionMatrix.NEVER_GRANTED] so the fixture never grants a real
     * mutation authority by accident.
     */
    private val fixtureMatrix: PermissionMatrix = PermissionMatrix.readonly(
        superRead = FIXTURE_PLACEHOLDER_AUTHORITY,
        systemRead = FIXTURE_PLACEHOLDER_AUTHORITY,
        tenantAdminRead = FIXTURE_PLACEHOLDER_AUTHORITY,
        tenantPemRead = FIXTURE_PLACEHOLDER_AUTHORITY,
    )

    private fun systemReadDto() = ManagerReadApprovalFlowTaskDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private suspend fun setupUserForLayer(
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = when (layer) {
        PermissionMatrix.Layer.SUPER -> setupSuperUser(fixtureMatrix, operation)
        PermissionMatrix.Layer.SYSTEM -> setupSystemUser(fixtureMatrix, operation)
        PermissionMatrix.Layer.TENANT_ADMIN -> setupTenantAdminUser(fixtureMatrix, operation, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
        PermissionMatrix.Layer.TENANT_PEM -> setupTenantPemUser(fixtureMatrix, operation, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun systemScopeReadAllowsEveryLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("approval-task-system-read-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowTaskController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, layer, "SYSTEM-scope read (override checkPermission unconditionally allows READ)")
        }
    }

    @Test
    fun systemScopeReadAllowsUnauthorizedUser() {
        withTransactionalRollback("approval-task-system-read-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerApprovalFlowTaskController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read for unauthorised user")
        }
    }

    @Test
    fun createEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-task-create-forbidden") {
            val user = setupSuperUser(fixtureMatrix, ScopedOperation.CREATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowTaskController.create(user.authentication, ManagerCreateApprovalFlowTaskDTO())
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "create on readonly-scoped controller")
        }
    }

    @Test
    fun updateEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-task-update-forbidden") {
            val user = setupSuperUser(fixtureMatrix, ScopedOperation.UPDATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowTaskController.update(user.authentication, ManagerUpdateApprovalFlowTaskDTO(id = 0L))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "update on readonly-scoped controller")
        }
    }

    @Test
    fun deleteEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("approval-task-delete-forbidden") {
            val user = setupSuperUser(fixtureMatrix, ScopedOperation.DELETE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerApprovalFlowTaskController.delete(user.authentication, BaseManagerDeleteDTO(ids = listOf(0L)))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "delete on readonly-scoped controller")
        }
    }

    companion object {
        // Sentinel string placed in every fixture slot; never intersects any real production
        // authority because it does not follow any registered naming convention and no role
        // seeds it into RBAC.
        private const val FIXTURE_PLACEHOLDER_AUTHORITY = "test.fixture.placeholder"
        private const val OWN_TENANT_ID: Long = 1L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
        private const val SYSTEM_SCOPE_ID: Long = 0L
    }
}
