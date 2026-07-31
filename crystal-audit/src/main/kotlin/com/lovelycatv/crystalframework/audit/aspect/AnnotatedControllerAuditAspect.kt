package com.lovelycatv.crystalframework.audit.aspect

import com.lovelycatv.crystalframework.audit.annotations.Audit
import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.service.AuditLogRecorder
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Aspect
@Component
@Order(GlobalConstants.AspectPriority.MANAGER_CONTROLLER_AUDIT)
class AnnotatedControllerAuditAspect(
    private val auditLogRecorder: AuditLogRecorder,
) {
    private val expressionParser = SpelExpressionParser()

    @Around("@annotation(com.lovelycatv.crystalframework.audit.annotations.Audit)")
    fun audit(joinPoint: ProceedingJoinPoint): Any? {
        val authentication = joinPoint.args.filterIsInstance<UserAuthentication>().firstOrNull()
            ?: return joinPoint.proceed()
        val annotation = (joinPoint.signature as MethodSignature).method.getAnnotation(Audit::class.java)
        val resourceIds = resolveResourceIds(annotation.resourceIds, joinPoint)

        val result = try {
            joinPoint.proceed()
        } catch (error: Throwable) {
            auditLogRecorder.record(
                authentication,
                null,
                annotation.action,
                annotation.resourceType,
                resourceIds,
                false,
                error.message,
            )
            throw error
        }

        if (result !is Mono<*>) {
            auditLogRecorder.record(
                authentication,
                null,
                annotation.action,
                annotation.resourceType,
                resourceIds,
                true,
                null,
            )
            return result
        }

        return Mono.deferContextual { context ->
            val requestInfo = AuditRequestContext.from(context)
            result
                .doOnSuccess {
                    auditLogRecorder.record(
                        authentication,
                        requestInfo,
                        annotation.action,
                        annotation.resourceType,
                        resourceIds,
                        true,
                        null,
                    )
                }
                .doOnError { error ->
                    auditLogRecorder.record(
                        authentication,
                        requestInfo,
                        annotation.action,
                        annotation.resourceType,
                        resourceIds,
                        false,
                        error.message,
                    )
                }
        }
    }

    private fun resolveResourceIds(expression: String, joinPoint: ProceedingJoinPoint): List<Long>? {
        if (expression.isBlank()) return null
        val signature = joinPoint.signature as MethodSignature
        val context = StandardEvaluationContext()
        signature.parameterNames.zip(joinPoint.args).forEach { (name, value) ->
            context.setVariable(name, value)
        }
        val value = expressionParser.parseExpression(expression).getValue(context)
        return when (value) {
            is Long -> listOf(value)
            is Collection<*> -> {
                if (value.any { it !is Long }) {
                    error("Audit resourceIds expression must resolve to Long or Collection<Long>")
                }
                value.filterIsInstance<Long>()
            }
            null -> null
            else -> error("Audit resourceIds expression must resolve to Long or Collection<Long>")
        }
    }
}
