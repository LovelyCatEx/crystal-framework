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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

class ManagerTenantPermissionControllerIntegrationTest(
    @Autowired private val managerTenantPermissionController: ManagerTenantPermissionController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_X_TENANT_PERMISSION_CREATE.name
            read = SystemPermission.ACTION_X_TENANT_PERMISSION_READ.name
            update = SystemPermission.ACTION_X_TENANT_PERMISSION_UPDATE.name
            delete = SystemPermission.ACTION_X_TENANT_PERMISSION_DELETE.name
        }
        tenantAdmin {
            read = SystemPermission.ACTION_TENANT_PERMISSION_READ.name
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
    fun readEndpointAuthorizesSuperAndTenantAdminLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER, PermissionMatrix.Layer.TENANT_ADMIN -> assertLayerAllowed(caught, layer, "read")
                else -> assertLayerDeniedByAuthorization(caught, layer, "read")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointAuthorizesSuperAndTenantAdminLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER, PermissionMatrix.Layer.TENANT_ADMIN -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }
}
