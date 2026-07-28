package com.lovelycatv.crystalframework.messagechannel.controller.manager

import com.lovelycatv.crystalframework.messagechannel.controller.manager.dto.ManagerReadMessageChannelDTO
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.assertDeniedByForbidden
import com.lovelycatv.crystalframework.test.permission.assertDeniedByUnauthorized
import com.lovelycatv.crystalframework.test.permission.assertLayerAllowed
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext

/**
 * Integration test for [ManagerMessageChannelController] — Standard Scoped with a full 16-slot
 * [PermissionMatrix]. Same shape as the DictType test: the read DTO carries `scope + scopeId`,
 * so the base class default `resolveScopeFromReadDTO` reads the fields directly (no DB
 * dependency). The authorize chain runs `checkPermission` (RBAC OR-check across
 * `layersFor(scope, op)`) then `checkOwnership` (TENANT-scope only: cross-tenant layer or
 * `scopeId == tenantId`).
 */
class ManagerMessageChannelControllerIntegrationTest(
    @Autowired private val managerMessageChannelController: ManagerMessageChannelController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix(
        superCreate = SystemPermission.ACTION_MESSAGE_CHANNEL_CREATE,
        superRead = SystemPermission.ACTION_MESSAGE_CHANNEL_READ,
        superUpdate = SystemPermission.ACTION_MESSAGE_CHANNEL_UPDATE,
        superDelete = SystemPermission.ACTION_MESSAGE_CHANNEL_DELETE,
        systemCreate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_CREATE,
        systemRead = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_READ,
        systemUpdate = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_UPDATE,
        systemDelete = SystemPermission.ACTION_SYSTEM_MESSAGE_CHANNEL_DELETE,
        tenantAdminCreate = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE,
        tenantAdminRead = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ,
        tenantAdminUpdate = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE,
        tenantAdminDelete = SystemPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE,
        tenantPemCreate = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_CREATE_PEM,
        tenantPemRead = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_READ_PEM,
        tenantPemUpdate = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_UPDATE_PEM,
        tenantPemDelete = TenantPermission.ACTION_TENANT_MESSAGE_CHANNEL_DELETE_PEM,
    )

    private fun systemReadDto() = ManagerReadMessageChannelDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.SYSTEM.typeId,
        scopeId = SYSTEM_SCOPE_ID,
    )

    private fun tenantReadDto(scopeId: Long) = ManagerReadMessageChannelDTO(
        page = 1, pageSize = 20,
        scope = ResourceScope.TENANT.typeId,
        scopeId = scopeId,
    )

    // ── SYSTEM scope reads ──

    @Test
    fun systemScopeReadAllowsSuperLayer() {
        withTransactionalRollback("message-channel-system-read-super") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadAllowsSystemLayer() {
        withTransactionalRollback("message-channel-system-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SYSTEM, "SYSTEM-scope read")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantAdminLayer() {
        withTransactionalRollback("message-channel-system-read-tenantAdmin") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_ADMIN reading SYSTEM scope")
        }
    }

    @Test
    fun systemScopeReadDeniesTenantPemLayer() {
        withTransactionalRollback("message-channel-system-read-tenantPem") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, systemReadDto()) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "TENANT_PEM reading SYSTEM scope")
        }
    }

    // ── TENANT scope reads ──

    @Test
    fun tenantScopeReadAllowsSuperLayerCrossTenant() {
        withTransactionalRollback("message-channel-tenant-read-super-foreign") {
            val user = setupSuperUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.SUPER, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesSystemLayer() {
        withTransactionalRollback("message-channel-tenant-read-system") {
            val user = setupSystemUser(matrix, ScopedOperation.READ)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "SYSTEM reading TENANT scope")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantAdminCrossTenant() {
        withTransactionalRollback("message-channel-tenant-read-tenantAdmin-foreign") {
            val user = setupTenantAdminUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_ADMIN, "TENANT-scope read (cross-tenant)")
        }
    }

    @Test
    fun tenantScopeReadAllowsTenantPemOnOwnTenant() {
        withTransactionalRollback("message-channel-tenant-read-tenantPem-own") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertLayerAllowed(caught, PermissionMatrix.Layer.TENANT_PEM, "TENANT-scope read (own tenant)")
        }
    }

    @Test
    fun tenantScopeReadDeniesTenantPemCrossTenant() {
        withTransactionalRollback("message-channel-tenant-read-tenantPem-foreign") {
            val user = setupTenantPemUser(matrix, ScopedOperation.READ, tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(FOREIGN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByUnauthorized(caught, "TENANT_PEM cross-tenant TENANT-scope read")
        }
    }

    @Test
    fun readDeniesUnauthorizedUser() {
        withTransactionalRollback("message-channel-read-denied-no-auth") {
            val user = setupUnauthorizedUser(tenantId = OWN_TENANT_ID, tenantMemberId = OWN_TENANT_MEMBER_ID)
            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching { managerMessageChannelController.read(user.authentication, tenantReadDto(OWN_TENANT_ID)) }.exceptionOrNull()
            }
            assertDeniedByForbidden(caught, "unauthorised fixture")
        }
    }

    companion object {
        private const val OWN_TENANT_ID: Long = 1L
        private const val FOREIGN_TENANT_ID: Long = 2L
        private const val OWN_TENANT_MEMBER_ID: Long = 100L
        private const val SYSTEM_SCOPE_ID: Long = 0L
    }
}
