package com.lovelycatv.crystalframework.shared.aspect

import com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.ForbiddenContext
import com.lovelycatv.crystalframework.shared.exception.ForbiddenException
import com.lovelycatv.crystalframework.shared.exception.ForbiddenReason
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.support.AopUtils
import org.springframework.core.annotation.Order
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Enforces [RequiresAuthority] on annotated methods (method-level annotation wins over class-level).
 * The controller methods it intercepts are WebFlux `suspend` functions bridged to `Mono<Any>`, so
 * the aspect composes the authority check reactively — no blocking, no `runBlocking`.
 *
 * Denial path funnels through [ForbiddenException] with a fully populated [ForbiddenContext] so
 * the frontend renders the required permissions and scope in the ForbiddenModal.
 */
@Aspect
@Component
@Order(GlobalConstants.AspectPriority.REQUIRES_AUTHORITY_CHECK)
class RequiresAuthorityAspect {

    @Around(
        "@annotation(com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority) || " +
            "@within(com.lovelycatv.crystalframework.shared.annotations.RequiresAuthority)"
    )
    fun enforce(joinPoint: ProceedingJoinPoint): Any? {
        val methodSignature = joinPoint.signature as MethodSignature
        val annotation = resolveAnnotation(joinPoint, methodSignature) ?: return joinPoint.proceed()

        val gate = if (annotation.anyOf.isNotEmpty()) Gate.ANY else Gate.ALL
        val required = gate.pickFrom(annotation)
        // Defensive: BeanFactoryPostProcessor rejects annotations with both arrays empty at
        // startup, so this branch should be unreachable in production.
        if (required.isEmpty()) {
            return joinPoint.proceed()
        }

        // Aspect intercepts controller methods that return Mono<Any> (WebFlux coroutine bridge).
        // Compose reactively: verify authorities, then delegate to the underlying invocation only
        // if the check passes.
        return ReactiveSecurityContextHolder.getContext()
            .flatMapIterable { it.authentication?.authorities ?: emptyList() }
            .mapNotNull { it.authority }
            .collectList()
            .flatMap { held ->
                val heldSet = held.filterNotNull().toSet()
                val granted = when (gate) {
                    Gate.ANY -> required.any { it in heldSet }
                    Gate.ALL -> required.all { it in heldSet }
                }
                if (granted) {
                    @Suppress("UNCHECKED_CAST")
                    joinPoint.proceed() as Mono<Any>
                } else {
                    Mono.error(
                        ForbiddenException(
                            "Access denied: required ${gate.label} of $required",
                            context = ForbiddenContext(
                                reason = ForbiddenReason.MISSING_PERMISSION,
                                requiredPermissions = required,
                                scope = annotation.scope,
                            ),
                        )
                    )
                }
            }
    }

    /**
     * Resolve the effective [RequiresAuthority]: the method-level annotation wins over the
     * class-level fallback so a class-wide default can be overridden per endpoint. Walks through
     * CGLIB proxies via [AopUtils.getTargetClass] before reading the class-level annotation.
     */
    private fun resolveAnnotation(
        joinPoint: ProceedingJoinPoint,
        methodSignature: MethodSignature,
    ): RequiresAuthority? {
        return methodSignature.method.getAnnotation(RequiresAuthority::class.java)
            ?: AopUtils.getTargetClass(joinPoint.target).getAnnotation(RequiresAuthority::class.java)
    }

    private enum class Gate(val label: String) {
        ANY("any"),
        ALL("all"),
        ;

        fun pickFrom(annotation: RequiresAuthority): List<String> = when (this) {
            ANY -> annotation.anyOf.toList()
            ALL -> annotation.allOf.toList()
        }
    }
}
