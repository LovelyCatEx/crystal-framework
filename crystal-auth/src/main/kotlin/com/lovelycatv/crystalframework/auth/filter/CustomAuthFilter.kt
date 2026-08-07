package com.lovelycatv.crystalframework.auth.filter

import co.elastic.apm.api.ElasticApm
import co.elastic.apm.api.Outcome
import co.elastic.apm.api.Span
import com.lovelycatv.crystalframework.shared.config.observability.ApmSpanConstants
import com.lovelycatv.crystalframework.shared.constants.SessionConstants
import com.lovelycatv.crystalframework.shared.exception.UnauthorizedException
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.crystalframework.shared.utils.reactor.contextMerge
import com.lovelycatv.vertex.log.logger
import io.jsonwebtoken.ExpiredJwtException
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicBoolean

class CustomAuthFilter(
    val unauthorizedPathPatterns: List<PathPattern>,
    val getUserAuthorities: suspend (userId: Long, tenantId: Long?, tenantMemberId: Long?) -> Collection<GrantedAuthority>,
    val getJWTSignKey: () -> String,
    /**
     * Throws [UnauthorizedException] when the token (identified by [userId] and its [tokenIssuedAt]
     * epoch-millis) has been force-logged-out. Invoked before authorities are resolved so a revoked
     * token never reaches the RBAC layer.
     */
    val assertNotForceLoggedOut: suspend (userId: Long, tokenIssuedAt: Long?) -> Unit,
) : WebFilter {
    private val logger = logger()

    companion object {
        // Brackets the filter's own pre-`chain.filter` work (session resolve + force-logout check +
        // authority lookup), which the auto-trace aspect cannot see because a WebFilter is not a
        // @Service / @Controller bean.
        private const val AUTH_SPAN_NAME = "CustomAuthFilter#authenticate"

        // Child spans splitting the authenticate work: the force-logout revocation check vs. the RBAC
        // authority lookup, so their individual timings are visible instead of only the filter total.
        private const val FORCE_LOGOUT_SPAN_NAME = "CustomAuthFilter#assertNotForceLoggedOut"
        private const val AUTHORITIES_SPAN_NAME = "CustomAuthFilter#getUserAuthorities"
    }

    /**
     * Runs [block] bracketed by a child span of [parent] named [name], so async/coroutine execution
     * still nests correctly under the parent instead of relying on the thread-local current span.
     * Shared by both authenticate sub-steps to avoid duplicating the span open / capture / end boilerplate.
     */
    private inline fun <T> withChildSpan(parent: Span, name: String, block: () -> T): T {
        val span = parent
            .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, ApmSpanConstants.SPAN_ACTION)
            .setName(name)
        try {
            return block()
        } catch (e: Throwable) {
            span.captureException(e)
            span.setOutcome(Outcome.FAILURE)
            throw e
        } finally {
            span.end()
        }
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {
        val requestPath = exchange.request.path.pathWithinApplication()

        // Check if the path matches any unauthorized endpoint pattern
        val isUnauthorized = unauthorizedPathPatterns.any { pattern ->
            pattern.matches(requestPath)
        }

        val authorization = exchange.request.headers["Authorization"]
            ?.firstOrNull()
            ?.replace("Bearer ", "")
            ?.trim()

        // Unauthorized does not require an Authorization header.
        if (!isUnauthorized && authorization == null) {
            exchange.request.headers["Authorization"]
                ?.firstOrNull()
                ?.replace("Bearer ", "")
                ?.trim()
                ?: throw UnauthorizedException("Authorization header is missing")
        }

        // fix: access unauthorized api with an invalid jwtKey will produce infinite loop.
        val claims = if (authorization != null) {
            try {
                JwtUtil.parseToken(getJWTSignKey.invoke(), authorization)
            } catch (e: Exception) {
                if (!isUnauthorized) {
                    if (e is ExpiredJwtException) {
                        throw UnauthorizedException("token expired")
                    } else {
                        logger.error("unexpected token parse exception", e)
                        throw UnauthorizedException("invalid token pattern")
                    }
                } else {
                    null
                }
            }
        } else {
            null
        }

        val span = ElasticApm.currentSpan()
            .startSpan(ApmSpanConstants.SPAN_TYPE, ApmSpanConstants.SPAN_SUBTYPE, ApmSpanConstants.SPAN_ACTION)
            .setName(AUTH_SPAN_NAME)
        val spanEnded = AtomicBoolean(false)
        val endSpan = { if (spanEnded.compareAndSet(false, true)) span.end() }

        return exchange.session.flatMap { session ->
            if (authorization != null && claims != null) {
                val userId = claims["userId"]
                    ?.toString()
                    ?.toLong()
                    ?: throw UnauthorizedException("userId is missing")

                val tenantId = claims["tenantId"]
                    ?.toString()
                    ?.toLong()

                val tenantMemberId = claims["tenantMemberId"]
                    ?.toString()
                    ?.toLong()

                if ((tenantId != null && tenantMemberId == null) || (tenantId == null && tenantMemberId != null)) {
                    throw UnauthorizedException("Could not resolve tenant authentication from token")
                }

                session.attributes[SessionConstants.AUDIT_USER_ID] = userId
                session.attributes[SessionConstants.AUDIT_TENANT_ID] = tenantId ?: 0L
                session.attributes[SessionConstants.AUDIT_TENANT_MEMBER_ID] = tenantMemberId ?: 0L

                val tokenIssuedAt = claims.issuedAt?.time

                mono {
                    withChildSpan(span, FORCE_LOGOUT_SPAN_NAME) {
                        assertNotForceLoggedOut.invoke(userId, tokenIssuedAt)
                    }
                    withChildSpan(span, AUTHORITIES_SPAN_NAME) {
                        getUserAuthorities.invoke(userId, tenantId, tenantMemberId)
                    }
                }
                    .doOnError { error ->
                        span.captureException(error)
                        span.setOutcome(Outcome.FAILURE)
                    }
                    .doFinally { endSpan() }
                    .flatMap {
                        val token = UsernamePasswordAuthenticationToken(
                            claims.subject,
                            null,
                            it
                        )

                        chain.filter(exchange).contextMerge(
                            ReactiveSecurityContextHolder.withAuthentication(token)
                        )
                    }
            } else {
                endSpan()
                chain.filter(exchange)
            }
        }.doOnError { endSpan() }
    }
}