package com.lovelycatv.crystalframework.shared.constants

import org.springframework.core.Ordered

object GlobalConstants {
    const val APP_VERSION = "1.13.1"

    const val REQUEST_MAPPING_PREFIX = "/api/{version}"

    object FilterPriority {
        const val LOGGER_FILTER = Ordered.HIGHEST_PRECEDENCE

        // After AuthFilter
        const val SYSTEM_MAINTENANCE_GUARD = Ordered.LOWEST_PRECEDENCE - 1000

        // After SystemMaintenanceGuardFilter
        const val SYSTEM_MODULE_GUARD = SYSTEM_MAINTENANCE_GUARD + 1

        // After LoggerFilter, before AuditFilter
        const val SECURITY_HEADERS = LOGGER_FILTER + 500

        // After LoggerFilter
        const val AUDIT_FILTER = LOGGER_FILTER + 1000
    }

    object AspectPriority {
        const val MANAGER_CONTROLLER_AUDIT = 0

        // Runs before MANAGER_CONTROLLER_PERMISSION_CHECK so that @RequiresAuthority denials fire
        // before the Manager Controller safety-net aspect proceeds; both are ForbiddenException
        // producers and the earlier one wins.
        const val REQUIRES_AUTHORITY_CHECK = 900

        const val MANAGER_CONTROLLER_PERMISSION_CHECK = 1000

        const val MAIL_SEND_LOG_RECORDER = 100
    }

    object ExtModule {
        const val DEFAULT_MODULE_DIR = "./ext/"
    }
}