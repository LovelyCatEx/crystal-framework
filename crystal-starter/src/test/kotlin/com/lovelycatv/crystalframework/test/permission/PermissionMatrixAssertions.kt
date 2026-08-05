package com.lovelycatv.crystalframework.test.permission

import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Shared assertions used by every WS E2 controller integration test to keep per-file boilerplate
 * minimal. All helpers operate on the `Throwable?` collected inside a
 * [PermissionMatrixIntegrationTestBase.withAuthenticatedUser] block — never on `runCatching`
 * results directly, so a `null` caught value always means the endpoint returned normally.
 *
 * The exception kinds each helper distinguishes correspond to the enforcement paths in the
 * manager-controller family:
 *
 *  - [ForbiddenException]           — thrown by every denial path, all mapped to HTTP 403:
 *    - [com.lovelycatv.crystalframework.shared.controller.StandardManagerController.authorize]
 *      when the RBAC OR-check across `matrix.layersFor(...)` fails. Standard main line.
 *    - [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY.assertXxxAllowed]
 *      (write attempted on a Readonly controller — reached before authorize).
 *    - [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController.assertAccess]
 *      when [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController.checkPermission]
 *      returns false (missing layer authority) OR when
 *      [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController.checkOwnership]
 *      returns false (authority held but tenant-id / scope-id mismatch).
 *    - [com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController]'s inline
 *      tenant-scope check on both missing-authority and cross-tenant-mismatch branches.
 *  - [UnauthorizedException]        — not thrown by any current manager-controller enforcement
 *    path; retained for defensive coverage in case a future refactor reintroduces 401 semantics
 *    for a corner case (e.g. token-expiry check inside a controller).
 *
 * Using distinct helpers keeps every controller test's failure message precise ("expected mutability
 * to reject the write" vs "expected the RBAC check to reject the read"), so a regression that flips
 * the enforcement path shows up in the assertion output rather than passing silently.
 */

/** Assert the endpoint completed normally (no exception). Used for allow-path assertions. */
fun assertLayerAllowed(caught: Throwable?, layer: PermissionMatrix.Layer, action: String) {
    assertNull(caught, "Layer $layer must be allowed for $action but threw: $caught")
}

/**
 * Assert the endpoint denied the caller via [ForbiddenException] raised by the RBAC OR-check inside
 * [com.lovelycatv.crystalframework.shared.controller.StandardManagerController.authorize] when
 * `matrix.layersFor(resourceScope, op)` matches none of the caller's authorities. Used by the
 * Standard main line and its Readonly variant.
 *
 * Named distinctly from [assertDeniedByForbidden] so the failure message pinpoints the RBAC-layer
 * origin ("expected the RBAC check to reject the read") even though both now assert the same
 * exception type — since the structured-403 unification, every manager-controller denial path is a
 * [ForbiddenException] so the frontend can render the ForbiddenModal.
 */
fun assertLayerDeniedByAuthorization(caught: Throwable?, layer: PermissionMatrix.Layer, action: String) {
    assertNotNull(caught, "Layer $layer must be denied for $action but the call succeeded")
    assertTrue(
        caught is ForbiddenException,
        "Layer $layer denied for $action must throw ForbiddenException, got ${caught::class}: $caught",
    )
}

/**
 * Assert the endpoint denied the caller via [ForbiddenException]. Two independent origins:
 *  - [com.lovelycatv.crystalframework.shared.controller.Mutability.READ_ONLY] on the write endpoints
 *    of a Readonly variant — reached before authorize/checkPermission.
 *  - [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController.assertAccess]
 *    (and [com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController] flows)
 *    when the RBAC layer authority is missing entirely.
 */
fun assertDeniedByForbidden(caught: Throwable?, context: String) {
    assertNotNull(caught, "$context must be denied but the call succeeded")
    assertTrue(
        caught is ForbiddenException,
        "$context must throw ForbiddenException, got ${caught::class}: $caught",
    )
}

/**
 * Assert the endpoint denied the caller via [UnauthorizedException]. Retained for defensive
 * coverage — currently no manager-controller path throws this (cross-tenant scope mismatch is
 * classified as 403 [ForbiddenException] per RFC 9110 §15.5.4, since the caller is authenticated
 * and the request is refused rather than lacking credentials).
 */
fun assertDeniedByUnauthorized(caught: Throwable?, context: String) {
    assertNotNull(caught, "$context must be denied but the call succeeded")
    assertTrue(
        caught is UnauthorizedException,
        "$context must throw UnauthorizedException, got ${caught::class}: $caught",
    )
}

/**
 * Assert the endpoint denied the caller either by [ForbiddenException] or [UnauthorizedException].
 * Since M1 (HTTP 401→403 status fix) the current codebase never throws Unauthorized from
 * manager-controller enforcement — the OR variant is retained so a future refactor that
 * reintroduces 401 for some corner case (e.g. token-expiry inside a controller) still passes.
 * Used when the test does not want to over-specify the enforcement path.
 */
fun assertDeniedByForbiddenOrUnauthorized(caught: Throwable?, context: String) {
    assertNotNull(caught, "$context must be denied but the call succeeded")
    assertTrue(
        caught is ForbiddenException || caught is UnauthorizedException,
        "$context must throw ForbiddenException or UnauthorizedException, got ${caught::class}: $caught",
    )
}
