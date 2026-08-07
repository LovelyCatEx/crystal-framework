package com.lovelycatv.crystalframework.shared.aspect

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Outcome
import co.elastic.apm.api.Span
import com.lovelycatv.crystalframework.shared.config.observability.ApmParentSpan
import com.lovelycatv.crystalframework.shared.config.observability.ApmSpanConstants
import com.lovelycatv.crystalframework.shared.config.observability.Untraced
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.CoroutinesUtils
import org.springframework.core.KotlinDetector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.startCoroutineUninterceptedOrReturn
import kotlinx.coroutines.reactor.awaitSingleOrNull

@Aspect
@Component
@ConditionalOnProperty(
    prefix = "crystalframework.observability.trace",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@Order(GlobalConstants.AspectPriority.APM_SPAN_TRACE)
class ApmSpanTraceAspect {

    companion object {
        private val log = LoggerFactory.getLogger(ApmSpanTraceAspect::class.java)
    }

    @Around(
        // @within matches the *declaring* type, so it covers methods written directly in an annotated
        // @Service / @Controller class. The standardized CRUD endpoints live in the non-annotated base
        // classes instead — controller methods in AbstractManagerController / StandardManagerController,
        // service methods as default methods on the CachedBaseService interface hierarchy — so @within
        // misses them. The two `within(<base>+)` clauses add those hierarchies back by declaring type.
        //
        // These are all *static* pointcuts: Spring decides proxy-eligibility from the bean class at
        // startup, so only beans in these hierarchies are proxied. (A dynamic @target(...) instead
        // forces Spring to proxy every bean for a runtime check, which fails on final Kotlin beans such
        // as @Bean-produced config classes that the kotlin-spring all-open plugin does not open.)
        "execution(public * com.lovelycatv.crystalframework..*(..)) && (" +
            "@within(org.springframework.stereotype.Service) " +
            "|| @within(org.springframework.web.bind.annotation.RestController) " +
            "|| @within(org.springframework.stereotype.Controller) " +
            "|| within(com.lovelycatv.crystalframework.shared.controller.AbstractManagerController+) " +
            "|| within(com.lovelycatv.crystalframework.shared.service.CachedBaseService+))",
    )
    fun trace(pjp: ProceedingJoinPoint): Any? {
        val signature = pjp.signature as MethodSignature
        val method = signature.method
        val targetClass = AopUtils.getTargetClass(pjp.target)
        val resolvedMethod = AopUtils.getMostSpecificMethod(method, targetClass)

        if (isSkipped(resolvedMethod, targetClass)) {
            return pjp.proceed()
        }

        val name = "${targetClass.simpleName}#${signature.name}"

        return when {
            KotlinDetector.isSuspendingFunction(resolvedMethod) -> traceSuspend(pjp, resolvedMethod, name)
            Mono::class.java.isAssignableFrom(resolvedMethod.returnType) -> {
                @Suppress("UNCHECKED_CAST")
                wrapMono(pjp.proceed() as Mono<Any>, name)
            }
            Flux::class.java.isAssignableFrom(resolvedMethod.returnType) -> {
                @Suppress("UNCHECKED_CAST")
                wrapFlux(pjp.proceed() as Flux<Any>, name)
            }
            else -> traceSync(pjp, name)
        }
    }

    private fun isSkipped(method: Method, targetClass: Class<*>): Boolean {
        if (AnnotatedElementUtils.hasAnnotation(method, Untraced::class.java)) return true
        if (AnnotatedElementUtils.hasAnnotation(targetClass, Untraced::class.java)) return true
        // Ignore Object methods (toString/hashCode/equals) that AspectJ may still see.
        return method.declaringClass == Any::class.java || method.declaringClass == Object::class.java
    }

    private fun traceSync(pjp: ProceedingJoinPoint, name: String): Any? {
        val parent: Span = ElasticApm.currentSpan()
        val span = parent.startSpan(
            ApmSpanConstants.SPAN_TYPE,
            ApmSpanConstants.SPAN_SUBTYPE,
            ApmSpanConstants.SPAN_ACTION,
        ).setName(name)
        val startNanos = System.nanoTime()
        return try {
            val result = pjp.proceed()
            span.setOutcome(Outcome.SUCCESS)
            result
        } catch (error: Throwable) {
            span.captureException(error)
            span.setOutcome(Outcome.FAILURE)
            throw error
        } finally {
            emitLog(name, startNanos)
            span.end()
        }
    }

    private fun <T : Any> wrapMono(mono: Mono<T>, name: String): Mono<T> =
        mono.transformDeferredContextual { source, ctx ->
            val parent = resolveParent(ctx)
            val span = parent.startSpan(
            ApmSpanConstants.SPAN_TYPE,
            ApmSpanConstants.SPAN_SUBTYPE,
            ApmSpanConstants.SPAN_ACTION,
        ).setName(name)
            val startNanos = System.nanoTime()
            source
                .contextWrite { it.put(ApmParentSpan::class.java, ApmParentSpan(span)) }
                .doOnError { error ->
                    span.captureException(error)
                    span.setOutcome(Outcome.FAILURE)
                }
                .doOnSuccess { span.setOutcome(Outcome.SUCCESS) }
                .doFinally {
                    emitLog(name, startNanos)
                    span.end()
                }
        }

    private fun <T : Any> wrapFlux(flux: Flux<T>, name: String): Flux<T> =
        flux.transformDeferredContextual { source, ctx ->
            val parent = resolveParent(ctx)
            val span = parent.startSpan(
            ApmSpanConstants.SPAN_TYPE,
            ApmSpanConstants.SPAN_SUBTYPE,
            ApmSpanConstants.SPAN_ACTION,
        ).setName(name)
            val startNanos = System.nanoTime()
            source
                .contextWrite { it.put(ApmParentSpan::class.java, ApmParentSpan(span)) }
                .doOnError { error ->
                    span.captureException(error)
                    span.setOutcome(Outcome.FAILURE)
                }
                .doOnComplete { span.setOutcome(Outcome.SUCCESS) }
                .doFinally {
                    emitLog(name, startNanos)
                    span.end()
                }
        }

    private fun resolveParent(ctx: reactor.util.context.ContextView): Span =
        if (ctx.hasKey(ApmParentSpan::class.java)) ctx.get<ApmParentSpan>(ApmParentSpan::class.java).span
        else ElasticApm.currentSpan()

    @Suppress("UNCHECKED_CAST")
    private fun traceSuspend(pjp: ProceedingJoinPoint, method: Method, name: String): Any? {
        val args = pjp.args
        val continuation = args.last() as Continuation<Any?>
        val realArgs = args.copyOfRange(0, args.size - 1)
        val target = pjp.target

        val mono: Mono<Any> = Mono.defer {
            Mono.from(
                CoroutinesUtils.invokeSuspendingFunction(method, target, *realArgs)
                    as org.reactivestreams.Publisher<Any>,
            )
        }
        
        return wrapMono(mono, name)
    }

    private fun emitLog(name: String, startNanos: Long) {
        if (!log.isDebugEnabled) return
        val durationMs = (System.nanoTime() - startNanos) / 1_000_000.0
        val traceId = runCatching { ElasticApm.currentSpan().traceId }.getOrDefault("")
        log.debug("[span] {} {}ms traceId={}", name, "%.2f".format(durationMs), traceId)
    }
}
