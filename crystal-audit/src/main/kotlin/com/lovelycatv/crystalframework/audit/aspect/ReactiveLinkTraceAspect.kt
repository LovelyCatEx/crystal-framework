package com.lovelycatv.crystalframework.audit.aspect

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.context.Context
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Aspect
@Component
class ReactiveLinkTraceAspect {
    
    private val logger: Logger = LogManager.getLogger(ReactiveLinkTraceAspect::class.java)
    
    @Value("\${monitor.base-package:com.lovelycatv.crystalframework}")
    private lateinit var basePackage: String
    
    @Value("\${monitor.enabled:true}")
    private var monitorEnabled: Boolean = true
    
    @Value("\${monitor.slow-threshold-ms:1}")
    private var slowThresholdMs: Long = 1000
    
    // Context Key 定义
    companion object {
        val TRACE_CONTEXT_KEY = TraceContextKey()
        
        // 使用全局 Map 存储完整的调用链（通过 traceId 关联）
        private val traceStore = ConcurrentHashMap<String, TraceNode>()
    }
    
    /**
     * 拦截所有 public 方法
     */
    @Around("execution(public * *(..)) && " +
            "(@within(org.springframework.stereotype.Controller) || " +
            "@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service))")
    fun monitorMethod(joinPoint: ProceedingJoinPoint): Any? {
        if (!monitorEnabled) {
            return joinPoint.proceed()
        }
        
        val signature = joinPoint.signature as MethodSignature
        val className = signature.declaringTypeName
        val methodName = signature.name
        
        // 只监控指定包名
        if (!className.startsWith(basePackage)) {
            return joinPoint.proceed()
        }
        
        val result = joinPoint.proceed()
        val fullMethodName = "$className.$methodName"
        
        // 处理响应式类型
        return when (result) {
            is Mono<*> -> {
                wrapMono(result as Mono<Any>, fullMethodName, joinPoint.args)
            }
            is Flux<*> -> {
                wrapFlux(result as Flux<Any>, fullMethodName, joinPoint.args)
            }
            else -> {
                // 同步方法（理论上 WebFlux 中不应该有）
                measureSyncMethod(fullMethodName, joinPoint.args, result)
            }
        }
    }
    
    /**
     * 包装 Mono 方法
     */
    private fun wrapMono(original: Mono<Any>, methodName: String, args: Array<Any?>): Mono<Any> {
        return original
            .transformDeferredContextual { mono, ctx ->
                val parentTrace = ctx.getOrDefault<TraceNode>(TRACE_CONTEXT_KEY, null)
                val currentTrace = createTraceNode(methodName, args, parentTrace)
                
                // 将当前节点存入 Context
                mono.contextWrite { context ->
                    context.put(TRACE_CONTEXT_KEY, currentTrace)
                }
                .doOnEach { signal ->
                    if (signal.isOnComplete || signal.hasError()) {
                        completeTraceNode(currentTrace, signal.hasError())
                        
                        // 如果是根节点，输出完整调用链
                        if (parentTrace == null) {
                            outputTraceResult(currentTrace)
                            // 清理存储
                            traceStore.remove(currentTrace.traceId)
                        } else {
                            // 更新父节点中的子节点引用
                            parentTrace.children.add(currentTrace)
                            traceStore[currentTrace.traceId] = currentTrace
                        }
                    }
                }
            }
    }
    
    /**
     * 包装 Flux 方法
     */
    private fun wrapFlux(original: Flux<Any>, methodName: String, args: Array<Any?>): Flux<Any> {
        return original
            .transformDeferredContextual { flux, ctx ->
                val parentTrace = ctx.getOrDefault<TraceNode>(TRACE_CONTEXT_KEY, null)
                val currentTrace = createTraceNode(methodName, args, parentTrace)
                
                flux.contextWrite { context ->
                    context.put(TRACE_CONTEXT_KEY, currentTrace)
                }
                .doOnEach { signal ->
                    if (signal.isOnComplete || signal.hasError()) {
                        completeTraceNode(currentTrace, signal.hasError())
                        
                        if (parentTrace == null) {
                            outputTraceResult(currentTrace)
                            traceStore.remove(currentTrace.traceId)
                        } else {
                            parentTrace.children.add(currentTrace)
                            traceStore[currentTrace.traceId] = currentTrace
                        }
                    }
                }
            }
    }
    
    /**
     * 创建追踪节点
     */
    private fun createTraceNode(methodName: String, args: Array<Any?>, parent: TraceNode?): TraceNode {
        val traceId = parent?.traceId ?: generateTraceId()
        val spanId = generateSpanId()
        
        return TraceNode(
            traceId = traceId,
            spanId = spanId,
            parentSpanId = parent?.spanId,
            methodName = methodName,
            parameters = simplifyParameters(args),
            startTime = System.nanoTime(),
            depth = parent?.depth?.plus(1) ?: 0
        )
    }
    
    /**
     * 完成追踪节点
     */
    private fun completeTraceNode(node: TraceNode, hasError: Boolean) {
        node.endTime = System.nanoTime()
        node.durationMs = (node.endTime - node.startTime) / 1_000_000.0
        node.hasError = hasError
    }
    
    /**
     * 测量同步方法
     */
    private fun measureSyncMethod(methodName: String, args: Array<Any?>, result: Any?): Any? {
        val startTime = System.nanoTime()
        var exception: Throwable? = null
        
        try {
            return result
        } catch (e: Throwable) {
            exception = e
            throw e
        } finally {
            val durationMs = (System.nanoTime() - startTime) / 1_000_000.0
            if (durationMs > slowThresholdMs) {
                logger.warn("Slow sync method [{}] cost {:.2f}ms, params: {}",
                    methodName, durationMs, simplifyParameters(args))
            }
            if (exception != null) {
                logger.error("Sync method [{}] failed", methodName, exception)
            }
        }
    }
    
    /**
     * 输出追踪结果
     */
    private fun outputTraceResult(rootNode: TraceNode) {
        if (rootNode.durationMs > slowThresholdMs) {
            logger.warn(formatSlowMethodLog(rootNode))
        } else {
            logger.debug(formatTraceLog(rootNode))
        }
    }
    
    /**
     * 格式化调用链日志
     */
    private fun formatTraceLog(node: TraceNode): String {
        val sb = StringBuilder()
        sb.append("\n========== REACTIVE CALL CHAIN ==========\n")
        sb.append("TraceId: ${node.traceId}\n")
        sb.append("Total Time: ${String.format("%.2f", node.durationMs)}ms\n")
        sb.append("\nCall Chain:\n")
        sb.append(formatTraceTree(node))
        sb.append("=========================================")
        return sb.toString()
    }
    
    /**
     * 格式化慢方法日志
     */
    private fun formatSlowMethodLog(rootNode: TraceNode): String {
        val sb = StringBuilder()
        sb.append("\n========== SLOW REACTIVE METHOD ==========\n")
        sb.append("TraceId: ${rootNode.traceId}\n")
        sb.append("Root Method: ${rootNode.methodName}\n")
        sb.append("Total Time: ${String.format("%.2f", rootNode.durationMs)}ms (Threshold: ${slowThresholdMs}ms)\n")
        sb.append("\nCall Chain:\n")
        sb.append(formatTraceTree(rootNode))
        
        // 找出所有慢的子节点
        val slowChildren = findSlowChildren(rootNode)
        if (slowChildren.isNotEmpty()) {
            sb.append("\nSlow Spans:\n")
            slowChildren.forEach { child ->
                sb.append("  - ${child.methodName}: ${String.format("%.2f", child.durationMs)}ms\n")
            }
        }
        sb.append("==========================================")
        return sb.toString()
    }
    
    /**
     * 格式化调用树
     */
    private fun formatTraceTree(node: TraceNode, prefix: String = "", isLast: Boolean = true): String {
        val sb = StringBuilder()
        val connector = if (isLast) "└── " else "├── "
        val childPrefix = if (isLast) "    " else "│   "
        
        sb.append(prefix)
        sb.append(connector)
        sb.append("${node.methodName}() ")
        sb.append("[${String.format("%.2f", node.durationMs)}ms]")
        
        if (node.hasError) {
            sb.append(" ✗ ERROR")
        }
        
        // 如果是慢方法且在阈值以上，显示参数
        if (node.durationMs > slowThresholdMs && node.parameters.isNotEmpty()) {
            sb.append(" params: ${node.parameters}")
        }
        sb.append("\n")
        
        val iterator = node.children.iterator()
        while (iterator.hasNext()) {
            val child = iterator.next()
            sb.append(formatTraceTree(child, prefix + childPrefix, !iterator.hasNext()))
        }
        
        return sb.toString()
    }
    
    /**
     * 找出所有慢的子节点
     */
    private fun findSlowChildren(node: TraceNode): List<TraceNode> {
        val slowNodes = mutableListOf<TraceNode>()
        if (node.durationMs > slowThresholdMs && node.parentSpanId != null) {
            slowNodes.add(node)
        }
        node.children.forEach { child ->
            slowNodes.addAll(findSlowChildren(child))
        }
        return slowNodes
    }
    
    /**
     * 简化参数
     */
    private fun simplifyParameters(args: Array<Any?>): String {
        if (args.isEmpty()) return ""
        
        return args.joinToString(", ") { arg ->
            when (arg) {
                null -> "null"
                is Mono<*> -> "Mono<?>"
                is Flux<*> -> "Flux<?>"
                else -> {
                    val str = arg.toString()
                    if (str.length > 50) str.substring(0, 50) + "..." else str
                }
            }
        }
    }
    
    private fun generateTraceId(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }
    
    private fun generateSpanId(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8)
    }
}

/**
 * Context Key 定义
 */
class TraceContextKey

/**
 * 扩展函数：从 Context 中获取值
 */
inline fun <reified T> Context.getOrNull(key: Any): T? {
    return if (hasKey(key)) {
        @Suppress("UNCHECKED_CAST")
        this.get(key) as T
    } else {
        null
    }
}

/**
 * 追踪节点数据类
 */
data class TraceNode(
    val traceId: String,
    val spanId: String,
    val parentSpanId: String?,
    val methodName: String,
    val parameters: String,
    val startTime: Long,
    val depth: Int = 0,
    var endTime: Long = 0,
    var durationMs: Double = 0.0,
    var hasError: Boolean = false,
    val children: MutableList<TraceNode> = mutableListOf()
)