package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixTestUser
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import com.lovelycatv.crystalframework.test.permission.assertLayerDeniedByAuthorization
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadUserDTO
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerUserController] — a Standard SYSTEM-only controller that mirrors
 * [ManagerOAuthAccountControllerIntegrationTest]. The matrix declares real `system.user.*`
 * authorities on the `system` slots and leaves every other slot as [PermissionMatrix.NOT_APPLICABLE];
 * therefore only the SYSTEM layer may reach the read endpoints, the other three layers must fail
 * the [com.lovelycatv.crystalframework.shared.controller.StandardManagerController.authorize] OR-check.
 */
class ManagerUserControllerIntegrationTest(
    @Autowired private val managerUserController: ManagerUserController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_USER_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_USER_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_USER_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_USER_DELETE.name,
    )

    private fun readDto() = ManagerReadUserDTO(page = 1, pageSize = 20)

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
    fun readEndpointAuthorizesOnlySystemLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("user-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerUserController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "read")
                else -> assertLayerDeniedByAuthorization(caught, layer, "read")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointAuthorizesOnlySystemLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("user-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerUserController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("user-read-denied-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerUserController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertLayerDeniedByAuthorization(caught, PermissionMatrix.Layer.SYSTEM, "read (unauthenticated fixture)")
        }
    }

}
