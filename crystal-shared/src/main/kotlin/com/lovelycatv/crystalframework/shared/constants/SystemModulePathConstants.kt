package com.lovelycatv.crystalframework.shared.constants

/**
 * Single source of truth for toggleable system module identifiers and the backend URL patterns
 * they own. Consumed by:
 *   - SystemModuleGuardFilter (crystal-system) — blocks backend APIs when a module is disabled.
 *   - SystemIntegratedInfoController (crystal-system) — reports the disabled module keys back
 *     to the frontend so the UI can hide menus locally.
 *
 * Frontend menu path prefixes are intentionally NOT defined here: the frontend owns its own
 * route layout and keeps a mirror of these module keys in `web/src/router/system-module-menu-paths.ts`.
 */
object SystemModulePathConstants {
    object Tenant {
        const val KEY = "tenant"

        val apiPathPatterns = listOf(
            // Everything user-facing under /tenant (incl. me-profile, benefits, invitation, oauth, profile, settings, ...)
            "/api/*/tenant/**",
            // Admin-side tenant management (department / member / role / tire / dict / message-channel / ...)
            "/api/*/manager/tenant/**",
        )
    }

    object Approval {
        const val KEY = "approval"

        val apiPathPatterns = listOf(
            // Future user-facing approval APIs (e.g. /approval/submit, /approval/my-instances)
            "/api/*/approval/**",
            // Approval-flow-* routes under any prefix (current: manager only; future: tenant-side, etc.)
            "/api/*/approval-flow-**/**",
            "/api/*/manager/approval-flow-**/**",
        )
    }
}
