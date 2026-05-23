package com.lovelycatv.crystalframework.shared.constants

import org.springframework.core.Ordered

object GlobalConstants {
    const val APP_VERSION = "1.2.3"

    const val REQUEST_MAPPING_PREFIX = "/api/{version}"

    object FilterPriority {
        const val LOGGER_FILTER = Ordered.HIGHEST_PRECEDENCE

        // After AuthFilter
        const val SYSTEM_MAINTENANCE_GUARD = Ordered.LOWEST_PRECEDENCE - 1000

        // After LoggerFilter
        const val AUDIT_FILTER = LOGGER_FILTER + 1000
    }

    object AspectPriority {
        const val MANAGER_CONTROLLER_AUDIT = 0

        const val MANAGER_CONTROLLER_PERMISSION_CHECK = 1000

        const val MAIL_SEND_LOG_RECORDER = 100
    }

    object ExtModule {
        const val DEFAULT_MODULE_DIR = "./ext/"
    }
}