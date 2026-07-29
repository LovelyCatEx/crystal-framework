package com.lovelycatv.crystalframework.mail.controller.manager.template

import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerReadMailTemplateDTO
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
 * Integration test for [ManagerMailTemplateController] — Standard SYSTEM-only.
 */
class ManagerMailTemplateControllerIntegrationTest(
    @Autowired private val managerMailTemplateController: ManagerMailTemplateController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnly(
        systemCreate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_CREATE.name,
        systemRead = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_READ.name,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_UPDATE.name,
        systemDelete = SystemPermission.ACTION_SYSTEM_MAIL_TEMPLATE_DELETE.name,
    )

    private fun readDto() = ManagerReadMailTemplateDTO(page = 1, pageSize = 20)

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
        withTransactionalRollback("mail-template-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMailTemplateController.read(user.authentication, readDto()) }.exceptionOrNull()
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
        withTransactionalRollback("mail-template-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMailTemplateController.readAll(user.authentication) }.exceptionOrNull()
            }
            when (layer) {
                PermissionMatrix.Layer.SYSTEM -> assertLayerAllowed(caught, layer, "readAll")
                else -> assertLayerDeniedByAuthorization(caught, layer, "readAll")
            }
        }
    }

    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("mail-template-read-denied-no-auth") {
            val user = setupUnauthorizedUser()
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMailTemplateController.read(user.authentication, readDto()) }.exceptionOrNull()
            }
            assertLayerDeniedByAuthorization(caught, PermissionMatrix.Layer.SYSTEM, "read (unauthenticated fixture)")
        }
    }
}
