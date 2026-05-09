/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.crystalframework.audit.context

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.web.server.ServerWebExchange
import reactor.util.context.Context
import kotlin.coroutines.coroutineContext

/**
 * Single source of truth for the current request's snapshot across the whole
 * controller pipeline.
 *
 * The snapshot is stored in two places at the same time so every caller can read it
 * regardless of what it has in hand:
 *
 *  - The reactor `Context` — visible to any `suspend` code (controllers, aspects,
 *    services, repositories). Spring WebFlux bridges the reactor context into the
 *    coroutine scope of a suspending handler, so [current] works in every suspend
 *    function invoked as part of the request.
 *  - The [ServerWebExchange] attributes — visible to any non-suspend code that
 *    holds an exchange (other `WebFilter`s, argument resolvers, error handlers).
 *    Read via [of].
 *
 * The writer ([install]) populates both locations atomically; producers of the
 * snapshot should never talk to either storage directly.
 */
object AuditRequestContext {

    /** Stable storage key, reused across both storages for debuggability. */
    const val KEY: String = "crystalframework.audit.requestInfo"

    /**
     * Retrieve the snapshot from the active coroutine's reactor context. Returns
     * `null` outside a web request (scheduled jobs, startup initializers, background
     * coroutines detached from the request scope).
     */
    suspend fun current(): AuditRequestInfo? {
        val reactor = currentCoroutineContext()[ReactorContext] ?: return null
        val ctx = reactor.context
        return if (ctx.hasKey(KEY)) ctx.get(KEY) else null
    }

    /**
     * Retrieve the snapshot from an exchange — use this from non-suspend code paths.
     * Returns `null` when the capture filter has not run yet.
     */
    fun of(exchange: ServerWebExchange): AuditRequestInfo? {
        return exchange.attributes[KEY] as? AuditRequestInfo
    }

    /**
     * Attach [info] to [exchange] and produce a reactor `Context` transformer that
     * also writes the same value into the reactor context. Intended for the capture
     * filter only.
     *
     * ```kotlin
     * return chain.filter(exchange).contextWrite(AuditRequestContext.install(exchange, info))
     * ```
     */
    fun install(exchange: ServerWebExchange, info: AuditRequestInfo): (Context) -> Context {
        exchange.attributes[KEY] = info
        return { ctx -> ctx.put(KEY, info) }
    }
}
