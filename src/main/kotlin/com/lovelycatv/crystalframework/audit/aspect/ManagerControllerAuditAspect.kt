package com.lovelycatv.crystalframework.audit.aspect

import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.service.AuditLogService
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.StandardManagerController
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.Order
import org.springframework.data.relational.core.mapping.Table
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.reflect.ParameterizedType

@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_AUDIT)
class ManagerControllerAuditAspect(
    private val auditLogService: AuditLogService
) {
    private val logger = logger()
    private val auditScope = CoroutineScope(Dispatchers.IO)

    /**
     * Cache: controller class -> resource type (table name from @Table on ENTITY)
     */
    private val resourceTypeCache = mutableMapOf<Class<*>, String?>()

    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.StandardManagerController.*(..))")
    fun audit(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val methodName = methodSignature.method.name

        val action = resolveAction(methodName) ?: return joinPoint.proceed()

        val targetClass = AopUtils.getTargetClass(joinPoint.target)
        val resourceType = resolveResourceType(targetClass)

        if (resourceType == null) {
            logger.warn("Could not resolve resource type for ${targetClass.simpleName}, skipping audit.")
            return joinPoint.proceed()
        }

        val userAuthentication = joinPoint.args
            .filterIsInstance<UserAuthentication>()
            .firstOrNull()

        if (userAuthentication == null) {
            logger.warn("No UserAuthentication found in method args for $methodSignature, skipping audit.")
            return joinPoint.proceed()
        }

        val resourceIds = extractResourceIds(joinPoint.args, methodName)

        @Suppress("UNCHECKED_CAST")
        val result = joinPoint.proceed() as Mono<Any>

        return result
            .doOnEach { signal ->
                if (signal.isOnNext || signal.isOnComplete) {
                    val auditRequestInfo = AuditRequestContext.from(signal.contextView)
                    fireAudit(userAuthentication, auditRequestInfo, action, resourceType, resourceIds, true, null)
                } else if (signal.isOnError) {
                    val auditRequestInfo = AuditRequestContext.from(signal.contextView)
                    fireAudit(userAuthentication, auditRequestInfo, action, resourceType, resourceIds, false, signal.throwable?.message)
                }
            }
    }

    private fun fireAudit(
        userAuthentication: UserAuthentication,
        auditRequestInfo: AuditRequestInfo?,
        action: AuditAction,
        resourceType: String,
        resourceIds: List<Long>?,
        success: Boolean,
        errorMessage: String?
    ) {
        auditScope.launch {
            try {
                auditLogService.record(
                    userAuthentication = userAuthentication,
                    auditRequestInfo = auditRequestInfo,
                    action = action,
                    resourceType = resourceType,
                    resourceIds = resourceIds,
                    success = success,
                    errorMessage = errorMessage
                )
            } catch (e: Exception) {
                logger.error("Failed to record audit log: ${e.message}", e)
            }
        }
    }

    private fun resolveAction(methodName: String): AuditAction? {
        return when (methodName) {
            "create" -> AuditAction.CREATE
            "read", "readAll" -> AuditAction.READ
            "update" -> AuditAction.UPDATE
            "delete" -> AuditAction.DELETE
            else -> null
        }
    }

    private fun resolveResourceType(controllerClass: Class<*>): String? {
        return resourceTypeCache.getOrPut(controllerClass) {
            val entityClass = resolveEntityClass(controllerClass)
            entityClass?.getAnnotation(Table::class.java)?.value
        }
    }

    private fun resolveEntityClass(clazz: Class<*>): Class<*>? {
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            val genericSuper = current.genericSuperclass
            if (genericSuper is ParameterizedType) {
                val rawType = genericSuper.rawType
                if (rawType == StandardManagerController::class.java) {
                    // ENTITY is the 3rd type argument (index 2)
                    val entityType = genericSuper.actualTypeArguments[2]
                    return entityType as? Class<*>
                }
            }
            current = current.superclass
        }
        return null
    }

    private fun extractResourceIds(args: Array<Any>, methodName: String): List<Long>? {
        return when (methodName) {
            "read", "readAll" -> {
                val dto = args.filterIsInstance<BaseManagerReadDTO>().firstOrNull()
                dto?.id?.let { listOf(it) }
            }
            "update" -> {
                val dto = args.filterIsInstance<BaseManagerUpdateDTO>().firstOrNull()
                dto?.let { listOf(it.id) }
            }
            "delete" -> {
                val dto = args.filterIsInstance<BaseManagerDeleteDTO>().firstOrNull()
                dto?.ids
            }
            else -> null
        }
    }
}
