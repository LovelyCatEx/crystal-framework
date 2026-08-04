package com.lovelycatv.crystalframework.shared.utils

import org.springframework.http.server.reactive.ServerHttpRequest

/**
 * Resolves the originating client IP for rate-limiting / logging purposes.
 *
 * Honors the first hop of `X-Forwarded-For` and then `X-Real-IP` (set by trusted reverse proxies),
 * falling back to the socket remote address. Returns `"unknown"` when nothing can be resolved so
 * callers always have a stable, non-null key.
 */
fun ServerHttpRequest.resolveClientIp(): String {
    val forwardedFor = this.headers.getFirst("X-Forwarded-For")
    if (!forwardedFor.isNullOrBlank()) {
        val firstHop = forwardedFor.substringBefore(",").trim()
        if (firstHop.isNotEmpty()) {
            return firstHop
        }
    }

    val realIp = this.headers.getFirst("X-Real-IP")
    if (!realIp.isNullOrBlank()) {
        return realIp.trim()
    }

    return this.remoteAddress?.address?.hostAddress ?: "unknown"
}
