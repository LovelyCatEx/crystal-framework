package com.lovelycatv.crystalframework.permission

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.rbac.tenant.constants.TenantPermission
import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.util.ClassUtils
import kotlin.test.assertTrue

// Enforces that every hard-coded @RequiresAuthority permission on every Spring bean method
// resolves to a real permission Declaration. Guards against typos and stale references.
//
// Successor to PreAuthorizeCoverageTest — after the C1 migration (2026-07-31), @PreAuthorize is
// banned by SpringSecurityAnnotationBanBeanFactoryPostProcessor at startup and all sites use
// @RequiresAuthority(anyOf = [...], allOf = [...], scope = ...) instead.
//
// Coverage source:
//  - SystemPermission.allPermissions()  → all system + super + tenantAdmin permission names
//  - TenantPermission.allPermissions()  → all tenantPem permission names
//  - Additional subordinate permission constant classes discovered at runtime
//    (e.g. MonitorPermission in crystal-monitor)
class RequiresAuthorityCoverageTest(
    @Autowired
    private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {

    @Test
    fun everyRequiresAuthorityLiteralResolvesToARegisteredPermission() {
        val knownNames = collectKnownPermissionNames()

        val orphans = collectRequiresAuthorityLiterals()
            .filter { (literal, _) -> literal !in knownNames }

        assertTrue(
            orphans.isEmpty(),
            "@RequiresAuthority permissions with no matching permission Declaration:\n" +
                orphans.joinToString("\n") { (literal, site) -> " - '$literal' at $site" } +
                "\n\nKnown permission names (${knownNames.size}):\n" +
                knownNames.sorted().joinToString("\n") { "   $it" }
        )
    }

    // Extract every `anyOf` and `allOf` string from every @RequiresAuthority annotation on every
    // Spring bean method. Class-level annotations are also scanned so future class-scope grants
    // are covered.
    private fun collectRequiresAuthorityLiterals(): List<Pair<String, String>> {
        return applicationContext.beanDefinitionNames
            .mapNotNull { name ->
                runCatching { applicationContext.getBean(name) }.getOrNull()
            }
            .flatMap { bean ->
                val targetClass = runCatching { ClassUtils.getUserClass(bean::class.java) }.getOrNull()
                    ?: return@flatMap emptyList<Pair<String, String>>()

                val sites = mutableListOf<Pair<String, String>>()

                // Class-level annotation
                targetClass.getAnnotation(RequiresAuthority::class.java)?.let { annotation ->
                    val siteLabel = targetClass.simpleName
                    annotation.anyOf.forEach { sites += it to siteLabel }
                    annotation.allOf.forEach { sites += it to siteLabel }
                }

                // Method-level annotations
                runCatching { targetClass.declaredMethods.toList() }.getOrDefault(emptyList())
                    .forEach { method ->
                        method.getAnnotation(RequiresAuthority::class.java)?.let { annotation ->
                            val siteLabel = "${targetClass.simpleName}#${method.name}"
                            annotation.anyOf.forEach { sites += it to siteLabel }
                            annotation.allOf.forEach { sites += it to siteLabel }
                        }
                    }

                sites
            }
            .distinct()
    }

    // Assemble the set of every valid permission name in the running application.
    private fun collectKnownPermissionNames(): Set<String> {
        val names = mutableSetOf<String>()
        names += SystemPermission.allPermissions().map { it.name }
        names += TenantPermission.allPermissions().map { it.name }
        names += collectSubordinatePermissionStrings()
        return names
    }

    private fun collectSubordinatePermissionStrings(): Set<String> {
        return SUBORDINATE_PERMISSION_CLASSES
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
        private val SUBORDINATE_PERMISSION_CLASSES = listOf(
            "com.lovelycatv.crystalframework.monitor.constants.MonitorPermission",
        )
    }
}
