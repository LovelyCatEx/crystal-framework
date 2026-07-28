package com.lovelycatv.crystalframework.shared.aspect

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.Order
import org.springframework.security.authorization.AuthorizationDeniedException
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
        throw AuthorizationDeniedException(
            "No authorization configured for ${targetClass.simpleName}. " +
                "Inject permissions: PermissionMatrix into the constructor."
        )
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
