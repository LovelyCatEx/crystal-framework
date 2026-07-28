package com.lovelycatv.crystalframework.audit.controller.manager.auditlog

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerDeleteAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerReadAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.systemOnlyReadonly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixTestUser
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import com.lovelycatv.crystalframework.test.permission.assertLayerDeniedByAuthorization
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerAuditLogController] — Readonly Standard, backed by
 * [PermissionMatrix.systemOnlyReadonly]. Read paths are exercised through the layer parameterised
 * matrix identically to the Standard SYSTEM-only template; write paths are additionally exercised
 * against a SUPER fixture so any regression that skips [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY]
 * would let the call through and immediately fail the assertion.
 */
class ManagerAuditLogControllerIntegrationTest(
    @Autowired private val managerAuditLogController: ManagerAuditLogController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    /**
     * Production controller: super holds the real `audit_log.read` authority and `systemRead` is
     * left as [PermissionMatrix.NOT_APPLICABLE] — no system-scoped consumer of audit logs exists.
     * `systemOnlyReadonly` fills every CUD slot with [PermissionMatrix.NEVER_GRANTED] so a fixture
     * built on a CUD operation lands with an empty authority set (matching production RBAC).
     */
    private val matrix: PermissionMatrix = PermissionMatrix.systemOnlyReadonly(
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        superRead = SystemPermission.ACTION_AUDIT_LOG_READ,
    )

    private fun readDto() = ManagerReadAuditLogDTO(page = 1, pageSize = 20)

    private suspend fun setupUserForLayer(
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = when (layer) {
        PermissionMatrix.Layer.SUPER -> setupSuperUser(matrix, operation)
        PermissionMatrix.Layer.SYSTEM -> setupSystemUser(matrix, operation)
        PermissionMatrix.Layer.TENANT_ADMIN -> setupTenantAdminUser(matrix, operation, tenantId = 0L, tenantMemberId = 0L)
        PermissionMatrix.Layer.TENANT_PEM -> setupTenantPemUser(matrix, operation, tenantId = 0L, tenantMemberId = 0L)
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readEndpointAuthorizesOnlySuperLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("audit-log-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerAuditLogController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertLayerAllowed(caught, layer, "read")
                else -> assertLayerDeniedByAuthorization(caught, layer, "read")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointAuthorizesOnlySuperLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("audit-log-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerAuditLogController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    /**
     * Ensures [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY] fires before
     * `authorize` — a SUPER fixture would otherwise pass the authorize step (its create authority
     * is [PermissionMatrix.NEVER_GRANTED], which the OR-check rejects, but we still need to prove
     * the mutability guard blocks first and returns [com.lovelycatv.crystalframework.shared.exception.ForbiddenException]
     * rather than [org.springframework.security.authorization.AuthorizationDeniedException]).
     */
    @Test
    fun createEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("audit-log-create-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.CREATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerAuditLogController.create(user.authentication, ManagerCreateAuditLogDTO())
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "create on readonly controller")
        }
    }

    @Test
    fun updateEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("audit-log-update-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.UPDATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerAuditLogController.update(user.authentication, ManagerUpdateAuditLogDTO(id = 0L))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "update on readonly controller")
        }
    }

    @Test
    fun deleteEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("audit-log-delete-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.DELETE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerAuditLogController.delete(user.authentication, ManagerDeleteAuditLogDTO(ids = listOf(0L)))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "delete on readonly controller")
        }
    }
}
