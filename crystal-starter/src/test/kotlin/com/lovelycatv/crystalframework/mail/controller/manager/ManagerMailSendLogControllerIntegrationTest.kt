package com.lovelycatv.crystalframework.mail.controller.manager

import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerCreateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerDeleteMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerReadMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerUpdateMailSendLogDTO
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
 * Integration test for [ManagerMailSendLogController] — Readonly Standard. Only SUPER holds
 * `mail.send_log.read`; the CUD slots are [PermissionMatrix.NEVER_GRANTED] and every write is
 * blocked by [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY] before
 * authorize even runs.
 */
class ManagerMailSendLogControllerIntegrationTest(
    @Autowired private val managerMailSendLogController: ManagerMailSendLogController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnlyReadonly(
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_SEND_LOG_READ.name,
    )

    private fun readDto() = ManagerReadMailSendLogDTO(page = 1, pageSize = 20)

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
        withTransactionalRollback("mail-send-log-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMailSendLogController.read(user.authentication, readDto()) }.exceptionOrNull()
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
        withTransactionalRollback("mail-send-log-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMailSendLogController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    @Test
    fun createEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("mail-send-log-create-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.CREATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerMailSendLogController.create(user.authentication, ManagerCreateMailSendLogDTO())
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "create on readonly controller")
        }
    }

    @Test
    fun updateEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("mail-send-log-update-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.UPDATE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerMailSendLogController.update(user.authentication, ManagerUpdateMailSendLogDTO(id = 0L))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "update on readonly controller")
        }
    }

    @Test
    fun deleteEndpointDeniedByReadonlyMutability() {
        withTransactionalRollback("mail-send-log-delete-forbidden") {
            val user = setupSuperUser(matrix, ScopedOperation.DELETE)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerMailSendLogController.delete(user.authentication, ManagerDeleteMailSendLogDTO(ids = listOf(0L)))
                }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "delete on readonly controller")
        }
    }
}
