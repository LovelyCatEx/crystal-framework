package com.lovelycatv.crystalframework.shared.constants

import org.springframework.core.Ordered

object GlobalConstants {
    const val APP_VERSION = "1.0.0"

    const val REQUEST_MAPPING_PREFIX = "/api/{version}"

    object FilterPriority {
        const val LOGGER_FILTER = Ordered.HIGHEST_PRECEDENCE

        // After AuthFilter
        const val SYSTEM_MAINTENANCE_GUARD = Ordered.LOWEST_PRECEDENCE - 1000

        const val AUDIT_FILTER = Ordered.HIGHEST_PRECEDENCE
    }
}