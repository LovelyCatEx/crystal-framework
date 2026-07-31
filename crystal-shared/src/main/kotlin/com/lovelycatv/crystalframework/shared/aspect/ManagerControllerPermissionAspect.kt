package com.lovelycatv.crystalframework.shared.aspect

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Safety-net aspect for `AbstractManagerController` subclasses. Two-way decision:
 *
 *  1. Scoped/Tenant main lines run authorisation in-line via [AbstractManagerController.authorize];
 *     skip the aspect entirely to avoid double-checking.
 *  2. If the subclass has a non-null `permissions: PermissionMatrix` field, authorisation runs in
 *     the subclass's `override authorize` — skip the aspect.
 *  3. Otherwise deny by default — an unconfigured controller must not serve requests undetected.
 */
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {

    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.AbstractManagerController+.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val controller = joinPoint.target

        // AopUtils.getTargetClass walks through CGLIB proxies and the inheritance chain,
        // which Class.getClass cannot do.
        val targetClass = AopUtils.getTargetClass(controller)

        // Scoped/Tenant main lines run authorisation in-line via their `authorize` hook.
        // Skipping the aspect here prevents double checking.
        if (StandardScopedManagerController::class.java.isAssignableFrom(targetClass) ||
            StandardTenantManagerController::class.java.isAssignableFrom(targetClass)
        ) {
            return joinPoint.proceed()
        }

        // Standard main line: the subclass injected a `permissions: PermissionMatrix?` field.
        // When non-null, authorisation runs in the subclass's `override authorize`; skip the aspect.
        if (hasNonNullPermissionsField(controller, targetClass)) {
            return joinPoint.proceed()
        }

        // Deny by default — no PermissionMatrix means the subclass has no configured authorisation.
        // Emit as a structured ForbiddenException so the frontend renders the ForbiddenModal; the
        // message still points ops at the underlying misconfiguration.
        throw ForbiddenException(
            "No authorization configured for ${targetClass.simpleName}. " +
                "Inject permissions: PermissionMatrix into the constructor.",
            context = ForbiddenContext(
                reason = ForbiddenReason.MISSING_PERMISSION,
                requiredPermissions = emptyList(),
                scope = readResourceScope(controller, targetClass),
            ),
        )
    }

    /**
     * Reflectively read the controller's `resourceScope: ResourceScope` (declared open on
     * [com.lovelycatv.crystalframework.shared.controller.AbstractManagerController]). Falls back to
     * [ResourceScope.SYSTEM] if the field is missing or unreadable, which is safe: this aspect only
     * fires on the misconfiguration path and callers still see a 403.
     */
    private fun readResourceScope(controller: Any, targetClass: Class<*>): ResourceScope {
        var cls: Class<*>? = targetClass
        while (cls != null && cls != Any::class.java) {
            val field = cls.declaredFields.firstOrNull {
                it.name == "resourceScope" && ResourceScope::class.java.isAssignableFrom(it.type)
            }
            if (field != null) {
                field.isAccessible = true
                return field.get(controller) as? ResourceScope ?: ResourceScope.SYSTEM
            }
            cls = cls.superclass
        }
        return ResourceScope.SYSTEM
    }

    /**
     * Reflectively check whether the subclass declares `permissions: PermissionMatrix?` and holds
     * a non-null value. Walks the entire inheritance chain so an intermediate base class also matches.
     */
    private fun hasNonNullPermissionsField(controller: Any, targetClass: Class<*>): Boolean {
        var cls: Class<*>? = targetClass
        while (cls != null && cls != Any::class.java) {
            val field = cls.declaredFields.firstOrNull {
                it.name == "permissions" && PermissionMatrix::class.java.isAssignableFrom(it.type)
            }
            if (field != null) {
                field.isAccessible = true
                return field.get(controller) != null
            }
            cls = cls.superclass
        }
        return false
    }
}
