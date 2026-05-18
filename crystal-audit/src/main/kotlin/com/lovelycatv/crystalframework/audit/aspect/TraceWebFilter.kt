package com.lovelycatv.crystalframework.audit.aspect

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context

@Component
@Order(1)
class TraceWebFilter : WebFilter {
    
    private val logger: Logger = LogManager.getLogger(TraceWebFilter::class.java)
    
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val traceId = generateTraceId()
        
        logger.info("Incoming request: {} {} [traceId={}]", 
            exchange.request.method, 
            exchange.request.path, 
            traceId)
        
        return chain.filter(exchange)
            .contextWrite { ctx ->
                ctx.put("trace-id", traceId)
                    .put("request-start-time", startTime)
            }
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.info("Request completed: {} {} [traceId={}, duration={}ms]",
                    exchange.request.method,
                    exchange.request.path,
                    traceId,
                    duration)
            }
            .doOnError { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.error("Request failed: {} {} [traceId={}, duration={}ms]",
                    exchange.request.method,
                    exchange.request.path,
                    traceId,
                    duration,
                    error)
            }
    }
    
    private fun generateTraceId(): String {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }
}