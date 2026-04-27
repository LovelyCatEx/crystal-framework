package com.lovelycatv.crystalframework.shared.aspect

import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitSingle
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(1)
class ManagerControllerPermissionAspect {
    private val logger = logger()

    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    suspend fun checkPermission(joinPoint: ProceedingJoinPoint): Any? {
        val controller = joinPoint.target

        val permissions = controller::class.java.getAnnotation(ManagerPermissions::class.java)
            ?: return joinPoint.proceed()

        val methodSignature = joinPoint.signature as MethodSignature
        val methodName = methodSignature.method.name

        val requiredPermissions = when (methodName) {
            "readAll" -> permissions.readAll.ifEmpty { permissions.read }
            "read" -> permissions.read
            "create" -> permissions.create
            "update" -> permissions.update
            "delete" -> permissions.delete
            else -> null
        }
            ?.filter { it.isNotEmpty() }
            ?.toList()

        if (requiredPermissions.isNullOrEmpty()) {
            logger.warn("No valid permission required for $methodSignature, skipped.")
            return joinPoint.proceed()
        }

        val authentication = ReactiveSecurityContextHolder
            .getContext()
            .mapNotNull { it.authentication }
            .awaitSingle()

        if (!hasAnyPermission(authentication, requiredPermissions)) {
            throw AccessDeniedException(
                "Access denied: Required any of permissions $requiredPermissions for method '$methodName'"
            )
        }

        return joinPoint.proceed()
    }

    private fun hasAnyPermission(
        authentication: Authentication,
        requiredPermissions: List<String>
    ): Boolean {
        val granted = authentication.authorities.mapNotNullTo(HashSet()) { it.authority }
        return requiredPermissions.any { it in granted }
    }
}
