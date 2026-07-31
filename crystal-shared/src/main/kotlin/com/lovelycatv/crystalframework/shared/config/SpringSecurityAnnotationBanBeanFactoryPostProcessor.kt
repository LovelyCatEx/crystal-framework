package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import jakarta.annotation.security.RolesAllowed
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.security.access.annotation.Secured
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.access.prepost.PreFilter
import org.springframework.stereotype.Component

/**
 * Aborts application context refresh if any Spring bean class carries a native Spring Security
 * authorisation annotation (`@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, `@PostFilter`,
 * `@Secured`, `@RolesAllowed`). The project standardises on [RequiresAuthority]; the SpEL-carrying
 * variants provide no structured [com.lovelycatv.crystalframework.shared.exception.ForbiddenContext]
 * to the frontend, so we ban them wholesale rather than let them coexist.
 *
 * Detection strategy: iterate every bean definition, resolve its target class through the bean
 * factory (which handles factory-method beans that `beanClassName` alone would miss), and reflect
 * through both class-level and method-level annotations. All violations are collected before
 * aborting so operators see the full list in one build, not one at a time.
 */
@Component
class SpringSecurityAnnotationBanBeanFactoryPostProcessor : BeanFactoryPostProcessor {

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val bannedClasses = loadBannedAnnotationClasses()
        // Bail out silently if not even one of the banned annotations is on the classpath. That
        // shouldn't happen with Spring Security on the classpath, but guarding against a
        // partial dependency graph keeps the check defensive rather than brittle.
        if (bannedClasses.isEmpty()) {
            return
        }

        val violations = mutableListOf<String>()
        for (beanName in beanFactory.beanDefinitionNames) {
            val beanClass = resolveBeanClass(beanFactory, beanName) ?: continue
            collectViolations(beanClass, bannedClasses, violations)
        }

        if (violations.isEmpty()) {
            return
        }

        // BeanCreationException at post-processing time causes ApplicationContext.refresh() to
        // fail, so `java -jar app.jar` exits non-zero with the violation report.
        throw BeanCreationException(
            buildString {
                appendLine(
                    "Detected native Spring Security authorisation annotation(s) on ${violations.size} location(s). "
                        + "Use @RequiresAuthority (com.lovelycatv.crystalframework.shared.annotations) instead:"
                )
                violations.forEach { appendLine("  - $it") }
            }.trim()
        )
    }

    /**
     * Look up the effective bean type via the bean factory. Falls back to the raw
     * `beanClassName` on the definition when `getType` returns null (e.g. some
     * `RootBeanDefinition` cases). Both channels are best-effort — an unresolvable class name
     * skips silently so a broken bean definition does not mask real violations elsewhere.
     */
    private fun resolveBeanClass(
        beanFactory: ConfigurableListableBeanFactory,
        beanName: String,
    ): Class<*>? {
        beanFactory.getType(beanName, false)?.let { return it }
        val beanDefinition = runCatching { beanFactory.getBeanDefinition(beanName) }.getOrNull()
            ?: return null
        val className = beanDefinition.beanClassName ?: return null
        return runCatching {
            Class.forName(className, false, beanFactory.beanClassLoader ?: this.javaClass.classLoader)
        }.getOrNull()
    }

    /**
     * Record every banned annotation on the class (type-level) or any declared method
     * (method-level). Walks only `declaredMethods` — inherited methods are inspected on their
     * declaring class in its own iteration, so the whole graph is still covered without double
     * reporting.
     */
    private fun collectViolations(
        beanClass: Class<*>,
        bannedClasses: List<Class<out Annotation>>,
        violations: MutableList<String>,
    ) {
        bannedClasses.forEach { annotationClass ->
            if (beanClass.isAnnotationPresent(annotationClass)) {
                violations.add("${beanClass.name} (class-level @${annotationClass.simpleName})")
            }
        }
        val methods = runCatching { beanClass.declaredMethods.toList() }.getOrDefault(emptyList())
        for (method in methods) {
            bannedClasses.forEach { annotationClass ->
                if (method.isAnnotationPresent(annotationClass)) {
                    violations.add("${beanClass.name}#${method.name} (@${annotationClass.simpleName})")
                }
            }
        }
    }

    /**
     * Load banned annotation classes reflectively so this module does not have to declare a
     * compile-time dependency on `jakarta.annotation-api` / Spring Security's `Secured`
     * package layout. If any specific class is missing on the classpath it is simply skipped
     * (the codebase never used it either, so nothing to ban).
     */
    private fun loadBannedAnnotationClasses(): List<Class<out Annotation>> {
        return BANNED_ANNOTATION_CLASS_NAMES.mapNotNull { className ->
            runCatching {
                @Suppress("UNCHECKED_CAST")
                Class.forName(className) as Class<out Annotation>
            }.getOrNull()
        }
    }

    companion object {
        private val BANNED_ANNOTATION_CLASS_NAMES = listOf(
            PreAuthorize::class.qualifiedName!!,
            PostAuthorize::class.qualifiedName!!,
            PreFilter::class.qualifiedName!!,
            PostFilter::class.qualifiedName!!,
            Secured::class.qualifiedName!!,
            RolesAllowed::class.qualifiedName!!,
        )
    }
}
