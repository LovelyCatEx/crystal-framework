/*
 * Copyright 2025 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */
package com.lovelycatv.crystalframework.auth.filter

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

class CustomAuthFilter(
    val unauthorizedPathPatterns: List<PathPattern>,
    val getUserAuthorities: suspend (userId: Long, tenantId: Long?) -> Collection<GrantedAuthority>,
    val getJWTSignKey: () -> String,
) : WebFilter {
    private val logger = logger()

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

        return exchange.session.flatMap { session ->
            if (authorization != null && claims != null) {
                val userId = claims["userId"]
                    ?.toString()
                    ?.toLong()
                    ?: throw UnauthorizedException("userId is missing")

                val tenantId = claims["tenantId"]
                    ?.toString()
                    ?.toLong()

                session.attributes[SessionConstants.AUDIT_USER_ID] = userId
                session.attributes[SessionConstants.AUDIT_TENANT_ID] = tenantId ?: 0L

                mono {
                    getUserAuthorities.invoke(userId, tenantId)
                }.flatMap {
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
                chain.filter(exchange)
            }
        }
    }
}