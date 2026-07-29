package com.lovelycatv.crystalframework.permission

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.ClassUtils
import kotlin.test.assertTrue

// Enforces that every hard-coded @PreAuthorize literal in the codebase resolves to a real
// permission Declaration. This guards against typos and stale references, since as of the
// permission naming redesign (2026-07-29) @PreAuthorize sites can no longer interpolate
// `${SystemPermission.XXX}` — the constants are Declarations, not compile-time String constants.
//
// Coverage source:
//  - SystemPermission.allPermissions()  → all system + super + tenantAdmin permission names
//  - TenantPermission.allPermissions()  → all tenantPem permission names
//  - Additional subordinate permission constant classes discovered at runtime
//    (e.g. MonitorPermission in crystal-monitor)
class PreAuthorizeCoverageTest(
    @Autowired
    private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    @Test
    fun everyPreAuthorizeLiteralResolvesToARegisteredPermission() {
        val knownNames = collectKnownPermissionNames()

        val orphans = collectPreAuthorizeLiterals()
            .filter { (literal, _) -> literal !in knownNames }

        assertTrue(
            orphans.isEmpty(),
            "@PreAuthorize literals with no matching permission Declaration:\n" +
                orphans.joinToString("\n") { (literal, site) -> " - '$literal' at $site" } +
                "\n\nKnown permission names (${knownNames.size}):\n" +
                knownNames.sorted().joinToString("\n") { "   $it" }
        )
    }

    // Extract all `hasAuthority(...)` / `hasAnyAuthority(...)` string literals from every
    // @PreAuthorize annotation on every Spring bean method.
    private fun collectPreAuthorizeLiterals(): List<Pair<String, String>> {
        val literalRegex = Regex("'([^']+)'")

        return applicationContext.beanDefinitionNames
            .mapNotNull { name ->
                runCatching { applicationContext.getBean(name) }.getOrNull()
            }
            .flatMap { bean ->
                val targetClass = runCatching { ClassUtils.getUserClass(bean::class.java) }.getOrNull()
                    ?: return@flatMap emptyList<Pair<String, String>>()
                runCatching {
                    targetClass.declaredMethods.toList()
                }.getOrDefault(emptyList()).mapNotNull { method ->
                    method.getAnnotation(PreAuthorize::class.java)?.let { annotation ->
                        annotation.value to "${targetClass.simpleName}#${method.name}"
                    }
                }
            }
            .flatMap { (expression, site) ->
                literalRegex.findAll(expression).map { it.groupValues[1] to site }.toList()
            }
            .distinct()
    }

    // Assemble the set of every valid permission name in the running application.
    // Sources: SystemPermission + TenantPermission Declarations; plus any subordinate
    // permission constant class (e.g. MonitorPermission in crystal-monitor) — those still use
    // `const val String` and are scanned via reflection.
    private fun collectKnownPermissionNames(): Set<String> {
        val names = mutableSetOf<String>()
        names += SystemPermission.allPermissions().map { it.name }
        names += TenantPermission.allPermissions().map { it.name }
        names += collectSubordinatePermissionStrings()
        return names
    }

    // Discover `object XxxPermission` classes whose fully-qualified name is not the two
    // top-level classes above and whose fields are `const val String`. Extract the string
    // values; for MENU-style `name:path` values, take the part before the colon.
    private fun collectSubordinatePermissionStrings(): Set<String> {
        val subordinateClassNames = SUBORDINATE_PERMISSION_CLASSES
        return subordinateClassNames
            .mapNotNull { fqcn -> runCatching { Class.forName(fqcn) }.getOrNull() }
            .flatMap { clazz ->
                val instance = clazz.getDeclaredField("INSTANCE").let {
                    it.isAccessible = true
                    it.get(null)
                }
                clazz.declaredFields.filter { it.type == String::class.java }.mapNotNull { field ->
                    field.isAccessible = true
                    val raw = field.get(instance) as? String ?: return@mapNotNull null
                    normalizePermissionName(raw)
                }
            }
            .toSet()
    }

    // Strip `:path` (menu) and `@componentId` (component) suffixes to recover the permission name.
    private fun normalizePermissionName(raw: String): String {
        val afterColon = raw.substringBefore(':')
        return afterColon.substringBefore('@')
    }

    companion object {
        // Add new subordinate permission classes here as modules register them.
        private val SUBORDINATE_PERMISSION_CLASSES = listOf(
            "com.lovelycatv.crystalframework.monitor.constants.MonitorPermission",
        )
    }
}
