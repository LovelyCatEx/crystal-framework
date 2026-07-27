package com.lovelycatv.crystalframework.shared.controller

/**
 * The five CRUD-style actions exposed by [AbstractManagerController] subclasses. Consumed by:
 *  - [AbstractManagerController.authorize] as the routing key for permission decisions
 *  - `ManagerControllerPermissionAspect` and `ManagerControllerAuditAspect` to map a reflected
 *    endpoint method name back to a well-typed action (avoiding magic strings)
 *
 * [READ] corresponds to `POST /query` (method name `read` on the historical Standard main line).
 */
enum class ManagerAction {
    READ_ALL,
    CREATE,
    READ,
    UPDATE,
    DELETE;

    companion object {
        /**
         * Map an endpoint method name to a [ManagerAction], or `null` if the method is not one of
         * the five recognised CRUD endpoints.
         */
        fun fromMethodName(methodName: String): ManagerAction? = when (methodName) {
            "readAll" -> READ_ALL
            "create" -> CREATE
            "read" -> READ
            "update" -> UPDATE
            "delete" -> DELETE
            else -> null
        }
    }
}
