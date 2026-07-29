package com.lovelycatv.crystalframework.shared.filter

import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private const val HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options"
private const val HEADER_X_FRAME_OPTIONS = "X-Frame-Options"
private const val HEADER_REFERRER_POLICY = "Referrer-Policy"
private const val HEADER_STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security"
private const val HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy"
private const val VALUE_NOSNIFF = "nosniff"
private const val VALUE_DENY = "DENY"
private const val VALUE_REFERRER_POLICY = "strict-origin-when-cross-origin"
private const val VALUE_HSTS = "max-age=31536000; includeSubDomains"
private const val VALUE_CSP = "default-src 'self'; frame-ancestors 'none'; base-uri 'self'"

@Component
@Order(GlobalConstants.FilterPriority.SECURITY_HEADERS)
class SecurityHeadersWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val headers = exchange.response.headers
        headers.setIfAbsent(HEADER_X_CONTENT_TYPE_OPTIONS, VALUE_NOSNIFF)
        headers.setIfAbsent(HEADER_X_FRAME_OPTIONS, VALUE_DENY)
        headers.setIfAbsent(HEADER_REFERRER_POLICY, VALUE_REFERRER_POLICY)
        headers.setIfAbsent(HEADER_STRICT_TRANSPORT_SECURITY, VALUE_HSTS)
        headers.setIfAbsent(HEADER_CONTENT_SECURITY_POLICY, VALUE_CSP)
        return chain.filter(exchange)
    }

    private fun HttpHeaders.setIfAbsent(name: String, value: String) {
        if (!this.containsHeader(name)) {
            this.set(name, value)
        }
    }
}
