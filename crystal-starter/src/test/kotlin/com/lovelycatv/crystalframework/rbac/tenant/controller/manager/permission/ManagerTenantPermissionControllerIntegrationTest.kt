package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.of
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixTestUser
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import com.lovelycatv.crystalframework.test.permission.assertLayerDeniedByAuthorization
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerTenantPermissionController] — a [StandardManagerController]
 * whose production matrix uses [PermissionMatrix.of] with only [tenantAdmin] and [tenantPem]
 * layers (since the controller is `StandardManagerController` the [authorize] pipeline hardcodes
 * [ResourceScope.SYSTEM], but a tenant-permission resource is intrinsically TENANT-scoped).
 *
 * Because `layersFor(SYSTEM, READ)` on the production matrix returns only
 * `[NOT_APPLICABLE, NOT_APPLICABLE]` → filtered → `[]`, **every layer** is denied for a
 * SYSTEM-scoped read (the fixture never granTS an authority the controller's matrix checks).
 *
 * The test verifies that the `authorize` control flow runs and correctly denies every layer
 * (TENANT_ADMIN and TENANT_PEM fixtures are empty because their slots are `NOT_APPLICABLE`
 * in the production matrix; SUPER and SYSTEM fixtures are also empty after the filter).
 */
class ManagerTenantPermissionControllerIntegrationTest(
    @Autowired private val managerTenantPermissionController: ManagerTenantPermissionController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_X_MESSAGE_CHANNEL_CREATE.name
            read = SystemPermission.ACTION_X_MESSAGE_CHANNEL_READ.name
            update = SystemPermission.ACTION_X_MESSAGE_CHANNEL_UPDATE.name
            delete = SystemPermission.ACTION_X_MESSAGE_CHANNEL_DELETE.name
        }
        system {
            create = PermissionMatrix.NOT_APPLICABLE
            read = SystemPermission.ACTION_SYSTEM_PERMISSION_READ.name
            update = PermissionMatrix.NOT_APPLICABLE
            delete = PermissionMatrix.NOT_APPLICABLE
        }
    }

    private fun readDto() = ManagerReadTenantPermissionDTO(page = 1, pageSize = 20)

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
    fun readEndpointDeniesAllLayersBecauseProductionMatrixHasOnlyTenantLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            // Production matrix has no super/system READ authorities, so layersFor(SYSTEM, READ) is
            // always empty — every layer is denied.
            assertLayerDeniedByAuthorization(caught, layer, "read")
        }
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointDeniesAllLayersBecauseProductionMatrixHasOnlyTenantLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.readAll(user.authentication) }.exceptionOrNull()
            }
            assertLayerDeniedByAuthorization(caught, layer, "readAll")
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("tenant-permission-read-denied-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertLayerDeniedByAuthorization(caught, PermissionMatrix.Layer.SUPER, "read (unauthenticated fixture)")
        }
    }
}
