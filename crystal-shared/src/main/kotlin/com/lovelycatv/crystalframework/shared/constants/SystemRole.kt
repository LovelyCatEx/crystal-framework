package com.lovelycatv.crystalframework.shared.constants

object SystemRole {
    const val ROLE_ROOT = "root"
    const val ROLE_ADMIN = "admin"
    const val ROLE_USER = "user"

    /**
     * System roles that must never be assignable via runtime APIs.
     * Only the system context (no authentication, e.g. bootstrap) or an existing root user
     * is permitted to grant these roles.
     */
    val PROTECTED_ROLE_NAMES: Set<String> = setOf(ROLE_ROOT, ROLE_ADMIN)
}