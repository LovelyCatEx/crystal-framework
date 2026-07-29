package com.lovelycatv.crystalframework.shared.controller

import com.lovelycatv.crystalframework.shared.exception.ForbiddenException

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
        override fun assertCreateAllowed() = Unit
        override fun assertUpdateAllowed() = Unit
        override fun assertDeleteAllowed() = Unit
    },
    READ_ONLY {
        override fun assertCreateAllowed(): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be created")

        override fun assertUpdateAllowed(): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be updated")

        override fun assertDeleteAllowed(): Nothing =
            throw ForbiddenException("This resource is read-only and cannot be deleted")
    };

    abstract fun assertCreateAllowed()
    abstract fun assertUpdateAllowed()
    abstract fun assertDeleteAllowed()
}
