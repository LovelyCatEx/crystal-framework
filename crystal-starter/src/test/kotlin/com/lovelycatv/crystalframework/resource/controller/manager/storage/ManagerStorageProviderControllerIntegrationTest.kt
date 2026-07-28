package com.lovelycatv.crystalframework.resource.controller.manager.storage

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.systemOnly
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
 * Integration test for [ManagerStorageProviderController] — Standard SYSTEM-only.
 */
class ManagerStorageProviderControllerIntegrationTest(
    @Autowired private val managerStorageProviderController: ManagerStorageProviderController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_STORAGE_PROVIDER_CREATE,
        superRead = SystemPermission.ACTION_STORAGE_PROVIDER_READ,
        superUpdate = SystemPermission.ACTION_STORAGE_PROVIDER_UPDATE,
        superDelete = SystemPermission.ACTION_STORAGE_PROVIDER_DELETE,
    )

    private fun readDto() = ManagerReadStorageProviderDTO(page = 1, pageSize = 20)

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
        withTransactionalRollback("storage-provider-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerStorageProviderController.read(user.authentication, readDto()) }.exceptionOrNull()
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
        withTransactionalRollback("storage-provider-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerStorageProviderController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("storage-provider-read-denied-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerStorageProviderController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertLayerDeniedByAuthorization(caught, PermissionMatrix.Layer.SUPER, "read (unauthenticated fixture)")
        }
    }
}
