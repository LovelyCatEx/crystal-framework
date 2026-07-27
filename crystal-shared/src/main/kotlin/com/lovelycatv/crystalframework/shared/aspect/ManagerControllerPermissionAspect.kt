package com.lovelycatv.crystalframework.shared.aspect

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController
import com.lovelycatv.crystalframework.shared.controller.StandardTenantManagerController
import com.lovelycatv.vertex.log.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.annotation.Order
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_PERMISSION_CHECK)
class ManagerControllerPermissionAspect {
    private val logger = logger()

    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.AbstractManagerController+.*(..))")
    fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val controller = joinPoint.target

        // AopUtils.getTargetClass + AnnotationUtils.findAnnotation walk through CGLIB
        // proxies and the inheritance chain, which Class.getAnnotation cannot do.
        val targetClass = AopUtils.getTargetClass(controller)

        // Scoped/Tenant main lines run authorisation in-line via their `authorize` hook.
        // Skipping the annotation-driven aspect here prevents double checking and keeps
        // subclasses that legitimately have no @ManagerPermissions from being rejected.
        if (StandardScopedManagerController::class.java.isAssignableFrom(targetClass) ||
            StandardTenantManagerController::class.java.isAssignableFrom(targetClass)
        ) {
            return joinPoint.proceed()
        }

        val permissions = AnnotationUtils.findAnnotation(targetClass, ManagerPermissions::class.java)
            ?: return joinPoint.proceed()

        val methodSignature = joinPoint.signature as MethodSignature
        val methodName = methodSignature.method.name

        val requiredPermissions = when (ManagerAction.fromMethodName(methodName)) {
            ManagerAction.READ_ALL -> permissions.readAll
            ManagerAction.READ -> permissions.read
            ManagerAction.CREATE -> permissions.create
            ManagerAction.UPDATE -> permissions.update
            ManagerAction.DELETE -> permissions.delete
            null -> null
        }
            ?.filter { it.isNotEmpty() }
            ?.toList()

        if (requiredPermissions.isNullOrEmpty()) {
            logger.warn("No valid permission required for $methodSignature, skipped.")
            return joinPoint.proceed()
        }

        return ReactiveSecurityContextHolder
            .getContext()
            .mapNotNull { it.authentication }
            .flatMap { authentication ->
                if (!hasAnyPermission(authentication, requiredPermissions)) {
                    throw AuthorizationDeniedException(
                        "Access denied: Required any of permissions $requiredPermissions for this action"
                    )
                }

                @Suppress("UNCHECKED_CAST")
                joinPoint.proceed() as Mono<Any>
            }
    }

    private fun hasAnyPermission(
        authentication: Authentication,
        requiredPermissions: List<String>
    ): Boolean {
        val granted = authentication.authorities.mapNotNullTo(HashSet()) { it.authority }
        return requiredPermissions.any { it in granted }
    }
}
