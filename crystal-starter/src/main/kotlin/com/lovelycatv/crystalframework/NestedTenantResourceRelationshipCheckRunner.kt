package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.shared.service.BaseTenantResourceManagerService
import com.lovelycatv.crystalframework.shared.service.EntityRelationshipCheckService
import com.lovelycatv.crystalframework.shared.types.tenant.entity.BaseTenantEntity
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

/**
 * Fail-fast startup guard for the tenant-side ownership-check contract.
 *
 * [EntityRelationshipCheckService.checkIsRelatedToRootParent] has a default implementation that
 * treats the *immediate* parent as the root. That is correct only for entities whose direct
 * parent IS the tenant (i.e. entities extending [BaseTenantEntity], which own a `tenant_id`
 * column). Entities nested deeper (their `getDirectParentId()` returns a mid-chain id such as a
 * departmentId or a dict typeId) MUST override `checkIsRelatedToRootParent` to delegate up the
 * chain — otherwise the default compares the wrong id space and silently breaks ownership
 * validation (own-tenant operations rejected, or, in the worst case, cross-tenant mis-attribution).
 *
 * This runner reflects over every [BaseTenantResourceManagerService] bean, resolves its ENTITY
 * generic, and for any entity that does NOT extend [BaseTenantEntity] verifies the service (or an
 * intermediate super type other than the base default provider) actually overrides the check.
 * A missing override aborts startup.
 *
 * The scoped counterpart (`resolveRootScope`) needs no guard here: it is already compiler-enforced
 * — scoped services are bound to `BaseScopedEntity` (always carry their own `scope/scope_id`), and
 * the sole tenant/scope hybrid implements `ScopedRelationshipCheckService` manually, so the
 * compiler forces its `resolveRootScope`.
 */
@Order(0)
@Component
class NestedTenantResourceRelationshipCheckRunner(
    private val applicationContext: ApplicationContext
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        val beans = applicationContext.getBeansOfType(BaseTenantResourceManagerService::class.java)
        val violations = mutableListOf<String>()

        beans.forEach { (beanName, bean) ->
            val implClass = ClassUtils.getUserClass(bean)
            val entityType = ResolvableType
                .forClass(BaseTenantResourceManagerService::class.java, implClass)
                .getGeneric(ENTITY_GENERIC_INDEX)
                .resolve()

            if (entityType == null) {
                logger.warn(
                    "Could not resolve ENTITY generic of tenant resource service '$beanName' " +
                        "(${implClass.simpleName}); skipping relationship-override check for it."
                )
                return@forEach
            }

            // Direct tenant child (owns a `tenant_id` column) → the default single-level check is correct.
            if (BaseTenantEntity::class.java.isAssignableFrom(entityType)) {
                return@forEach
            }

            // Nested entity → the parent is NOT the tenant root, so the override is mandatory.
            if (!isRootParentCheckOverridden(implClass)) {
                violations += "$beanName (${implClass.simpleName} / entity ${entityType.simpleName})"
            }
        }

        if (violations.isNotEmpty()) {
            throw IllegalStateException(buildViolationMessage(violations))
        }

        logger.info(
            "Nested tenant resource relationship check passed: all deeply-nested tenant resource " +
                "services override '$ROOT_PARENT_CHECK_METHOD_NAME'."
        )
    }

    /**
     * True if the collection overload of `checkIsRelatedToRootParent` is declared anywhere in the
     * impl's type hierarchy other than the base default provider ([EntityRelationshipCheckService]),
     * i.e. the service genuinely overrides it. kotlin-reflect's [declaredMemberFunctions] excludes
     * synthetic bridge/continuation noise, so this is independent of the `-jvm-default` mode.
     */
    private fun isRootParentCheckOverridden(implClass: Class<*>): Boolean {
        val baseProvider = EntityRelationshipCheckService::class
        val candidates = implClass.kotlin.allSupertypes.mapNotNull { it.classifier as? KClass<*> } + implClass.kotlin
        return candidates
            .filter { it != baseProvider }
            .any { klass ->
                klass.declaredMemberFunctions.any { function ->
                    function.name == ROOT_PARENT_CHECK_METHOD_NAME &&
                        function.valueParameters.firstOrNull()?.type?.jvmErasure
                            ?.let { Collection::class.java.isAssignableFrom(it.java) } == true
                }
            }
    }

    private fun buildViolationMessage(violations: List<String>): String = buildString {
        appendLine("Startup aborted: nested tenant resource service(s) missing '$ROOT_PARENT_CHECK_METHOD_NAME' override.")
        appendLine(
            "Their entity does NOT extend ${BaseTenantEntity::class.simpleName} (direct parent is not the tenant), " +
                "so the default single-level check compares the wrong id and breaks tenant ownership validation."
        )
        appendLine(
            "Override '$ROOT_PARENT_CHECK_METHOD_NAME(ids, rootParentId)' to delegate to the parent service " +
                "(see TenantDepartmentMemberManagerServiceImpl / TenantDictItemManagerServiceImpl). Offenders:"
        )
        violations.forEach { appendLine("  - $it") }
    }

    companion object {
        private const val ENTITY_GENERIC_INDEX = 1
        private const val ROOT_PARENT_CHECK_METHOD_NAME = "checkIsRelatedToRootParent"
    }
}
