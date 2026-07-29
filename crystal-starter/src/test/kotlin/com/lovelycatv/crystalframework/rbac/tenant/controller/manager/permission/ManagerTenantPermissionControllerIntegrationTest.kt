package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.permission

import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
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
 * Integration test for [ManagerTenantPermissionController] — the sole Standard controller declared
 * with the DSL `PermissionMatrix.of { ... }` and whose SUPER + SYSTEM layers each carry a distinct
 * authority. The matrix mirrors the production controller exactly so the OR-check in
 * [com.lovelycatv.crystalframework.shared.controller.StandardManagerController.authorize] observes
 * the same `layersFor(SYSTEM, READ) = [superRead, systemRead]` two-value array.
 *
 * Because `layersFor(SYSTEM, ...)` unions both slots, both SUPER and SYSTEM fixtures satisfy the
 * check for read (each fixture supplies its layer's own authority string). TENANT_ADMIN and
 * TENANT_PEM slots remain [PermissionMatrix.NOT_APPLICABLE] and are filtered out by the base class
 * fixture builder — those users receive an empty authority set and must be denied.
 *
 * For CUD, only `super*` holds a real authority; `system*` is [PermissionMatrix.NOT_APPLICABLE].
 * The read tests here therefore have a wider allow-set than the CUD tests would; only read is
 * exercised, in line with the sample template.
 */
class ManagerTenantPermissionControllerIntegrationTest(
    @Autowired private val managerTenantPermissionController: ManagerTenantPermissionController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.of {
        `super` {
            create = SystemPermission.ACTION_TENANT_PERMISSION_CREATE.name
            read = TenantPermission.ACTION_ROLE_PERMISSION_READ.name
            update = SystemPermission.ACTION_TENANT_PERMISSION_UPDATE.name
            delete = SystemPermission.ACTION_TENANT_PERMISSION_DELETE.name
        }
        system {
            create = PermissionMatrix.NOT_APPLICABLE
            read = SystemPermission.ACTION_TENANT_PERMISSION_READ.name
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
    fun readEndpointAuthorizesSuperAndSystemLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER, PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "read")
                else -> assertLayerDeniedByAuthorization(caught, layer, "read")
            }
        }
    }

    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointAuthorizesSuperAndSystemLayers(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("tenant-permission-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerTenantPermissionController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER, PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
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
