/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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