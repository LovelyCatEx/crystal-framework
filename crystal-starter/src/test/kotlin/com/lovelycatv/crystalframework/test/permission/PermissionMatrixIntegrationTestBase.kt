package com.lovelycatv.crystalframework.test.permission

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ScopedOperation
import com.lovelycatv.crystalframework.user.entity.UserEntity
import com.lovelycatv.crystalframework.user.service.UserServiceTest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder

/**
 * Base class for integration tests that exercise the four-layer
 * [PermissionMatrix] (`super` / `system` / `tenantAdmin` / `tenantPem`).
 *
 * Two orthogonal concerns are addressed:
 *
 *  - **User fixtures** — [setupSuperUser], [setupSystemUser], [setupTenantAdminUser],
 *    [setupTenantPemUser] each create a real [UserEntity] (via
 *    [UserServiceTest.mockRegisteredUser]) so that any downstream service needing a real user id
 *    finds one, and record which authorities the layer should grant for the requested
 *    [ScopedOperation]. Only authorities corresponding to the target layer/operation are granted
 *    — the other layers are left empty, exactly matching the real production flow where a role
 *    only holds permissions matching its declared layer.
 *
 *  - **Security context** — [withAuthenticatedUser] runs `action` in a coroutine whose upstream
 *    `Mono` has the reactor security context populated. `ReactiveSecurityContextHolder.getContext()`
 *    (called from [com.lovelycatv.crystalframework.shared.utils.RbacUtils]) sees the granted
 *    authorities during the call and disappears after `action` returns. This mirrors how
 *    `CustomAuthFilter` installs the token in production, without needing to boot a real HTTP
 *    filter chain or JWT.
 *
 * The RBAC database rows (`user_permissions`, `user_roles`, `user_role_permission_relations`) are
 * not touched: the AOP aspect / `authorize` hook reads authorities from the reactor context, so
 * bypassing the Redis-cached `getUserAuthorities` call gives faster, deterministic tests without
 * cache-invalidation coupling. Tests that specifically verify the persistence path (e.g. RBAC
 * bootstrap seeding) should extend [CrystalFrameworkApplicationTests] directly.
 */
abstract class PermissionMatrixIntegrationTestBase(
    @Autowired protected val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    protected val userServiceTest: UserServiceTest by lazy { getTestClassInstance(applicationContext) }

    /**
     * Create a user fixture for the `super` layer. Authorities granted =
     * `matrix.superFor(operation)` (skipped when the slot is [PermissionMatrix.NOT_APPLICABLE] or
     * [PermissionMatrix.NEVER_GRANTED] — those layer/operation combinations are not real
     * authorities and installing them into the reactor context would misrepresent reality).
     */
    protected suspend fun setupSuperUser(
        matrix: PermissionMatrix,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = setupLayerUser(
        matrix = matrix,
        layer = PermissionMatrix.Layer.SUPER,
        operation = operation,
        tenantId = null,
        tenantMemberId = null,
    )

    /**
     * Create a user fixture for the `system` layer. No `tenantId` — SYSTEM-scoped principals do
     * not carry a tenant on their JWT.
     */
    protected suspend fun setupSystemUser(
        matrix: PermissionMatrix,
        operation: ScopedOperation,
    ): PermissionMatrixTestUser = setupLayerUser(
        matrix = matrix,
        layer = PermissionMatrix.Layer.SYSTEM,
        operation = operation,
        tenantId = null,
        tenantMemberId = null,
    )

    /**
     * Create a user fixture for the `tenantAdmin` layer. Holds a `tenant.*` authority that
     * bypasses tenant-id equality (cross-tenant admin). Caller supplies [tenantId] and
     * [tenantMemberId] so the returned [UserAuthentication] mirrors a real tenant-scoped JWT.
     */
    protected suspend fun setupTenantAdminUser(
        matrix: PermissionMatrix,
        operation: ScopedOperation,
        tenantId: Long,
        tenantMemberId: Long,
    ): PermissionMatrixTestUser = setupLayerUser(
        matrix = matrix,
        layer = PermissionMatrix.Layer.TENANT_ADMIN,
        operation = operation,
        tenantId = tenantId,
        tenantMemberId = tenantMemberId,
    )

    /**
     * Create a user fixture for the `tenantPem` layer. Holds an `i.tenant.*` authority that only
     * authorises actions inside the caller's own tenant. Requires [tenantId] and [tenantMemberId]
     * for the same reason as [setupTenantAdminUser].
     */
    protected suspend fun setupTenantPemUser(
        matrix: PermissionMatrix,
        operation: ScopedOperation,
        tenantId: Long,
        tenantMemberId: Long,
    ): PermissionMatrixTestUser = setupLayerUser(
        matrix = matrix,
        layer = PermissionMatrix.Layer.TENANT_PEM,
        operation = operation,
        tenantId = tenantId,
        tenantMemberId = tenantMemberId,
    )

    /**
     * Create a user fixture that holds no matrix-granted authorities (only the default
     * `ROLE_USER` from registration is present, and it does not intersect any `*.create`/`*.read`
     * permission on any manager resource). Used for deny-by-default assertions.
     */
    protected suspend fun setupUnauthorizedUser(
        tenantId: Long? = null,
        tenantMemberId: Long? = null,
    ): PermissionMatrixTestUser {
        val user = userServiceTest.mockRegisteredUser()
        return PermissionMatrixTestUser(
            user = user,
            authentication = UserAuthentication(
                userId = user.id,
                username = user.getUsername(),
                tenantId = tenantId,
                tenantMemberId = tenantMemberId,
            ),
            authorities = emptySet(),
            layer = PermissionMatrix.Layer.SUPER, // sentinel — no meaning for unauthorised users
        )
    }

    /**
     * Install the fixture's authorities into the reactor security context for the duration of
     * [action]. Uses a `mono { }` coroutine builder + `.contextWrite(...)` so that
     * [ReactiveSecurityContextHolder.getContext] (called by
     * [com.lovelycatv.crystalframework.shared.utils.RbacUtils]) resolves to the injected token
     * inside `action`, and the context is restored after `action` returns.
     *
     * The token's principal is the user's username (matching `CustomAuthFilter`, which uses
     * `claims.subject` — also the username — as the principal).
     */
    protected suspend fun withAuthenticatedUser(
        user: PermissionMatrixTestUser,
        action: suspend () -> Unit,
    ) {
        val token = UsernamePasswordAuthenticationToken(
            user.authentication.username,
            null,
            user.authorities,
        )
        mono { action() }
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(token))
            .awaitFirstOrNull()
    }

    private suspend fun setupLayerUser(
        matrix: PermissionMatrix,
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
        tenantId: Long?,
        tenantMemberId: Long?,
    ): PermissionMatrixTestUser {
        val user = userServiceTest.mockRegisteredUser()
        val authorities = authoritiesFor(matrix, layer, operation)
        return PermissionMatrixTestUser(
            user = user,
            authentication = UserAuthentication(
                userId = user.id,
                username = user.getUsername(),
                tenantId = tenantId,
                tenantMemberId = tenantMemberId,
            ),
            authorities = authorities,
            layer = layer,
        )
    }

    /**
     * Convert the layer/operation slot into a set of granted authorities suitable for the reactor
     * security context. Sentinel values ([PermissionMatrix.NOT_APPLICABLE] /
     * [PermissionMatrix.NEVER_GRANTED]) are dropped — a real role can never hold them, and
     * granting them in a test would misrepresent the production authority set.
     */
    private fun authoritiesFor(
        matrix: PermissionMatrix,
        layer: PermissionMatrix.Layer,
        operation: ScopedOperation,
    ): Set<GrantedAuthority> {
        val value = matrix.of(layer, operation)
        return if (value == PermissionMatrix.NOT_APPLICABLE || value == PermissionMatrix.NEVER_GRANTED) {
            emptySet()
        } else {
            setOf(SimpleGrantedAuthority(value))
        }
    }
}
