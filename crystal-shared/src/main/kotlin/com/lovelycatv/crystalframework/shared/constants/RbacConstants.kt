package com.lovelycatv.crystalframework.shared.constants

object RbacConstants {
    const val ROLE_PREFIX = "ROLE_"
    const val TENANT_ROLE_PREFIX = "I_ROLE_"

    /** Redis key prefix for the per-user force-logout timestamp (millis). */
    const val FORCE_LOGOUT_KEY_PREFIX = "forceLogout:"
}