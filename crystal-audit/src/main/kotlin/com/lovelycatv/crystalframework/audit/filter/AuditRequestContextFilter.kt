/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.audit.filter

import com.lovelycatv.crystalframework.audit.context.AuditRequestContext
import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SessionConstants
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Captures the per-request metadata once, at the outermost edge of the filter chain,
 * and installs it via [AuditRequestContext.install] so every downstream consumer —
 * suspend or not — can read it uniformly.
 *
 * Registered as a bean by
 * [com.lovelycatv.crystalframework.audit.config.AuditModuleConfig] with the highest
 * precedence, so the snapshot is visible to every other filter (including the
 * existing `LoggerFilter`), the security chain, the controller method, any AOP
 * aspects wrapped around it, and the services it calls.
 */
@Order(GlobalConstants.FilterPriority.AUDIT_FILTER)
@Component
class AuditRequestContextFilter(
    private val snowIdGenerator: SnowIdGenerator
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request

        val remoteIp = resolveRemoteIp(exchange)
        val userAgent = request.headers.getFirst("User-Agent")

        val info = AuditRequestInfo(
            requestId = snowIdGenerator.nextId(),
            httpMethod = request.method.name(),
            path = request.path.pathWithinApplication().value(),
            remoteIp = remoteIp,
            userAgent = userAgent
        )

        return exchange.session.flatMap { session ->
            session.attributes[SessionConstants.AUDIT_REMOTE_IP] = remoteIp ?: ""
            session.attributes[SessionConstants.AUDIT_USER_AGENT] = userAgent ?: ""

            chain
                .filter(exchange)
                .contextWrite(AuditRequestContext.install(exchange, info))
        }
    }

    /**
     * Prefer proxy-aware headers over the raw socket peer, so reverse-proxied
     * deployments record the real client address. Falls back to the socket.
     */
    private fun resolveRemoteIp(exchange: ServerWebExchange): String? {
        val headers = exchange.request.headers
        val forwarded = headers.getFirst("X-Forwarded-For")
            ?.split(',')
            ?.firstOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        if (forwarded != null) return forwarded

        val realIp = headers.getFirst("X-Real-IP")?.trim()?.takeIf { it.isNotEmpty() }
        if (realIp != null) return realIp

        return exchange.request.remoteAddress?.address?.hostAddress
    }
}
