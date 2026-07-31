package com.lovelycatv.crystalframework.audit.aspect

import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.service.AuditLogRecorder
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.controller.AbstractManagerController
import com.lovelycatv.crystalframework.shared.controller.ManagerAction
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.vertex.log.logger
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
    private val auditLogRecorder: AuditLogRecorder,
) {
    private val logger = logger()

    /**
     * Cache: controller class -> resource type (table name from @Table on ENTITY)
     */
    private val resourceTypeCache = mutableMapOf<Class<*>, String?>()

    @Around("execution(* com.lovelycatv.crystalframework.shared.controller.AbstractManagerController+.*(..))")
    fun audit(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val methodName = methodSignature.method.name

        val managerAction = ManagerAction.fromMethodName(methodName) ?: return joinPoint.proceed()
        val action = resolveAction(managerAction) ?: return joinPoint.proceed()

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

        val resourceIds = extractResourceIds(joinPoint.args, managerAction)

        @Suppress("UNCHECKED_CAST")
        val result = joinPoint.proceed() as Mono<Any>

        return Mono.deferContextual { context ->
            val auditRequestInfo = AuditRequestContext.from(context)
            result
                .doOnSuccess {
                    auditLogRecorder.record(userAuthentication, auditRequestInfo, action, resourceType, resourceIds, true, null)
                }
                .doOnError { error ->
                    auditLogRecorder.record(userAuthentication, auditRequestInfo, action, resourceType, resourceIds, false, error.message)
                }
        }
    }

    private fun resolveAction(managerAction: ManagerAction): AuditAction? = when (managerAction) {
        ManagerAction.CREATE -> AuditAction.CREATE
        ManagerAction.READ, ManagerAction.READ_ALL -> AuditAction.READ
        ManagerAction.UPDATE -> AuditAction.UPDATE
        ManagerAction.DELETE -> AuditAction.DELETE
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
                val rawType = genericSuper.rawType as? Class<*>
                if (rawType != null && AbstractManagerController::class.java.isAssignableFrom(rawType)) {
                    // ENTITY is the 3rd type argument (index 2) — the shape is preserved across
                    // AbstractManagerController and every concrete main line beneath it.
                    val entityType = genericSuper.actualTypeArguments[AbstractManagerController.PARAMETERIZED_ENTITY_INDEX]
                    return entityType as? Class<*>
                }
            }
            current = current.superclass
        }
        return null
    }

    private fun extractResourceIds(args: Array<Any>, action: ManagerAction): List<Long>? {
        return when (action) {
            ManagerAction.READ, ManagerAction.READ_ALL -> {
                val dto = args.filterIsInstance<BaseManagerReadDTO>().firstOrNull()
                dto?.id?.let { listOf(it) }
            }
            ManagerAction.UPDATE -> {
                val dto = args.filterIsInstance<BaseManagerUpdateDTO>().firstOrNull()
                dto?.let { listOf(it.id) }
            }
            ManagerAction.DELETE -> {
                val dto = args.filterIsInstance<BaseManagerDeleteDTO>().firstOrNull()
                dto?.ids
            }
            ManagerAction.CREATE -> null
        }
    }
}
