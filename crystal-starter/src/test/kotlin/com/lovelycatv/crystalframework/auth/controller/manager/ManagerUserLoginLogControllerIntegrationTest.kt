package com.lovelycatv.crystalframework.auth.controller.manager

import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerCreateUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerDeleteUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerReadUserLoginLogDTO
import com.lovelycatv.crystalframework.auth.controller.manager.dto.ManagerUpdateUserLoginLogDTO
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
 * Integration test for [ManagerUserLoginLogController] — Readonly Standard. Only SUPER holds
 * `user.login_log.read`; CUD is unconditionally blocked by
 * [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY].
 */
class ManagerUserLoginLogControllerIntegrationTest(
    @Autowired private val managerUserLoginLogController: ManagerUserLoginLogController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnlyReadonly(
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        superRead = SystemPermission.ACTION_USER_LOGIN_LOG_READ,
    )

    private fun readDto() = ManagerReadUserLoginLogDTO(page = 1, pageSize = 20)

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
        withTransactionalRollback("user-login-log-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerUserLoginLogController.read(user.authentication, readDto()) }.exceptionOrNull()
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
        withTransactionalRollback("user-login-log-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerUserLoginLogController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    @Test
    fun createEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("user-login-log-create-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.CREATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerUserLoginLogController.create(user.authentication, ManagerCreateUserLoginLogDTO())
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "create on readonly controller")
        }
    }

    @Test
    fun updateEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("user-login-log-update-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.UPDATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerUserLoginLogController.update(user.authentication, ManagerUpdateUserLoginLogDTO(id = 0L))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "update on readonly controller")
        }
    }

    @Test
    fun deleteEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("user-login-log-delete-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.DELETE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerUserLoginLogController.delete(user.authentication, ManagerDeleteUserLoginLogDTO(ids = listOf(0L)))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "delete on readonly controller")
        }
    }
}
