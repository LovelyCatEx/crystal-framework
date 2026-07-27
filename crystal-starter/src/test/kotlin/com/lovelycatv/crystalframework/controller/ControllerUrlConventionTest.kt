package com.lovelycatv.crystalframework.controller

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping
import kotlin.test.assertTrue

// Enforces the URL naming convention documented in
// docs/develop/controller/url-naming.md.
//
// Scope: only URL patterns that contain "/manager/" are checked. The
// resource segment (directly after "/manager/") must be singular, and
// every segment must be kebab-case (lowercase, digits, dashes, and
// {...} template variables).
class ControllerUrlConventionTest(
    @Autowired
    private val handlerMappings: List<RequestMappingInfoHandlerMapping>,
) : CrystalFrameworkApplicationTests() {

    @Test
    fun managerUrlsMustFollowNamingConvention() {
        val allPatterns = handlerMappings
            .flatMap { it.handlerMethods.keys }
            .flatMap { info ->
                info.patternsCondition?.patterns?.map { it.patternString }.orEmpty()
            }
            .distinct()

        val managerPatterns = allPatterns.filter { MANAGER_URL_MARKER in it }
        assertTrue(managerPatterns.isNotEmpty(), "No manager URL patterns detected from Spring context")

        val casingViolations = managerPatterns.filter { !it.matches(KEBAB_CASE_PATH_REGEX) }
        val pluralViolations = managerPatterns.filter { pattern ->
            val resource = pattern
                .substringAfter(MANAGER_URL_MARKER)
                .substringBefore('/')
            resource.endsWith('s') && resource !in PLURAL_ALLOWLIST
        }

        val messages = buildList {
            if (casingViolations.isNotEmpty()) {
                add("URLs contain uppercase letters or underscores (must be kebab-case): $casingViolations")
            }
            if (pluralViolations.isNotEmpty()) {
                add("Manager resource segments must be singular: $pluralViolations")
            }
        }
        assertTrue(messages.isEmpty(), messages.joinToString("\n"))
    }

    companion object {
        private const val MANAGER_URL_MARKER = "/manager/"

        private val KEBAB_CASE_PATH_REGEX = Regex("^[a-z0-9/{}\\-.]+$")

        // Words that legitimately end with 's' — not plurals. Extend here when
        // a new singular resource happens to end in 's' (e.g. analysis).
        private val PLURAL_ALLOWLIST = setOf<String>()
    }
}
