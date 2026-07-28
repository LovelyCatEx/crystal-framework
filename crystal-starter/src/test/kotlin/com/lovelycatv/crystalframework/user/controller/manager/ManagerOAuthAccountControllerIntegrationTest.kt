package com.lovelycatv.crystalframework.user.controller.manager

import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.systemOnly
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixIntegrationTestBase
import com.lovelycatv.crystalframework.test.permission.PermissionMatrixTestUser
import com.lovelycatv.crystalframework.user.controller.manager.dto.ManagerReadOAuthAccountDTO
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authorization.AuthorizationDeniedException
import kotlin.test.assertTrue

/**
 * Integration test for [ManagerOAuthAccountController] — the first controller migrated to the
 * unified [PermissionMatrix] model (WS B). Exercises the AOP aspect + `authorize` hook chain
 * end-to-end through a real Spring bean (so `ManagerControllerPermissionAspect`'s reflection
 * detects the non-null `permissions` field and delegates to
 * `StandardManagerController.authorize`).
 *
 * The controller is configured with [PermissionMatrix.Companion.systemOnly]: only the `super`
 * layer carries real authorities (`oauth.account.*`), the `system` / `tenantAdmin` / `tenantPem`
 * slots are [PermissionMatrix.NOT_APPLICABLE]. Therefore the parametrised layer-matrix tests
 * expect `SUPER` to be allowed and the other three layers to be denied.
 *
 * The `read` and `readAll` (`GET /list`) endpoints are both covered because their permission
 * paths differ: `read` is inherited from [com.lovelycatv.crystalframework.shared.controller.AbstractManagerController]
 * and routes through `preflight` → `mutability` → `authorize`; `readAll` is declared directly on
 * [com.lovelycatv.crystalframework.shared.controller.StandardManagerController] and invokes
 * `authorize` inline. Both must reach the same allow/deny verdict for the same user.
 */
class ManagerOAuthAccountControllerIntegrationTest(
    @Autowired private val managerOAuthAccountController: ManagerOAuthAccountController,
    @Autowired applicationContext: ApplicationContext,
) : PermissionMatrixIntegrationTestBase(applicationContext) {

    private val matrix: PermissionMatrix = PermissionMatrix.systemOnly(
        systemCreate = PermissionMatrix.NOT_APPLICABLE,
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        systemUpdate = PermissionMatrix.NOT_APPLICABLE,
        systemDelete = PermissionMatrix.NOT_APPLICABLE,
        superCreate = SystemPermission.ACTION_OAUTH_ACCOUNT_CREATE,
        superRead = SystemPermission.ACTION_OAUTH_ACCOUNT_READ,
        superUpdate = SystemPermission.ACTION_OAUTH_ACCOUNT_UPDATE,
        superDelete = SystemPermission.ACTION_OAUTH_ACCOUNT_DELETE,
    )

    private fun readDto() = ManagerReadOAuthAccountDTO(page = 1, pageSize = 20)

    /**
     * Provision a fixture user for [layer] + [operation]. TENANT_ADMIN / TENANT_PEM require
     * non-null `tenantId` / `tenantMemberId` on their [com.lovelycatv.crystalframework.shared.types.UserAuthentication];
     * pass 0L placeholders because a SYSTEM-only controller never reads them, and the authorities
     * set for those layers is empty anyway (matrix declares NOT_APPLICABLE at the CUD/read slot).
     */
    private suspend fun setupUserForLayer(
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = when (layer) {
        PermissionMatrix.Layer.SUPER -> setupSuperUser(matrix, operation)
        PermissionMatrix.Layer.SYSTEM -> setupSystemUser(matrix, operation)
        PermissionMatrix.Layer.TENANT_ADMIN -> setupTenantAdminUser(
            matrix = matrix,
            operation = operation,
            tenantId = 0L,
            tenantMemberId = 0L,
        )
        PermissionMatrix.Layer.TENANT_PEM -> setupTenantPemUser(
            matrix = matrix,
            operation = operation,
            tenantId = 0L,
            tenantMemberId = 0L,
        )
    }

    /**
     * Only the `super` layer holds `oauth.account.read` in this controller's matrix; the other
     * three layers carry [PermissionMatrix.NOT_APPLICABLE] which the base class translates to an
     * empty authority set. Reading with an empty authority set must be denied.
     */
    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readEndpointAuthorizesOnlySuperLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("oauth-account-read-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)

            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerOAuthAccountController.read(user.authentication, readDto())
                }.exceptionOrNull()
            }

            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertNull(
                    caught,
                    "SUPER layer holds oauth.account.read and must be allowed, got: $caught",
                )
                else -> {
                    assertNotNull(caught, "Layer $layer must be denied (NOT_APPLICABLE slot)")
                    assertTrue(
                        caught is AuthorizationDeniedException,
                        "Expected AuthorizationDeniedException, got ${caught!!::class}",
                    )
                }
            }
        }
    }

    /**
     * Same coverage as [readEndpointAuthorizesOnlySuperLayer] but for the sibling `readAll`
     * (`GET /list`) endpoint declared on [com.lovelycatv.crystalframework.shared.controller.StandardManagerController].
     * That method invokes `authorize` inline (no `preflight` / `mutability` chain), so this test
     * catches any drift between the two authorisation paths.
     */
    @ParameterizedTest
    @EnumSource(PermissionMatrix.Layer::class)
    fun readAllEndpointAuthorizesOnlySuperLayer(layer: PermissionMatrix.Layer) {
        withTransactionalRollback("oauth-account-readAll-layer-$layer") {
            val user = setupUserForLayer(layer, ScopedOperation.READ)

            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerOAuthAccountController.readAll(user.authentication)
                }.exceptionOrNull()
            }

            when (layer) {
                PermissionMatrix.Layer.SUPER -> assertNull(
                    caught,
                    "SUPER layer holds oauth.account.read and must be allowed, got: $caught",
                )
                else -> {
                    assertNotNull(caught, "Layer $layer must be denied (NOT_APPLICABLE slot)")
                    assertTrue(
                        caught is AuthorizationDeniedException,
                        "Expected AuthorizationDeniedException, got ${caught!!::class}",
                    )
                }
            }
        }
    }

    /**
     * A logged-in user with no matrix-granted authorities must be denied at the `authorize` hook.
     * This is the deny-by-default assertion: the AOP aspect detects the non-null `permissions`
     * field and delegates to `authorize`, which OR-checks the matrix layers and refuses when the
     * user's authority set does not intersect any of them.
     */
    @Test
    fun readEndpointDeniesUserWithoutMatrixAuthorities() {
        withTransactionalRollback("oauth-account-read-denied-no-auth") {
            val user = setupUnauthorizedUser()

            var caught: Throwable? = null
            withAuthenticatedUser(user) {
                caught = runCatching {
                    managerOAuthAccountController.read(user.authentication, readDto())
                }.exceptionOrNull()
            }

            assertNotNull(caught, "Unauthorised user must be denied")
            assertTrue(
                caught is AuthorizationDeniedException,
                "Expected AuthorizationDeniedException, got ${caught!!::class}",
            )
        }
    }
}
