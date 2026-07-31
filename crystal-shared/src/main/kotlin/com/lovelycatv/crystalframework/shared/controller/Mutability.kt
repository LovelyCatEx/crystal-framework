package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope

/**
 * Declares whether a manager controller's write endpoints (create / update / delete) are allowed to
 * execute. Consumed by [AbstractManagerController] to short-circuit forbidden mutations before any
 * authorization or service call runs.
 *
 * `assertXxxAllowed()` throws [ForbiddenException] on [READ_ONLY]; the global exception handler
 * turns the throw into an `ApiResponse.forbidden` response body, matching the historical shape of
 * the hand-rolled `ReadonlyManagerController` overrides.
 */
enum class Mutability {
    READ_WRITE {
        override fun assertCreateAllowed(scope: ResourceScope) = Unit
        override fun assertUpdateAllowed(scope: ResourceScope) = Unit
        override fun assertDeleteAllowed(scope: ResourceScope) = Unit
    },
    READ_ONLY {
        override fun assertCreateAllowed(scope: ResourceScope): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be created",
                context = ForbiddenContext(reason = ForbiddenReason.PROTECTED_RESOURCE, scope = scope))

        override fun assertUpdateAllowed(scope: ResourceScope): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be updated",
                context = ForbiddenContext(reason = ForbiddenReason.PROTECTED_RESOURCE, scope = scope))

        override fun assertDeleteAllowed(scope: ResourceScope): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be deleted",
                context = ForbiddenContext(reason = ForbiddenReason.PROTECTED_RESOURCE, scope = scope))
    };

    abstract fun assertCreateAllowed(scope: ResourceScope)
    abstract fun assertUpdateAllowed(scope: ResourceScope)
    abstract fun assertDeleteAllowed(scope: ResourceScope)
}
